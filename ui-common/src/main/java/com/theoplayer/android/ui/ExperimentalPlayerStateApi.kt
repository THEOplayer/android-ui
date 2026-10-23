package com.theoplayer.android.ui

/**
 * Marks declarations that are part of the experimental state holder API of the THEOplayer Open
 * Video UI for Android.
 *
 * The state holder API is experimental and may change in future releases.
 */
@RequiresOptIn(
    message = "This state holder API is experimental and may change in future releases.",
    level = RequiresOptIn.Level.WARNING,
)
@Retention(AnnotationRetention.BINARY)
@Target(
    AnnotationTarget.CLASS,
    AnnotationTarget.FUNCTION,
    AnnotationTarget.PROPERTY,
    AnnotationTarget.CONSTRUCTOR,
)
annotation class ExperimentalPlayerStateApi
