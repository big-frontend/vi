package com.electrolytej.vi

import java.io.File
import kotlin.test.Test

val PWD = File(System.getProperty("user.dir"))
class ApFileTest{
    private val PROPERTY_IGNORES = "vi.optimizer.duplicated.files.ignores"
    @Test
    fun `t`() {
       println("res/drawable/icon.png".toByteArray(Charsets.UTF_8).size)
       println("r/d/y.png".toByteArray(Charsets.UTF_8).size)
       println("r/yyy.png".toByteArray(Charsets.UTF_8).size)
       println("yyyyy1.png".toByteArray(Charsets.UTF_8).size)
    }
}
