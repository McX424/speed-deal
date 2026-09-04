package com.mcx424.speeddeal.ui

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
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
import com.mcx424.speeddeal.TimerViewModel
import com.mcx424.speeddeal.ui.theme.AccentAmber
import com.mcx424.speeddeal.ui.theme.AccentCyan
import com.mcx424.speeddeal.ui.theme.AccentGreen
import com.mcx424.speeddeal.ui.theme.AccentRed
import com.mcx424.speeddeal.ui.theme.Ink
import com.mcx424.speeddeal.ui.theme.SeatIdle
import com.mcx424.speeddeal.ui.theme.TextMuted
import com.mcx424.speeddeal.ui.theme.TextPrimary
import kotlin.math.roundToInt
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
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

    var showSettings by remember { mutableStateOf(false) }
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    val scope = rememberCoroutineScope()

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                Brush.verticalGradient(
                    listOf(Ink, Color(0xFF0E1624), Ink)
                )
            )
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 20.dp)
                .padding(top = 36.dp, bottom = 12.dp)
                .navigationBarsPadding(),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = "P${state.currentPlayer}",
                style = MaterialTheme.typography.headlineLarge,
                color = TextPrimary,
                fontWeight = FontWeight.Bold
            )
            Text(
                text = when {
                    state.isRunning -> "YOUR TURN"
                    else -> "READY"
                },
                style = MaterialTheme.typography.labelLarge,
                color = TextMuted,
                letterSpacing = 2.sp
            )

            Spacer(modifier = Modifier.height(8.dp))

            Box(
                contentAlignment = Alignment.Center,
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f)
            ) {
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
                Text(
                    text = formatTime(state.remainingSeconds),
                    style = MaterialTheme.typography.displayLarge.copy(fontSize = 88.sp),
                    color = timerColor,
                    fontWeight = FontWeight.Bold,
                    textAlign = TextAlign.Center
                )
            }

            // Primary: Start when idle, Next when running
            Button(
                onClick = {
                    if (state.isRunning) onEndTurn() else onStartResume()
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(72.dp),
                shape = RoundedCornerShape(18.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = AccentCyan,
                    contentColor = Ink
                )
            ) {
                Text(
                    text = if (state.isRunning) "NEXT" else "START",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold
                )
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Pause + Reset on the main play screen
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                FilledTonalButton(
                    onClick = onPause,
                    enabled = state.isRunning,
                    modifier = Modifier
                        .weight(1f)
                        .height(48.dp),
                    shape = RoundedCornerShape(14.dp),
                    colors = ButtonDefaults.filledTonalButtonColors(
                        containerColor = SeatIdle,
                        contentColor = TextPrimary,
                        disabledContainerColor = SeatIdle.copy(alpha = 0.45f),
                        disabledContentColor = TextMuted
                    )
                ) {
                    Icon(
                        imageVector = Icons.Default.Pause,
                        contentDescription = null
                    )
                    Spacer(Modifier.width(8.dp))
                    Text("Pause", fontWeight = FontWeight.SemiBold)
                }
                FilledTonalButton(
                    onClick = onResetTurn,
                    modifier = Modifier
                        .weight(1f)
                        .height(48.dp),
                    shape = RoundedCornerShape(14.dp),
                    colors = ButtonDefaults.filledTonalButtonColors(
                        containerColor = SeatIdle,
                        contentColor = TextPrimary
                    )
                ) {
                    Icon(
                        imageVector = Icons.Default.Refresh,
                        contentDescription = null
                    )
                    Spacer(Modifier.width(8.dp))
                    Text("Reset", fontWeight = FontWeight.SemiBold)
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Collapsed settings affordance — swipe-up / tap opens sheet; does not pause timer.
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(16.dp))
                    .clickable { showSettings = true }
                    .padding(vertical = 10.dp),
                horizontalArrangement = Arrangement.Center,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = Icons.Default.KeyboardArrowUp,
                    contentDescription = "Open settings",
                    tint = TextMuted
                )
                Spacer(Modifier.width(6.dp))
                Text(
                    text = "Settings · ${state.playerCount} players · ${state.turnSeconds}s",
                    color = TextMuted,
                    style = MaterialTheme.typography.labelLarge
                )
            }
        }

        if (showSettings) {
            ModalBottomSheet(
                onDismissRequest = { showSettings = false },
                sheetState = sheetState,
                containerColor = Color(0xFF121A26),
                contentColor = TextPrimary,
                dragHandle = {
                    Box(
                        modifier = Modifier
                            .padding(top = 12.dp, bottom = 4.dp)
                            .size(width = 40.dp, height = 4.dp)
                            .clip(RoundedCornerShape(2.dp))
                            .background(TextMuted.copy(alpha = 0.5f))
                    )
                }
            ) {
                SettingsSheetContent(
                    playerCount = state.playerCount,
                    turnSeconds = state.turnSeconds,
                    onPlayerCountChange = onPlayerCountChange,
                    onTurnSecondsChange = onTurnSecondsChange,
                    onDone = {
                        scope.launch {
                            sheetState.hide()
                            showSettings = false
                        }
                    }
                )
            }
        }
    }
}

@Composable
private fun SettingsSheetContent(
    playerCount: Int,
    turnSeconds: Int,
    onPlayerCountChange: (Int) -> Unit,
    onTurnSecondsChange: (Int) -> Unit,
    onDone: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .navigationBarsPadding()
            .padding(horizontal = 20.dp)
            .padding(bottom = 24.dp)
    ) {
        Text(
            text = "Settings",
            style = MaterialTheme.typography.titleLarge,
            color = TextPrimary,
            fontWeight = FontWeight.Bold
        )
        Text(
            text = "Changes apply immediately. Timer keeps running.",
            style = MaterialTheme.typography.bodyLarge,
            color = TextMuted
        )

        Spacer(Modifier.height(20.dp))

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
                    contentPadding = PaddingValues(0.dp)
                ) {
                    Text("$n", fontWeight = FontWeight.Bold)
                }
            }
        }

        Spacer(Modifier.height(20.dp))

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
            valueRange = TimerViewModel.MIN_TURN_SECONDS.toFloat()..TimerViewModel.MAX_TURN_SECONDS.toFloat(),
            steps = TimerViewModel.MAX_TURN_SECONDS - TimerViewModel.MIN_TURN_SECONDS - 1,
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
            Text(
                "${TimerViewModel.MIN_TURN_SECONDS}s",
                color = TextMuted,
                style = MaterialTheme.typography.labelLarge
            )
            Text(
                "${TimerViewModel.MAX_TURN_SECONDS}s",
                color = TextMuted,
                style = MaterialTheme.typography.labelLarge
            )
        }

        Spacer(Modifier.height(16.dp))

        Button(
            onClick = onDone,
            modifier = Modifier
                .fillMaxWidth()
                .height(48.dp),
            shape = RoundedCornerShape(14.dp),
            colors = ButtonDefaults.buttonColors(
                containerColor = AccentCyan,
                contentColor = Ink
            )
        ) {
            Text("Done", fontWeight = FontWeight.Bold)
        }
    }
}

private fun formatTime(totalSeconds: Int): String {
    val m = totalSeconds / 60
    val s = totalSeconds % 60
    return "%d:%02d".format(m, s)
}
