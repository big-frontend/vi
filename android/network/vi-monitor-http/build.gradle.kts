plugins {
    id("com.android.library")
    kotlin("android")
    kotlin("kapt")
}

val AGP_VERSION :String by project
val KOTLIN_VERSION :String by project
apply("from" to "$rootDir/aar.gradle")
android{
    namespace = "com.electrolytej.network"
}
dependencies{

}
