package com.electrolytej.vi

import com.android.build.gradle.api.BaseVariant
import pink.madis.apk.arsc.ResourceFile
import java.io.File
import java.io.Writer
import java.util.zip.ZipEntry
import java.util.zip.ZipFile

interface BaseOptimizer {
    fun start(variant: BaseVariant?, symbols: SymbolList, ap_: File) {

    }

    fun processArsc(resourceFile: ResourceFile): Boolean

    /**
     * true：拦截不传递给下一个优化器
     */
    fun processRes(srcFile: ZipFile, destDir: File, zipEntry: ZipEntry): Boolean

    fun end(ap_: File) {
    }


}