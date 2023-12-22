plugins {
    id("java-library")
    kotlin("jvm")
    kotlin("kapt")
    id("io.johnsonlee.sonatype-publish-plugin")
}
val GROUP_ID :String by project
group = GROUP_ID
version = project.findProperty("version")?.takeIf { it != Project.DEFAULT_VERSION } ?: "1.0.0-SNAPSHOT"

java {
    sourceCompatibility = JavaVersion.VERSION_11
    targetCompatibility = JavaVersion.VERSION_11
}
val AGP_VERSION :String by project
val KOTLIN_VERSION :String by project
dependencies{
    kapt("com.google.auto.service:auto-service:1.0")
    implementation("com.didiglobal.booster:booster-api:4.16.3")
    implementation("com.didiglobal.booster:booster-task-compression:4.16.3")
    compileOnly(gradleApi())
    compileOnly("com.android.tools.build:gradle:${AGP_VERSION}")
    implementation("org.jetbrains.kotlin:kotlin-stdlib:${KOTLIN_VERSION}")
    implementation("org.json:json:20201115")
    implementation("com.google.code.gson:gson:2.8.2")
    implementation("pink.madis.apk.arsc:android-chunk-utils:0.0.7")
}
