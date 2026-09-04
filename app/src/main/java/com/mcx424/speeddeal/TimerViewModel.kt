package com.mcx424.speeddeal

import android.app.Application
import android.media.AudioManager
import android.media.ToneGenerator
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

data class TimerUiState(
    val playerCount: Int = 4,
    val currentPlayer: Int = 1,
    val turnSeconds: Int = 25,
    val remainingSeconds: Int = 25,
    val isRunning: Boolean = false,
    val totalTurns: Int = 0
)

class TimerViewModel(application: Application) : AndroidViewModel(application) {

    private val _uiState = MutableStateFlow(TimerUiState())
    val uiState: StateFlow<TimerUiState> = _uiState.asStateFlow()

    private var tickerJob: Job? = null
    private var toneGenerator: ToneGenerator? = null

    init {
        try {
            toneGenerator = ToneGenerator(AudioManager.STREAM_ALARM, 100)
        } catch (_: Exception) {
            toneGenerator = null
        }
        // Idle on fresh open — user taps Start; do not auto-start.
    }

    fun setPlayerCount(count: Int) {
        val clamped = count.coerceIn(2, 6)
        _uiState.update { state ->
            val player = state.currentPlayer.coerceIn(1, clamped)
            state.copy(playerCount = clamped, currentPlayer = player)
        }
    }

    fun setTurnSeconds(seconds: Int) {
        val clamped = seconds.coerceIn(MIN_TURN_SECONDS, MAX_TURN_SECONDS)
        _uiState.update { state ->
            // Apply immediately when changed (including mid-turn).
            state.copy(turnSeconds = clamped, remainingSeconds = clamped)
        }
    }

    fun startOrResume() {
        if (_uiState.value.isRunning) return
        _uiState.update { it.copy(isRunning = true) }
        startTicker()
    }

    fun pause() {
        tickerJob?.cancel()
        tickerJob = null
        _uiState.update { it.copy(isRunning = false) }
    }

    fun endTurnEarly() {
        advanceToNextPlayer(playSound = false)
    }

    fun resetCurrentTurn() {
        // Stop timer, restore full turn length, stay on current player, idle (Start).
        pause()
        _uiState.update { it.copy(remainingSeconds = it.turnSeconds) }
    }

    private fun startTicker() {
        tickerJob?.cancel()
        tickerJob = viewModelScope.launch {
            while (isActive) {
                delay(1000L)
                val current = _uiState.value
                if (!current.isRunning) break
                val next = current.remainingSeconds - 1
                if (next <= 0) {
                    advanceToNextPlayer(playSound = true)
                } else {
                    _uiState.update { it.copy(remainingSeconds = next) }
                }
            }
        }
    }

    private fun advanceToNextPlayer(playSound: Boolean) {
        if (playSound) {
            playTurnEndFeedback()
        }
        _uiState.update { state ->
            val nextPlayer = if (state.currentPlayer >= state.playerCount) 1 else state.currentPlayer + 1
            state.copy(
                currentPlayer = nextPlayer,
                remainingSeconds = state.turnSeconds,
                isRunning = true,
                totalTurns = state.totalTurns + 1
            )
        }
        // Restart ticker cleanly after advance
        startTicker()
    }

    private fun playTurnEndFeedback() {
        try {
            toneGenerator?.startTone(ToneGenerator.TONE_CDMA_ALERT_CALL_GUARD, 450)
        } catch (_: Exception) {
            // ignore
        }
        try {
            val app = getApplication<Application>()
            val vibrator = if (android.os.Build.VERSION.SDK_INT >= 31) {
                val vm = app.getSystemService(VibratorManager::class.java)
                vm?.defaultVibrator
            } else {
                @Suppress("DEPRECATION")
                app.getSystemService(Vibrator::class.java)
            }
            vibrator?.vibrate(
                VibrationEffect.createOneShot(120, VibrationEffect.DEFAULT_AMPLITUDE)
            )
        } catch (_: Exception) {
            // ignore
        }
    }

    override fun onCleared() {
        tickerJob?.cancel()
        toneGenerator?.release()
        toneGenerator = null
        super.onCleared()
    }

    companion object {
        const val MIN_TURN_SECONDS = 10
        const val MAX_TURN_SECONDS = 60
        const val DEFAULT_TURN_SECONDS = 25
    }
}
