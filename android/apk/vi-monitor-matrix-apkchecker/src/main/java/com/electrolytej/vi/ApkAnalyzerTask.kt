package com.electrolytej.vi

import com.android.build.gradle.api.BaseVariant
import com.didiglobal.booster.kotlinx.touch
import org.gradle.api.DefaultTask
import org.gradle.api.tasks.Internal
import org.gradle.api.tasks.TaskAction
import org.json.JSONObject
import java.io.File


/**
 * 分析 整包 + 模块
 */
abstract class ApkAnalyzerTask : DefaultTask() {
    @get:Internal
    lateinit var variant: BaseVariant

    @TaskAction
    fun analyze() {
        val logger = project.getReport("resolved-artifacts.txt").touch().printWriter()
        val res = variant.getResolvedRes()
        for ((name, paths) in res) {
            println("res收集 ${name}  ${paths}")
            if (paths.isEmpty()) continue
            logger.println("$name res收集")
            paths.forEach { path ->
                logger.println(" * $path")
            }
        }
        val assets = variant.getResolvedAssets()
        for ((name, paths) in assets) {
            println("assets收集 ${name}  ${paths}")
            if (paths.isEmpty()) continue
            logger.println("$name assets收集")
            paths.forEach { path ->
                logger.println(" * $path")
            }
        }
        logger.close()

        val configPath = "${project.rootDir}/apk-checker-config.json"
        var mappingFile: File?
        try {
            mappingFile = variant.mappingFileProvider?.get()?.singleFile
        } catch (e: Exception) {
            mappingFile = variant.mappingFile

        }
        val f = project.file(configPath)
        f.inputStream().bufferedReader().use { reader ->
            val config = JSONObject(reader.readText())
//            if (!config.has("--apk")) { }
            config.put("--apk", variant.outputs.first().outputFile.absolutePath)
            if (mappingFile?.exists() == true) {
                config.put("--mappingTxt", mappingFile.absolutePath)
            }
            config.put(
                "--resolvedArtifactsTxt",
                "${project.buildDir}/reports/apk-checker/resolved-artifacts.txt"
            )
//            if (!config.has("--output")){}
            config.put("--output", "${project.buildDir}/reports/apk-checker/apk-checker-result")
            val options = config.optJSONArray("options")
            for (i in 0 until options.length()) {
                val o = options.getJSONObject(i)
                if (o["name"] == "-checkMultiSTL") {
                    o.put("--toolnm", project.findToolnm()?.absolutePath)
                } else if (o["name"] == "-unusedResources") {
                    o.put("--rTxt", variant.findRTxtFile().absolutePath)
                } else if (o["name"] == "-unstrippedSo") {
                    o.put("--toolnm", project.findToolnm()?.absolutePath)
                }
            }
            f.outputStream().bufferedWriter().use { writer ->
                writer.write(config.toString(4))
            }
        }
        project.javaexec {
            it.main = "-version"
        }
        //https://bugs.openjdk.org/browse/JDK-8211795
        //ArrayIndexOutOfBoundsException in PNGImageReader的问题在"11.0.16被fixed
        project.javaexec {
            it.main = "-jar"
            it.args = listOf(
                findApkAnalyzer().absolutePath,
                "--config",
                configPath
            )
        }
    }

    fun findApkAnalyzer() =
        Jar.getResourceAsFile(
            "/matrix-apk-canary-2.0.9.jar",
            ApkAnalysisVariantProcessor::class.java
        )
}