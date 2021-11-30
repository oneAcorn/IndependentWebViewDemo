package com.acorn.independentwebview.messenger

import android.app.Service
import android.content.Intent
import android.os.IBinder
import android.os.Messenger

/**
 * Created by acorn on 2021/11/29.
 */
class MessengerService : Service() {
    private val messenger = Messenger(ServiceMessengerHandler())

    override fun onBind(intent: Intent?): IBinder? {
        return messenger.binder;
    }
}