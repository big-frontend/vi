package com.jamesfchen.booster

import com.android.build.gradle.BaseExtension
import com.android.build.gradle.api.BaseVariant
import com.android.build.gradle.internal.cxx.configure.getNdkVersionInfo
import com.didiglobal.booster.gradle.symbolList
import org.apache.tools.ant.taskdefs.condition.Os
import org.gradle.api.Project
import org.gradle.api.file.ConfigurableFileCollection
import java.io.File
import java.io.FileInputStream
import java.util.*
fun BaseVariant.findMappingTxtFile(): File? {
    return mappingFileProvider.get().singleFile
}
fun BaseVariant.findRTxtFile(): File {
    return symbolList.singleFile
}
fun Project.findToolnm(): File {
    val extension = (extensions.findByType(BaseExtension::class.java) as BaseExtension)
    val adb = extension.adbExecutable
    println("cjf")
    println(extension.ndkVersion)
    println(extension.ndkPath)
//    val SO_ARCH = 'arm-linux-androideabi'
    val SO_ARCH = "aarch64-linux-android"
    val isWindows = Os.isFamily(Os.FAMILY_WINDOWS)
    val prebuiltPath = "${adb.parentFile.parentFile}${File.separator}ndk${File.separator}${extension.ndkVersion}${File.separator}toolchains${File.separator}${SO_ARCH}-4.9${File.separator}prebuilt"
    val platform = if(isWindows) "windows-x86_64" else "linux-x86_64" //darwin-x86_64
    val nm = if(isWindows)  "${SO_ARCH}-nm.exe" else "${SO_ARCH}-nm"
    return File("${prebuiltPath}${File.separator}${platform}${File.separator}bin${File.separator}${nm}")
}

fun Project.findApkAnalyzer() =
    com.jamesfchen.booster.Jar.getResourceAsFile("/matrix-apk-canary-2.0.8.jar", com.jamesfchen.booster.ApkAnalysisVariantProcessor::class.java)



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