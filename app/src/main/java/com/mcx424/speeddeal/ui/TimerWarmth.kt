package com.mcx424.speeddeal.ui

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.lerp
import com.mcx424.speeddeal.ui.theme.TextPrimary
import com.mcx424.speeddeal.ui.theme.TimeUpRed
import com.mcx424.speeddeal.ui.theme.WarnAmber
import kotlin.math.max

/**
 * End-of-turn "warm up" ramp for the countdown: white → amber → red.
 *
 * - Warming starts at [warnStartSeconds] = max(25% of the turn, 10 s), capped at the turn length
 *   (25 s turn → 10 s left; 60 s turn → 15 s left).
 * - Full red at [RED_AT_SECONDS] (5 s) left, held until zero / TIME UP.
 * - Pure amber is reached [AMBER_STOP] (40%) of the way through the ramp (8 s left on a 25 s turn).
 * - Colours are interpolated in Oklab (Compose `lerp`) so the blend stays clean, never muddy.
 */
object TimerWarmth {
    const val WARN_FRACTION = 0.25f
    const val MIN_WARN_SECONDS = 10f
    const val RED_AT_SECONDS = 5f
    const val AMBER_STOP = 0.4f

    /** Final-seconds pulse window (gentle breathe), same as the full-red point. */
    const val PULSE_AT_SECONDS = RED_AT_SECONDS

    fun warnStartSeconds(turnSeconds: Int): Float =
        max(turnSeconds * WARN_FRACTION, MIN_WARN_SECONDS).coerceAtMost(turnSeconds.toFloat())

    /** 0 = not warming yet (white), 1 = full red. [remaining] may be fractional. */
    fun progress(remaining: Float, turnSeconds: Int): Float {
        val start = warnStartSeconds(turnSeconds)
        if (start <= RED_AT_SECONDS) return if (remaining <= RED_AT_SECONDS) 1f else 0f
        return ((start - remaining) / (start - RED_AT_SECONDS)).coerceIn(0f, 1f)
    }

    fun color(remaining: Float, turnSeconds: Int): Color {
        val t = progress(remaining, turnSeconds)
        return when {
            t <= 0f -> TextPrimary
            t >= 1f -> TimeUpRed
            t <= AMBER_STOP -> lerp(TextPrimary, WarnAmber, t / AMBER_STOP)
            else -> lerp(WarnAmber, TimeUpRed, (t - AMBER_STOP) / (1f - AMBER_STOP))
        }
    }

    fun shouldPulse(remaining: Float): Boolean = remaining > 0f && remaining <= PULSE_AT_SECONDS
}
