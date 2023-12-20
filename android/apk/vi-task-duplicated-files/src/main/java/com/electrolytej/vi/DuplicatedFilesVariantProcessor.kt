package com.electrolytej.vi

import com.android.SdkConstants
import com.android.build.gradle.api.BaseVariant
import com.android.build.gradle.internal.tasks.factory.dependsOn
import com.didiglobal.booster.build.Build
import com.didiglobal.booster.compression.CompressionReport
import com.didiglobal.booster.compression.CompressionResult
import com.didiglobal.booster.compression.CompressionResults
import com.didiglobal.booster.gradle.processResTaskProvider
import com.didiglobal.booster.gradle.processedRes
import com.didiglobal.booster.gradle.project
import com.didiglobal.booster.kotlinx.file
import com.didiglobal.booster.kotlinx.search
import com.didiglobal.booster.kotlinx.touch
import com.didiglobal.booster.task.spi.VariantProcessor
import com.didiglobal.booster.transform.util.transform
import com.google.auto.service.AutoService
import org.apache.commons.compress.archivers.zip.ZipArchiveEntry
import org.gradle.api.DefaultTask
import org.gradle.api.tasks.Internal
import org.gradle.api.tasks.TaskAction
import java.io.File
import java.text.DecimalFormat
import java.util.zip.ZipEntry
import java.util.zip.ZipFile

@AutoService(VariantProcessor::class)
class DuplicatedFilesVariantProcessor : VariantProcessor {
    override fun process(variant: BaseVariant) {
        val compress = variant.project.tasks.register(
            "remove${variant.name.capitalize()}DuplicatedFiles", RemoveDuplicatedFiles::class.java
        ) {
            it.group = "booster"
            it.description = "remove duplicated files for ${variant.name}"
            it.variant = variant
        }
        variant.processResTaskProvider?.let { processRes ->
            compress.dependsOn(processRes)
            processRes.configure {
                it.finalizedBy(compress)
            }
        }

    }
}

internal abstract class RemoveDuplicatedFiles : DefaultTask() {

    @get:Internal
    lateinit var variant: BaseVariant

    @TaskAction
    fun compress() {
        val mapOfDuplicatesReplacements: MutableMap<String, String> = HashMap()
        //1.find duplicated files
        variant.findDuplicatedFiles(mapOfDuplicatesReplacements)
        //2.remove duplicated files
        removeDuplicatedFiles(mapOfDuplicatesReplacements)

//        val results = CompressionResults()
//        variant.compressProcessedRes(results)
//        variant.generateReport(results)
    }

    fun removeDuplicatedFiles(mapOfDuplicatesReplacements: MutableMap<String, String> = HashMap()) {
//        val replaceIterator = mapOfDuplicatesReplacements.keys.iterator()
//        while (replaceIterator.hasNext()) {
//            val sourceFile = replaceIterator.next()
//            val sourceRes = ApkUtil.entryToResourceName(sourceFile)
//            val sourceId = mapOfResources[sourceRes]!!
//            val targetFile = mapOfDuplicatesReplacements[sourceFile]
//            val targetRes = ApkUtil.entryToResourceName(targetFile)
//            val targetId = mapOfResources[targetRes]!!
//            val success = ArscUtil.replaceFileResource(resTable, sourceId, sourceFile, targetId, targetFile)
//            if (!success) {
////                Log.w(TAG, "replace %s(%s) with %s(%s) failed!", sourceRes, sourceFile, targetRes, targetFile)
//                replaceIterator.remove()
//            }
//        }
    }

}

private fun BaseVariant.findDuplicatedFiles(results: MutableMap<String, String> = HashMap()) {
    val files = processedRes.search {
        it.name.startsWith(SdkConstants.FN_RES_BASE) && it.extension == SdkConstants.EXT_RES
    }
    files.parallelStream().forEach { ap_ ->
        val s0 = ap_.length()
        ZipFile(ap_).use {
            val asIterator = it.entries().asIterator()

        }
//        ap_.repack {
//            !NO_COMPRESS.contains(it.name.substringAfterLast('.'))
//        }
        val s1 = ap_.length()
//        results.add(CompressionResult(ap_, s0, s1, ap_))
    }
}

private fun File.repack(shouldCompress: (ZipEntry) -> Boolean) {
    val dest = File.createTempFile(
        SdkConstants.FN_RES_BASE + SdkConstants.RES_QUALIFIER_SEP, SdkConstants.DOT_RES
    )

    ZipFile(this).use {
        it.transform(dest, { origin: ZipEntry ->
            ZipArchiveEntry(origin).apply {
                method = if (shouldCompress(origin)) ZipEntry.DEFLATED else origin.method
            }
        })
    }

    if (this.delete()) {
        if (!dest.renameTo(this)) {
            dest.copyTo(this, true)
        }
    }
}

private fun BaseVariant.generateReport(results: CompressionResults) {
    val base = project.buildDir.toURI()
    val table = results.map {
        val delta = it.second - it.third
        CompressionReport(
            base.relativize(it.first.toURI()).path,
            it.second,
            it.third,
            delta,
            if (delta == 0L) "0" else decimal(delta),
            if (delta == 0L) "0%" else percentage((delta).toDouble() * 100 / it.second),
            decimal(it.second),
            it.fourth
        )
    }
    val maxWith1 = table.maxOfOrNull { it.first.length } ?: 0
    val maxWith5 = table.maxOfOrNull { it.fifth.length } ?: 0
    val maxWith6 = table.maxOfOrNull { it.sixth.length } ?: 0
    val maxWith7 = table.maxOfOrNull { it.seventh.length } ?: 0
    val fullWith = maxWith1 + maxWith5 + maxWith6 + 8

    project.buildDir.file("reports", Build.ARTIFACT, name, "report.txt").touch().printWriter()
        .use { logger ->
            // sort by reduced size and original size
            table.sortedWith(compareByDescending<CompressionReport> {
                it.fourth
            }.thenByDescending {
                it.second
            }).forEach {
                logger.println(
                    "${it.sixth.padStart(maxWith6)} ${it.first.padEnd(maxWith1)} ${
                        it.fifth.padStart(
                            maxWith5
                        )
                    } ${it.seventh.padStart(maxWith7)} ${it.eighth}"
                )
            }
            logger.println("-".repeat(maxWith1 + maxWith5 + maxWith6 + 2))
            logger.println(" TOTAL ${decimal(table.sumOf { it.fourth.toDouble() }).padStart(fullWith - 13)}")
        }

}

internal val percentage: (Number) -> String = DecimalFormat("#,##0.00'%'")::format

internal val decimal: (Number) -> String = DecimalFormat("#,##0")::format