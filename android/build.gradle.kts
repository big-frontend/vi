// Top-level build file where you can add configuration options common to all sub-projects/modules.
buildscript {
    repositories {
        mavenLocal()
        maven { url = uri("https://s01.oss.sonatype.org/content/repositories/public") }
        maven { url = uri("https://maven.aliyun.com/repository/public/") } //central+jcenter
        maven { url = uri("https://maven.aliyun.com/repository/google/") }
        maven { url = uri("https://maven.aliyun.com/repository/gradle-plugin") }
//        maven { url = uri("https://maven.oschina.net/content/groups/public/") }
        maven { url = uri("https://jitpack.io") }
        mavenCentral()
        google()
        gradlePluginPortal()
    }
    dependencies {
//        val AGP_VERSION:String by project
//        classpath("com.android.tools.build:gradle:${AGP_VERSION}")
//        val KOTLIN_VERSION:String by project
//        classpath("org.jetbrains.kotlin:kotlin-gradle-plugin:${KOTLIN_VERSION}")
        classpath("io.github.jamesfchen:module-publisher-plugin:1.4.3")
        val booster_version = "4.16.3"
        classpath("com.didiglobal.booster:booster-gradle-plugin:$booster_version")
        //包体积优化
//        classpath("com.didiglobal.booster:booster-task-compression-cwebp:$booster_version")
//        classpath("com.didiglobal.booster:booster-task-compression-processed-res:$booster_version")
//        classpath("com.didiglobal.booster:booster-transform-r-inline:$booster_version")
//        classpath("com.didiglobal.booster:booster-transform-br-inline:$booster_version")
//        classpath("com.didiglobal.booster:booster-task-resource-deredundancy:$booster_version")
        //        classpath("io.github.electrolytej:vi-optimizer-minify-resource:1.0.0-SNAPSHOT")
//        classpath("io.github.electrolytej:vi-optimizer-duplicated-resource:1.0.0-SNAPSHOT")
//        classpath("io.github.electrolytej:vi-optimizer-unused-resource:1.0.0-SNAPSHOT")
//        classpath("io.github.electrolytej:vi-optimizer-obfuscated-resource:1.0.0-SNAPSHOT")
//        classpath("io.github.electrolytej:vi-monitor-matrix-apkchecker:1.0.0-SNAPSHOT")

//        classpath("com.didiglobal.booster:booster-task-analyser:$booster_version")
//            classpath("com.didiglobal.booster:booster-transform-usage:$booster_version")
//        classpath("com.didiglobal.booster:booster-task-list-shared-library:$booster_version")
//        classpath("com.didiglobal.booster:booster-task-list-artifact:$booster_version")
//        classpath("com.didiglobal.booster:booster-task-check-snapshot:$booster_version")
//        classpath("com.didiglobal.booster:booster-transform-usage:$booster_version")
//        classpath("io.github.electrolytej:vi-transform-startup:1.0.0-SNAPSHOT")
        //cpu trace
//        classpath("com.bytedance.btrace:rhea-gradle-plugin:2.0.1")

    }
}
plugins {
    id("com.android.application") version "7.4.1" apply false
    id("com.android.library") version "7.4.1" apply false
//    kotlin("android") version "1.8.22" apply false
//    kotlin("kapt") version "1.8.22" apply false
//    kotlin("jvm") version "1.8.22" apply false
    alias(libs.plugins.kotlin.jvm) apply false
    alias(libs.plugins.kotlin.serialization) apply false
    alias(libs.plugins.kotlin.android) apply false
    id("io.johnsonlee.sonatype-publish-plugin") version "1.7.0" apply false
//    id("org.jetbrains.kotlin.jvm") version "1.8.22" apply false
//    id("org.jetbrains.kotlin.android") version "1.8.22" apply false
    id("io.johnsonlee.buildprops") version "1.2.0" apply false
    id("com.github.johnrengelman.shadow") version "6.1.0" apply false
//    id("com.bytedance.rhea-trace") version "2.0.1" apply false
    id("com.bytedance.rhea-trace") apply false
}
allprojects {
    repositories {
        mavenLocal()
        maven { url = uri("https://s01.oss.sonatype.org/content/repositories/public") }
        maven { url = uri("https://maven.aliyun.com/repository/public/") } //central+jcenter
        maven { url = uri("https://maven.aliyun.com/repository/google/") }
        maven { url = uri("https://maven.aliyun.com/repository/gradle-plugin") }
//        maven { url = uri("https://maven.oschina.net/content/groups/public/") }
        maven { url = uri("https://jitpack.io") }
        mavenCentral()
        google()
        gradlePluginPortal()
    }

    tasks.withType<JavaCompile> {
        sourceCompatibility = JavaVersion.VERSION_11.majorVersion
        targetCompatibility = JavaVersion.VERSION_11.majorVersion
    }
    tasks.withType<Test>().configureEach {
        testLogging {
            exceptionFormat = org.gradle.api.tasks.testing.logging.TestExceptionFormat.FULL
            showExceptions = true
            showCauses = true
            showStackTraces = true
        }
    }
    tasks.withType<org.jetbrains.kotlin.gradle.tasks.KotlinCompile> {
        kotlinOptions {
            jvmTarget = JavaVersion.VERSION_11.majorVersion
            apiVersion = "1.5"
            freeCompilerArgs = listOf("-Xno-optimized-callable-references")
        }
    }
//    tasks.withType<org.jetbrains.kotlin.gradle.tasks.KotlinTest>{
//        kotlinOptions {
//            jvmTarget = JavaVersion.VERSION_11.majorVersion
//            apiVersion = "1.5"
//            freeCompilerArgs = listOf("-Xno-optimized-callable-references")
//        }
//    }
}