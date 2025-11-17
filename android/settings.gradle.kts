pluginManagement {
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
}
dependencyResolutionManagement {
//    repositoriesMode.set(RepositoriesMode.FAIL_ON_PROJECT_REPOS)
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
}
includeBuild("composite-builds")
include(":app")
include(":vi-api")
include(":vi-lint-rules")
include(
    ":apk:matrix-apk-canary",
    ":apk:matrix-arscutil",
    ":apk:matrix-commons",
    ":apk:vi-apk-commons",
    ":apk:vi-monitor-matrix-apkchecker",
    ":apk:vi-optimizer-minify-resource",
    ":apk:vi-optimizer-duplicated-resource",
    ":apk:vi-optimizer-obfuscated-resource",
    ":apk:vi-optimizer-unused-resource",
    ":apk:vi-linter-apksize",
    ":apk:vi-lint-rules",
)
include(
    ":startup:vi-monitor-startup",
    ":startup:vi-optimizer-tasklist-composer",
    ":startup:vi-optimizer-startup"
)
include(
    ":render:vi-monitor-render"
)
include(
//    ":stability:breakpad",
    ":stability:vi-monitor-bugly",
    ":stability:vi-monitor-anr",
)
include(
    ":network:vi-tool-mitmproxy",
    ":network:vi-monitor-http"
)
include(
    ":cpu-tracer:btrace:rhea-core",
    ":cpu-tracer:btrace:rhea-trace-processor",
//    ":cpu-tracer:btrace:rhea-gradle-plugin",
)

includeBuild(
    "cpu-tracer/btrace/rhea-gradle-plugin"
)
include(":modules:aModule")
include(":modules:bModule")
