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
    namespace = "com.theoplayer.android.ui.common"
    compileSdk = libs.versions.android.compileSdk.get().toInt()

    defaultConfig {
        minSdk = libs.versions.android.minSdk.get().toInt()

        vectorDrawables {
            useSupportLibrary = true
        }
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

kotlin {
    compilerOptions {
        apiVersion = KotlinVersion.KOTLIN_2_0
        jvmTarget = JvmTarget.JVM_1_8
        optIn.add("com.theoplayer.android.ui.InternalUiApi")
    }
}

dependencies {
    implementation(platform(libs.androidx.compose.bom))

    implementation(libs.androidx.ktx)
    implementation(libs.androidx.activity.compose)
    implementation(libs.androidx.compose.ui.ui)

    testImplementation(libs.junit4)

    compileOnly(libs.theoplayer.compile)
    api(libs.theoplayer)

    dokkaPlugin(libs.dokka.plugin)
}

dokka {
    moduleName = "android-ui-common"

    dokkaSourceSets.configureEach {
        sourceLink {
            localDirectory = file("src/main/java")
            remoteUrl("https://github.com/THEOplayer/android-ui/blob/${version}/ui-common/src/main/java")
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
            artifactId = "android-ui-common"
            version = project.version as String
            artifact(dokkaJavadocJar)
            afterEvaluate {
                from(components["release"])
            }
        }
    }
}
