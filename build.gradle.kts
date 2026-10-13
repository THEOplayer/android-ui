import org.jetbrains.changelog.date

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
    alias(libs.plugins.dokka) apply false
    alias(libs.plugins.changelog)
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
