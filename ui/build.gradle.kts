import java.time.Year
import kotlin.text.Typography.copyright

buildscript {
    dependencies {
        classpath(libs.dokka.base)
    }
}

plugins {
    alias(libs.plugins.android.library)
    alias(libs.plugins.kotlin.android)
    alias(libs.plugins.dokka)
    alias(libs.plugins.dokka.javadoc)
    id("maven-publish")
}

android {
    namespace = "com.theoplayer.android.ui"
    compileSdk = 35

    defaultConfig {
        minSdk = 21

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
        kotlinCompilerExtensionVersion = "1.5.15"
    }
    packaging {
        resources {
            excludes += "/META-INF/{AL2.0,LGPL2.1}"
        }
    }
    publishing {
        singleVariant("release") {
            withSourcesJar()
            // We use Dokka for JavaDoc generation, see dokkaJavadocJar below
            // withJavadocJar()
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
    implementation(libs.androidx.compose.material3)
    implementation(libs.androidx.compose.material.iconsExtended)
    testImplementation(libs.junit4)
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.espresso)
    androidTestImplementation(libs.androidx.compose.ui.testJunit4)
    debugImplementation(libs.androidx.compose.ui.tooling)
    debugImplementation(libs.androidx.compose.ui.testManifest)

    implementation(libs.theoplayer) {
        version {
            strictly("[5.0, 10.0)")
        }
    }

    dokkaPlugin(libs.dokka.plugin)
}

dokka {
    moduleName = rootProject.name

    dokkaSourceSets.main {
        sourceLink {
            localDirectory = file("src/main/java")
            remoteUrl("https://github.com/THEOplayer/android-ui/blob/${version}/ui/src/main/java")
            remoteLineSuffix = "#L"
        }

        externalDocumentationLinks {
            register("com.theoplayer.android.api") {
                url("https://www.theoplayer.com/docs/theoplayer/v8/api-reference/android/")
                // Default package-list URL doesn't work on theoplayer.com... Use GitHub for now.
                packageListUrl("https://raw.githubusercontent.com/THEOplayer/documentation/refs/heads/main/theoplayer/static/theoplayer/v8/api-reference/android/package-list")
            }
        }
    }

    pluginsConfiguration.html {
        customAssets.from("assets/logo-icon.svg")
        footerMessage = "$copyright ${Year.now().value} THEO Technologies"
    }

    dokkaPublications.html {
        outputDirectory = rootDir.resolve("site/api")
    }

    dokkaPublications.javadoc {
        enabled = true
    }
}

val dokkaJavadocJar = tasks.register<Jar>("dokkaJavadocJar") {
    group = "dokka"
    from(tasks.dokkaGeneratePublicationJavadoc)
    dependsOn(tasks.dokkaGeneratePublicationJavadoc)
    archiveClassifier.set("javadoc")
}

publishing {
    repositories {
        maven {
            name = "reposilite"
            url = uri("https://maven.theoplayer.com/releases")
            credentials {
                username = System.getenv("REPOSILITE_USERNAME")
                password = System.getenv("REPOSILITE_PASSWORD")
            }
        }
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
            groupId = "com.theoplayer.android-ui"
            artifactId = "android-ui"
            version = project.version as String
            artifact(dokkaJavadocJar)
            afterEvaluate {
                from(components["release"])
            }
        }
    }
}
