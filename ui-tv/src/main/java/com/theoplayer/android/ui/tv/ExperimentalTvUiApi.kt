package com.theoplayer.android.ui.tv

/**
 * Marks declarations that are part of the experimental THEOplayer Android TV UI API.
 */
@RequiresOptIn(
    message = "This TV UI API is experimental and may change in future releases.",
    level = RequiresOptIn.Level.WARNING
)
@Retention(AnnotationRetention.BINARY)
@Target(
    AnnotationTarget.CLASS,
    AnnotationTarget.FUNCTION,
    AnnotationTarget.PROPERTY,
    AnnotationTarget.CONSTRUCTOR
)
annotation class ExperimentalTvUiApi
