@file:JvmName("DependencyUtil")

package com.electrolytej.vi

import com.android.build.gradle.api.BaseVariant
import com.android.build.gradle.internal.publishing.AndroidArtifacts
import com.didiglobal.booster.gradle.getArtifactCollection
import com.didiglobal.booster.gradle.project
import org.gradle.api.artifacts.component.ModuleComponentIdentifier
import org.gradle.api.artifacts.component.ProjectComponentIdentifier
import java.io.File
import java.util.zip.ZipFile

fun getResolvedArtifacts(
    variant: BaseVariant,
    type: AndroidArtifacts.ArtifactType,
    callBack1: (ModuleComponentIdentifier, File) -> Unit,
    callBack2: (ProjectComponentIdentifier, File) -> Unit
) {
    val artifactCollection = variant.getArtifactCollection(
        AndroidArtifacts.ConsumedConfigType.RUNTIME_CLASSPATH,
        AndroidArtifacts.ArtifactScope.ALL,
        type
    )
    artifactCollection.forEach { artifact ->
        val artifactFile = artifact.file
        val componentIdentifier = artifact.id.componentIdentifier
        if (componentIdentifier is ModuleComponentIdentifier) {
            callBack1.invoke(componentIdentifier, artifactFile)

        } else if (componentIdentifier is ProjectComponentIdentifier) {
            callBack2.invoke(componentIdentifier, artifactFile)

        }
    }
}

fun getResolvedArtifacts(variant: BaseVariant) {
    getResolvedArtifacts(
        variant,
        AndroidArtifacts.ArtifactType.AAR_OR_JAR,
//        AndroidArtifacts.ArtifactType.AAR,
//        AndroidArtifacts.ArtifactType.JAR,
        { componentIdentifier: ModuleComponentIdentifier, artifactFile: File ->
            println("code收集 ${componentIdentifier.displayName}  ${artifactFile.absolutePath}")
//            processAarArtifact(artifactFile)
        }, { componentIdentifier: ProjectComponentIdentifier, artifactFile: File ->
            val moduleName = componentIdentifier.projectName
            val projectDir = variant.project.project(componentIdentifier.projectPath).projectDir
            println("code收集 $moduleName  ${artifactFile.absolutePath}")
        }
    )
    getResolvedArtifacts(
        variant,
        AndroidArtifacts.ArtifactType.ANDROID_RES,
        { componentIdentifier: ModuleComponentIdentifier, artifactFile: File ->
            println("res收集 ${componentIdentifier.displayName}  ${artifactFile.absolutePath}")
//            processAarArtifact(artifactFile)
        }, { componentIdentifier: ProjectComponentIdentifier, artifactFile: File ->
            val moduleName = componentIdentifier.projectName
            val projectDir = variant.project.project(componentIdentifier.projectPath).projectDir
            println("res收集 $moduleName  ${artifactFile.absolutePath}")
        }
    )

    getResolvedArtifacts(
        variant,
        AndroidArtifacts.ArtifactType.ASSETS,
        { componentIdentifier: ModuleComponentIdentifier, artifactFile: File ->
            println("assets收集 ${componentIdentifier.displayName}  ${artifactFile.absolutePath}")
//            processAarArtifact(artifactFile)
        }, { componentIdentifier: ProjectComponentIdentifier, artifactFile: File ->
            val moduleName = componentIdentifier.projectName
            val projectDir = variant.project.project(componentIdentifier.projectPath).projectDir
            println("assets收集 $moduleName  ${artifactFile.absolutePath}")
        }
    )
//        val artifactCollection3 = variant.getArtifactFileCollection(
//            AndroidArtifacts.ConsumedConfigType.RUNTIME_CLASSPATH,
//            AndroidArtifacts.ArtifactScope.ALL, AndroidArtifacts.ArtifactType.RES_SHARED_STATIC_LIBRARY
//        )

}

private fun processAarArtifact(aarFile: File) {
    val extractDir: File = createTempDir("aar_extract_", "")
    try {
        // 解压 AAR 文件
        unzip(aarFile, extractDir)

        // 采集 AAR 中的资源数据，这里简单示例采集 res 目录下的文件信息，可根据实际需求扩展
        val resDir = File(extractDir, "res")
        if (resDir.exists() && resDir.isDirectory) {
            val resourceData = collectResourceData(resDir)
            // 这里可调用落库逻辑，比如将 resourceData 存入数据库
            // saveResourceDataToDB(artifact, resourceData)
//            logger.info("Collected resources from AAR ${artifact.name}: $resourceData")
//            println("Collected resources from AAR ${artifact.name}: $resourceData")
        }
    } catch (e: Exception) {
//        logger.error("Error processing AAR ${artifact.name}: ${e.message}", e)
//        println("Error processing AAR ${artifact.name}: ${e.message}")
    } finally {
        // 清理临时目录
        extractDir.deleteRecursively()
    }
}

private fun unzip(zipFile: File, targetDir: File) {
    ZipFile(zipFile).use { zip ->
        zip.entries().asSequence().forEach { entry ->
            val entryFile = File(targetDir, entry.name)
            if (entry.isDirectory) {
                entryFile.mkdirs()
            } else {
                entryFile.parentFile?.mkdirs()
                zip.getInputStream(entry).use { input ->
                    entryFile.outputStream().use { output ->
                        input.copyTo(output)
                    }
                }
            }
        }
    }
}

private fun collectResourceData(resDir: File): List<String> {
    val resourcePaths = mutableListOf<String>()
    resDir.walk().forEach { file ->
        if (file.isFile) {
            resourcePaths.add(file.relativeTo(resDir).path)
        }
    }
    return resourcePaths
}