package com.electrolytej.vi

import com.android.build.gradle.LibraryExtension
import org.gradle.api.Project
import org.gradle.api.artifacts.Configuration
import java.io.File

object AarPathHelper {

    /**
     * 获取模块的 AAR 输出文件
     * @param moduleProject 目标模块项目
     * @param variantName 变体名称 (e.g., "release")
     */
    fun getModuleAarOutput(moduleProject: Project, variantName: String): File {
        val android = moduleProject.extensions.getByType(LibraryExtension::class.java)
        val variant = android.libraryVariants.find { it.name.equals(variantName, true) }
            ?: throw IllegalArgumentException("Variant $variantName not found in ${moduleProject.path}")

        return variant.outputs
            .map { it.outputFile }
            .first { it.extension == "aar" }
    }

    /**
     * 从配置中获取 AAR 文件
     * @param configuration 配置 (e.g., runtimeClasspath)
     */
    fun getAarFilesFromConfiguration(configuration: Configuration): Set<File> {
        return configuration
            .resolvedConfiguration
            .resolvedArtifacts
            .map { it.file }
            .filter { it.extension == "aar" }
            .toSet()
    }

    /**
     * 获取特定依赖的 AAR 文件
     * @param configuration 配置
     * @param group 依赖组
     * @param module 模块名称
     */
    fun getSpecificAarFile(
        configuration: Configuration,
        group: String,
        module: String
    ): File? {
        return configuration
            .resolvedConfiguration
            .resolvedArtifacts
            .firstOrNull {
                it.moduleVersion.id.group == group &&
                        it.moduleVersion.id.name == module
            }
            ?.file
    }
}