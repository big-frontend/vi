import org.gradle.api.tasks.testing.logging.TestExceptionFormat

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
        classpath("com.didiglobal.booster:booster-transform-usage:$booster_version")
        classpath("io.github.electrolytej:vi-task-matrix-apkchecker:1.0.0-SNAPSHOT")
//        classpath("io.github.electrolytej:vi-transform-startup:1.0.0-SNAPSHOT")
    }
}
plugins {
    id("com.android.application") version "7.4.1" apply false
    id("com.android.library") version "7.4.1" apply false
//    kotlin("android") version "1.8.22" apply false
//    kotlin("jvm") version "1.8.22" apply false
    id("io.johnsonlee.sonatype-publish-plugin") version "1.7.0" apply false
    id("org.jetbrains.kotlin.jvm") version "1.8.22" apply false
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
    tasks.withType<org.jetbrains.kotlin.gradle.tasks.KotlinCompile> {
        kotlinOptions { jvmTarget = JavaVersion.VERSION_11.majorVersion }
    }
    tasks.withType<Test>().configureEach {
        testLogging {
            exceptionFormat = TestExceptionFormat.FULL
            showExceptions = true
            showCauses = true
            showStackTraces = true
        }
    }
}

tasks.register("clean", Delete::class.java) {
    description = "Remove all the build files and intermediate build outputs"
//    delete(allprojects.map { it.buildDir })
    delete(rootProject.buildDir)
}