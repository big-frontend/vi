import com.github.jengelman.gradle.plugins.shadow.tasks.ShadowJar
plugins {
    id("java-library")
    kotlin("jvm")
    kotlin("kapt")
    id("io.johnsonlee.sonatype-publish-plugin")
    id("com.github.johnrengelman.shadow")
    id("io.johnsonlee.buildprops")
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
    implementation(fileTree(mapOf("dir" to "libs", "include" to listOf("*.jar"))))
    testImplementation("org.testng:testng:6.9.6")
    kapt("com.google.auto.service:auto-service:1.0")
    compileOnly(gradleApi())
    compileOnly("com.android.tools.build:gradle:${AGP_VERSION}")
    compileOnly("com.android.tools.build:builder:${AGP_VERSION}")
    implementation("com.didiglobal.booster:booster-api:4.16.3")
    implementation("com.didiglobal.booster:booster-task-compression:4.16.3")
    implementation("com.didiglobal.booster:booster-aapt2:4.16.3")
    implementation("com.didiglobal.booster:booster-transform-asm:4.16.3")
    implementation("org.jetbrains.kotlin:kotlin-stdlib:${KOTLIN_VERSION}")
    implementation("org.json:json:20201115")
    implementation("com.google.code.gson:gson:2.8.2")
    implementation("pink.madis.apk.arsc:android-chunk-utils:0.0.7")
    implementation("org.smali:smali:2.2.7")
    implementation("org.smali:baksmali:2.2.7")
    testCompileOnly("com.android.tools.build:gradle:${AGP_VERSION}")
    testCompileOnly("com.android.tools.build:builder:${AGP_VERSION}")
    testImplementation("org.jetbrains.kotlin:kotlin-test:$KOTLIN_VERSION")
    testImplementation("org.jetbrains.kotlin:kotlin-test-junit:$KOTLIN_VERSION")
    testImplementation("io.bootstage.testkit:testkit-gradle-plugin:1.4.0")
    testImplementation(gradleTestKit())
}
tasks.register("shadowViJar", ShadowJar::class.java) {
    classifier = "all"
//    baseName = project.name
//    version = project.version.toInt()
}
