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
}
