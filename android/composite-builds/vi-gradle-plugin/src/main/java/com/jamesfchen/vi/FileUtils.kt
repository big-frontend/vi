@file:JvmName("FileUtils")
package com.jamesfchen.vi

import java.io.File
object FileUtils{
    fun copyFile(originalApkFile: File, file: File) {
        originalApkFile.copyTo(file, overwrite = true)
    }

    fun deleteRecursivelyIfExists(unzipDir: File) {
        if (!unzipDir.exists()) return
        unzipDir.deleteRecursively()
    }
}
