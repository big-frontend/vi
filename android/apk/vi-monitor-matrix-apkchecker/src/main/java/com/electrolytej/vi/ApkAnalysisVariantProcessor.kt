package com.electrolytej.vi

import com.android.build.gradle.api.BaseVariant
import com.android.build.gradle.internal.tasks.factory.dependsOn
import com.didiglobal.booster.BOOSTER
import com.didiglobal.booster.gradle.assembleTaskProvider
import com.didiglobal.booster.gradle.project
import com.didiglobal.booster.task.spi.VariantProcessor
import com.google.auto.service.AutoService
import org.gradle.api.DefaultTask
import org.gradle.api.UnknownTaskException
import org.gradle.api.tasks.Internal
import org.gradle.api.tasks.TaskAction
import org.gradle.configurationcache.extensions.capitalized
@AutoService(VariantProcessor::class)
class ApkAnalysisVariantProcessor : VariantProcessor {
    companion object {
        private const val TASK_NAME = "listModules"
    }

    override fun process(variant: BaseVariant) {
        println("${variant.project.name}: ${variant.name}")
        variant.project.tasks.let { tasks ->
            val tp0 = tasks.register(
                "analyse${variant.name.capitalize()}ApkWithMatrix",
                ApkAnalyzerTask::class.java
            ) {
                it.group = BOOSTER
                it.variant = variant
            }
            tp0.configure {
                it.dependsOn(variant.assembleTaskProvider)
            }

            val tp1 = tasks.register(
                "report${variant.name.capitalize()}ApkAnalyseResult",
                ApkReportTask::class.java
            ) {
                it.group = BOOSTER
                it.variant = variant
            }

            tp1.configure {
                it.dependsOn(tp0)
            }

            val listModules = try {
                tasks.named(TASK_NAME)
            } catch (e: UnknownTaskException) {
                tasks.register(TASK_NAME) {
                    it.group = BOOSTER
                    it.description = "List build modules"
                }
            }
            tasks.register("list${variant.name.capitalized()}Modules", ListModules::class.java) {
                it.group = BOOSTER
                it.description = "List build modules for ${variant.name}"
                it.variant = variant
                it.outputs.upToDateWhen { false }
            }.also {
                listModules.dependsOn(it)
            }
        }

    }
}

internal open class ListModules : DefaultTask() {
    @get:Internal
    lateinit var variant: BaseVariant

    init {

    }

    @TaskAction
    fun list() {
//        val code = variant.getResolvedCode()
        val res = variant.getResolvedRes()
        val assets = variant.getResolvedAssets()
        val r = variant.getSymbolList()
        for ((name, symbols) in r) {
            if (symbols.isEmpty()) {
                println("R收集 ${name} Inline R symbols failed: R.txt doesn't exist or blank")
                continue
            }
            println("R收集 ${name}  ${symbols}")
//            symbols.forEach {
//                println("  - R.id.$it")
//            }
        }
    }
}
