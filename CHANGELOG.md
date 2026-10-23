# Changelog

> **Tags:**
> - 💥 Breaking Change
> - 🚀 New Feature
> - 🐛 Bug Fix
> - 👎 Deprecation
> - 📝 Documentation
> - 🏠 Internal
> - 💅 Polish

## [Unreleased]

* 💅 On Android TV, pressing Center or Enter on the `SeekBar` now toggles play or pause, so viewers don't have to move focus to the play button first. Custom UIs can use the new `SeekBarState.togglePlayPause()` for this. ([#148](https://github.com/THEOplayer/android-ui/pull/148))
* 💅 While scrubbing the `SeekBar`, the player now only seeks once the viewer stops moving for a moment or lets go, instead of on every position change. `SeekBarState` now extends `PlaybackTimeState`, and `CurrentTimeDisplay` shows the position being scrubbed to. ([#149](https://github.com/THEOplayer/android-ui/pull/149))
* 🐛 The `LiveButton` is now disabled while an ad is playing, so viewers can no longer seek to the live point during an ad. ([#TBD](https://github.com/THEOplayer/android-ui/pull/TBD))

## [1.15.0] (2026-10-23)

* 🚀 Added `android-ui-tv`, an experimental UI library for Android TV built on Compose for TV. It includes a D-pad friendly `DefaultUI`. Code shared with `android-ui` now lives in a new `android-ui-common` artifact; `android-ui` depends on it, so apps don't need any changes. ([#107](https://github.com/THEOplayer/android-ui/pull/107), [#121](https://github.com/THEOplayer/android-ui/pull/121), [#124](https://github.com/THEOplayer/android-ui/pull/124), [#125](https://github.com/THEOplayer/android-ui/pull/125), [#126](https://github.com/THEOplayer/android-ui/pull/126), [#128](https://github.com/THEOplayer/android-ui/pull/128), [#131](https://github.com/THEOplayer/android-ui/pull/131))
* 🚀 Added experimental state holders (such as `rememberPlayButtonState` and `rememberSeekBarState`), so custom UIs can reuse the logic of the built-in components. The components have new overloads that take their state, and `LocalTHEOplayerView` provides the current `THEOplayerView`. `UIController` and `DefaultUI` now use these state holders internally. ([#108](https://github.com/THEOplayer/android-ui/pull/108), [#110](https://github.com/THEOplayer/android-ui/pull/110), [#111](https://github.com/THEOplayer/android-ui/pull/111), [#114](https://github.com/THEOplayer/android-ui/pull/114), [#115](https://github.com/THEOplayer/android-ui/pull/115), [#116](https://github.com/THEOplayer/android-ui/pull/116))
* 🚀 Added `UIController(theoplayerView)` and `DefaultUI(theoplayerView)`, which don't create a `Player`. Use `rememberTHEOplayerView(config)` to create a `THEOplayerView` that follows the lifecycle of the composition. `UIController(config)` and `DefaultUI(config)` no longer create a `Player` either, and `UIControllerScope.player` is only created when accessed. ([#119](https://github.com/THEOplayer/android-ui/pull/119))
* 🚀 `UIController` now handles keys: pressing any key shows the controls (which helps on tablets with a keyboard), media keys (play/pause, fast-forward and rewind) control playback even while the controls are hidden, and Back closes an open menu. Use the new `PlayerAnimations.controlsHideDelay` to configure how long the controls stay visible after user interaction. ([#122](https://github.com/THEOplayer/android-ui/pull/122), [#124](https://github.com/THEOplayer/android-ui/pull/124))
* 🚀 `UIController` now moves subtitles up or down while the controls are visible, so they don't render behind the top or bottom chrome. Moving subtitles up requires THEOplayer 9.5.0 or higher. ([#129](https://github.com/THEOplayer/android-ui/pull/129))
* 🚀 Added `TextTrackStyleMenu` to change the style of subtitles, such as the font family, font color and background color. In `DefaultUI`, open it from the subtitles menu. Use `rememberTextTrackStyleState` to build a custom menu. Changing the font family requires THEOplayer 9.9.0 or higher. ([#130](https://github.com/THEOplayer/android-ui/issues/130), [#143](https://github.com/THEOplayer/android-ui/pull/143))
* 🚀 Added `MenuScope.closeAllMenus()` to close all open menus at once. On mobile, tapping outside the menu sheet now closes all open menus, instead of only going back to the previous menu. ([#144](https://github.com/THEOplayer/android-ui/pull/144))
* 🐛 Fixed `AudioTrackList`, `SubtitleTrackList`, `QualityList` and `PlaybackRateList` not scrolling to the selected item, and not announcing it to accessibility services. ([#127](https://github.com/THEOplayer/android-ui/pull/127))
* 🐛 Fixed `UIController` and `DefaultUI` not keeping the player at the aspect ratio of the video (or 16:9 before the video is loaded). ([#140](https://github.com/THEOplayer/android-ui/pull/140))
* 🐛 Fixed menus losing their scroll position (and on Android TV, their focused item) when returning from a submenu. ([#143](https://github.com/THEOplayer/android-ui/pull/143))
* 💅 Menus on mobile now open in a sheet with a translucent background, so they're easier to read on top of the video: a bottom sheet on narrow players, and a side sheet at the end of the player on wide players. ([#141](https://github.com/THEOplayer/android-ui/pull/141))
* 💅 Opening a submenu or going back to the previous menu now only animates the menu's contents, with a short slide and a fade, on both mobile and TV. Use the new `PlayerAnimations.menuTransitionDuration` to change how long this takes. ([#141](https://github.com/THEOplayer/android-ui/pull/141))
* 📝 The API documentation now has a separate section for each module (`android-ui`, `android-ui-common` and `android-ui-tv`). ([#128](https://github.com/THEOplayer/android-ui/pull/128))

## [1.14.2] (2026-10-13)

* 🐛 Fixed a listener leak in picture-in-picture mode detection on Android 15 and higher. ([#113](https://github.com/THEOplayer/android-ui/pull/113))
* 🐛 Fixed the player controls not being disposed when `UIController` leaves the composition. ([#117](https://github.com/THEOplayer/android-ui/pull/117))

## [1.14.1] (2026-04-20)

* 🐛 Fixed the menu background disappearing after opening. ([#100](https://github.com/THEOplayer/android-ui/pull/100))

## [1.14.0] (2026-04-20)

* 🚀 Added support for THEOplayer 11.0. ([#98](https://github.com/THEOplayer/android-ui/pull/98))
* 🚀 Updated to Jetpack Compose version 1.10.6 ([BOM](https://developer.android.com/jetpack/compose/bom) 2026.03.01).

## [1.13.4] (2026-04-07)

* 🐛 The language menu now prefers to show CEA-608/708 closed caption tracks with their localized language name (if available) instead of their language code (e.g. "en") or channel number (e.g. "CC1"). ([#84](https://github.com/THEOplayer/android-ui/pull/84), [#95](https://github.com/THEOplayer/android-ui/pull/95))

## [1.13.3] (2026-03-23)

* 🐛 Changed the minimum supported THEOplayer version to 7.6.0. ([#85](https://github.com/THEOplayer/android-ui/pull/85))
  * This was effectively already the minimum version as of Open Video UI for Android version 1.7.2, but it wasn't noticed until now.
  * Future versions will be properly tested with the minimum supported THEOplayer version to avoid similar compatibility issues.
* 🐛 `Player.pictureInPicture` now also checks whether the `Activity` itself is in picture-in-picture mode, in case the activity has custom picture-in-picture logic (that does not use THEOplayer's `PiPManager` API). ([#89](https://github.com/THEOplayer/android-ui/pull/89/))

## [1.13.2] (2026-03-03)

* 🐛 Fixed `PictureInPictureButton` to only be shown when the backing `THEOplayerView` has a valid `PiPConfiguration`. ([#81](https://github.com/THEOplayer/android-ui/pull/81))

## [1.13.1] (2026-01-05)

* 🐛 Changed THEOplayer to be an `api` dependency in Gradle. ([#76](https://github.com/THEOplayer/android-ui/pull/76))
* 🐛 The alpha value of the `UIController`'s background color is now correctly preserved. ([#78](https://github.com/THEOplayer/android-ui/issues/78), [#79](https://github.com/THEOplayer/android-ui/pull/79))

## [1.13.0] (2025-09-12)

* 💥 The `minSdk` is now API 23 (Android 6.0 "Marshmallow"), to align with THEOplayer 10.0. ([#74](https://github.com/THEOplayer/android-ui/pull/74))
* 💥 Open Video UI for Android is now compiled using the Kotlin 2 compiler. Update your app to use Kotlin Gradle Plugin 2.0.0 or newer. ([#74](https://github.com/THEOplayer/android-ui/pull/74))
* 💥 Updated to Jetpack Compose version 1.9.0 ([BOM](https://developer.android.com/jetpack/compose/bom) 2025.08.01).
* 🚀 Added support for THEOplayer 10.0. ([#74](https://github.com/THEOplayer/android-ui/pull/74))

## [1.12.0] (2025-09-08)

* 🚀 Added `PictureInPictureButton`. ([#19](https://github.com/THEOplayer/android-ui/issues/19), [#70](https://github.com/THEOplayer/android-ui/pull/70))
* 🚀 The default UI now shows a minimal set of controls while playing an ad. ([#71](https://github.com/THEOplayer/android-ui/pull/71))
* 🚀 `UIController` no longer hides all controls while playing an ad. ([#71](https://github.com/THEOplayer/android-ui/pull/71))

## [1.11.1] (2025-08-01)

* 🐛 Fixed clicking on overlays from OptiView Ads not working. ([#68](https://github.com/THEOplayer/android-ui/pull/68))

## [1.11.0] (2025-04-29)

* 💥 Bumped `compileSdk` to API 35 (Android 15).
* 🚀 Added localization support.
  * See `res/values/strings.xml` for the full list of translatable strings, which you can override in your app's `strings.xml`.
  * For more information, see [Localize your app on Android Developers](https://developer.android.com/guide/topics/resources/localization).

## [1.10.0] (2025-04-02)

* 🚀 Added support for THEOplayer 9.0. ([#61](https://github.com/THEOplayer/android-ui/pull/61))

## [1.9.4] (2024-12-18)

* 🐛 Revert to `compileSdk` 34. ([#56](https://github.com/THEOplayer/android-ui/pull/56/))

## [1.9.3] (2024-12-17)

* 💥 Updated to Jetpack Compose version 1.7.5 ([BOM](https://developer.android.com/jetpack/compose/bom) 2024.11.00).
* 🐛 Fix `SeekBar` not working for livestreams with a large `player.seekable.start(0)`,
  such as MPEG-DASH streams that use Unix timestamps for their MPD timeline. ([#52](https://github.com/THEOplayer/android-ui/pull/52))

## [1.9.2] (2024-10-15)

* 🐛 Fix `Player.cast` not available before first source change.

## [1.9.1] (2024-10-01)

* 🐛 Fix `DurationDisplay` to show the time of the live point when playing a live or DVR stream.
* 🐛 Fix `CurrentTimeDisplay` to show the time offset to the live point when playing a live or DVR stream with `showRemaining = true`.
* 💅 Changed `DefaultUi` to hide the current time display when playing a live stream.
* 💅 Changed `DefaultUi` to show the time offset to the live point when playing a DVR stream.
* 💅 Changed `LanguageMenuButton` to automatically hide itself when there are no alternative audio or subtitle tracks to select.

## [1.9.0] (2024-09-10)

* 💥 Updated to Jetpack Compose version 1.7.0 ([BOM](https://developer.android.com/jetpack/compose/bom) 2024.09.00).
* 💥 Changed `colors` parameter in `IconButton` and `LiveButton` to be an `IconButtonColors`.
* 🚀 Added support for Android Lollipop (API 21), to align with the THEOplayer Android SDK.
* 🚀 Added `rememberPlayer(THEOplayerView)` to create a `Player` wrapping an existing `THEOplayerView`.

## [1.8.0] (2024-09-06)

* 🚀 Added support for THEOplayer 8.0. ([#37](https://github.com/THEOplayer/android-ui/pull/37))

## [1.7.4] (2024-09-02)

* 🐛 Fixed a crash when playing a live stream on Chromecast.

## [1.7.3] (2024-09-02)

* 🐛 Fixed the Chromecast button never appearing. ([#34](https://github.com/THEOplayer/android-ui/pull/34))
* 🐛 Fixed the seek bar being disabled while casting. ([#35](https://github.com/THEOplayer/android-ui/issues/35), [#36](https://github.com/THEOplayer/android-ui/pull/36))
* 📝 Added a ["Setting up Chromecast" guide](./docs/guides/chromecast.md).

## [1.7.2] (2024-08-28)

* 🐛 Fixed ad clickthrough not working. ([#33](https://github.com/THEOplayer/android-ui/pull/33))
* 🐛 Fixed UI not re-appearing after playing an ad. ([#33](https://github.com/THEOplayer/android-ui/pull/33))
* 🐛 Fixed exiting fullscreen disabling [edge-to-edge display](https://developer.android.com/develop/ui/views/layout/edge-to-edge-manually). ([#32](https://github.com/THEOplayer/android-ui/pull/32))

## [1.7.1] (2024-08-20)

* 🐛 Disable system gestures on the `SeekBar` component. ([#30](https://github.com/THEOplayer/android-ui/pull/30))

## [1.7.0] (2024-08-12)

* 💥 Updated to Jetpack Compose version 1.6.8 ([BOM](https://developer.android.com/jetpack/compose/bom) 2024.06.00).
* 🚀 Added basic support for advertisements. (Requires THEOplayer SDK version 7.10.0 or higher.)

## [1.6.0] (2024-04-16)

* 🚀 Added support for THEOplayer Android SDK version 7.

## [1.5.0] (2024-02-21)

* 💥 Updated to Jetpack Compose version 1.6.1 ([BOM](https://developer.android.com/jetpack/compose/bom) 2024.02.00).
* 🐛 Fixed dragging the `SeekBar` when
  using [Compose Material 3 version 1.2.0](https://developer.android.com/jetpack/androidx/releases/compose-material3#1.2.0)
  or higher. ([#24](https://github.com/THEOplayer/android-ui/issues/24))

## [1.4.0] (2023-11-27)

* 💥 Updated to Jetpack Compose version 1.5.4 ([BOM](https://developer.android.com/jetpack/compose/bom) 2023.10.01).
* 💅 Renamed project to "THEOplayer Open Video UI for Android".

## [1.3.4] (2023-10-17)

* 🚀 Allow THEOplayer Android SDK 6.

## [1.3.3] (2023-07-13)

* 💅 `UIController` now sizes itself to match the video's aspect ratio, except if this were to
  conflict with a different size constraint (such as `Modifier.fillMaxSize()`).

## [1.3.2] (2023-07-13)

* 🏠 Publish to THEOplayer's own Maven repository.

## [1.3.1] (2023-06-30)

* 🚀 Added `Player.source`, `.videoWidth` and `.videoHeight` properties.
* 🚀 Added `Player.play()` and `.pause()` shortcut methods.
* 🐛 Fixed player not following device rotation while fullscreen.
* 💅 When autoplaying a new video, the UI now starts out as hidden.

## [1.3.0] (2023-06-29)

* 🚀 Added `THEOplayerTheme.playerAnimations` to control the animation settings of the various UI components.
* 🐛 Fix consuming apps unable to install different version of the THEOplayer Android SDK.

## [1.2.0] (2023-06-28)

* 💥 Renamed `PlayerState` to `Player`.
* 🚀 Added overloads to `DefaultUI` and `UIController` that accept a `Player`.
  This allows constructing a player instance in advance, and even moving it between custom UIs when recomposing.
* 🚀 Added `UIControllerScope.player` as an non-null alternative to `Player.current`.

## [1.1.0] (2023-06-27)

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

## [1.0.0] (2023-04-05)

* 🚀 Initial release.

[Unreleased]: https://github.com/THEOplayer/android-ui/compare/v1.15.0...HEAD
[1.15.0]: https://github.com/THEOplayer/android-ui/compare/v1.14.2...v1.15.0
[1.14.2]: https://github.com/THEOplayer/android-ui/compare/v1.14.1...v1.14.2
[1.14.1]: https://github.com/THEOplayer/android-ui/compare/v1.14.0...v1.14.1
[1.14.0]: https://github.com/THEOplayer/android-ui/compare/v1.13.4...v1.14.0
[1.13.4]: https://github.com/THEOplayer/android-ui/compare/v1.13.3...v1.13.4
[1.13.3]: https://github.com/THEOplayer/android-ui/compare/v1.13.2...v1.13.3
[1.13.2]: https://github.com/THEOplayer/android-ui/compare/v1.13.1...v1.13.2
[1.13.1]: https://github.com/THEOplayer/android-ui/compare/v1.13.0...v1.13.1
[1.13.0]: https://github.com/THEOplayer/android-ui/compare/v1.12.0...v1.13.0
[1.12.0]: https://github.com/THEOplayer/android-ui/compare/v1.11.1...v1.12.0
[1.11.1]: https://github.com/THEOplayer/android-ui/compare/v1.11.0...v1.11.1
[1.11.0]: https://github.com/THEOplayer/android-ui/compare/v1.10.0...v1.11.0
[1.10.0]: https://github.com/THEOplayer/android-ui/compare/v1.9.4...v1.10.0
[1.9.4]: https://github.com/THEOplayer/android-ui/compare/v1.9.3...v1.9.4
[1.9.3]: https://github.com/THEOplayer/android-ui/compare/v1.9.2...v1.9.3
[1.9.2]: https://github.com/THEOplayer/android-ui/compare/v1.9.1...v1.9.2
[1.9.1]: https://github.com/THEOplayer/android-ui/compare/v1.9.0...v1.9.1
[1.9.0]: https://github.com/THEOplayer/android-ui/compare/v1.8.0...v1.9.0
[1.8.0]: https://github.com/THEOplayer/android-ui/compare/v1.7.4...v1.8.0
[1.7.4]: https://github.com/THEOplayer/android-ui/compare/v1.7.3...v1.7.4
[1.7.3]: https://github.com/THEOplayer/android-ui/compare/v1.7.2...v1.7.3
[1.7.2]: https://github.com/THEOplayer/android-ui/compare/v1.7.1...v1.7.2
[1.7.1]: https://github.com/THEOplayer/android-ui/compare/v1.7.0...v1.7.1
[1.7.0]: https://github.com/THEOplayer/android-ui/compare/v1.6.0...v1.7.0
[1.6.0]: https://github.com/THEOplayer/android-ui/compare/v1.5.0...v1.6.0
[1.5.0]: https://github.com/THEOplayer/android-ui/compare/v1.4.0...v1.5.0
[1.4.0]: https://github.com/THEOplayer/android-ui/compare/v1.3.4...v1.4.0
[1.3.4]: https://github.com/THEOplayer/android-ui/compare/v1.3.3...v1.3.4
[1.3.3]: https://github.com/THEOplayer/android-ui/compare/v1.3.2...v1.3.3
[1.3.2]: https://github.com/THEOplayer/android-ui/compare/v1.3.1...v1.3.2
[1.3.1]: https://github.com/THEOplayer/android-ui/compare/v1.3.0...v1.3.1
[1.3.0]: https://github.com/THEOplayer/android-ui/compare/v1.2.0...v1.3.0
[1.2.0]: https://github.com/THEOplayer/android-ui/compare/v1.1.0...v1.2.0
[1.1.0]: https://github.com/THEOplayer/android-ui/compare/v1.0.0...v1.1.0
[1.0.0]: https://github.com/THEOplayer/android-ui/commits/v1.0.0
