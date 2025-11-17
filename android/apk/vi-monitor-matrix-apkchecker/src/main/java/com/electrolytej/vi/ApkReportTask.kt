package com.electrolytej.vi

import com.android.build.gradle.api.BaseVariant
import org.gradle.api.DefaultTask
import org.gradle.api.tasks.Internal
import org.gradle.api.tasks.TaskAction

abstract class ApkReportTask : DefaultTask() {
    @get:Internal
    lateinit var variant: BaseVariant

    @TaskAction
    fun report() {

    }
}