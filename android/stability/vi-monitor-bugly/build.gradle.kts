plugins {
    `kotlin-dsl`
    id("java-library")
    kotlin("jvm")
    kotlin("kapt")
    id("io.johnsonlee.sonatype-publish-plugin")
}
val GROUP_ID :String by project
group = GROUP_ID
version = project.findProperty("version")?.takeIf { it != Project.DEFAULT_VERSION } ?: "1.0.0-SNAPSHOT"

val AGP_VERSION :String by project
val KOTLIN_VERSION :String by project
dependencies{
    implementation(fileTree(mapOf("dir" to "libs", "include" to listOf("*.jar"))))
    kapt("com.google.auto.service:auto-service:1.0")
    compileOnly(gradleApi())
    compileOnly(kotlin("gradle-plugin", version = KOTLIN_VERSION))
    compileOnly("com.android.tools.build:gradle:${AGP_VERSION}")
    implementation("com.didiglobal.booster:booster-api:4.16.3")
    implementation("org.jetbrains.kotlin:kotlin-stdlib:${KOTLIN_VERSION}")
    implementation("com.squareup.okhttp3:okhttp:4.5.0")
}
