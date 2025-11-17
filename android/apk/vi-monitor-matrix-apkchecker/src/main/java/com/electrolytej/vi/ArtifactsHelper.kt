@file:JvmName("ArtifactsHelper")

package com.electrolytej.vi

import com.android.build.gradle.api.BaseVariant
import com.android.build.gradle.internal.publishing.AndroidArtifacts
import com.didiglobal.booster.gradle.getArtifactCollection
import com.didiglobal.booster.gradle.project
import org.gradle.api.artifacts.component.ModuleComponentIdentifier
import org.gradle.api.artifacts.component.ProjectComponentIdentifier
import java.io.File
fun BaseVariant.getArtifactCollection(type: AndroidArtifacts.ArtifactType) = getArtifactCollection(
    AndroidArtifacts.ConsumedConfigType.RUNTIME_CLASSPATH,
    AndroidArtifacts.ArtifactScope.ALL,
    type
)

fun BaseVariant.getResolvedArtifacts(
    type: AndroidArtifacts.ArtifactType,
    callBack1: (ModuleComponentIdentifier, File) -> Unit,
    callBack2: (ProjectComponentIdentifier, File) -> Unit,
) {
    val variant = this
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

fun BaseVariant.getResolvedCode() {
    getResolvedArtifacts(
//        AndroidArtifacts.ArtifactType.CLASSES,
//        AndroidArtifacts.ArtifactType.DEX,
        AndroidArtifacts.ArtifactType.CLASSES_DIR,
//        AndroidArtifacts.ArtifactType.AAR_OR_JAR,
//        AndroidArtifacts.ArtifactType.AAR,
//        AndroidArtifacts.ArtifactType.JAR,
        { componentIdentifier: ModuleComponentIdentifier, artifactFile: File ->
            println("code收集 ${componentIdentifier.displayName}  ${artifactFile.absolutePath}")
//            processAarArtifact(artifactFile)
        }, { componentIdentifier: ProjectComponentIdentifier, artifactFile: File ->
            val moduleName = componentIdentifier.projectName
            val projectDir = project.project(componentIdentifier.projectPath).projectDir
            println("code收集 $moduleName  ${artifactFile.absolutePath}")
        }
    )

    getResolvedArtifacts(
        AndroidArtifacts.ArtifactType.CLASSES_JAR,
        { componentIdentifier: ModuleComponentIdentifier, artifactFile: File ->
            println("code1收集 ${componentIdentifier.displayName}  ${artifactFile.absolutePath}")
//            processAarArtifact(artifactFile)
        }, { componentIdentifier: ProjectComponentIdentifier, artifactFile: File ->
            val moduleName = componentIdentifier.projectName
            val projectDir = project.project(componentIdentifier.projectPath).projectDir
            println("code1收集 $moduleName  ${artifactFile.absolutePath}")
        }
    )
    getResolvedArtifacts(
        AndroidArtifacts.ArtifactType.SOURCES_JAR,
        { componentIdentifier: ModuleComponentIdentifier, artifactFile: File ->
            println("code2收集 ${componentIdentifier.displayName}  ${artifactFile.absolutePath}")
//            processAarArtifact(artifactFile)
        }, { componentIdentifier: ProjectComponentIdentifier, artifactFile: File ->
            val moduleName = componentIdentifier.projectName
            val projectDir = project.project(componentIdentifier.projectPath).projectDir
            println("code2收集 $moduleName  ${artifactFile.absolutePath}")
        }
    )
}

fun BaseVariant.getResolvedArtifacts(type: AndroidArtifacts.ArtifactType): Map<String, Set<String>> {
    var prefix: String? = null
    if (type == AndroidArtifacts.ArtifactType.ANDROID_RES) {
        prefix = "res"
    } else if (type == AndroidArtifacts.ArtifactType.ASSETS) {
        prefix = "assets"
    }
    val artifactCollection = getArtifactCollection(type)
    val map = mutableMapOf<String, Set<String>>()
    artifactCollection.forEach { artifact ->
        val artifactFile = artifact.file
        val componentIdentifier = artifact.id.componentIdentifier
        if (componentIdentifier is ModuleComponentIdentifier) {
            val resourcePaths = collectPaths(artifactFile, prefix)
            map[componentIdentifier.displayName] = resourcePaths
        } else if (componentIdentifier is ProjectComponentIdentifier) {
            val projectName = componentIdentifier.projectName
            val projectDir = project.project(componentIdentifier.projectPath).projectDir
            val resourcePaths = collectPaths(artifactFile, prefix)
            map[projectName] = resourcePaths
        }
    }
    return map
}


fun BaseVariant.getSymbolList(): Map<String, SymbolList> {
    val artifactCollection = getArtifactCollection(AndroidArtifacts.ArtifactType.COMPILE_SYMBOL_LIST)
    val map = mutableMapOf<String, SymbolList>()
    artifactCollection.forEach { artifact ->
        val artifactFile = artifact.file
        val componentIdentifier = artifact.id.componentIdentifier
        if (componentIdentifier is ModuleComponentIdentifier) {
            println("R收集 ${componentIdentifier.displayName}  ${artifactFile.absolutePath}")
            val symbols = SymbolList.from(artifactFile)
            map[componentIdentifier.displayName] = symbols
        } else if (componentIdentifier is ProjectComponentIdentifier) {
            val projectName = componentIdentifier.projectName
            println("R收集 ${projectName}  ${artifactFile.absolutePath}")
            val projectDir = project.project(componentIdentifier.projectPath).projectDir
            val symbols = SymbolList.from(artifactFile)
            map[projectName] = symbols
        }
    }
    return map
}

fun BaseVariant.getResolvedRes(): Map<String, Set<String>> {
    val res = getResolvedArtifacts(AndroidArtifacts.ArtifactType.ANDROID_RES)
    return res
}

fun BaseVariant.getResolvedAssets(): Map<String, Set<String>> {
    val assets = getResolvedArtifacts(AndroidArtifacts.ArtifactType.ASSETS)
    return assets
}

private fun collectPaths(baseDir: File, prefix: String? = null): Set<String> {
    if (!baseDir.exists() || !baseDir.isDirectory) {
        return emptySet()
    }
    val paths = mutableSetOf<String>()
    baseDir.walk().forEach { file ->
        if (file.isFile) {
            if (prefix.isNullOrEmpty()) {
                paths.add(file.relativeTo(baseDir).path)
            } else {
                paths.add("$prefix/${file.relativeTo(baseDir).path}")
            }
        }
    }
    return paths
}