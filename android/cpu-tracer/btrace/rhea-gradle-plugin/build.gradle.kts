plugins {
    id("java-gradle-plugin")
    kotlin("plugin.serialization") version "1.4.20"

}
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
    val AGP_VERSION = "7.4.1"
    val KOTLIN_VERSION = "1.8.22"
    val  asm_version="6.2.1"
    compileOnly(gradleApi())
    compileOnly("com.android.tools.build:gradle:${AGP_VERSION}")
    implementation("org.jetbrains.kotlin:kotlin-stdlib:${KOTLIN_VERSION}")
    implementation("org.ow2.asm:asm:$asm_version")
    implementation("org.ow2.asm:asm-tree:$asm_version")
    implementation("org.ow2.asm:asm-util:$asm_version")
}
