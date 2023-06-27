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
                username =
                    if (settings.extra.has("gpr.user")) settings.extra["gpr.user"] as String?
                    else System.getenv("USERNAME")
                password =
                    if (settings.extra.has("gpr.key")) settings.extra["gpr.key"] as String?
                    else System.getenv("TOKEN")
            }
        }
    }
}
rootProject.name = "THEOplayer Android UI"
include(":app")
include(":ui")
