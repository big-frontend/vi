package com.electrolytej.vi

import com.android.build.gradle.AppExtension
import com.android.build.gradle.BaseExtension
import com.android.build.gradle.LibraryExtension
import com.didiglobal.booster.kotlinx.Wildcard
import com.didiglobal.booster.kotlinx.file
import java.io.File
import kotlin.test.Test
import kotlin.test.assertFalse
import com.android.build.gradle.api.BaseVariant
import com.didiglobal.booster.gradle.getAndroid
import org.gradle.testfixtures.ProjectBuilder
import java.util.zip.ZipFile

val PWD = File(System.getProperty("user.dir"))

class ApFileTest {
    private val PROPERTY_IGNORES = "vi.optimizer.duplicated.files.ignores"

    @Test
    fun `remove dup file in ap file`() {
        val symbols = SymbolList.from(PWD.file("src", "test", "resources", "R.txt"))
        val ap_ = PWD.file("src", "test", "resources", "resources-debug.ap_")
        val ignores = System.getProperty(PROPERTY_IGNORES, "").trim().split(',')
            .filter(String::isNotEmpty)
            .map(Wildcard.Companion::valueOf).toSet()
        assertFalse { symbols.isEmpty() }
                        val optimizers = listOf(DuplicatedFilesOptimizer())
                //    val dest = File.createTempFile(SdkConstants.FN_RES_BASE + SdkConstants.RES_QUALIFIER_SEP, SdkConstants.DOT_RES)
                optimizers.forEach { it.start(null, symbols, ap_) }
                ap_.minify(optimizers)
                optimizers.forEach { it.end(ap_) }
    }
    @Test
    fun `remove dup file in assets`(){
        val apk = PWD.file("src", "test", "resources", "app-release.apk")
        val mapOfDuplicatesReplacements = mutableMapOf<String, Triple<Long, Long, String>>()
        var total = 0L
        ZipFile(apk).use {
            it.findDuplicatedFiles(
                filter = { entry ->
                    entry.name.startsWith("assets/")
                },
                each = { dup, replace ->
                    println(" * replace ${dup} with $replace\t${dup.size}bytes crc32/${dup.crc}")
                    mapOfDuplicatesReplacements[dup.name] = Triple(dup.crc, dup.size, replace.name)
                    total +=dup.size
                })
        }
        println("total : $total bytes")

    }

}