package com.electrolytej.vi

import com.android.build.gradle.BaseExtension
import com.android.build.gradle.api.BaseVariant
import com.didiglobal.booster.gradle.symbolList
import com.didiglobal.booster.kotlinx.OS
import org.apache.tools.ant.taskdefs.condition.Os
import org.gradle.api.Project
import org.gradle.api.file.ConfigurableFileCollection
import java.io.File
import java.io.FileInputStream
import java.util.*


fun Project.findBuildTools(): File {
    val extension = project.extensions.findByType(BaseExtension::class.java)
    extension?.buildToolsVersion
        ?: throw java.lang.IllegalArgumentException("不存在build tools,可能需要配置")
    return findBuildTools(extension.buildToolsVersion)
}

fun Project.findBuildTools(buildToolsVersion: String): File {
    return File("${findSdkLocation()}/build-tools/${buildToolsVersion}")
}

fun Project.findAndroidJar(sdk: Int): ConfigurableFileCollection =
    files("${findSdkLocation()}/platforms/android-$sdk/android.jar")

fun Project.findSdkLocation(): File {
    val rootDir = project.rootDir
    val localProperties = File(rootDir, "local.properties")
    if (localProperties.exists()) {
        val properties = Properties()
        FileInputStream(localProperties).use { instr ->
            properties.load(instr)
        }
        var sdkDirProp = properties.getProperty("sdk.dir")
        return if (sdkDirProp != null) {
            File(sdkDirProp)
        } else {
            sdkDirProp = properties.getProperty("android.dir")
            if (sdkDirProp != null) {
                File(rootDir, sdkDirProp)
            } else {
                throw RuntimeException("No sdk.dir property defined in local.properties file.")
            }
        }
    } else {
        val envVar = System.getenv("ANDROID_HOME")
        if (envVar != null) {
            return File(envVar)
        } else {
            val property = System.getProperty("android.home")
            if (property != null) {
                return File(property)
            }
        }
    }
    throw RuntimeException("Can't find SDK path")
}
