pluginManagement {
    repositories {
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
dependencyResolutionManagement {
//    repositoriesMode.set(RepositoriesMode.FAIL_ON_PROJECT_REPOS)
    repositories {
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
includeBuild("composite-builds")
include(":app")
include(":vi-api")
include(":vi-lint-rules")
include(
    ":apk:matrix-apk-canary",
    ":apk:matrix-arscutil",
    ":apk:matrix-commons",
    ":apk:vi-monitor-matrix-apkchecker",
    ":apk:vi-optimizer-duplicated-files",
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
    ":stability:breakpad",
    ":stability:vi-monitor-bugly",
    ":stability:vi-monitor-anr",
)

