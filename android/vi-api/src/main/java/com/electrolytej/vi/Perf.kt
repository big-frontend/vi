package com.electrolytej.vi

import android.app.Application
import android.content.Context
import android.util.Log
import androidx.lifecycle.ProcessLifecycleOwner
import com.electrolytej.vi.lifecycle.AbsActivitiesLifecycleObserver
import com.electrolytej.vi.lifecycle.AbsAppLifecycleObserver
import com.electrolytej.vi.lifecycle.AbsLifecycleObserver
import com.electrolytej.vi.lifecycle.activitiesListeners
import com.electrolytej.vi.lifecycle.initLifecycle
import lab.galaxy.yahfa.HookMain
import java.util.ServiceLoader

object Perf {
    internal lateinit var app: Application
    var inited = false
    @JvmStatic
    fun attachBaseContext(base: Context) {
        //只监测主进程性能
        HookMain.doHookDefault(base.classLoader,base)
        ActivityThreadHacker.hackSysHandlerCallback()
//        TraceUtil.i(com.electrolytej.startup.TAG_STARTUP_MONITOR,"PerfApp#attachBaseContext");
    }

    @JvmStatic
    fun onCreate(application: Application) {
        if (inited) return
        //由于ProcessLifecycleOwnerInitializer multiprocess=true，这样每个进程都会有一个ContentProvider对象，
        //进程使用ContentProvider不用通过framework跨进程获取，自己进程本身就有一个，这样能提升性能，但是也增加了进程开销
        app = application
        try {
            DebugOverlayController.requestPermission(app)
        } catch (ignore: Exception) {
        }
        initLifecycle(application)
        registerLifecycle(app)
        inited = true
    }

    private fun registerLifecycle(application: Application) {
        val items =
            ServiceLoader.load(AbsLifecycleObserver::class.java, Thread.currentThread().contextClassLoader)
                .sortedBy {
                    it.javaClass.getAnnotation(Priority::class.java)?.value ?: 0
                }
        items.forEach {
            try {
                it.bindApplication(application)
                if (it is AbsAppLifecycleObserver) ProcessLifecycleOwner.get().lifecycle.addObserver(it)
                if (it is AbsActivitiesLifecycleObserver) activitiesListeners.add(it)
            } catch (e: InstantiationException) {
            } catch (e: IllegalAccessException) {
            } catch (e: Exception) {
                Log.e("HookInfo", Log.getStackTraceString(e))
            }
        }
    }



}