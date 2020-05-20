package com.acorn.independentwebview

import android.app.Application

/**
 * Created by acorn on 2020/5/20.
 */
class MyApplication : Application() {
    override fun onCreate() {
        super.onCreate()
        logI("Application onCreate 多进程只调用一次")
    }
}