package com.electrolytej.vi

import com.android.SdkConstants
import com.android.build.gradle.api.BaseVariant
import com.android.build.gradle.internal.tasks.factory.dependsOn
import com.didiglobal.booster.annotations.Priority
import com.didiglobal.booster.gradle.processResTaskProvider
import com.didiglobal.booster.gradle.processedRes
import com.didiglobal.booster.gradle.project
import com.didiglobal.booster.gradle.symbolList
import com.didiglobal.booster.kotlinx.search
import com.didiglobal.booster.task.spi.VariantProcessor
import com.google.auto.service.AutoService
import org.gradle.api.DefaultTask
import org.gradle.api.tasks.Internal
import org.gradle.api.tasks.TaskAction
import pink.madis.apk.arsc.ResourceFile
import java.io.File
import java.io.FileInputStream
import java.text.DecimalFormat
import java.util.ServiceLoader
import java.util.zip.ZipEntry
import java.util.zip.ZipFile


@AutoService(VariantProcessor::class)
class MinifyResourceVariantProcessor : VariantProcessor {
    override fun process(variant: BaseVariant) {

        val minifyApFile = variant.project.tasks.register(
            "minify${variant.name.capitalize()}ApFile", MinifyApFiles::class.java
        ) {
            it.group = "booster"
            it.description = "minify ap file for ${variant.name}"
            it.variant = variant
        }
        variant.processResTaskProvider?.let { processRes ->
            minifyApFile.dependsOn(processRes)
            processRes.configure {
                it.finalizedBy(minifyApFile)
            }
        }
//        val removeUnusedFiles = variant.project.tasks.register(
//            "remove${variant.name.capitalize()}UnusedFiles", RemoveUnusedFiles::class.java
//        ) {
//            it.group = "booster"
//            it.description = "remove unused files for ${variant.name}"
//            it.variant = variant
//        }
//        variant.packageTaskProvider?.let { packageApk ->
//            packageApk.configure {
//                it.doFirst {
//                }
//            }
//        }
    }
}

//internal abstract class RemoveUnusedFiles : DefaultTask() {
//    @get:Internal
//    lateinit var variant: BaseVariant
//
//}

open class MinifyApFiles : DefaultTask() {

    @get:Internal
    lateinit var variant: BaseVariant
    @TaskAction
    fun minify() {
        val symbols = SymbolList.from(variant.symbolList.single())
        val optimizers = ServiceLoader.load(BaseOptimizer::class.java, Thread.currentThread().contextClassLoader)
                .sortedBy {
                    it.javaClass.getAnnotation(Priority::class.java)?.value ?: 0
                }
        val files = variant.processedRes.search {
            it.name.startsWith(SdkConstants.FN_RES_BASE) && it.extension == SdkConstants.EXT_RES
        }
        files.parallelStream().forEach { ap_ ->
            //    val dest = File.createTempFile(SdkConstants.FN_RES_BASE + SdkConstants.RES_QUALIFIER_SEP, SdkConstants.DOT_RES)
            optimizers.forEach {
                it.start(variant, symbols, ap_)
            }
            ap_.minify(optimizers)
            optimizers.forEach { it.end(ap_) }
        }
    }
}

const val ARSC_FILE_NAME = "resources.arsc"
const val MANIFEST_FILE_NAME = "AndroidManifest.xml"
fun File.minify(optimizers: List<BaseOptimizer>) {
    val shrunkApDir = File(this.parent, "${this.name}_shrunk")
    shrunkApDir.deleteRecursivelyIfExists()
    shrunkApDir.mkdir()
    val compressedEntry = HashSet<String>()
    ZipFile(this).use { zipInputFile ->
        for (zipEntry in zipInputFile.entries()) {
            if (zipEntry.name.startsWith("res/")) {
                val (s, s1, s2) = zipEntry.entryToResource()
                if (zipEntry.method == ZipEntry.DEFLATED) {
                    compressedEntry.add(zipEntry.name)
                }
//                    logger_.warn("unzip ${zipEntry.name} to file ${destFile}")
                optimizers.forEach {
                    if (it.processRes(zipInputFile, shrunkApDir, zipEntry)) {
                        return@forEach
                    }
                }
            } else if (zipEntry.name.equals(ARSC_FILE_NAME)) {
                val arscFile = File(shrunkApDir, ARSC_FILE_NAME)
                zipInputFile.extractEntry(arscFile, ARSC_FILE_NAME)
                val destArscFile = File(shrunkApDir, "shrinked_${ARSC_FILE_NAME}")
                FileInputStream(arscFile).use { arscStream ->
                    val resourceFile = ResourceFile.fromInputStream(arscStream)
                    optimizers.forEach {
                        if (it.processArsc(resourceFile)) {
                            return@forEach
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

            } else if (zipEntry.name.equals(MANIFEST_FILE_NAME)) {
                if (zipEntry.method == ZipEntry.DEFLATED) {
                    compressedEntry.add(zipEntry.name)
                }
//                logger_.warn("unzip ${zipEntry.name} to file ${destFile}")
                val androidManifestFile = File(shrunkApDir, MANIFEST_FILE_NAME)
                zipInputFile.extractEntry(androidManifestFile, zipEntry)
//                optimizers.forEach {
//                    if (it.processAndroidManifest(androidManifestFile)) {
//                        return@forEach
//                    }
//                }
            } else {
                println("ap file:other ${zipEntry.name}")
            }
        }
    }
    val destFile = File(this.parentFile, "tmp")
    shrunkApDir.zipFile(destFile) { zipEntry ->
        compressedEntry.contains(zipEntry.name)
    }
    if (this.delete()) {
        if (!destFile.renameTo(this)) {
            destFile.copyTo(this, overwrite = true)
        }
    }
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