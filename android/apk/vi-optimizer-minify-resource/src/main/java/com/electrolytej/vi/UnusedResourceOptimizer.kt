package com.electrolytej.vi

import com.android.build.gradle.api.BaseVariant
import org.gradle.api.Project
import pink.madis.apk.arsc.ResourceFile
import java.io.File
import java.io.PrintWriter
import java.util.zip.ZipEntry
import java.util.zip.ZipFile

class UnusedResourceOptimizer(
    val variant: BaseVariant?,
    val symbols: SymbolList,
    val logger: L
) : BaseOptimizer() {
    companion object {
        private val UNUSED_PROPERTY_IGNORES = "vi.optimizer.unused.files.ignores"
    }

    override fun processArsc(resourceFile: ResourceFile): Boolean {
        return false
    }

    override fun processRes(srcFile: ZipFile, destDir: File, zipEntry: ZipEntry): Boolean {
        return false
    }
}