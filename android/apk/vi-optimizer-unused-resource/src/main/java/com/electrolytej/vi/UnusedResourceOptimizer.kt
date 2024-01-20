package com.electrolytej.vi

import com.android.build.gradle.api.BaseVariant
import com.google.auto.service.AutoService
import pink.madis.apk.arsc.ResourceFile
import java.io.File
import java.util.zip.ZipEntry
import java.util.zip.ZipFile
@AutoService(BaseOptimizer::class)
class UnusedResourceOptimizer: BaseOptimizer {
    companion object {
        private val UNUSED_PROPERTY_IGNORES = "vi.optimizer.unused.files.ignores"
    }
    override fun processRes(srcFile: ZipFile, destDir: File, zipEntry: ZipEntry): Boolean {
        println("UnusedResourceOptimizer processRes")
        return false
    }

    override fun processArsc(resourceFile: ResourceFile): Boolean {
        println("UnusedResourceOptimizer processArsc")
        return false
    }


}