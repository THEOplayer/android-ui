// Top-level build file where you can add configuration options common to all sub-projects/modules.
buildscript {
    repositories {
        gradlePluginPortal()
        google()
        mavenCentral()
    }
    dependencies {
        classpath("com.android.tools.build:gradle:7.4.2")
        classpath("org.jetbrains.kotlin:kotlin-gradle-plugin:1.8.10")
    }
    val compose_ui_version by extra("1.3.3")
    val material3_version by extra("1.0.1")
    val theoplayer_version by extra("6.1.0")
    val dokka_version by extra("1.8.10")
}
plugins {
    val dokka_version: String by extra
    id("com.android.application") version "7.4.2" apply false
    id("com.android.library") version "7.4.2" apply false
    id("org.jetbrains.kotlin.android") version "1.8.10" apply false
    id("org.jetbrains.dokka") version dokka_version apply false
}