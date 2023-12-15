package com.jamesfchen.booster

import com.android.build.gradle.api.BaseVariant
import com.didiglobal.booster.gradle.assembleTaskProvider
import com.didiglobal.booster.gradle.project
import com.didiglobal.booster.task.spi.VariantProcessor
import com.google.auto.service.AutoService

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