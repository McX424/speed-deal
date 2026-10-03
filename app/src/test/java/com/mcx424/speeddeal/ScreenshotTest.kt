package com.mcx424.speeddeal

import android.os.Looper
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onAllNodesWithText
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.onRoot
import androidx.compose.ui.test.performClick
import com.github.takahirom.roborazzi.captureRoboImage
import java.time.Duration
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.Shadows.shadowOf
import org.robolectric.annotation.Config
import org.robolectric.annotation.GraphicsMode

/**
 * Renders the real MainActivity at a typical phone size (1080x2400 @ 420dpi ≈ 411x914dp)
 * in each timer state. Output: app/build/outputs/roborazzi/ (git-ignored).
 * If the personal logo drawable is present the screenshots include it, so never commit them.
 */
@RunWith(RobolectricTestRunner::class)
@GraphicsMode(GraphicsMode.Mode.NATIVE)
@Config(sdk = [34], qualifiers = "w411dp-h914dp-port-420dpi")
class ScreenshotTest {

    @get:Rule
    val compose = createAndroidComposeRule<MainActivity>()

    private fun advanceSeconds(seconds: Long) {
        shadowOf(Looper.getMainLooper()).idleFor(Duration.ofSeconds(seconds))
        compose.waitForIdle()
    }

    private fun advanceUntilText(text: String, maxSeconds: Int) {
        repeat(maxSeconds) {
            if (compose.onAllNodesWithText(text).fetchSemanticsNodes().isNotEmpty()) return
            advanceSeconds(1)
        }
        compose.onNodeWithText(text).assertExists()
    }

    private fun shot(name: String) {
        compose.waitForIdle()
        compose.onRoot().captureRoboImage("build/outputs/roborazzi/$name.png")
    }

    @Test
    fun idle() {
        compose.onNodeWithText("START").assertExists()
        compose.onNodeWithText("RESET").assertExists()
        shot("idle")
    }

    @Test
    fun running() {
        compose.onNodeWithText("START").performClick()
        advanceSeconds(7)
        compose.onNodeWithText("STOP").assertExists()
        compose.onNodeWithText("TURN 1").assertExists()
        shot("running")
    }

    @Test
    fun paused() {
        compose.onNodeWithText("START").performClick()
        advanceSeconds(12)
        compose.onNodeWithText("STOP").performClick()
        compose.onNodeWithText("PAUSED").assertExists()
        shot("paused")
    }

    @Test
    fun timeUp() {
        compose.onNodeWithText("START").performClick()
        advanceUntilText("TIME UP", maxSeconds = 30)
        compose.onNodeWithText("STOP").assertExists()
        shot("timeup")
        // After the 2 s hold the next turn starts automatically.
        advanceSeconds(3)
        compose.onNodeWithText("TURN 2").assertExists()
    }

    @Test
    fun resetWhileRunningStartsNextTurn() {
        compose.onNodeWithText("START").performClick()
        advanceSeconds(5)
        compose.onNodeWithText("RESET").performClick()
        compose.onNodeWithText("0:25").assertExists()
        compose.onNodeWithText("TURN 2").assertExists()
        compose.onNodeWithText("STOP").performClick()
        compose.onNodeWithText("RESET").performClick()
        compose.onNodeWithText("READY").assertExists()
        compose.onNodeWithText("0:25").assertExists()
    }

    /** Advance the view-model clock (main looper) and the Compose frame clock together. */
    private fun stepBoth(ms: Long) {
        shadowOf(Looper.getMainLooper()).idleFor(Duration.ofMillis(ms))
        compose.mainClock.advanceTimeBy(ms)
    }

    /**
     * Run a 25 s turn until [secondsLeft] has *just* appeared (start of that second), with the
     * Compose clock under manual control so the smooth ramp/pulse are captured at a real instant.
     */
    private fun runUntilShowing(secondsLeft: Int, manualFromSeconds: Int = secondsLeft + 1) {
        compose.onNodeWithText("START").performClick()
        // Infinite animations (the final-seconds breathe) only tick under the manual clock, so
        // switch to manual before the pulse window when capturing it.
        advanceUntilText("0:%02d".format(manualFromSeconds), maxSeconds = 30)
        compose.mainClock.autoAdvance = false
        val target = "0:%02d".format(secondsLeft)
        repeat(200) {
            if (compose.onAllNodesWithText(target).fetchSemanticsNodes().isNotEmpty()) return@repeat
            stepBoth(50)
        }
        compose.onNodeWithText(target).assertExists()
        compose.mainClock.advanceTimeByFrame()
    }

    @Test
    fun warnAmber() {
        runUntilShowing(8)
        compose.onRoot().captureRoboImage("build/outputs/roborazzi/warn-amber.png")
    }

    @Test
    fun warnRed() {
        runUntilShowing(3, manualFromSeconds = 8)
        // ~Peak of the breathe (500 ms into the 1 s cycle) so the pulse shows in the still.
        stepBoth(400)
        compose.onRoot().captureRoboImage("build/outputs/roborazzi/warn-red.png")
    }

    /**
     * Frames across the last ~11 s of a 25 s turn (8 fps), through TIME UP into the next turn.
     * Both clocks advance together: the Android main looper (view-model ticks) and the Compose
     * frame clock (smooth ramp + pulse).
     */
    @Test
    fun warningFrames() {
        runUntilShowing(11)
        val stepMs = 125L
        val frames = ((11 + 3) * 1000 / stepMs).toInt()
        for (i in 0 until frames) {
            compose.onRoot().captureRoboImage("build/outputs/roborazzi/warning-frames/f%03d.png".format(i))
            stepBoth(stepMs)
        }
    }
}
