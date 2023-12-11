// Top-level build file where you can add configuration options common to all sub-projects/modules.
buildscript {
    repositories {
        maven { url = uri("./local-repo") }
        mavenLocal()
        maven { url = uri("https://s01.oss.sonatype.org/content/repositories/public") }
        maven { url = uri("https://maven.oschina.net/content/groups/public/") }
        maven { url = uri("https://maven.aliyun.com/repository/google/") }
        maven { url = uri("https://plugins.gradle.org/m2/") }
        maven { url = uri("https://artifact.bytedance.com/repository/byteX/") }
        google()
        mavenCentral()
        gradlePluginPortal()
    }
}
plugins {
    id("com.android.application") version "7.4.1" apply false
    id("com.android.library") version "7.4.1" apply false
    id("org.jetbrains.kotlin.android") version "1.8.22" apply false
    id("io.github.jamesfchen.vi-plugin") apply false
}
allprojects {
//    tasks.withType(JavaCompile::class.java).configureEach { task ->
//        task.options.encoding = 'UTF-8'
//        task.sourceCompatibility = JavaVersion.VERSION_11
//        task.targetCompatibility = JavaVersion.VERSION_11
//    }
//    tasks.withType(org.jetbrains.kotlin.gradle.dsl.KotlinJvmCompile::class.java).configureEach { task ->
//        task.kotlinOptions {
//            jvmTarget = '1.8'
//        }
//    }
}

tasks.register("clean", Delete::class.java) {
    description = "Remove all the build files and intermediate build outputs"
//    delete(allprojects.map { it.buildDir })
    delete(rootProject.buildDir)
}