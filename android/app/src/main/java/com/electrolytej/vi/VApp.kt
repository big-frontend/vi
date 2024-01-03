package com.electrolytej.vi

import android.app.Application
import android.content.Context
import com.electroyltej.startup.AppDelegate

/**
 * Copyright ® $ 2024
 * All right reserved.
 *
 * @author: electrolyteJ
 * @since: Jan/04/2024  Thu
 */
class VApp : Application() {
    var appDelegate: AppDelegate = AppDelegate()
    override fun attachBaseContext(base: Context?) {
        super.attachBaseContext(base)
        appDelegate.attachBaseContext(base)
    }

    override fun onCreate() {
        super.onCreate()
        appDelegate.onCreate()
    }
}