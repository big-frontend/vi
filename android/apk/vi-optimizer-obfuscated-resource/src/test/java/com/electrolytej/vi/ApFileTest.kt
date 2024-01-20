package com.electrolytej.vi

import com.didiglobal.booster.kotlinx.file
import pink.madis.apk.arsc.ResourceFile
import java.io.File
import java.util.zip.ZipEntry
import kotlin.test.Test
import kotlin.test.assertFalse
import java.util.zip.ZipFile

val PWD = File(System.getProperty("user.dir"))
class ApFileTest {
    private val PROPERTY_IGNORES = "vi.optimizer.obfuscated.resource.ignores"
    private val logger = L.create()
    @Test
    fun `parse zip file`() {
        val ap_ = PWD.file("src", "test", "resources", "resources-debug.ap_")
        val arscFile = PWD.file("src", "test", "resources", ARSC_FILE_NAME)
        val abcFadeInFile = PWD.file("src", "test", "resources", "abc_fade_in.xml")
        val l = mutableListOf<String>()
        ZipFile(ap_).use { zipInputFile ->
            zipInputFile.extractEntry(arscFile, ARSC_FILE_NAME)
            zipInputFile.extractEntry(abcFadeInFile, "res/anim/abc_fade_in.xml")
            for (entry in zipInputFile.entries()) {
                if (!entry.isDirectory && entry.name.startsWith("res") && entry.name != ARSC_FILE_NAME) {
                    val split = entry.name.split("/")
                    val resTypeBuilder = ProguardStringBuilder()
                    l.add("res/${split[1]}/${resTypeBuilder.generateNextProguard()}")
                }
            }
            for (s0 in l) {
                for (s1 in l) {
                    if (s0 == s1) {
                        throw IllegalStateException("有相同  $s0 $s1")
                    }
                }

            }

        }
    }

    @Test
    fun `obfuscated resource in ap file`() {
        val symbols = SymbolList.from(PWD.file("src", "test", "resources", "R.txt"))
        val ap_ = PWD.file("src", "test", "resources", "resources-debug.ap_")
//        val ignores = System.getProperty(PROPERTY_IGNORES, "").trim().split(',')
//            .filter(String::isNotEmpty)
//            .map(Wildcard.Companion::valueOf).toSet()
        assertFalse { symbols.isEmpty() }
        val optimizers = listOf(ObfuscatedResourceOptimizer())
        optimizers.forEach { it.start(null, symbols, ap_) }
        ap_.minify(optimizers)
        optimizers.forEach { it.end(ap_) }
    }

}

