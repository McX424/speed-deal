package com.mcx424.speeddeal

import android.app.Application
import android.media.AudioManager
import android.media.ToneGenerator
import android.os.Build
import android.os.VibrationEffect
import android.os.Vibrator
import android.os.VibratorManager
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch

enum class TimerPhase {
    /** Full duration loaded, not counting. */
    IDLE,

    /** Counting down. */
    RUNNING,

    /** Stopped part-way through a turn. */
    PAUSED,

    /** Hit zero: brief alert, then the next turn starts automatically. */
    TIME_UP
}

data class TimerUiState(
    val turnSeconds: Int = TimerViewModel.DEFAULT_TURN_SECONDS,
    val remainingSeconds: Int = TimerViewModel.DEFAULT_TURN_SECONDS,
    val phase: TimerPhase = TimerPhase.IDLE,
    val turnNumber: Int = 1
) {
    /** True while the timer is live (counting, or in the time-up hold before the next turn). */
    val isActive: Boolean get() = phase == TimerPhase.RUNNING || phase == TimerPhase.TIME_UP
}

/**
 * A single repeating turn timer: counts down the selected turn length, alerts at zero,
 * then starts the next turn automatically.
 */
class TimerViewModel(application: Application) : AndroidViewModel(application) {

    private val _uiState = MutableStateFlow(TimerUiState())
    val uiState: StateFlow<TimerUiState> = _uiState.asStateFlow()

    private var tickerJob: Job? = null
    private var toneGenerator: ToneGenerator? = try {
        ToneGenerator(AudioManager.STREAM_ALARM, 100)
    } catch (_: Exception) {
        null
    }

    fun setTurnSeconds(seconds: Int) {
        val clamped = seconds.coerceIn(MIN_TURN_SECONDS, MAX_TURN_SECONDS)
        // Applies immediately, including mid-turn.
        _uiState.update { it.copy(turnSeconds = clamped, remainingSeconds = clamped) }
    }

    /** START when stopped, STOP when live. */
    fun toggleStartStop() {
        when (_uiState.value.phase) {
            TimerPhase.IDLE, TimerPhase.PAUSED -> start()
            TimerPhase.RUNNING -> stop()
            // Stopping during the time-up alert cancels the auto-restart; turn is over.
            TimerPhase.TIME_UP -> {
                cancelTicker()
                _uiState.update {
                    it.copy(
                        phase = TimerPhase.IDLE,
                        remainingSeconds = it.turnSeconds,
                        turnNumber = it.turnNumber + 1
                    )
                }
            }
        }
    }

    /**
     * Returns the timer to the selected duration. If the timer is live, the next turn
     * starts straight away (one tap to pass the turn); if stopped, it stays stopped.
     */
    fun reset() {
        val state = _uiState.value
        cancelTicker()
        if (state.isActive) {
            _uiState.update {
                it.copy(
                    phase = TimerPhase.RUNNING,
                    remainingSeconds = it.turnSeconds,
                    turnNumber = it.turnNumber + 1
                )
            }
            startTicker()
        } else {
            _uiState.update { it.copy(phase = TimerPhase.IDLE, remainingSeconds = it.turnSeconds) }
        }
    }

    private fun start() {
        _uiState.update { state ->
            state.copy(
                phase = TimerPhase.RUNNING,
                remainingSeconds = if (state.remainingSeconds <= 0) state.turnSeconds else state.remainingSeconds
            )
        }
        startTicker()
    }

    private fun stop() {
        cancelTicker()
        _uiState.update { state ->
            state.copy(
                phase = if (state.remainingSeconds >= state.turnSeconds) TimerPhase.IDLE else TimerPhase.PAUSED
            )
        }
    }

    private fun cancelTicker() {
        tickerJob?.cancel()
        tickerJob = null
    }

    private fun startTicker() {
        cancelTicker()
        tickerJob = viewModelScope.launch {
            while (isActive) {
                delay(1000L)
                val current = _uiState.value
                if (current.phase != TimerPhase.RUNNING) break
                val next = current.remainingSeconds - 1
                if (next > 0) {
                    _uiState.update { it.copy(remainingSeconds = next) }
                } else {
                    _uiState.update { it.copy(remainingSeconds = 0, phase = TimerPhase.TIME_UP) }
                    playTimeUpFeedback()
                    delay(TIME_UP_HOLD_MS)
                    // Next turn starts automatically.
                    _uiState.update {
                        it.copy(
                            phase = TimerPhase.RUNNING,
                            remainingSeconds = it.turnSeconds,
                            turnNumber = it.turnNumber + 1
                        )
                    }
                }
            }
        }
    }

    private fun playTimeUpFeedback() {
        try {
            toneGenerator?.startTone(ToneGenerator.TONE_CDMA_ALERT_CALL_GUARD, 450)
        } catch (_: Exception) {
            // ignore
        }
        try {
            val app = getApplication<Application>()
            val vibrator = if (Build.VERSION.SDK_INT >= 31) {
                app.getSystemService(VibratorManager::class.java)?.defaultVibrator
            } else {
                @Suppress("DEPRECATION")
                app.getSystemService(Vibrator::class.java)
            }
            vibrator?.vibrate(VibrationEffect.createOneShot(120, VibrationEffect.DEFAULT_AMPLITUDE))
        } catch (_: Exception) {
            // ignore
        }
    }

    override fun onCleared() {
        cancelTicker()
        toneGenerator?.release()
        toneGenerator = null
        super.onCleared()
    }

    companion object {
        const val MIN_TURN_SECONDS = 10
        const val MAX_TURN_SECONDS = 60
        const val DEFAULT_TURN_SECONDS = 25
        const val TIME_UP_HOLD_MS = 2000L
    }
}
