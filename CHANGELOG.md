# Changelog

> **Tags:**
> - 💥 Breaking Change
> - 🚀 New Feature
> - 🐛 Bug Fix
> - 👎 Deprecation
> - 📝 Documentation
> - 🏠 Internal
> - 💅 Polish

## Unreleased

* 💥 Renamed `PlayerState` to `Player`.
* 🚀 Added overloads to `DefaultUI` and `UIController` that accept a `Player`.
  This allows constructing a player instance in advance, and even moving it between custom UIs when recomposing.
* 🚀 Added `UIControllerScope.player` as an non-null alternative to `Player.current`. 

## v1.1.0 (2023-06-27)

* 💥 Update to THEOplayer Android SDK 5.
  To migrate, switch to `com.theoplayer.theoplayer-sdk-android:core` in your Gradle dependencies.
  ```diff
    dependencies {
  -   implementation "com.theoplayer.theoplayer-sdk-android:unified:+"
  +   implementation "com.theoplayer.theoplayer-sdk-android:core:5.+"
      implementation "com.theoplayer.android-ui:android-ui:+"
    }
  ```
* 🚀 Added a `UIController` overload which accepts a `THEOplayerView` directly.

## v1.0.0 (2023-04-05)

* 🚀 Initial release.
