package com.mcx424.speeddeal.ui

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.mcx424.speeddeal.TimerUiState
import com.mcx424.speeddeal.ui.theme.AccentAmber
import com.mcx424.speeddeal.ui.theme.AccentCyan
import com.mcx424.speeddeal.ui.theme.AccentGreen
import com.mcx424.speeddeal.ui.theme.AccentRed
import com.mcx424.speeddeal.ui.theme.CardDark
import com.mcx424.speeddeal.ui.theme.Ink
import com.mcx424.speeddeal.ui.theme.SeatActive
import com.mcx424.speeddeal.ui.theme.SeatIdle
import com.mcx424.speeddeal.ui.theme.TextMuted
import com.mcx424.speeddeal.ui.theme.TextPrimary
import kotlin.math.roundToInt

@Composable
fun SpeedDealScreen(
    state: TimerUiState,
    onPlayerCountChange: (Int) -> Unit,
    onTurnSecondsChange: (Int) -> Unit,
    onStartResume: () -> Unit,
    onPause: () -> Unit,
    onEndTurn: () -> Unit,
    onResetTurn: () -> Unit
) {
    val progress = if (state.turnSeconds > 0) {
        state.remainingSeconds.toFloat() / state.turnSeconds.toFloat()
    } else 0f

    val timerColor by animateColorAsState(
        targetValue = when {
            state.remainingSeconds <= 5 -> AccentRed
            state.remainingSeconds <= 15 -> AccentAmber
            else -> AccentGreen
        },
        animationSpec = tween(300),
        label = "timerColor"
    )

    val animatedProgress by animateFloatAsState(
        targetValue = progress,
        animationSpec = tween(400),
        label = "progress"
    )

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(
                Brush.verticalGradient(
                    listOf(Ink, Color(0xFF0E1624), Ink)
                )
            )
            .padding(horizontal = 20.dp, vertical = 28.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = "SPEED DEAL",
            style = MaterialTheme.typography.labelLarge,
            color = AccentCyan,
            letterSpacing = 4.sp
        )
        Text(
            text = "Table turn timer",
            style = MaterialTheme.typography.bodyLarge,
            color = TextMuted
        )

        Spacer(modifier = Modifier.height(20.dp))

        SeatRow(
            playerCount = state.playerCount,
            currentPlayer = state.currentPlayer
        )

        Spacer(modifier = Modifier.height(12.dp))

        Text(
            text = "P${state.currentPlayer}",
            style = MaterialTheme.typography.headlineLarge,
            color = TextPrimary,
            fontWeight = FontWeight.Bold
        )

        Spacer(modifier = Modifier.height(8.dp))

        Box(
            contentAlignment = Alignment.Center,
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f)
        ) {
            // Soft glow ring
            Box(
                modifier = Modifier
                    .size(280.dp)
                    .clip(CircleShape)
                    .background(timerColor.copy(alpha = 0.08f))
                    .border(
                        width = 6.dp,
                        color = timerColor.copy(alpha = 0.35f + animatedProgress * 0.4f),
                        shape = CircleShape
                    )
            )
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text(
                    text = formatTime(state.remainingSeconds),
                    style = MaterialTheme.typography.displayLarge.copy(fontSize = 88.sp),
                    color = timerColor,
                    fontWeight = FontWeight.Bold,
                    textAlign = TextAlign.Center
                )
                Text(
                    text = if (state.isRunning) "RUNNING" else "PAUSED",
                    style = MaterialTheme.typography.labelLarge,
                    color = TextMuted,
                    letterSpacing = 2.sp
                )
            }
        }

        Button(
            onClick = onEndTurn,
            modifier = Modifier
                .fillMaxWidth()
                .height(64.dp),
            shape = RoundedCornerShape(18.dp),
            colors = ButtonDefaults.buttonColors(
                containerColor = AccentCyan,
                contentColor = Ink
            )
        ) {
            Text(
                text = "END TURN",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold
            )
        }

        Spacer(modifier = Modifier.height(16.dp))

        Row(
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            FilledTonalButton(
                onClick = { if (state.isRunning) onPause() else onStartResume() },
                modifier = Modifier.weight(1f).height(48.dp),
                shape = RoundedCornerShape(14.dp)
            ) {
                Icon(
                    imageVector = if (state.isRunning) Icons.Default.Pause else Icons.Default.PlayArrow,
                    contentDescription = null
                )
                Spacer(Modifier.width(8.dp))
                Text(if (state.isRunning) "Pause" else "Start")
            }
            IconButton(
                onClick = onResetTurn,
                modifier = Modifier
                    .size(48.dp)
                    .clip(RoundedCornerShape(14.dp))
                    .background(CardDark)
            ) {
                Icon(Icons.Default.Refresh, contentDescription = "Reset turn", tint = TextPrimary)
            }
        }

        Spacer(modifier = Modifier.height(20.dp))

        SettingsCard(
            playerCount = state.playerCount,
            turnSeconds = state.turnSeconds,
            onPlayerCountChange = onPlayerCountChange,
            onTurnSecondsChange = onTurnSecondsChange
        )
    }
}

@Composable
private fun SeatRow(playerCount: Int, currentPlayer: Int) {
    Row(
        horizontalArrangement = Arrangement.spacedBy(10.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        for (i in 1..playerCount) {
            val active = i == currentPlayer
            val bg = if (active) SeatActive else SeatIdle
            val fg = if (active) Ink else TextMuted
            Box(
                contentAlignment = Alignment.Center,
                modifier = Modifier
                    .size(if (active) 48.dp else 40.dp)
                    .clip(CircleShape)
                    .background(bg)
                    .then(
                        if (active) Modifier.border(2.dp, AccentCyan, CircleShape) else Modifier
                    )
            ) {
                Text(
                    text = "P$i",
                    color = fg,
                    fontWeight = FontWeight.Bold,
                    fontSize = if (active) 14.sp else 12.sp
                )
            }
        }
    }
}

@Composable
private fun SettingsCard(
    playerCount: Int,
    turnSeconds: Int,
    onPlayerCountChange: (Int) -> Unit,
    onTurnSecondsChange: (Int) -> Unit
) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(20.dp),
        color = CardDark.copy(alpha = 0.9f)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text("Players", style = MaterialTheme.typography.titleMedium, color = TextPrimary)
            Spacer(Modifier.height(8.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                for (n in 2..6) {
                    val selected = n == playerCount
                    FilledTonalButton(
                        onClick = { onPlayerCountChange(n) },
                        modifier = Modifier.size(width = 52.dp, height = 40.dp),
                        shape = RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.filledTonalButtonColors(
                            containerColor = if (selected) AccentGreen else SeatIdle,
                            contentColor = if (selected) Ink else TextPrimary
                        ),
                        contentPadding = androidx.compose.foundation.layout.PaddingValues(0.dp)
                    ) {
                        Text("$n", fontWeight = FontWeight.Bold)
                    }
                }
            }

            Spacer(Modifier.height(16.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text("Turn length", style = MaterialTheme.typography.titleMedium, color = TextPrimary)
                Text(
                    text = "${turnSeconds}s",
                    style = MaterialTheme.typography.titleMedium,
                    color = AccentAmber,
                    fontWeight = FontWeight.Bold
                )
            }
            Slider(
                value = turnSeconds.toFloat(),
                onValueChange = { onTurnSecondsChange(it.roundToInt()) },
                valueRange = 10f..180f,
                steps = 33,
                colors = SliderDefaults.colors(
                    thumbColor = AccentGreen,
                    activeTrackColor = AccentGreen,
                    inactiveTrackColor = SeatIdle
                )
            )
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text("10s", color = TextMuted, style = MaterialTheme.typography.labelLarge)
                Text("180s", color = TextMuted, style = MaterialTheme.typography.labelLarge)
            }
        }
    }
}

private fun formatTime(totalSeconds: Int): String {
    val m = totalSeconds / 60
    val s = totalSeconds % 60
    return "%d:%02d".format(m, s)
}
