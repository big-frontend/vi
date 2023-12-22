package com.electrolytej.vi

import com.android.build.gradle.AppExtension
import com.android.build.gradle.api.ApplicationVariant
import com.android.build.gradle.api.BaseVariant
import com.didiglobal.booster.gradle.project
import com.didiglobal.booster.task.spi.VariantProcessor
import org.gradle.api.Project
import org.gradle.api.artifacts.ProjectDependency
import java.io.File

abstract class BaseSymbolVariantProcessor : VariantProcessor{
    override fun process(variant: BaseVariant) {
        val project = variant.project
        project.afterEvaluate {
            val appExtension = project.extensions.getByType(AppExtension::class.java)
            appExtension.applicationVariants.forEach { variant ->
                val variantName = variant.name.capitalize()//DevPhoneRelease
                project.tasks.create("report${variantName}Mapping")
                    .apply { group = "booster" }.doLast {
                        val mappingFile = variant.mappingFileProvider.get().singleFile
                        if (!mappingFile.exists()) throw  IllegalArgumentException("mapping文件不存在 $mappingFile")
                        uploadMapping(variant, mappingFile)
                    }
                project.tasks.create("report${variantName}Symtab")
                    .apply { group = "booster" }.doLast {
                        //                if (containTask(project.rootProject, "package${variantName}JniLibs")) {
//                    soTask.dependsOn project . tasks ["package${variantName}JniLibs"]
//                } else if (containTask(project.rootProject, "externalNativeBuild${variantName}")) {
//                    soTask.dependsOn project . tasks ["externalNativeBuild${variantName}"]
//                }
                        val symtabs = mutableListOf<File>()
                        // Get debug SO files of dependent project.
                        project.configurations.getByName("implementation").dependencies.withType(
                            ProjectDependency::class.java
                        ).forEach {
                            val dProject = it.dependencyProject
                            symtabs.addAll(getSymtabs(dProject, variant.name))

                        }
                        // Get debug SO files of this project.
                        symtabs.addAll(getSymtabs(project, variant.name))
                        if (symtabs.isEmpty()) throw  IllegalArgumentException("symtabs文件不存在")
                        uploadSymtabs(variant, symtabs)
                    }

            }
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
    private fun getSymtabs(project: Project?, variantName: String): List<File> {
        val symtabs = mutableListOf<File>()
        if (project == null) {
            return symtabs
        }
        val variantFilter = "**/${variantName}/obj/**/*.so"
        val genericFilter = "**/obj/**/*.so"
        var collection = project.fileTree(project.buildDir) {
            include(variantFilter)
        }
        if (!collection.isEmpty) {
            symtabs.addAll(collection.files)
            return symtabs
        }
        collection = project.fileTree(project.projectDir) {
            include(variantFilter)
        }
        if (!collection.isEmpty) {
            symtabs.addAll(collection.files)
            return symtabs
        }
        collection = project.fileTree(project.projectDir) {
            include(genericFilter)
        }
        symtabs.addAll(collection.files)
        return symtabs
    }

    abstract fun uploadMapping(variant: ApplicationVariant, mappingFile: File)
    abstract fun uploadSymtabs(variant: ApplicationVariant, symtabs: List<File>)
}