plugins {
    id("com.android.application")
    id("com.didiglobal.booster")
    id("org.jetbrains.kotlin.android")
    id("com.bytedance.rhea-trace")
}
rheaTrace {
    compilation {
        traceFilterFilePath = "${rootDir}/traceFilter.txt"
        needPackageWithMethodMap = true
        applyMethodMappingFilePath = ""
    }
}

//apply(from = "${rootDir}/apm_config.gradle")
android {

    signingConfigs {
        create("release") {
            storeFile = file("${rootDir}/key.jks")
            storePassword = "123456"
            keyAlias = "key0"
            keyPassword = "123456"
        }
        getByName("debug") {
            storeFile = file("${rootDir}/key.jks")
            storePassword = "123456"
            keyAlias = "key0"
            keyPassword = "123456"
        }
    }
    namespace = "com.electrolytej.vi.app"
    compileSdk = 34
    val NDK_VERSION: String by project
    ndkVersion = NDK_VERSION
    defaultConfig {
        applicationId = "com.electrolytej.vi"
        minSdk = 21
        versionCode = 1
        versionName = "1.0"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
        signingConfig = signingConfigs.getByName("release")
        externalNativeBuild {
            cmake {
                abiFilters += setOf("arm64-v8a", "armeabi-v7a")
            }
        }
    }

    buildTypes {
        debug {
            isMinifyEnabled = false
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
        }
        release {
            isMinifyEnabled = true
            isShrinkResources = true
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
    buildFeatures {
        viewBinding = true
    }
    packagingOptions {
        resources.pickFirsts.add("com/electrolytej/vi/BuildConfig.class")
        pickFirsts.add("com/electrolytej/vi/BuildConfig.class")
        pickFirst("lib/armeabi-v7a/libc++_shared.so")
        pickFirst("lib/arm64-v8a/libc++_shared.so")
        pickFirst("lib/armeabi-v7a/libwechatbacktrace.so")
        pickFirst("lib/arm64-v8a/libwechatbacktrace.so")
        pickFirst("lib/armeabi-v7a/libfbjni.so")
        pickFirst("lib/arm64-v8a/libfbjni.so")
        pickFirst("lib/armeabi-v7a/libbytehook.so")
        pickFirst("lib/arm64-v8a/libbytehook.so")
    }
}

dependencies {

    implementation("androidx.core:core-ktx:1.8.0")
    implementation("androidx.appcompat:appcompat:1.6.1")
    implementation("com.google.android.material:material:1.5.0")
    implementation("androidx.constraintlayout:constraintlayout:2.1.4")
    implementation("androidx.navigation:navigation-fragment-ktx:2.7.2")
    implementation("androidx.navigation:navigation-ui-ktx:2.7.2")
    testImplementation("junit:junit:4.13.2")
    androidTestImplementation("androidx.test.ext:junit:1.1.5")
    androidTestImplementation("androidx.test.espresso:espresso-core:3.5.1")
    implementation(project(":startup:vi-optimizer-tasklist-composer"))
    implementation(project(":vi-api"))
    implementation(project(":stability:vi-monitor-anr"))
    implementation(project(":startup:vi-monitor-startup"))
    implementation(project(":render:vi-monitor-render"))
    debugImplementation(project(":network:vi-tool-mitmproxy"))
    implementation(project(":network:vi-monitor-http"))
//    implementation("com.bytedance.btrace:rhea-core:2.0.1")
    implementation(project(":cpu-tracer:btrace:rhea-core"))
    implementation(project(":modules:aModule"))
    implementation(project(":modules:bModule"))
    implementation("com.iqiyi.xcrash:xcrash-android-lib:3.0.0")
}