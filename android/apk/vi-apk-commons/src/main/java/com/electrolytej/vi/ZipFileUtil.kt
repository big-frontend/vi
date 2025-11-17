package com.electrolytej.vi

import java.io.BufferedInputStream
import java.io.BufferedOutputStream
import java.io.ByteArrayOutputStream
import java.io.File
import java.io.FileInputStream
import java.io.FileOutputStream
import java.io.IOException
import java.util.zip.CRC32
import java.util.zip.ZipEntry
import java.util.zip.ZipFile
import java.util.zip.ZipOutputStream

fun File.zipFile(destFile: File, shouldCompress: (ZipEntry) -> Boolean) {
    ZipOutputStream(FileOutputStream(destFile)).use { zipOutputStream ->
        zipFile(zipOutputStream, this, canonicalPath, shouldCompress)
    }
    this.deleteRecursivelyIfExists()
}

fun File.deleteRecursivelyIfExists() {
    if (!exists()) return
    deleteRecursively()
}

private fun zipFile(
    zipOutputStream: ZipOutputStream,
    file: File,
    rootDir: String,
    shouldCompress: (ZipEntry) -> Boolean
) {
    if (file.isDirectory) {
        val unZipFiles = file.listFiles()
        for (subFile in unZipFiles) {
            zipFile(zipOutputStream, subFile, rootDir, shouldCompress)
        }
    } else {
        val entryName =
            file.canonicalPath.substring(rootDir.length + 1).replace(File.separatorChar, '/')
        val zipEntry = ZipEntry(entryName)
        val method = if (shouldCompress(zipEntry)) ZipEntry.DEFLATED else ZipEntry.STORED
        zipEntry.method = method
        addZipEntry(zipOutputStream, zipEntry, file)
    }
}

fun File?.createOrExistsFile(): Boolean {
    if (this == null) return false
    if (this.exists()) return this.isFile()
    return if (!this.getParentFile().createOrExistsDir()) false else try {
        this.createNewFile()
    } catch (e: IOException) {
        e.printStackTrace()
        false
    }
}

fun File?.createOrExistsDir(): Boolean {
    return this != null && if (this.exists()) this.isDirectory() else this.mkdirs()
}

fun ZipFile.extractEntry(destFile: File, entryName: String?) {
    if (entryName == null) return
    extractEntry(destFile, getEntry(entryName))
}

@Throws(IOException::class)
fun ZipFile.extractEntry(destFile: File, zipEntry: ZipEntry?) {
    if (zipEntry == null) return
    BufferedOutputStream(FileOutputStream(destFile)).use { outputStream ->
        BufferedInputStream(getInputStream(zipEntry)).use { bufferedInput ->
            var len: Int
            val buffer = ByteArray(4096)
            while (bufferedInput.read(buffer).also { len = it } != -1) {
                outputStream.write(buffer, 0, len)
            }
        }
        outputStream.flush()
    }
}

fun File?.readFileContent(): ByteArray {
    if (this == null) return byteArrayOf()
    ByteArrayOutputStream().use { output ->
        BufferedInputStream(FileInputStream(this)).use { bufferedInput ->
            var len: Int
            val buffer = ByteArray(4096)
            while (bufferedInput.read(buffer).also { len = it } != -1) {
                output.write(buffer, 0, len)
            }
        }
        output.flush()
        return output.toByteArray()
    }
}

@Throws(IOException::class)
fun addZipEntry(zipOutputStream: ZipOutputStream, zipEntry: ZipEntry, file: File?) {
    val writeEntry = ZipEntry(zipEntry.name)
    val content = file.readFileContent()
    if (zipEntry.method == ZipEntry.DEFLATED) {
        writeEntry.setMethod(ZipEntry.DEFLATED)
    } else {
        writeEntry.setMethod(ZipEntry.STORED)
        val crc32 = CRC32()
        crc32.update(content)
        writeEntry.setCrc(crc32.value)
    }
    writeEntry.setSize(content.size.toLong())
    zipOutputStream.putNextEntry(writeEntry)
    zipOutputStream.write(content)
    zipOutputStream.flush()
    zipOutputStream.closeEntry()
}


