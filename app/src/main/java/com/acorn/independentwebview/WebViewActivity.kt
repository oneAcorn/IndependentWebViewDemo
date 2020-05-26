package com.acorn.independentwebview

import android.content.*
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.os.IBinder
import android.support.v7.app.AlertDialog
import android.support.v7.app.AppCompatActivity
import android.view.View
import android.webkit.JsPromptResult
import android.webkit.JsResult
import android.webkit.WebChromeClient
import android.webkit.WebView
import android.widget.Toast
import com.acorn.independentwebview.provider.SPHelper
import com.acorn.independentwebview.service.MyService
import com.acorn.independentwebview.utils.isLocalAppProcess
import com.acorn.independentwebview.utils.logI
import com.tencent.mmkv.MMKV
import kotlinx.android.synthetic.main.activity_webview.*
import org.greenrobot.eventbus.EventBus


class WebViewActivity : AppCompatActivity(), View.OnClickListener {
    private lateinit var aidlInterface: IMyAidlInterface

    /**
     * 1 通过loadUrl()方法调用js
     * 2 通过evaluateJavascript()方法调用js
     * 3 js通过addJavascriptInterface()方法调用Android 此方法在Android4.2之前有安全漏洞
     * 4 js通过onJsPrompt()方法调用Android
     * 5 js通过shouldOverrideUrlLoading()方法调用Android 此方法在顺丰优选大量使用可参考,所以不在这里写了
     */
    private var type: Int = -1
    private var isConn = false
    private val serviceConn = object : ServiceConnection {
        override fun onServiceDisconnected(name: ComponentName?) {
            isConn = false
        }

        override fun onServiceConnected(name: ComponentName?, service: IBinder?) {
            aidlInterface = IMyAidlInterface.Stub.asInterface(service)
            isConn = true
        }

    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_webview)
        initWebView()
        bindService()
        SPHelper.init(application)
        logI("WebViewActivity 是否是主进程:${isLocalAppProcess(this)}")

        testOpenActivityBtn.setOnClickListener(this)
        testEventBusBtn.setOnClickListener(this)
        addDataInBtn.setOnClickListener(this)
        addDataOutBtn.setOnClickListener(this)
        testBtn.setOnClickListener(this)
        getTokenBtn.setOnClickListener(this)
        mmkvWriteBtn.setOnClickListener(this)
        mmkvReadBtn.setOnClickListener(this)
    }

    private fun initWebView() {
        with(webView.settings) {
            setSupportZoom(true)
            // 设置与Js交互的权限
            javaScriptEnabled = true
            // 设置允许JS弹窗
            javaScriptCanOpenWindowsAutomatically = true
        }

        type = intent.getIntExtra("type", -1)
        testWebBtn.visibility = if (type == 1 || type == 2) View.VISIBLE else View.GONE
        if (type == 1 || type == 2) {
            webView.loadUrl("file:///android_asset/javascript.html")

            testWebBtn.setOnClickListener {
                //JS代码调用一定要在 onPageFinished（） 回调之后才能调用，否则不会调用。
                if (type == 1) {
                    webView.loadUrl("javascript:callJS()")
                } else if (type == 2) {
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) { //此方法最低支持android 4.4
                        webView.evaluateJavascript("javascript:callJS()") {
                            Toast.makeText(this@WebViewActivity, "js回调$it", Toast.LENGTH_LONG)
                                .show()
                        }
                    }
                }
            }

            // 由于设置了弹窗检验调用结果,所以需要支持js对话框
            // webview只是载体，内容的渲染需要使用webviewChromClient类去实现
            // 通过设置WebChromeClient对象处理JavaScript的对话框
            //设置响应js 的Alert()函数
            webView.webChromeClient = object : WebChromeClient() {
                override fun onJsAlert(
                    view: WebView?,
                    url: String?,
                    message: String?,
                    result: JsResult?
                ): Boolean {
                    val b = AlertDialog.Builder(this@WebViewActivity)
                    b.setTitle("Alert")
                    b.setMessage(message)
                    b.setPositiveButton(android.R.string.ok,
                        DialogInterface.OnClickListener { dialog, which ->
                            result?.confirm()
                        })
                    b.setCancelable(false)
                    b.create().show()
                    return true
                }
            }
        } else if (type == 3) {
            webView.addJavascriptInterface(AndroidToJs(), "test")
            webView.loadUrl("file:///android_asset/callAndroid.html")
        } else if (type == 4) {
            webView.loadUrl("file:///android_asset/prompt.html")
            webView.webChromeClient = object : WebChromeClient() {
                // 参数message:代表promt（）的内容（不是url）
                // 参数result:代表输入框的返回值
                override fun onJsPrompt(
                    view: WebView?,
                    url: String?,
                    message: String?,
                    defaultValue: String?,
                    result: JsPromptResult?
                ): Boolean {
                    // 根据协议的参数，判断是否是所需要的url(原理同方式2)
                    // 一般根据scheme（协议格式） & authority（协议名）判断（前两个参数）
                    //假定传入进来的 url = "js://webview?arg1=111&arg2=222"（同时也是约定好的需要拦截的）
                    val uri = Uri.parse(message)
                    // 如果url的协议 = 预先约定的 js 协议
                    // 就解析往下解析参数
                    if (uri.scheme == "js") {

                        // 如果 authority  = 预先约定协议里的 webview，即代表都符合约定的协议
                        // 所以拦截url,下面JS开始调用Android需要的方法
                        if (uri.authority == "demo") {

                            // 执行JS所需要调用的逻辑
                            println("js调用了Android的方法")
                            // 可以在协议上带有参数并传递到Android上
//                            val params = HashMap()
//                            val collection = uri.getQueryParameterNames()

                            //参数result:代表消息框的返回值(输入值)
                            result?.confirm("js调用了Android的方法成功啦")
                        }
                        return true
                    }
                    return super.onJsPrompt(view, url, message, defaultValue, result)
                }
            }
        } else {
            webView.loadUrl("https://cn.bing.com")
        }
    }

    private fun bindService() {
        bindService(Intent(this, MyService::class.java), serviceConn, Context.BIND_AUTO_CREATE)
    }

    override fun onDestroy() {
        super.onDestroy()
        if (isConn)
            unbindService(serviceConn)
        webView.destroy()
    }

    override fun onClick(v: View) {
        when (v.id) {
            R.id.testOpenActivityBtn -> { //测试打开新的Activity
                startActivity(Intent(this, MainActivity::class.java))
            }
            R.id.testEventBusBtn -> { //测试Eventbus跨进程能否通信
                //看来没用..
                EventBus.getDefault().post("abc")
            }
            R.id.addDataInBtn -> { //使用in传递数据
                if (isConn) {
                    //这里In是有效的,这里是客户端
                    val book = Book("web1")
                    aidlInterface.addBookIn(book)
                    Toast.makeText(this, "客户端不受影响:${book.name}", Toast.LENGTH_LONG).show()
                }
            }
            R.id.addDataOutBtn -> { //使用out传递数据
                if (isConn) {
                    //这里out是有效的,这里是客户端
                    val book = Book("web2")
                    aidlInterface.addBookOut(book)
                    Toast.makeText(this, "客户端受到影响:${book.name}", Toast.LENGTH_LONG).show()
                }
            }
            R.id.testBtn -> { //测试查询接口
                if (isConn) {
                    Toast.makeText(this, aidlInterface.requestBookList().map {
                        it.name
                    }.joinToString(), Toast.LENGTH_LONG).show()
                }
            }
            R.id.getTokenBtn -> { //sharedprefrces+contentprovider测试
                Toast.makeText(
                    this,
                    "通过ContentProvider获取token:${SPHelper.getString("token", "默认值")}",
                    Toast.LENGTH_LONG
                ).show()
            }
            R.id.mmkvWriteBtn -> {
                val mmkv = MMKV.mmkvWithID("processId",MMKV.MULTI_PROCESS_MODE)
                mmkv.encode("key1", "web修改")
                mmkv.encode("key2", 31)
            }
            R.id.mmkvReadBtn -> {
                val mmkv = MMKV.mmkvWithID("processId",MMKV.MULTI_PROCESS_MODE)
                val msg = "key1:${mmkv.decodeString("key1")},key2:${mmkv.decodeInt("key2")}"
                Toast.makeText(this, msg, Toast.LENGTH_LONG).show()
                logI(msg)
            }
        }
    }
}