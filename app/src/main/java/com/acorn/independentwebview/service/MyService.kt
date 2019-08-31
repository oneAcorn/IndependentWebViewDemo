package com.acorn.independentwebview.service

import android.app.Service
import android.content.Intent
import android.os.IBinder
import com.acorn.independentwebview.IMyAidlInterface

class MyService : Service() {
    override fun onBind(intent: Intent?): IBinder? {
        return MyBinder()
    }

    class MyBinder : IMyAidlInterface.Stub() {
        override fun myAction(msg: String?): String {
            return "I see,$msg right?"
        }
    }
}