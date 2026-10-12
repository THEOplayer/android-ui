package com.theoplayer.android.ui

/**
 * Marks declarations that are internal to the THEOplayer Open Video UI for Android.
 *
 * These are only public so they can be shared between the UI modules, and may change at any time.
 */
@RequiresOptIn(
    message = "This API is internal to the THEOplayer Open Video UI for Android and may change at any time.",
    level = RequiresOptIn.Level.ERROR
)
@Retention(AnnotationRetention.BINARY)
@Target(
    AnnotationTarget.CLASS,
    AnnotationTarget.FUNCTION,
    AnnotationTarget.PROPERTY,
    AnnotationTarget.CONSTRUCTOR
)
annotation class InternalUiApi
