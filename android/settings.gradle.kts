pluginManagement {
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
dependencyResolutionManagement {
    repositoriesMode.set(RepositoriesMode.FAIL_ON_PROJECT_REPOS)
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
includeBuild("composite-builds")
include(":app")