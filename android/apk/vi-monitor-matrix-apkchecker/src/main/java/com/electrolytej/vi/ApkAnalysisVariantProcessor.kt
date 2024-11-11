package com.electrolytej.vi

import com.android.build.gradle.BaseExtension
import com.android.build.gradle.api.BaseVariant
import com.android.build.gradle.internal.publishing.AndroidArtifacts
import com.android.builder.model.v2.ide.AndroidArtifact
import com.didiglobal.booster.gradle.assembleTaskProvider
import com.didiglobal.booster.gradle.packageBundleTaskProvider
import com.didiglobal.booster.gradle.project
import com.didiglobal.booster.gradle.symbolList
import com.didiglobal.booster.kotlinx.OS
import com.didiglobal.booster.kotlinx.matches
import com.didiglobal.booster.task.spi.VariantProcessor
import com.google.auto.service.AutoService
import org.apache.tools.ant.taskdefs.condition.Os
import org.gradle.api.DefaultTask
import org.gradle.api.Project
import org.gradle.api.artifacts.Dependency
import org.gradle.api.tasks.Internal
import org.gradle.api.tasks.TaskAction
import org.json.JSONObject
import java.io.File

@AutoService(VariantProcessor::class)
class ApkAnalysisVariantProcessor : VariantProcessor {
    override fun process(variant: BaseVariant) {
        val tp0 = variant.project.tasks.register("analyse${variant.name.capitalize()}ApkWithMatrix", ApkAnalyzerTask::class.java){
            it.group = "booster"
            it.variant = variant
        }
        tp0.configure{
            it.dependsOn(variant.assembleTaskProvider)
        }

        val tp1 = variant.project.tasks.register("report${variant.name.capitalize()}ApkAnalyseResult", ApkReportTask::class.java){
            it.group = "booster"
            it.variant = variant
        }

        tp1.configure{
            it.dependsOn(tp0)
        }
    }
}
abstract class ApkReportTask : DefaultTask() {
    @get:Internal
    lateinit var variant: BaseVariant
    @TaskAction
    fun report() {

    }
}

/**
 * 分析 整包 + 模块
 */
abstract class ApkAnalyzerTask : DefaultTask() {
    @get:Internal
    lateinit var variant: BaseVariant
    @TaskAction
    fun analyze() {
        val configPath = "${project.rootDir}/apk-checker-config.json"
        val mappingFile = variant.mappingFileProvider?.get()?.singleFile
        val f = project.file(configPath)
        f.inputStream().bufferedReader().use { reader ->
            val config = JSONObject(reader.readText())
            if (!config.has("--apk")) config.put("--apk", variant.outputs.first().outputFile.absolutePath)
            if (mappingFile?.exists() == true){ config.put("--mappingTxt", mappingFile.absolutePath) }
            if (!config.has("--output")) config.put("--output", "${project.buildDir}/outputs/apk-checker-result")
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


        //        getResolvedArtifacts(variant,AndroidArtifacts.ConsumedConfigType.RUNTIME_CLASSPATH)
//        variant.project.extensions.apk
//        Dependency.APK
    }
    fun findApkAnalyzer() =
        Jar.getResourceAsFile("/matrix-apk-canary-2.0.8.jar", ApkAnalysisVariantProcessor::class.java)
}

fun BaseVariant.findRTxtFile(): File {
    return symbolList.singleFile
}

fun Project.findToolnm(): File? {
    val extension = (extensions.findByType(BaseExtension::class.java) as BaseExtension)
    val adb = extension.adbExecutable
    val ndkVersion = extension.ndkVersion
    if (ndkVersion.isNullOrEmpty()){
        return null
    }
//    val SO_ARCH = 'arm-linux-androideabi'
    val SO_ARCH = "aarch64-linux-android"
    val platform = if (Os.isFamily(Os.FAMILY_WINDOWS)){
        "windows-x86_64"
    }else if (Os.isFamily(Os.FAMILY_MAC) || OS.isMac()){
        "darwin-x86_64"
    }else{
        "linux-x86_64"
    }
    val nm = if (Os.isFamily(Os.FAMILY_WINDOWS)){
        "${SO_ARCH}-nm.exe"
    }else if (Os.isFamily(Os.FAMILY_MAC) || OS.isMac()){
        "${SO_ARCH}-nm"
    }else{
        "${SO_ARCH}-nm"
    }
    return File(adb.parentFile.parentFile,"ndk").resolve(ndkVersion).resolve("toolchains")
        .resolve("${SO_ARCH}-4.9").resolve("prebuilt").resolve(platform).resolve("bin").resolve(nm)
}

