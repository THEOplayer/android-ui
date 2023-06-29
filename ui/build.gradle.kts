import org.jetbrains.dokka.base.DokkaBase
import org.jetbrains.dokka.base.DokkaBaseConfiguration
import org.jetbrains.dokka.gradle.DokkaTask
import kotlin.text.Typography.copyright

buildscript {
    dependencies {
        classpath("org.jetbrains.dokka:dokka-base:1.8.10")
    }
}

plugins {
    id("com.android.library")
    id("org.jetbrains.kotlin.android")
    id("org.jetbrains.dokka")
    id("maven-publish")
}

android {
    namespace = "com.theoplayer.android.ui"
    compileSdk = 33

    defaultConfig {
        minSdk = 24
        targetSdk = 33

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
        vectorDrawables {
            useSupportLibrary = true
        }
    }

    buildTypes {
        getByName("release") {
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
    kotlinOptions {
        jvmTarget = "1.8"
    }
    buildFeatures {
        compose = true
    }
    composeOptions {
        kotlinCompilerExtensionVersion = "1.4.3"
    }
    packagingOptions {
        resources {
            excludes += "/META-INF/{AL2.0,LGPL2.1}"
        }
    }
    publishing {
        singleVariant("release") {
            withSourcesJar()
            withJavadocJar()
        }
    }
}

dependencies {
    val compose_ui_version: String by rootProject.extra
    val material3_version: String by rootProject.extra
    val theoplayer_version: String by rootProject.extra
    val dokka_version: String by rootProject.extra

    implementation("androidx.core:core-ktx:1.9.0")
    implementation("androidx.lifecycle:lifecycle-runtime-ktx:2.6.0")
    implementation("androidx.activity:activity-compose:1.6.1")
    implementation("androidx.appcompat:appcompat:1.6.1")
    implementation("androidx.compose.ui:ui:$compose_ui_version")
    implementation("androidx.compose.ui:ui-tooling-preview:$compose_ui_version")
    implementation("androidx.compose.material3:material3:$material3_version")
    implementation("androidx.compose.material:material-icons-extended:1.3.1")
    testImplementation("junit:junit:4.13.2")
    androidTestImplementation("androidx.test.ext:junit:1.1.5")
    androidTestImplementation("androidx.test.espresso:espresso-core:3.5.1")
    androidTestImplementation("androidx.compose.ui:ui-test-junit4:$compose_ui_version")
    debugImplementation("androidx.compose.ui:ui-tooling:$compose_ui_version")
    debugImplementation("androidx.compose.ui:ui-test-manifest:$compose_ui_version")

    implementation("com.theoplayer.theoplayer-sdk-android:core") {
        version {
            strictly("[5.0, 6.0[")
            prefer(theoplayer_version)
        }
    }

    dokkaPlugin("org.jetbrains.dokka:android-documentation-plugin:$dokka_version")
}

publishing {
    repositories {
        maven {
            name = "GitHubPackages"
            url = uri("https://maven.pkg.github.com/THEOplayer/android-ui")
            credentials {
                username = System.getenv("GITHUB_ACTOR")
                password = System.getenv("GITHUB_TOKEN")
            }
        }
    }

    publications {
        register<MavenPublication>("release") {
            val libraryVersion: String by rootProject.extra
            groupId = "com.theoplayer.android-ui"
            artifactId = "android-ui"
            version = libraryVersion
            afterEvaluate {
                from(components["release"])
            }
        }
    }
}

tasks.withType<DokkaTask>().configureEach {
    pluginConfiguration<DokkaBase, DokkaBaseConfiguration> {
        customAssets = listOf(file("assets/logo-icon.svg"))
        footerMessage = "$copyright 2023 THEO Technologies"
    }
}

tasks.named<DokkaTask>("dokkaHtml").configure {
    outputDirectory.set(rootDir.resolve("docs/api"))
}