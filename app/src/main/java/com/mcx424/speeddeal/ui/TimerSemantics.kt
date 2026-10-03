package com.mcx424.speeddeal.ui

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.semantics.SemanticsPropertyKey
import androidx.compose.ui.semantics.SemanticsPropertyReceiver

/** Test hooks on the countdown text (colour is quantised to 0.25 s so it doesn't recompose per frame). */
const val TIMER_TEST_TAG = "timer"

val TimerColorKey = SemanticsPropertyKey<Color>("TimerColor")
var SemanticsPropertyReceiver.timerColor by TimerColorKey

val TimerPulsingKey = SemanticsPropertyKey<Boolean>("TimerPulsing")
var SemanticsPropertyReceiver.timerPulsing by TimerPulsingKey
