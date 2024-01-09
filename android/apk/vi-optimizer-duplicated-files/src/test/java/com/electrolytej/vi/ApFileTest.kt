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

val PWD = File(System.getProperty("user.dir"))

class ApFileTest {
    private val PROPERTY_IGNORES = "vi.optimizer.duplicated.files.ignores"

    @Test
    fun `remove dup file in ap file`() {
        val symbols = SymbolList.from(PWD.file("src", "test", "resources", "R.txt"))
        val ap_ = PWD.file("src", "test", "resources", "resources-heytapPureRelease.ap_")
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

}
