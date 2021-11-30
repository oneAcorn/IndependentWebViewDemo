package com.acorn.independentwebview

import android.app.Service
import android.content.ComponentName
import android.content.Intent
import android.content.ServiceConnection
import android.os.*
import android.support.v7.app.AppCompatActivity
import com.acorn.independentwebview.messenger.ClientMessengerHandler
import com.acorn.independentwebview.messenger.MessengerService
import kotlinx.android.synthetic.main.activity_messenger.*

/**
 * Created by acorn on 2021/11/29.
 */
class MessengerActivity : AppCompatActivity() {
    private val mClientMessenger = Messenger(ClientMessengerHandler())

    //服务端的Messenger
    private var mServiceMessenger: Messenger? = null
    private val serviceConnection = object : ServiceConnection {
        override fun onServiceConnected(name: ComponentName?, service: IBinder?) {
            mServiceMessenger = Messenger(service)
        }

        override fun onServiceDisconnected(name: ComponentName?) {
            mServiceMessenger = null
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_messenger)
        bindMessengerService()

        sendMsgBtn.setOnClickListener {
            sendMsg()
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        unbindService(serviceConnection)
    }

    private fun sendMsg() {
        val msg = Message.obtain()
        msg.what = 1
        val bundle = Bundle()
        bundle.putString("args", "hello")
        msg.data = bundle
        msg.replyTo = mClientMessenger

        try {
            mServiceMessenger?.send(msg)
        } catch (e: RemoteException) {
            e.printStackTrace()
        }
    }

    private fun bindMessengerService() {
        val intent = Intent(this, MessengerService::class.java)
        bindService(intent, serviceConnection, Service.BIND_AUTO_CREATE)
    }
}