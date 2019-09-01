package com.acorn.independentwebview

import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.ServiceConnection
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.os.IBinder
import android.widget.Toast
import com.acorn.independentwebview.service.MyService
import kotlinx.android.synthetic.main.activity_main.*

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
    }
}
