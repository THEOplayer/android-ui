# THEOplayer Android SDK UI Connector

This repository is maintained by [THEO Technologies](https://www.theoplayer.com/) and contains the THEOplayer Android SDK UI Connector available with THEOplayer Unified Android SDK.

The THEOplayer Unified Android SDK enables you to quickly deliver content playback on Android, Android TV and Fire TV.

Using this connector allows you to augment the user experience delivered through the Unified Android SDK.

This connector contains different UI components such as:
[THEOPlayPauseButton.kt](ui/src/main/java/com/theoplayer/android/ui/THEOPlayPauseButton.kt) and
[THEOCurrentTime.kt](ui/src/main/java/com/theoplayer/android/ui/THEOCurrentTime.kt).
<br/>
These components are available to be used separately and can be used to create a complete customizable UI.
Each of these components contains the functionality to handle **its own state**.
For example, THEOPlayPauseButton would show the correct icon based on the playback state (playing or paused).

Additionally, this connector contains a default UI implementation which can be used optionally, called [DefaultUI.kt](ui/src/main/java/com/theoplayer/android/ui/DefaultUI.kt).
<br/>
DefaultUI contains a full UI implementation that is ready to be used directly out of the box.
It implements handling the different states of the UI **as a whole**, showing the correct components at the correct state.
For example, showing THEOSpinner when the player is in a buffering state.


## Prerequisites

The THEOplayer Android SDK UI Connector requires the application to import the THEOplayer Unified Android SDK since the connector relies on its public APIs.
For more details about importing THEOplayer Unified Android SDK check the [documentation](https://docs.theoplayer.com/getting-started/01-sdks/02-android-unified/00-getting-started.md).


## Installation

The THEOplayer Android SDK UI Connector is available through Jitpack using the `com.theoplayer.android-ui` group which is different from the group of THEOplayer Android SDK.

To set up the dependency follow these steps:

In your **project** level `build.gradle` file add the Jitpack repository:

```groovy
allprojects {
    repositories {
        google()
        mavenCentral()
        maven { url 'https://jitpack.io' }
    }
}
```

In your **module** level `build.gradle` file add the UI Connector:

```groovy
implementation 'com.theoplayer.android-ui:+'
```

>Notes:
>* The `+` will fetch the latest released version of the connector.
>* Android Studio will recommend replacing the `+` with the exact version of the connector.
>* The THEOplayer Unified Android SDK and the THEOplayer Android SDK UI Connector are stable when using the same version.
  It's not recommended to use different versions for Unified Android SDK and the Connectors.

## Usage

### Components
To use the different available components simply add them into your UI layout, either through XML or programmatically.
Note that this would require also adding a logic to show/hide the relevant component as needed.

### DefaultUI
Initializing DefaultUI is similar to the initialization of THEOplayerView.
It can be done in one of two ways: through XML or through code.

<br/>

#### Initializing through XML

DefaultUI extends `RelativeLayout` which means it can be treated as a regular ViewGroup.
To add DefaultUI in your XML and connect it to a THEOplayerView, simply wrap the THEOplayerView with DefaultUI.

> Note:
> DefaultUI would only automatically connect to the THEOplayerView, if it is initialized using XML and has THEOplayerView as a child view. 

```xml
<com.theoplayer.android.ui.DefaultUI
    android:id="@+id/theoplayer_default_ui"
    android:layout_width="match_parent"
    android:layout_height="match_parent">
    <com.theoplayer.android.api.THEOplayerView
        android:id="@+id/theoplayerview"
        android:layout_width="match_parent"
        android:layout_height="match_parent" />
</com.theoplayer.android.ui.DefaultUI>
```

Then, DefaultUI can be accessed by querying the view using the `id`.
```kotlin
val defaultUI = findViewById<DefaultUI>(R.id.theoplayer_default_ui)
```

See [activity_main.xml](demo/src/main/res/layout/activity_main.xml) in the demo app for a complete example.

<br/>

#### Initializing programmatically

>Assuming THEOplayerView is already created programmatically.

To initialize DefaultUI:
1. Simply use one of the available constructors of DefaultUI. The most basic constructor requires only a `Context`.
2. Pass the THEOplayerView to DefaultUI.
3. Add both THEOplayerView and DefaultUI to your view hierarchy.

```kotlin
val theoplayerview: THEOplayerView

val defaultUI = DefaultUI(context)
defaultUI.setTHEOplayerView(theoplayerview)

tpvContainer.addView(theoplayerview, LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT))
tpvContainer.addView(defaultUI, LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT))
```

## License

The contents of this package are subject to the [THEOplayer license](https://www.theoplayer.com/terms).
