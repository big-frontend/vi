package com.electrolytej.anr

import android.app.Application
import android.content.Context
import android.util.Log
import androidx.annotation.Keep
import com.electrolytej.vi.AbsHookRegistry
import com.electrolytej.vi.lifecycle.AbsLifecycleObserver
import com.google.auto.service.AutoService

const val TAG_ANR_MONITOR = "anr-monitor"
@AutoService(AbsLifecycleObserver::class)
@Keep
class AnrItem : AbsLifecycleObserver() {
    private val messageTracker = MessageTracker()
    override fun bindApplication(application: Application) {
        Log.e(TAG_ANR_MONITOR, "anr item bindApplication")
    }

    override fun onAppCreate() {
//        messageTracker.start()
    }
}
@AutoService(AbsHookRegistry::class)
@Keep
class AnrHookRegistry : AbsHookRegistry() {
    override fun registerItem(h: MutableList<String>) {
        Log.e(TAG_ANR_MONITOR, "anr hook registry registerItem")
        h.add(Hook_MainActivity_onCreate::class.java.name)
    }

    override fun attachBaseContext(base: Context) {
        Log.e(TAG_ANR_MONITOR, "anr hook registry attachBaseContext")
    }

}