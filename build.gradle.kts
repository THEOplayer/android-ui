// Top-level build file where you can add configuration options common to all sub-projects/modules.
buildscript {
    repositories {
        gradlePluginPortal()
        google()
        mavenCentral()
    }
    dependencies {
        classpath(libs.gradle.plugin)
        classpath(libs.kotlin.gradle.plugin)
    }
    val dokka_version by extra("1.8.10")
}
plugins {
    val dokka_version: String by extra
    id("com.android.application") version "8.1.3" apply false
    id("com.android.library") version "8.1.3" apply false
    id("org.jetbrains.kotlin.android") version "1.8.10" apply false
    id("org.jetbrains.dokka") version dokka_version apply false
}