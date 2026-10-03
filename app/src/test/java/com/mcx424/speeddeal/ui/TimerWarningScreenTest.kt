package com.mcx424.speeddeal.ui

import android.os.Looper
import android.provider.Settings
import androidx.compose.runtime.snapshots.Snapshot
import org.robolectric.Shadows.shadowOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.test.SemanticsMatcher
import androidx.compose.ui.test.assert
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.test.core.app.ApplicationProvider
import com.mcx424.speeddeal.TimerPhase
import com.mcx424.speeddeal.TimerUiState
import com.mcx424.speeddeal.ui.theme.SpeedDealTheme
import com.mcx424.speeddeal.ui.theme.TextPrimary
import com.mcx424.speeddeal.ui.theme.TimeUpRed
import com.mcx424.speeddeal.ui.theme.WarnAmber
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import org.robolectric.annotation.GraphicsMode

/** Drives the real screen with fixed states and checks the countdown's colour/pulse semantics. */
@RunWith(RobolectricTestRunner::class)
@GraphicsMode(GraphicsMode.Mode.NATIVE)
@Config(sdk = [34], qualifiers = "w411dp-h914dp-port-420dpi")
class TimerWarningScreenTest {

    @get:Rule
    val compose = createComposeRule()

    private var state by mutableStateOf(TimerUiState())

    private fun setScreen(reduceMotion: Boolean? = false) {
        compose.mainClock.autoAdvance = false
        compose.setContent {
            SpeedDealTheme {
                if (reduceMotion == null) {
                    // Use the real system-setting lookup.
                    SpeedDealScreen(state = state, onTurnSecondsChange = {}, onStartStop = {}, onReset = {})
                } else {
                    SpeedDealScreen(
                        state = state, onTurnSecondsChange = {}, onStartStop = {}, onReset = {},
                        reduceMotion = reduceMotion
                    )
                }
            }
        }
        frame()
    }

    private fun frame() {
        // Push state writes made from the test thread to the recomposer, then render one frame.
        Snapshot.sendApplyNotifications()
        shadowOf(Looper.getMainLooper()).idle()
        compose.mainClock.advanceTimeByFrame()
    }

    private fun timer() = compose.onNodeWithTag(TIMER_TEST_TAG)

    private fun timerColor(): Color =
        timer().fetchSemanticsNode().config[TimerColorKey]

    private fun pulsing(): Boolean =
        timer().fetchSemanticsNode().config[TimerPulsingKey]

    private fun assertColorNear(expected: Color, actual: Color) {
        assertEquals(expected.red, actual.red, 0.02f)
        assertEquals(expected.green, actual.green, 0.02f)
        assertEquals(expected.blue, actual.blue, 0.02f)
        assertEquals(expected.alpha, actual.alpha, 0.02f)
    }

    private fun running(remaining: Int, turn: Int = 25) {
        state = TimerUiState(turnSeconds = turn, remainingSeconds = remaining, phase = TimerPhase.RUNNING)
        frame()
        frame()
    }

    @After
    fun restoreAnimations() {
        Settings.Global.putFloat(
            ApplicationProvider.getApplicationContext<android.app.Application>().contentResolver,
            Settings.Global.ANIMATOR_DURATION_SCALE, 1f
        )
    }

    @Test
    fun colourAtSeveralRemainingTimes() {
        setScreen()
        running(20); assertColorNear(TextPrimary, timerColor()); assertTrue(!pulsing())
        running(10); assertColorNear(TextPrimary, timerColor())
        running(8); assertColorNear(WarnAmber, timerColor()); assertTrue(!pulsing())
        running(5); assertColorNear(TimeUpRed, timerColor()); assertTrue(pulsing())
        running(3); assertColorNear(TimeUpRed, timerColor()); assertTrue(pulsing())
        running(15, turn = 60); assertColorNear(TextPrimary, timerColor())
        running(11, turn = 60); assertColorNear(WarnAmber, timerColor())
    }

    @Test
    fun resetAndNewTurnGoBackToWhiteWithNoPulseInstantly() {
        setScreen()
        running(3)
        assertTrue(pulsing())
        // Reset while running == new turn at full length: white, no pulse, on the very next frame.
        state = state.copy(remainingSeconds = 25, turnNumber = 2)
        frame()
        assertColorNear(TextPrimary, timerColor())
        assertTrue(!pulsing())

        running(2)
        // Reset while stopped -> idle at full length.
        state = TimerUiState(turnSeconds = 25, remainingSeconds = 25, phase = TimerPhase.IDLE)
        frame()
        assertColorNear(TextPrimary, timerColor())
        assertTrue(!pulsing())
    }

    @Test
    fun pauseHoldsDimmedColourButStopsPulse() {
        setScreen()
        running(3)
        assertTrue(pulsing())
        state = state.copy(phase = TimerPhase.PAUSED)
        frame()
        assertTrue(!pulsing())
        assertColorNear(TimeUpRed.copy(alpha = 0.6f), timerColor())
    }

    @Test
    fun reduceMotionDropsPulseButKeepsColourRamp() {
        setScreen(reduceMotion = true)
        running(3)
        assertTrue(!pulsing())
        assertColorNear(TimeUpRed, timerColor())
        running(8)
        assertColorNear(WarnAmber, timerColor())
    }

    @Test
    fun systemAnimatorScaleZeroIsHonoured() {
        val context = ApplicationProvider.getApplicationContext<android.app.Application>()
        Settings.Global.putFloat(context.contentResolver, Settings.Global.ANIMATOR_DURATION_SCALE, 0f)
        assertTrue(isReduceMotionEnabled(context))
        setScreen(reduceMotion = null)
        running(3)
        timer().assert(SemanticsMatcher.expectValue(TimerPulsingKey, false))
        assertColorNear(TimeUpRed, timerColor())
    }
}
