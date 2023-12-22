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
include(
    ":apk:matrix-apk-canary",
    ":apk:matrix-arscutil",
    ":apk:matrix-commons",
    ":apk:vi-task-matrix-apkchecker",
    ":apk:vi-task-duplicated-files",
    ":apk:vi-apksize-lint",
    ":apk:vi-lint-rules",

    )
include(
    ":startup:vi-startup-monitor",
    ":startup:vi-tasklist-composer-api",
)
include(
    ":render:vi-render-monitor"
)
include(
    ":stability:breakpad",
    ":stability:vi-task-bugly"
)
include(":vi-api")
include(":vi-lint-rules")
include(":startup:vi-transform-startup")
