package com.acorn.independentwebview

import android.app.Application
import com.acorn.independentwebview.utils.isLocalAppProcess
import com.acorn.independentwebview.utils.logI

/**
 * Created by acorn on 2020/5/20.
 */
class MyApplication : Application() {
    override fun onCreate() {
        super.onCreate()
        // 当新进程中组件被外部激活或者唤醒，系统会首先fork该组件所在进程，并创建该进程的Application对象并初始化，
        // 然后再启动该组件，这个和启动应用的流程一样。如上文Push进程启动的时候，
        // 就会导致应用的Aplication会被初始化一次。这个情况下就会出现一个应用的Aplication被多次创建并初始化，
        // 若应用配置定义自己的Aplication,并在oncreate（）函数中进行了自己业务相关的逻辑，常见的是应用启动统计，
        // 这个时候就会由于其他进程如后台进程启动而导致了应用启动次数统计增加而且实际应用根本没有启动，
        // 这样就导致数据不准。这个时候怎么办呢？就可以利用isLocalAppProcess判断是否是当前应用进程，
        // 方法如下"方法 在Application的onCreate()初始化中增加判断代码，
        // 当不是的当前应用进程直接return,不要走下面逻辑代码
        logI("Application onCreate 是否为主线程${isLocalAppProcess(this)}")
    }
}