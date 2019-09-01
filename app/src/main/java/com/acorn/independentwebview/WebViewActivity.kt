package com.acorn.independentwebview

import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.ServiceConnection
import android.os.Bundle
import android.os.IBinder
import android.support.v7.app.AppCompatActivity
import android.widget.Toast
import com.acorn.independentwebview.service.MyService
import kotlinx.android.synthetic.main.activity_webview.*

class WebViewActivity : AppCompatActivity() {
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
        setContentView(R.layout.activity_webview)
        initWebView()
        bindService()

        addDataInBtn.setOnClickListener {
            if (isConn) {
                //这里In是有效的,这里是客户端
                val book = Book("web1")
                aidlInterface.addBookIn(book)
                Toast.makeText(this, "客户端不受影响:${book.name}", Toast.LENGTH_LONG).show()
            }
        }

        addDataOutBtn.setOnClickListener {
            if (isConn) {
                //这里out是有效的,这里是客户端
                val book = Book("web2")
                aidlInterface.addBookOut(book)
                Toast.makeText(this, "客户端受到影响:${book.name}", Toast.LENGTH_LONG).show()
            }
        }

        testBtn.setOnClickListener {
            if (isConn) {
                Toast.makeText(this, aidlInterface.requestBookList().map {
                    it.name
                }.joinToString(), Toast.LENGTH_LONG).show()
            }
        }
    }

    private fun initWebView() {
        with(webView.settings) {
            setSupportZoom(true)
            javaScriptEnabled = true
        }

        webView.loadUrl("https://cn.bing.com")
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
}