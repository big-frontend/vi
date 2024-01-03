package com.electrolytej.vi

import android.util.Log
import com.electroyltej.startup.Job
import com.electroyltej.startup.Phase
import com.electroyltej.startup.RunnablePolicy
import com.electroyltej.startup.Process

/**
 * Copyright ® $ 2017
 * All right reserved.
 *
 * @since: May/17/2022  Tue
 */
@Job(
    name = "StartUpTask1",
    attachProcesses = [Process.MAIN],
    policy = RunnablePolicy.IF_IDLE,
    appPhase = Phase.ONCREATE,
    priority = 1,
    deps = []
)
class StartUpTask3 : Runnable {
    override fun run() {
        Log.d("electrolytej","StartUpTask1")
    }
}