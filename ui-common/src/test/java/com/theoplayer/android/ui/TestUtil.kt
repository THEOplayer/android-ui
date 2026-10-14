package com.theoplayer.android.ui

import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.TestScope
import kotlinx.coroutines.test.UnconfinedTestDispatcher

internal fun TestScope.launchObserve(block: suspend () -> Unit): Job =
    backgroundScope.launch(UnconfinedTestDispatcher(testScheduler)) { block() }
