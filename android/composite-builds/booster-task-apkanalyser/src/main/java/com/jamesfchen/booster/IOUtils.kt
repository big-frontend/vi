@file:JvmName("IOUtils")
package com.jamesfchen.booster

import java.io.InputStream
import java.io.OutputStream

fun copy(inputStream: InputStream, outputStream: OutputStream) {
    inputStream.copyTo(outputStream)
}
