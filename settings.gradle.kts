pluginManagement {
    repositories {
        google()
        mavenCentral()
        gradlePluginPortal()
    }
}
dependencyResolutionManagement {
    repositoriesMode.set(RepositoriesMode.FAIL_ON_PROJECT_REPOS)
    repositories {
        google()
        mavenCentral()
        maven { url = uri("https://jitpack.io") }
        maven {
            url = uri("https://maven.pkg.github.com/THEOplayer/android-ui")
            credentials {
                // Define gpr.user and gpr.key preferably in your local ~/.gradle/gradle.properties
                username = settings.extra["gpr.user"] as String? ?: System.getenv("USERNAME")
                password = settings.extra["gpr.key"] as String? ?: System.getenv("TOKEN")
            }
        }
    }
}
rootProject.name = "THEOplayer Android UI"
include(":app")
include(":ui")
