package com.acorn.independentwebview.messenger

import android.os.Handler
import android.os.Message
import com.acorn.independentwebview.utils.logI

/**
 * Created by acorn on 2021/11/29.
 */
class ClientMessengerHandler :Handler() {
    override fun handleMessage(msg: Message?) {
        super.handleMessage(msg)
        logI("客户端收到消息:${msg?.what}")
    }
}