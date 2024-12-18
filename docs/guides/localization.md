---
sidebar_position: 3
---

# Localization

The Open Video UI for Android can be localized to different languages,
enabling you to reach audiences from different regions of the world.

Localization works
by [providing alternative resources](https://developer.android.com/guide/topics/resources/providing-resources#AlternativeResources)
in one or more languages. You can choose to [change the text of the default language](#change-default-language)
to target a single audience, or [add alternative languages](#add-alternative-languages) to target many audiences.

## Changing the default language {#change-default-language}

By default, the Open Video UI ships with English texts only. If your app targets a non-English speaking audience,
you can override these texts with translations for another language.

1. Copy the English string resources
   from [`res/values/strings.xml`](https://github.com/THEOplayer/android-ui/blob/main/ui/src/main/res/values/strings.xml)
   to your app's resources for the default locale (i.e. `res/values/strings.xml`).
2. Change the resource values to the translated texts.
   For example, to change the title of the "Language" menu, you would change the contents
   of the `theoplayer_ui_menu_language` resource:

   ```xml title="res/values/strings.xml"
   <resources>
     <string name="theoplayer_ui_menu_language">Langue</string> <!-- translated to French -->
   </resources>
   ```

   :::tip
   You can also use
   the [Translations Editor in Android Studio](https://developer.android.com/studio/write/translations-editor)
   to edit these values.
   :::

3. Build and run your app. The translated texts should now appear in your player UI.

## Add alternative languages {#add-alternative-languages}

If your app targets many audiences speaking different languages, you can add multiple translations using
locale-specific resources.

1. Copy the English string resources
   from [`res/values/strings.xml`](https://github.com/THEOplayer/android-ui/blob/main/ui/src/main/res/values/strings.xml)
   to your app's resources for the default locale (i.e. `res/values/strings.xml`).
2. Add a new string resources file for a new locale.
   For example, for French, create the file: `res/values-fr/strings.xml`.
3. Add translated versions for each English string resource to the new resources file.
   For example, to translate the title of the "Language" menu, you would add an entry for `theoplayer_ui_menu_language`:
   ```xml title="res/values-fr/strings.xml"
   <resources>
     <string name="theoplayer_ui_menu_language">Langue</string>
   </resources>
   ```

## Remarks

### Update translations when upgrading Open Video UI

Newer versions of the Open Video UI for Android may add new string resources that need to be translated.

When using custom translations in your app, we recommend pinning the `com.theoplayer.android-ui:android-ui` dependency
in your app's `build.gradle` to a specific version. Avoid using `+` in the dependency's version range.

```groovy title="build.gradle"
dependencies {
    implementation "com.theoplayer.android-ui:android-ui:1.9.0"
}
```

When you decide to upgrade Open Video UI to the latest version, make sure to also update your translations.
Check the history for [`res/values/strings.xml`](https://github.com/THEOplayer/android-ui/commits/main/ui/src/main/res/values/strings.xml)
to see whether any string resources were added or changed since the previous version.

## More information

- [Localize your app on Android Developers](https://developer.android.com/guide/topics/resources/localization)
- [Support different languages and cultures on Android Developers](https://developer.android.com/training/basics/supporting-devices/languages)
