package com.acorn.independentwebview

import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.ServiceConnection
import android.os.Bundle
import android.os.IBinder
import android.support.v7.app.AppCompatActivity
import android.widget.Toast
import kotlinx.android.synthetic.main.activity_webview.*

class WebViewActivity : AppCompatActivity() {
    private lateinit var aidlInterface: IMyAidlInterface

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_webview)
        initWebView()
        bindService()

        testBtn.setOnClickListener {
            Toast.makeText(this, aidlInterface.myAction("wang"), Toast.LENGTH_LONG).show()
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
        bindService(Intent("com.acorn.server"), object : ServiceConnection {
            override fun onServiceDisconnected(name: ComponentName?) {
            }

            override fun onServiceConnected(name: ComponentName?, service: IBinder?) {
                aidlInterface = IMyAidlInterface.Stub.asInterface(service)
            }

        }, Context.BIND_AUTO_CREATE)
    }

    override fun onDestroy() {
        super.onDestroy()
        webView.destroy()
    }
}