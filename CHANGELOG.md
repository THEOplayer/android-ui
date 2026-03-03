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

* 🐛 Fixed `PictureInPictureButton` to only be shown when the backing `THEOplayerView` has a valid `PiPConfiguration`. ([#81](https://github.com/THEOplayer/android-ui/pull/81))

## v1.13.1 (2026-01-05)

* 🐛 Changed THEOplayer to be an `api` dependency in Gradle. ([#76](https://github.com/THEOplayer/android-ui/pull/76))
* 🐛 The alpha value of the `UIController`'s background color is now correctly preserved. ([#78](https://github.com/THEOplayer/android-ui/issues/78), [#79](https://github.com/THEOplayer/android-ui/pull/79))

## v1.13.0 (2025-09-12)

* 💥 The `minSdk` is now API 23 (Android 6.0 "Marshmallow"), to align with THEOplayer 10.0. ([#74](https://github.com/THEOplayer/android-ui/pull/74))
* 💥 Open Video UI for Android is now compiled using the Kotlin 2 compiler. Update your app to use Kotlin Gradle Plugin 2.0.0 or newer. ([#74](https://github.com/THEOplayer/android-ui/pull/74))
* 💥 Updated to Jetpack Compose version 1.9.0 ([BOM](https://developer.android.com/jetpack/compose/bom) 2025.08.01).
* 🚀 Added support for THEOplayer 10.0. ([#74](https://github.com/THEOplayer/android-ui/pull/74))

## v1.12.0 (2025-09-08)

* 🚀 Added `PictureInPictureButton`. ([#19](https://github.com/THEOplayer/android-ui/issues/19), [#70](https://github.com/THEOplayer/android-ui/pull/70))
* 🚀 The default UI now shows a minimal set of controls while playing an ad. ([#71](https://github.com/THEOplayer/android-ui/pull/71))
* 🚀 `UIController` no longer hides all controls while playing an ad. ([#71](https://github.com/THEOplayer/android-ui/pull/71))

## v1.11.1 (2025-08-01)

* 🐛 Fixed clicking on overlays from OptiView Ads not working. ([#68](https://github.com/THEOplayer/android-ui/pull/68))

## v1.11.0 (2025-04-29)

* 💥 Bumped `compileSdk` to API 35 (Android 15).
* 🚀 Added localization support.
  * See `res/values/strings.xml` for the full list of translatable strings, which you can override in your app's `strings.xml`.
  * For more information, see [Localize your app on Android Developers](https://developer.android.com/guide/topics/resources/localization).

## v1.10.0 (2025-04-02)

* 🚀 Added support for THEOplayer 9.0. ([#61](https://github.com/THEOplayer/android-ui/pull/61))

## v1.9.4 (2024-12-18)

* 🐛 Revert to `compileSdk` 34. ([#56](https://github.com/THEOplayer/android-ui/pull/56/))

## v1.9.3 (2024-12-17)

* 💥 Updated to Jetpack Compose version 1.7.5 ([BOM](https://developer.android.com/jetpack/compose/bom) 2024.11.00).
* 🐛 Fix `SeekBar` not working for livestreams with a large `player.seekable.start(0)`,
  such as MPEG-DASH streams that use Unix timestamps for their MPD timeline. ([#52](https://github.com/THEOplayer/android-ui/pull/52))

## v1.9.2 (2024-10-15)

* 🐛 Fix `Player.cast` not available before first source change.

## v1.9.1 (2024-10-01)

* 🐛 Fix `DurationDisplay` to show the time of the live point when playing a live or DVR stream.
* 🐛 Fix `CurrentTimeDisplay` to show the time offset to the live point when playing a live or DVR stream with `showRemaining = true`.
* 💅 Changed `DefaultUi` to hide the current time display when playing a live stream.
* 💅 Changed `DefaultUi` to show the time offset to the live point when playing a DVR stream.
* 💅 Changed `LanguageMenuButton` to automatically hide itself when there are no alternative audio or subtitle tracks to select.

## v1.9.0 (2024-09-10)

* 💥 Updated to Jetpack Compose version 1.7.0 ([BOM](https://developer.android.com/jetpack/compose/bom) 2024.09.00).
* 💥 Changed `colors` parameter in `IconButton` and `LiveButton` to be an `IconButtonColors`.
* 🚀 Added support for Android Lollipop (API 21), to align with the THEOplayer Android SDK.
* 🚀 Added `rememberPlayer(THEOplayerView)` to create a `Player` wrapping an existing `THEOplayerView`.

## v1.8.0 (2024-09-06)

* 🚀 Added support for THEOplayer 8.0. ([#37](https://github.com/THEOplayer/android-ui/pull/37))

## v1.7.4 (2024-09-02)

* 🐛 Fixed a crash when playing a live stream on Chromecast.

## v1.7.3 (2024-09-02)

* 🐛 Fixed the Chromecast button never appearing. ([#34](https://github.com/THEOplayer/android-ui/pull/34))
* 🐛 Fixed the seek bar being disabled while casting. ([#35](https://github.com/THEOplayer/android-ui/issues/35), [#36](https://github.com/THEOplayer/android-ui/pull/36))
* 📝 Added a ["Setting up Chromecast" guide](./docs/guides/chromecast.md).

## v1.7.2 (2024-08-28)

* 🐛 Fixed ad clickthrough not working. ([#33](https://github.com/THEOplayer/android-ui/pull/33))
* 🐛 Fixed UI not re-appearing after playing an ad. ([#33](https://github.com/THEOplayer/android-ui/pull/33))
* 🐛 Fixed exiting fullscreen disabling [edge-to-edge display](https://developer.android.com/develop/ui/views/layout/edge-to-edge-manually). ([#32](https://github.com/THEOplayer/android-ui/pull/32))

## v1.7.1 (2024-08-20)

* 🐛 Disable system gestures on the `SeekBar` component. ([#30](https://github.com/THEOplayer/android-ui/pull/30)) 

## v1.7.0 (2024-08-12)

* 💥 Updated to Jetpack Compose version 1.6.8 ([BOM](https://developer.android.com/jetpack/compose/bom) 2024.06.00).
* 🚀 Added basic support for advertisements. (Requires THEOplayer SDK version 7.10.0 or higher.)

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
