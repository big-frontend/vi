package com.electrolytej.vi

import com.android.build.gradle.api.BaseVariant
import com.didiglobal.booster.gradle.assembleTaskProvider
import com.didiglobal.booster.gradle.project
import com.didiglobal.booster.task.spi.VariantProcessor
import com.google.auto.service.AutoService
import org.gradle.api.DefaultTask
import org.gradle.api.tasks.Internal
import org.gradle.api.tasks.TaskAction
import org.json.JSONObject
import java.io.File

@AutoService(VariantProcessor::class)
class ApkAnalysisVariantProcessor : VariantProcessor {
    override fun process(variant: BaseVariant) {
        val tp = variant.project.tasks.register("analyze${variant.name.capitalize()}Apk", ApkAnalyzerTask::class.java){
            it.group = "booster"
            it.variant = variant
        }
        tp.configure{
            it.dependsOn(variant.assembleTaskProvider)
        }
//        variant.project.gradle.projectsEvaluated { gradle ->
//            gradle.rootProject.allprojects {project->
//                tp.configure {
//
//                    it.dependsOn("${project.path}:assemble${variant.name.capitalize()}")
//                }
//            }
//        }
    }
}
abstract class ApkAnalyzerTask : DefaultTask() {
    @get:Internal
    lateinit var variant: BaseVariant
    @TaskAction
    fun analyze() {
        val configPath = "${project.rootDir}/apk-checker-config.json"
        var mappingFile: File? = null
        try {
            mappingFile = variant.findMappingTxtFile()
        }catch (_: Exception){ }
        val f = project.file(configPath)
        f.inputStream().bufferedReader().use { reader ->
            val config = JSONObject(reader.readText())
            config.put("--apk", variant.outputs.first().outputFile.absolutePath)
            if (mappingFile?.exists() == true){ config.put("--mappingTxt", mappingFile.absolutePath) }
            config.put("--output", "${project.buildDir}/outputs/apk-checker-result")
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
                project.findApkAnalyzer().absolutePath,
                "--config",
                configPath
            )
        }
    }
}