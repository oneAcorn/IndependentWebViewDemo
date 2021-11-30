package com.acorn.independentwebview.messenger

import android.os.Handler
import android.os.Message
import android.os.RemoteException
import android.widget.Toast
import com.acorn.independentwebview.utils.logI

/**
 * Created by acorn on 2021/11/29.
 */
class ServiceMessengerHandler : Handler() {
    override fun handleMessage(msg: Message?) {
        super.handleMessage(msg)
        logI("服务端收到消息:${msg?.what}")
        val replyMsg = Message.obtain()
        replyMsg.what = 3
        try {
            msg?.replyTo?.send(replyMsg)
        } catch (e: RemoteException) {
            e.printStackTrace()
        }
    }
}