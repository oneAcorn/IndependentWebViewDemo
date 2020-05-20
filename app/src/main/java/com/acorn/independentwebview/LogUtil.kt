package com.acorn.independentwebview

import android.util.Log

/**
 * Created by acorn on 2020/5/3.
 */
fun logI(str: String) {
    Log.i("acornTag", str)
}

fun logE(str: String) {
    Log.e("acornTag", str)
}

fun logE(e: Throwable) {
    Log.e("acornTag", "err", e)
}