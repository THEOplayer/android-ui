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

* 🚀 Allow THEOplayer Android SDK 6.

## v1.3.3 (2023-07-13)

* 💅 `UIController` now sizes itself to match the video's aspect ratio, except if this were to
  conflict with a different size constraint (such as `Modifier.fillMaxSize()`).

## v1.3.2 (2023-07-13)

* 🏠 Publish to THEOplayer's own Maven repository.

## v1.3.1 (2023-06-30)

* 🚀 Added `Player.source`, `.videoWidth` and `.videoHeight` properties.
* 🚀 Added `Player.play()` and `.pause()` shortcut methods.
* 🐛 Fixed player not following device rotation while fullscreen.
* 💅 When autoplaying a new video, the UI now starts out as hidden.

## v1.3.0 (2023-06-29)

* 🚀 Added `THEOplayerTheme.playerAnimations` to control the animation settings of the various UI components.
* 🐛 Fix consuming apps unable to install different version of the THEOplayer Android SDK.

## v1.2.0 (2023-06-28)

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
      implementation "com.theoplayer.android-ui:android-ui:1.+"
    }
  ```
* 🚀 Added a `UIController` overload which accepts a `THEOplayerView` directly.

## v1.0.0 (2023-04-05)

* 🚀 Initial release.
