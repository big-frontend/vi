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
        //1.find duplicated files from ap file
        val mapOfDuplicatesReplacements = mutableMapOf<String, Triple<Long, Long, String>>()
        ZipFile(ap_).use {
            it.findDuplicatedFiles {
                filter = { entry ->
                    val ign = ignores.any { it.matches(entry.name) }
                    if (ign) {
                        println("Ignore `${entry.name}`")
                    }
                    entry.name.startsWith("res/") && !ign
                }
                foreach = { dup, replace ->
                    mapOfDuplicatesReplacements[dup.name] =
                        Triple(dup.crc, dup.size, replace.name)
                }
            }
        }
        val maxWidth = mapOfDuplicatesReplacements.map { it.key.length }.maxOrNull()?.plus(10) ?: 10
        var total = 0L
        val s0 = ap_.length()
        println("Delete files:")
        if (mapOfDuplicatesReplacements.isNotEmpty()) {
            //2.remove duplicated files  and repack ap file
            total = ap_.removeDuplicatedFiles(symbols, mapOfDuplicatesReplacements)
            mapOfDuplicatesReplacements.forEach { dup, (crc32, size, replace) ->
                println(" * replace $dup with $replace\t$size bytes $crc32")
            }
        }
        val s1 = ap_.length()
        println("-".repeat(maxWidth))
        println("Total: $total bytes, ap length: ${s0 - s1} bytes")
    }

}
