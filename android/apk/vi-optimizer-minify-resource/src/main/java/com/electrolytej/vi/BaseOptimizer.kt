package com.electrolytej.vi

import pink.madis.apk.arsc.ResourceFile
import java.io.File
import java.io.Writer
import java.util.zip.ZipEntry
import java.util.zip.ZipFile

abstract class BaseOptimizer {


    open fun start(ap_: File) {

    }

    abstract fun processArsc(resourceFile: ResourceFile): Boolean

    /**
     * true：拦截不传递给下一个优化器
     */
    abstract fun processRes(srcFile: ZipFile, destDir: File, zipEntry: ZipEntry): Boolean

    open fun end(ap_: File) {
    }


}