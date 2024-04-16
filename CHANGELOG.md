# Changelog

> **Tags:**
> - 💥 Breaking Change
> - 🚀 New Feature
> - 🐛 Bug Fix
> - 👎 Deprecation
> - 📝 Documentation
> - 🏠 Internal
> - 💅 Polish

## v1.6.0 (2024-04-16)

* 🚀 Added support for THEOplayer Android SDK version 7.

## v1.5.0 (2024-02-21)

* 💥 Updated to Jetpack Compose version 1.6.1 ([BOM](https://developer.android.com/jetpack/compose/bom) 2024.02.00).
* 🐛 Fixed dragging the `SeekBar` when
  using [Compose Material 3 version 1.2.0](https://developer.android.com/jetpack/androidx/releases/compose-material3#1.2.0)
  or higher. ([#24](https://github.com/THEOplayer/android-ui/issues/24))

## v1.4.0 (2023-11-27)

* 💥 Updated to Jetpack Compose version 1.5.4 ([BOM](https://developer.android.com/jetpack/compose/bom) 2023.10.01).
* 💅 Renamed project to "THEOplayer Open Video UI for Android".

## v1.3.4 (2023-10-17)

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
