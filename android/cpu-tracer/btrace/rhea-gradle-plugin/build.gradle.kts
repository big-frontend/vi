plugins {
    `kotlin-dsl`
    id("java")
    alias(libs.plugins.kotlin.jvm) apply false
//    id("io.johnsonlee.sonatype-publish-plugin")
    id("java-gradle-plugin")
    id("com.gradle.plugin-publish") version "1.2.1"

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
version = "2.0.1"
group = "com.bytedance"
gradlePlugin {
    plugins {
        create("btrace") {
            id = "com.bytedance.rhea-trace"
            implementationClass = "com.bytedance.rheatrace.plugin.RheaTracePlugin"
            displayName = "btrace-plugin"
            description = "btrace-plugin"
        }
    }
}
dependencies {
    val  asm_version="6.2.1"
    compileOnly(gradleApi())
    compileOnly(libs.android.gradlePlugin)
    compileOnly(libs.android.tools.common)
    compileOnly(libs.android.tools.builder)
    compileOnly(libs.kotlin.stdlib)
    implementation("org.ow2.asm:asm:${asm_version}")
    implementation("org.ow2.asm:asm-commons:$asm_version")
    implementation("org.ow2.asm:asm-tree:$asm_version")
    implementation("org.ow2.asm:asm-util:$asm_version")
    implementation("org.apache.httpcomponents.client5:httpclient5:5.1.3")
}
