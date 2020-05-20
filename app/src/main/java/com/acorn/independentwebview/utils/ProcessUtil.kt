package com.acorn.independentwebview.utils

import android.app.ActivityManager
import android.content.Context
import android.os.Process

/**
 * Created by acorn on 2020/5/20.
 */

/**
 * 是否为主线程
 */
fun isLocalAppProcess(context: Context, packageName: String = context.packageName): Boolean {
    val myPid = Process.myPid()
    val activityManager = context.getSystemService(Context.ACTIVITY_SERVICE) as ActivityManager
    var myProcess: ActivityManager.RunningAppProcessInfo? = null
    for (process in activityManager.runningAppProcesses) {
        if (process.pid == myPid) {
            myProcess = process
            break
        }
    }
    logI("processName:${myProcess?.processName},packageName:$packageName")
    return myProcess?.processName?.equals(packageName) == true
}