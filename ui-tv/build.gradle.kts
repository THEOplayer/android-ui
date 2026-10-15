import org.jetbrains.kotlin.gradle.dsl.JvmTarget
import org.jetbrains.kotlin.gradle.dsl.KotlinVersion

buildscript {
    dependencies {
        classpath(libs.dokka.base)
    }
}

plugins {
    alias(libs.plugins.android.library)
    alias(libs.plugins.compose.compiler)
    alias(libs.plugins.dokka)
    alias(libs.plugins.dokka.javadoc)
    id("android-ui.library-conventions")
}

android {
    namespace = "com.theoplayer.android.ui.tv"
    compileSdk = libs.versions.android.compileSdk.get().toInt()

    defaultConfig {
        minSdk = libs.versions.android.minSdk.get().toInt()
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
    buildFeatures {
        compose = true
    }
    testOptions {
        unitTests.isIncludeAndroidResources = true
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

kotlin {
    compilerOptions {
        apiVersion = KotlinVersion.KOTLIN_2_0
        jvmTarget = JvmTarget.JVM_1_8
        optIn.add("com.theoplayer.android.ui.InternalUiApi")
        optIn.add("com.theoplayer.android.ui.ExperimentalPlayerStateApi")
        optIn.add("com.theoplayer.android.ui.tv.ExperimentalTvUiApi")
    }
}

dependencies {
    api(project(":ui-common"))

    implementation(platform(libs.androidx.compose.bom))

    implementation(libs.androidx.tv.material)
    implementation(libs.androidx.compose.animation)
    implementation(libs.androidx.compose.foundation)
    implementation(libs.androidx.compose.material.iconsExtended)
    implementation(libs.androidx.compose.ui.ui)
    implementation(libs.androidx.activity.compose)
    implementation(libs.androidx.compose.ui.toolingPreview)

    testImplementation(libs.junit4)
    testImplementation(libs.mockk)
    testImplementation(libs.robolectric)
    testImplementation(libs.androidx.compose.ui.testJunit4)
    testImplementation(libs.theoplayer)

    debugImplementation(libs.androidx.compose.ui.tooling)
    debugImplementation(libs.androidx.compose.ui.testManifest)

    dokkaPlugin(libs.dokka.plugin)
}

dokka {
    moduleName = "android-ui-tv"

    dokkaSourceSets.configureEach {
        sourceLink {
            localDirectory = file("src/main/java")
            remoteUrl("https://github.com/THEOplayer/android-ui/blob/${version}/ui-tv/src/main/java")
            remoteLineSuffix = "#L"
        }

        externalDocumentationLinks {
            register("com.theoplayer.android.api") {
                url("https://optiview.dolby.com/docs/theoplayer/v11/api-reference/android/")
                packageListUrl("https://optiview.dolby.com/docs/theoplayer/v11/api-reference/android/package-list")
            }
        }
    }
}

val dokkaJavadocJar = tasks.register<Jar>("dokkaJavadocJar") {
    group = "dokka"
    from(tasks.dokkaGeneratePublicationJavadoc)
    dependsOn(tasks.dokkaGeneratePublicationJavadoc)
    archiveClassifier.set("javadoc")
}

publishing {
    publications {
        register<MavenPublication>("release") {
            groupId = "com.theoplayer.android-ui"
            artifactId = "android-ui-tv"
            version = project.version as String
            artifact(dokkaJavadocJar)
            afterEvaluate {
                from(components["release"])
            }
        }
    }
}
