plugins {
    id("com.android.library")
//    id("com.didiglobal.booster")
    kotlin("android")
    kotlin("kapt")
}
//val AGP_VERSION: String by project
//val KOTLIN_VERSION: String by project
//val COMPILE_SDK_VERSION: String by project
//val BUILD_TOOLS_VERSION: String by project
//val MIN_SDK_VERSION: Int by project
//val TARGET_SDK_VERSION: Int by project
//val VERSION_CODE: String by project
//val VERSION_NAME: String by project

android {
    compileSdk = 34
    namespace = "com.electrolytej.tool.network"
//    compileSdkVersion = COMPILE_SDK_VERSION
//    buildToolsVersion = BUILD_TOOLS_VERSION

    defaultConfig {
//        minSdk = MIN_SDK_VERSION
//        targetSdk = TARGET_SDK_VERSION
    }

    buildTypes {
        debug {
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
        }
        release {
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )

        }
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_11
        targetCompatibility = JavaVersion.VERSION_11
    }
    kotlinOptions {
        jvmTarget = "11"
    }
}
//dependencies{
//
//}
