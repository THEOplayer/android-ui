import org.jetbrains.kotlin.gradle.dsl.JvmTarget
import org.jetbrains.kotlin.gradle.dsl.KotlinVersion

buildscript {
    dependencies {
        classpath(libs.dokka.base)
    }
}

plugins {
    alias(libs.plugins.android.library)
    alias(libs.plugins.kotlin.android)
    alias(libs.plugins.compose.compiler)
    alias(libs.plugins.dokka)
    alias(libs.plugins.dokka.javadoc)
    id("maven-publish")
}

android {
    namespace = "com.theoplayer.android.ui"
    compileSdk = libs.versions.android.compileSdk.get().toInt()

    defaultConfig {
        minSdk = libs.versions.android.minSdk.get().toInt()

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
    }

    buildTypes {
        release {
            isMinifyEnabled = false
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
        }
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_1_8
        targetCompatibility = JavaVersion.VERSION_1_8
    }
    kotlin {
        compilerOptions {
            apiVersion = KotlinVersion.KOTLIN_2_0
            jvmTarget = JvmTarget.JVM_1_8
        }
    }
    buildFeatures {
        compose = true
    }
    packaging {
        resources {
            excludes += "/META-INF/{AL2.0,LGPL2.1}"
        }
    }
    publishing {
        singleVariant("release") {
            withSourcesJar()
        }
    }
}

dependencies {
    implementation(platform(libs.androidx.compose.bom))

    implementation(libs.androidx.ktx)
    implementation(libs.androidx.lifecycle.compose)
    implementation(libs.androidx.activity.compose)
    implementation(libs.androidx.appcompat)
    implementation(libs.androidx.compose.ui.ui)
    implementation(libs.androidx.compose.ui.toolingPreview)
    testImplementation(libs.junit4)
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.espresso)
    androidTestImplementation(libs.androidx.compose.ui.testJunit4)
    debugImplementation(libs.androidx.compose.ui.tooling)
    debugImplementation(libs.androidx.compose.ui.testManifest)

    implementation(libs.theoplayer)
}