package com.electrolytej.anr

import android.app.Application
import android.util.Log
import androidx.annotation.Keep
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
        messageTracker.start()
    }
}