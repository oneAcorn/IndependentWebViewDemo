package com.acorn.independentwebview

import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.ServiceConnection
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.os.IBinder
import android.widget.Toast
import com.acorn.independentwebview.provider.SPHelper
import com.acorn.independentwebview.service.MyService
import com.acorn.independentwebview.utils.isLocalAppProcess
import com.acorn.independentwebview.utils.logI
import com.tencent.mmkv.MMKV
import kotlinx.android.synthetic.main.activity_main.*
import org.greenrobot.eventbus.EventBus
import org.greenrobot.eventbus.Subscribe
import org.greenrobot.eventbus.ThreadMode

class MainActivity : AppCompatActivity() {
    private lateinit var aidlInterface: IMyAidlInterface
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
        setContentView(R.layout.activity_main)
        bindService()
        SPHelper.init(application)
        EventBus.getDefault().register(this)
        logI("MainActivity 是否是主进程:${isLocalAppProcess(this)}")

        addDataBtn.setOnClickListener {
            if (isConn) {
                aidlInterface.addBookInOut(Book("通信书"))
            }
        }

        addDataInBtn.setOnClickListener {
            if (isConn) {
                //这里受到影响了,因为这里是服务端,in是服务端可以改服务端的
                val book = Book("Main1")
                aidlInterface.addBookIn(book)
                Toast.makeText(this, "客户端不受影响:${book.name}", Toast.LENGTH_LONG).show()
            }
        }

        addDataOutBtn.setOnClickListener {
            if (isConn) {
                //这里服务端可以取到name,因为这里也是服务端
                val book = Book("Main2")
                aidlInterface.addBookOut(book)
                Toast.makeText(this, "客户端受到影响:${book.name}", Toast.LENGTH_LONG).show()
            }
        }

        webviewBtn.setOnClickListener {
            startActivity(Intent(this@MainActivity, WebViewActivity::class.java))
        }

        loadUrlJsBtn.setOnClickListener {
            startWebViewActivity(1)
        }

        evaluateJsBtn.setOnClickListener {
            startWebViewActivity(2)
        }

        jsInterfaceBtn.setOnClickListener {
            startWebViewActivity(3)
        }

        promptBtn.setOnClickListener {
            startWebViewActivity(4)
        }

        contentProviderBtn.setOnClickListener {
            SPHelper.save("token", "我是token")
        }

        mmkvSaveBtn.setOnClickListener {
            val mmkv = MMKV.defaultMMKV()
            mmkv.encode("key1", "我是字符串")
            mmkv.encode("key2", 30)
        }

        mmkvReadBtn.setOnClickListener {
            val mmkv = MMKV.mmkvWithID("processId",MMKV.MULTI_PROCESS_MODE)
            val msg = "key1:${mmkv.decodeString("key1")},key2:${mmkv.decodeInt("key2")}"
            Toast.makeText(this, msg, Toast.LENGTH_LONG).show()
            logI(msg)
        }
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    fun onEvent(msg: String) { //收不到跨进程发过来的消息
        Toast.makeText(this, "收到消息:$msg", Toast.LENGTH_SHORT).show()
    }

    private fun startWebViewActivity(type: Int) {
        val intent = Intent(this, WebViewActivity::class.java)
        intent.putExtra("type", type)
        startActivity(intent)
    }

    private fun bindService() {
        bindService(Intent(this, MyService::class.java), serviceConn, Context.BIND_AUTO_CREATE)
    }

    override fun onDestroy() {
        super.onDestroy()
        if (isConn)
            unbindService(serviceConn)
        EventBus.getDefault().unregister(this)
    }
}
