package com.electrolytej.vi

import com.android.SdkConstants
import com.android.build.gradle.api.BaseVariant
import com.android.build.gradle.internal.tasks.factory.dependsOn
import com.didiglobal.booster.gradle.getProperty
import com.didiglobal.booster.gradle.getReport
import com.didiglobal.booster.gradle.processResTaskProvider
import com.didiglobal.booster.gradle.processedRes
import com.didiglobal.booster.gradle.project
import com.didiglobal.booster.gradle.symbolList
import com.didiglobal.booster.kotlinx.Wildcard
import com.didiglobal.booster.kotlinx.search
import com.didiglobal.booster.kotlinx.touch
import com.didiglobal.booster.task.spi.VariantProcessor
import com.google.auto.service.AutoService
import org.gradle.api.DefaultTask
import org.gradle.api.logging.Logging
import org.gradle.api.tasks.Internal
import org.gradle.api.tasks.TaskAction
import pink.madis.apk.arsc.ResourceFile
import java.io.File
import java.io.FileInputStream
import java.io.PrintWriter
import java.text.DecimalFormat
import java.util.zip.ZipEntry
import java.util.zip.ZipFile


private val logger_ = Logging.getLogger(DuplicatedFilesVariantProcessor::class.java)

@AutoService(VariantProcessor::class)
class DuplicatedFilesVariantProcessor : VariantProcessor {
    override fun process(variant: BaseVariant) {
        val removeDuplicatedFiles = variant.project.tasks.register(
            "remove${variant.name.capitalize()}DuplicatedFiles", RemoveDuplicatedFiles::class.java
        ) {
            it.group = "booster"
            it.description = "remove duplicated files for ${variant.name}"
            it.variant = variant
        }
        variant.processResTaskProvider?.let { processRes ->
            removeDuplicatedFiles.dependsOn(processRes)
            processRes.configure {
                it.finalizedBy(removeDuplicatedFiles)
            }
        }
    }
}

internal abstract class RemoveDuplicatedFiles : DefaultTask() {

    @get:Internal
    lateinit var variant: BaseVariant
    private lateinit var symbols: SymbolList
    private lateinit var logger: PrintWriter
    private val PROPERTY_IGNORES = "vi.optimizer.duplicated.files.ignores"
    private lateinit var ignores: Set<Wildcard>

    @TaskAction
    fun remove() {
        this.symbols = SymbolList.from(variant.symbolList.single())
        this.ignores = project.getProperty(PROPERTY_IGNORES, "").trim().split(',')
            .filter(String::isNotEmpty)
            .map(Wildcard.Companion::valueOf).toSet()

        this.logger = variant.getReport("vi-optimizer-duplicated-files", "report.txt").touch().printWriter()
        logger.use {
            if (this.symbols.isEmpty()) {
                logger_.error("remove duplicated files failed: R.txt doesn't exist or blank")
                logger.println("Inlining R symbols failed: R.txt doesn't exist or blank")
                return
            }
            logger.println("$PROPERTY_IGNORES=$ignores\n")
            val files = variant.processedRes.search {
                it.name.startsWith(SdkConstants.FN_RES_BASE) && it.extension == SdkConstants.EXT_RES
            }
            files.parallelStream().forEach { ap_ ->
                //    val dest = File.createTempFile(SdkConstants.FN_RES_BASE + SdkConstants.RES_QUALIFIER_SEP, SdkConstants.DOT_RES)
                //1.find duplicated files from ap file
                val mapOfDuplicatesReplacements = mutableMapOf<String, Triple<Long, Long, String>>()
                ZipFile(ap_).use {
                    it.findDuplicatedFiles {
                        filter = { entry ->
                            val ign = ignores.any { it.matches(entry.name) }
                            if (ign) {
                                logger.println("Ignore `${entry.name}`")
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
                logger.println("Delete files:")
                if (mapOfDuplicatesReplacements.isNotEmpty()) {
                    //2.remove duplicated files  and repack ap file
                    total = ap_.removeDuplicatedFiles(symbols, mapOfDuplicatesReplacements)
                    mapOfDuplicatesReplacements.forEach { dup, (crc32, size, replace) ->
                        val (srcResId, srcResType, srcResName) = dup.entryToResource()
                        val srcResIdInt = symbols.getInt(srcResType, srcResName)
                        val (destResId, destResType, destResName) = replace.entryToResource()
                        val destResIdInt = symbols.getInt(destResType, destResName)
                        logger.println(" * replace 0x${srcResIdInt.toString(16)}($dup) with 0x${destResIdInt.toString(16)}($replace)\t${size}bytes crc32/$crc32")
                    }
                }
                val s1 = ap_.length()
                logger.println("-".repeat(maxWidth))
                logger.println("Total: $total bytes, ap length: ${s0 - s1} bytes")
            }
        }
    }

}

const val ARSC_FILE_NAME = "resources.arsc"

fun File.removeDuplicatedFiles(
    symbols: SymbolList, mapOfDuplicatesReplacements: MutableMap<String, Triple<Long, Long, String>>
): Long {
    val shrunkApFile = File(parent, "${name}_shrunk")
    shrunkApFile.deleteRecursivelyIfExists()
    shrunkApFile.mkdir()
    val arscFile = File(shrunkApFile, ARSC_FILE_NAME)
    var total = 0L
    val compressedEntry = HashSet<String>()
    ZipFile(this).use { zipInputFile ->
        zipInputFile.extractEntry(arscFile, ARSC_FILE_NAME)
        val destArscFile = File(shrunkApFile, "shrinked_${ARSC_FILE_NAME}")
        FileInputStream(arscFile).use { arscStream ->
            val resourceFile = ResourceFile.fromInputStream(arscStream)
            val replaceIterator = mapOfDuplicatesReplacements.keys.iterator()
            while (replaceIterator.hasNext()) {
                val srcEntryName = replaceIterator.next()
                val (srcResId, srcResType, srcResName) = srcEntryName.entryToResource()
                val srcResIdInt = symbols.getInt(srcResType, srcResName)
                val (crc32, size, destEntryName) = mapOfDuplicatesReplacements[srcEntryName] ?: continue
                val (destResId, destResType, destResName) = destEntryName.entryToResource()
                val destResIdInt = symbols.getInt(destResType, destResName)
                val sourcePkgId = srcResIdInt.getPackageId()
                val targetPkgId = destResIdInt.getPackageId()
                var success = false
                success = if (sourcePkgId != targetPkgId) {
                    System.out.printf("sourcePkgId %d != targetPkgId %d, quit replace!%n", sourcePkgId, targetPkgId)
                    false
                }else{
                    resourceFile.replaceFileResource(srcEntryName, destEntryName)
                }
                if (!success) {
//            logger.println("replace ${srcResId}($srcEntryName) with $destResId($destEntryName) failed!")
                    logger_.error("replace ${srcResId}($srcEntryName) with $destResId($destEntryName) failed!")
                    replaceIterator.remove()
                } else {
//            logger_.warn(" - replace $srcEntryName with $destEntryName\t$size bytes $crc32")
                    total += size
                }
            }
            destArscFile.outputStream().use {
                it.write(resourceFile.toByteArray())
                it.flush()
            }
        }
        if (arscFile.delete()) {
            if (!destArscFile.renameTo(arscFile)) {
                destArscFile.copyTo(arscFile, overwrite = true)
                destArscFile.delete()
            }
        }
        zipInputFile.extractEntries {
            destDir = shrunkApFile
            filter = { zipEntry ->
                !mapOfDuplicatesReplacements.containsKey(zipEntry.name) && zipEntry.name != ARSC_FILE_NAME
            }
            foreach = { zipEntry, destFile ->
                if (zipEntry.name.startsWith("res/")) {
                    val (s, s1, s2) = zipEntry.entryToResource()
                    if (s.isNotEmpty()) {
                        if (zipEntry.method == ZipEntry.DEFLATED) {
                            compressedEntry.add(zipEntry.name)
                        }
//                    logger_.warn("unzip ${zipEntry.name} to file ${destFile}")
                    } else {
                        logger_.error("parse entry ${zipEntry.name} resource name failed!")
                    }
                } else {
                    if (zipEntry.method == ZipEntry.DEFLATED) {
                        compressedEntry.add(zipEntry.name)
                    }
//                logger_.warn("unzip ${zipEntry.name} to file ${destFile}")
                }
            }
        }
    }
    val destFile = File(parentFile, "tmp")
    shrunkApFile.zipFile(destFile) { zipEntry ->
        compressedEntry.contains(zipEntry.name)
    }
    if (delete()) {
        if (!destFile.renameTo(this)) {
            destFile.copyTo(this, overwrite = true)
        }
    }
    return total
}

fun File.deleteRecursivelyIfExists() {
    if (!exists()) return
    deleteRecursively()
}

//private fun BaseVariant.generateReport(results: CompressionResults) {
//    val base = project.buildDir.toURI()
//    val table = results.map {
//        val delta = it.second - it.third
//        CompressionReport(
//            base.relativize(it.first.toURI()).path,
//            it.second,
//            it.third,
//            delta,
//            if (delta == 0L) "0" else decimal(delta),
//            if (delta == 0L) "0%" else percentage((delta).toDouble() * 100 / it.second),
//            decimal(it.second),
//            it.fourth
//        )
//    }
//    val maxWith1 = table.maxOfOrNull { it.first.length } ?: 0
//    val maxWith5 = table.maxOfOrNull { it.fifth.length } ?: 0
//    val maxWith6 = table.maxOfOrNull { it.sixth.length } ?: 0
//    val maxWith7 = table.maxOfOrNull { it.seventh.length } ?: 0
//    val fullWith = maxWith1 + maxWith5 + maxWith6 + 8
//
//    project.buildDir.file("reports", Build.ARTIFACT, name, "report.txt").touch().printWriter()
//        .use { logger ->
//            // sort by reduced size and original size
//            table.sortedWith(compareByDescending<CompressionReport> {
//                it.fourth
//            }.thenByDescending {
//                it.second
//            }).forEach {
//                logger.println(
//                    "${it.sixth.padStart(maxWith6)} ${it.first.padEnd(maxWith1)} ${
//                        it.fifth.padStart(
//                            maxWith5
//                        )
//                    } ${it.seventh.padStart(maxWith7)} ${it.eighth}"
//                )
//            }
//            logger.println("-".repeat(maxWith1 + maxWith5 + maxWith6 + 2))
//            logger.println(" TOTAL ${decimal(table.sumOf { it.fourth.toDouble() }).padStart(fullWith - 13)}")
//        }
//
//}

internal val percentage: (Number) -> String = DecimalFormat("#,##0.00'%'")::format

internal val decimal: (Number) -> String = DecimalFormat("#,##0")::format