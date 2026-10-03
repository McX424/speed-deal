package com.mcx424.speeddeal.ui

import androidx.compose.ui.graphics.Color
import com.mcx424.speeddeal.ui.theme.TextPrimary
import com.mcx424.speeddeal.ui.theme.TimeUpRed
import com.mcx424.speeddeal.ui.theme.WarnAmber
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotEquals
import org.junit.Assert.assertTrue
import org.junit.Test

/** Pure JVM tests for the white → amber → red ramp. */
class TimerWarmthTest {

    private fun assertColor(expected: Color, actual: Color, tol: Float = 0.01f) {
        assertEquals("red", expected.red, actual.red, tol)
        assertEquals("green", expected.green, actual.green, tol)
        assertEquals("blue", expected.blue, actual.blue, tol)
    }

    @Test
    fun warnStartIsQuarterOfTurnWithTenSecondFloorCappedAtTurn() {
        assertEquals(10f, TimerWarmth.warnStartSeconds(25), 0f)
        assertEquals(15f, TimerWarmth.warnStartSeconds(60), 0f)
        assertEquals(12f, TimerWarmth.warnStartSeconds(48), 0f)
        assertEquals(10f, TimerWarmth.warnStartSeconds(40), 0f)
        assertEquals(10f, TimerWarmth.warnStartSeconds(10), 0f) // capped at the turn length
    }

    @Test
    fun colourAtSeveralRemainingTimesOn25sTurn() {
        assertColor(TextPrimary, TimerWarmth.color(25f, 25))
        assertColor(TextPrimary, TimerWarmth.color(10f, 25)) // ramp starts here
        assertColor(WarnAmber, TimerWarmth.color(8f, 25)) // pure amber 40% through
        assertColor(TimeUpRed, TimerWarmth.color(5f, 25)) // full red
        assertColor(TimeUpRed, TimerWarmth.color(3f, 25)) // held
        assertColor(TimeUpRed, TimerWarmth.color(0f, 25))

        // In-between values are real blends, not steps.
        val nine = TimerWarmth.color(9f, 25)
        assertNotEquals(TextPrimary, nine)
        assertNotEquals(WarnAmber, nine)
        assertTrue(nine.blue < TextPrimary.blue && nine.blue > WarnAmber.blue)
        val sixAndHalf = TimerWarmth.color(6.5f, 25)
        assertTrue(sixAndHalf.green < WarnAmber.green && sixAndHalf.green > TimeUpRed.green)
    }

    @Test
    fun sixtySecondTurnStartsWarmingAtFifteen() {
        assertColor(TextPrimary, TimerWarmth.color(20f, 60))
        assertColor(TextPrimary, TimerWarmth.color(15f, 60))
        assertNotEquals(TextPrimary, TimerWarmth.color(14f, 60))
        assertColor(WarnAmber, TimerWarmth.color(11f, 60))
        assertColor(TimeUpRed, TimerWarmth.color(5f, 60))
    }

    @Test
    fun rampIsContinuousAndMonotonic() {
        var last = -1f
        var r = 12f
        while (r >= 0f) {
            val p = TimerWarmth.progress(r, 25)
            assertTrue("progress must not go backwards at $r", p >= last)
            // Small time step -> small colour step (no jumps).
            val a = TimerWarmth.color(r, 25)
            val b = TimerWarmth.color(r - 0.05f, 25)
            assertTrue(kotlin.math.abs(a.red - b.red) < 0.03f)
            assertTrue(kotlin.math.abs(a.green - b.green) < 0.03f)
            assertTrue(kotlin.math.abs(a.blue - b.blue) < 0.06f)
            last = p
            r -= 0.05f
        }
    }

    @Test
    fun pulseOnlyInFinalFiveSeconds() {
        assertFalse(TimerWarmth.shouldPulse(8f))
        assertFalse(TimerWarmth.shouldPulse(5.01f))
        assertTrue(TimerWarmth.shouldPulse(5f))
        assertTrue(TimerWarmth.shouldPulse(0.5f))
        assertFalse(TimerWarmth.shouldPulse(0f))
    }
}
