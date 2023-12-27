package com.electrolytej.vi

import com.didiglobal.booster.gradle.getProperty
import com.didiglobal.booster.kotlinx.Wildcard
import com.didiglobal.booster.kotlinx.file
import java.io.File
import java.util.zip.ZipFile
import kotlin.test.Test
import kotlin.test.assertFalse

val PWD = File(System.getProperty("user.dir"))

class ApFileTest {
    private val PROPERTY_IGNORES = "vi.optimizer.duplicated.files.ignores"
    @Test
    fun `remove dup file in ap file`() {
        val symbols = SymbolList.from(PWD.file("src", "test", "resources", "R.txt"))
        val ignores = System.getProperty(PROPERTY_IGNORES, "").trim().split(',')
            .filter(String::isNotEmpty)
            .map(Wildcard.Companion::valueOf).toSet()
        assertFalse { symbols.isEmpty() }
        val ap_ = PWD.file("src", "test", "resources", "resources-debug.ap_")
        val mapOfDuplicatesReplacements = mutableMapOf<String, Triple<Long, Long, String>>()
        ZipFile(ap_).use {
            it.findDuplicatedFiles {
                filter = { entry ->
                    val entryName = entry.name
                    entryName.startsWith("res/") && !ignores.any { it.matches(entryName) }
                }
                foreach = { dup, replace ->
                    mapOfDuplicatesReplacements[dup.name] =
                        Triple(dup.crc, dup.size, replace.name)
                }
            }
        }
        if (mapOfDuplicatesReplacements.isNotEmpty()) {
            //2.remove duplicated files  and repack ap file
            val s0 = ap_.length()
            val total = ap_.removeDuplicatedFiles(symbols, mapOfDuplicatesReplacements)
            val s1 = ap_.length()
//                results.add(CompressionResult(ap_, s0, s1, ap_))
            println("Delete files:")
            mapOfDuplicatesReplacements.forEach { dup, (crc32, size, replace) ->
                println(" * replace $dup with $replace\t$size $crc32")
            }
            val maxWidth = mapOfDuplicatesReplacements.map { it.key.length }.maxOrNull()?.plus(10) ?: 10
            println("-".repeat(maxWidth))
            println("Total: $total bytes, ap length: ${s0-s1} bytes")
        }
    }

}
