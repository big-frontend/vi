package com.electrolytej.vi

import android.app.Application
import android.content.Context

abstract class AbsHookRegistry {
    abstract fun registerItem(h:MutableList<String>)
    abstract fun attachBaseContext(base: Context)
}
