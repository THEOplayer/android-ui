import org.jetbrains.changelog.date
import java.time.Year
import kotlin.text.Typography.copyright

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
}
plugins {
    alias(libs.plugins.android.application) apply false
    alias(libs.plugins.android.library) apply false
    alias(libs.plugins.compose.compiler) apply false
    alias(libs.plugins.dokka)
    alias(libs.plugins.changelog)
}

dependencies {
    dokka(project(":ui"))
    dokka(project(":ui-common"))
    dokka(project(":ui-tv"))
}

dokka {
    moduleName = rootProject.name

    pluginsConfiguration.html {
        customAssets.from("ui/assets/logo-icon.svg")
        footerMessage = "$copyright ${Year.now().value} Dolby Laboratories, Inc. All rights reserved."
    }

    dokkaPublications.html {
        outputDirectory = rootDir.resolve("site/api")
    }
}

changelog {
    version = project.version.toString()
    path = file("CHANGELOG.md").canonicalPath
    header = provider { "${version.get()} (${date()})" }
    headerParserRegex = """(\d+\.\d+\.\d+)""".toRegex()
    introduction =
        """
        > **Tags:**
        > - 💥 Breaking Change
        > - 🚀 New Feature
        > - 🐛 Bug Fix
        > - 👎 Deprecation
        > - 📝 Documentation
        > - 🏠 Internal
        > - 💅 Polish
        """.trimIndent()
    itemPrefix = "*"
    keepUnreleasedSection = true
    unreleasedTerm = "Unreleased"
    groups = listOf()
    lineSeparator = "\n"
    combinePreReleases = true
    repositoryUrl = "https://github.com/THEOplayer/android-ui"
}
