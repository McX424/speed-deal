package com.mcx424.speeddeal.ui

import android.annotation.SuppressLint
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedButton
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
import androidx.compose.ui.graphics.FilterQuality
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.res.imageResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.foundation.text.BasicText
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.State
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.withFrameNanos
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.draw.drawWithContent
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import com.mcx424.speeddeal.R
import com.mcx424.speeddeal.TimerPhase
import com.mcx424.speeddeal.TimerUiState
import com.mcx424.speeddeal.TimerViewModel
import com.mcx424.speeddeal.ui.theme.Background
import com.mcx424.speeddeal.ui.theme.Border
import com.mcx424.speeddeal.ui.theme.HighlightCool
import com.mcx424.speeddeal.ui.theme.SurfaceCard
import com.mcx424.speeddeal.ui.theme.TextPrimary
import com.mcx424.speeddeal.ui.theme.TextSecondary
import com.mcx424.speeddeal.ui.theme.TimeUpRed
import com.mcx424.speeddeal.ui.theme.TimerTextStyle
import com.mcx424.speeddeal.ui.theme.WordmarkTextStyle
import kotlin.math.roundToInt
import kotlinx.coroutines.launch

/** Name of the optional, git-ignored logo drawable (res/drawable-nodpi/logo_monopoly_deal.png). */
private const val LOGO_DRAWABLE_NAME = "logo_monopoly_deal"

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SpeedDealScreen(
    state: TimerUiState,
    onTurnSecondsChange: (Int) -> Unit,
    onStartStop: () -> Unit,
    onReset: () -> Unit,
    reduceMotion: Boolean = rememberReduceMotion()
) {
    var showSettings by remember { mutableStateOf(false) }
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    val scope = rememberCoroutineScope()

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Background)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .statusBarsPadding()
                .navigationBarsPadding()
                .padding(horizontal = 20.dp)
                .padding(top = 20.dp, bottom = 8.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Header()

            TimerBlock(
                state = state,
                reduceMotion = reduceMotion,
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f)
            )

            ControlButtons(
                state = state,
                onStartStop = onStartStop,
                onReset = onReset
            )

            Spacer(modifier = Modifier.height(10.dp))

            // Collapsed turn-length affordance: tap opens the sheet; does not pause the timer.
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(14.dp))
                    .clickable { showSettings = true }
                    .padding(vertical = 12.dp),
                horizontalArrangement = Arrangement.Center,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = Icons.Default.KeyboardArrowUp,
                    contentDescription = stringResource(R.string.settings_open),
                    tint = TextSecondary
                )
                Spacer(Modifier.width(6.dp))
                Text(
                    text = stringResource(R.string.settings_affordance, state.turnSeconds),
                    color = TextSecondary,
                    style = MaterialTheme.typography.labelLarge
                )
            }
        }

        if (showSettings) {
            ModalBottomSheet(
                onDismissRequest = { showSettings = false },
                sheetState = sheetState,
                containerColor = SurfaceCard,
                contentColor = TextPrimary,
                scrimColor = Background.copy(alpha = 0.6f),
                dragHandle = {
                    Box(
                        modifier = Modifier
                            .padding(top = 12.dp, bottom = 4.dp)
                            .size(width = 40.dp, height = 4.dp)
                            .clip(RoundedCornerShape(2.dp))
                            .background(Border)
                    )
                }
            ) {
                TurnLengthSheetContent(
                    turnSeconds = state.turnSeconds,
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

/**
 * Logo (if the git-ignored drawable is present in this build) with "SPEED" underneath.
 * Public builds without the logo fall back to a plain text wordmark.
 */
@Composable
private fun Header() {
    val logoRes = rememberOptionalDrawable(LOGO_DRAWABLE_NAME)
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        if (logoRes != 0) {
            Image(
                bitmap = ImageBitmap.imageResource(id = logoRes),
                contentDescription = stringResource(R.string.logo_description),
                contentScale = ContentScale.Fit,
                filterQuality = FilterQuality.High,
                modifier = Modifier
                    .fillMaxWidth(0.88f)
                    .widthIn(max = 380.dp)
            )
            Spacer(Modifier.height(14.dp))
            SpacedWordmark(text = stringResource(R.string.wordmark_speed))
        } else {
            SpacedWordmark(
                text = stringResource(R.string.wordmark_speed),
                style = WordmarkTextStyle.copy(fontSize = 40.sp, letterSpacing = 16.sp)
            )
            Spacer(Modifier.height(4.dp))
            SpacedWordmark(
                text = stringResource(R.string.wordmark_fallback),
                style = WordmarkTextStyle.copy(fontSize = 16.sp, letterSpacing = 10.sp),
                color = TextSecondary
            )
        }
    }
}

/** Letter-spaced capitals, optically centred (trailing letter-spacing compensated). */
@Composable
private fun SpacedWordmark(
    text: String,
    style: TextStyle = WordmarkTextStyle,
    color: Color = TextPrimary
) {
    Text(
        text = text,
        style = style,
        color = color,
        textAlign = TextAlign.Center,
        modifier = Modifier.padding(start = with(LocalDensity.current) {
            style.letterSpacing.toDp()
        })
    )
}

@SuppressLint("DiscouragedApi")
@Composable
private fun rememberOptionalDrawable(name: String): Int {
    val context = LocalContext.current
    return remember(name) {
        context.resources.getIdentifier(name, "drawable", context.packageName)
    }
}

/** Fast flash for the time-up state; only composed while time is up (and motion is allowed). */
@Composable
private fun rememberTimeUpFlash(): State<Float> {
    val flash = rememberInfiniteTransition(label = "timeUpFlash")
    return flash.animateFloat(
        initialValue = 1f,
        targetValue = 0.35f,
        animationSpec = infiniteRepeatable(tween(350), RepeatMode.Reverse),
        label = "timeUpFlashAlpha"
    )
}

/** Gentle ~1 Hz breathe for the final seconds: 0 → 1 → 0 per second, eased. */
@Composable
private fun rememberBreathe(): State<Float> {
    val breathe = rememberInfiniteTransition(label = "finalSecondsBreathe")
    return breathe.animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(tween(500, easing = FastOutSlowInEasing), RepeatMode.Reverse),
        label = "breathe"
    )
}

private val NoMotion: State<Float> = mutableFloatStateOf(0f)
private val FullAlpha: State<Float> = mutableFloatStateOf(1f)

/**
 * Smooth (per-frame) remaining time while running. The view model ticks once a second; between
 * ticks this interpolates from N down to N-1 using the frame clock, so the colour ramp and progress
 * bar glide instead of stepping. Values are only read in draw/layer lambdas (no per-frame recomposition).
 */
@Composable
private fun rememberSmoothRemaining(state: TimerUiState): () -> Float {
    val base = remember { mutableIntStateOf(state.remainingSeconds) }
    val value = remember { mutableFloatStateOf(state.remainingSeconds.toFloat()) }
    LaunchedEffect(state.remainingSeconds, state.phase) {
        base.intValue = state.remainingSeconds
        value.floatValue = state.remainingSeconds.toFloat()
        if (state.phase == TimerPhase.RUNNING && state.remainingSeconds > 0) {
            val start = withFrameNanos { it }
            var done = false
            while (!done) {
                withFrameNanos { now ->
                    val elapsed = ((now - start) / 1_000_000_000f).coerceIn(0f, 1f)
                    value.floatValue = state.remainingSeconds - elapsed
                    done = elapsed >= 1f
                }
            }
        }
    }
    val running = state.phase == TimerPhase.RUNNING
    val remaining = state.remainingSeconds
    // Until the effect has caught up with a new value (reset / new turn / pause), use the exact
    // value, so reset to white is instant (no stale frame).
    return remember(running, remaining) {
        { if (running && base.intValue == remaining) value.floatValue else remaining.toFloat() }
    }
}

@Composable
private fun TimerBlock(
    state: TimerUiState,
    reduceMotion: Boolean,
    modifier: Modifier = Modifier
) {
    val phase = state.phase
    val turnSeconds = state.turnSeconds
    val isTimeUp = phase == TimerPhase.TIME_UP
    val smoothRemaining = rememberSmoothRemaining(state)

    // Composition-level flags only change at thresholds, so they don't recompose per frame.
    val inPulseWindow by remember(phase, smoothRemaining) {
        derivedStateOf { phase == TimerPhase.RUNNING && TimerWarmth.shouldPulse(smoothRemaining()) }
    }
    val pulsing = inPulseWindow && !reduceMotion
    val breathe = if (pulsing) rememberBreathe() else NoMotion
    val flash = if (isTimeUp && !reduceMotion) rememberTimeUpFlash() else FullAlpha

    // Colour for the current instant (read in draw, so it updates every frame without recomposing).
    val colorNow: () -> Color = remember(phase, turnSeconds, smoothRemaining) {
        {
            when (phase) {
                TimerPhase.IDLE -> TextPrimary
                TimerPhase.TIME_UP -> TimeUpRed
                TimerPhase.RUNNING -> TimerWarmth.color(smoothRemaining(), turnSeconds)
                // Paused holds the current ramp colour, dimmed, and never pulses.
                TimerPhase.PAUSED -> TimerWarmth.color(smoothRemaining(), turnSeconds).copy(alpha = 0.6f)
            }
        }
    }
    // Quantised copy for semantics/tests (changes at most 4x a second, only inside the ramp).
    val semanticsColor by remember(colorNow, phase, turnSeconds, smoothRemaining) {
        derivedStateOf {
            val q = kotlin.math.ceil(smoothRemaining() * 4f) / 4f
            when (phase) {
                TimerPhase.RUNNING -> TimerWarmth.color(q, turnSeconds)
                TimerPhase.PAUSED -> TimerWarmth.color(q, turnSeconds).copy(alpha = 0.6f)
                else -> colorNow()
            }
        }
    }

    val semColorNow = semanticsColor

    val statusText = when (phase) {
        TimerPhase.IDLE -> stringResource(R.string.status_ready)
        TimerPhase.RUNNING -> stringResource(R.string.status_running, state.turnNumber)
        TimerPhase.PAUSED -> stringResource(R.string.status_paused)
        TimerPhase.TIME_UP -> stringResource(R.string.status_time_up)
    }

    Box(modifier = modifier, contentAlignment = Alignment.Center) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(28.dp))
                .background(SurfaceCard)
                .drawWithContent {
                    drawContent()
                    val borderColor = if (isTimeUp) TimeUpRed.copy(alpha = flash.value) else Border
                    drawRoundRect(
                        color = borderColor,
                        cornerRadius = CornerRadius(28.dp.toPx()),
                        style = Stroke(width = 1.dp.toPx())
                    )
                }
                .padding(horizontal = 20.dp, vertical = 28.dp)
        ) {
            Text(
                text = statusText,
                style = MaterialTheme.typography.labelMedium,
                color = if (isTimeUp) TimeUpRed else TextSecondary
            )
            Spacer(Modifier.height(4.dp))
            BasicText(
                text = formatTime(state.remainingSeconds),
                // Fixed-width (tabular) digits: nothing shifts as the numbers change or pulse.
                style = TimerTextStyle.copy(textAlign = TextAlign.Center),
                color = { colorNow() },
                maxLines = 1,
                modifier = Modifier
                    .testTag(TIMER_TEST_TAG)
                    .semantics {
                        timerColor = semColorNow
                        timerPulsing = pulsing
                    }
                    // Layer-only transforms: scale/alpha never trigger a relayout.
                    .graphicsLayer {
                        val b = breathe.value
                        val s = 1f + 0.04f * b
                        scaleX = s
                        scaleY = s
                        alpha = (1f - 0.15f * b) * flash.value
                    }
            )
            Spacer(Modifier.height(16.dp))
            // Thin progress track; fill follows the smooth remaining time and the warm colour.
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(4.dp)
                    .clip(RoundedCornerShape(2.dp))
                    .background(Border)
                    .drawBehind {
                        val fraction = if (turnSeconds > 0) {
                            (smoothRemaining() / turnSeconds).coerceIn(0f, 1f)
                        } else 0f
                        val fill = when (phase) {
                            TimerPhase.IDLE -> HighlightCool
                            TimerPhase.TIME_UP -> TimeUpRed
                            else -> {
                                val c = colorNow()
                                if (TimerWarmth.progress(smoothRemaining(), turnSeconds) <= 0f) {
                                    HighlightCool.copy(alpha = c.alpha)
                                } else c
                            }
                        }
                        drawRoundRect(
                            color = fill,
                            size = Size(size.width * fraction, size.height),
                            cornerRadius = CornerRadius(size.height / 2f)
                        )
                    }
            )
        }
    }
}

/** Start/Stop + Reset: always visible, large, at the bottom. */
@Composable
private fun ControlButtons(
    state: TimerUiState,
    onStartStop: () -> Unit,
    onReset: () -> Unit
) {
    val live = state.isActive
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Button(
            onClick = onStartStop,
            modifier = Modifier
                .weight(1.4f)
                .height(84.dp),
            shape = RoundedCornerShape(22.dp),
            colors = ButtonDefaults.buttonColors(
                containerColor = HighlightCool,
                contentColor = Background
            )
        ) {
            Icon(
                imageVector = if (live) Icons.Default.Pause else Icons.Default.PlayArrow,
                contentDescription = null,
                modifier = Modifier.size(30.dp)
            )
            Spacer(Modifier.width(8.dp))
            Text(
                text = stringResource(if (live) R.string.action_stop else R.string.action_start),
                fontSize = 22.sp,
                fontWeight = FontWeight.Bold,
                letterSpacing = 2.sp
            )
        }
        OutlinedButton(
            onClick = onReset,
            modifier = Modifier
                .weight(1f)
                .height(84.dp),
            shape = RoundedCornerShape(22.dp),
            border = BorderStroke(1.dp, Border),
            colors = ButtonDefaults.outlinedButtonColors(
                containerColor = SurfaceCard,
                contentColor = TextPrimary
            )
        ) {
            Icon(
                imageVector = Icons.Default.Refresh,
                contentDescription = null,
                modifier = Modifier.size(26.dp)
            )
            Spacer(Modifier.width(8.dp))
            Text(
                text = stringResource(R.string.action_reset),
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold,
                letterSpacing = 1.5.sp
            )
        }
    }
}

@Composable
private fun TurnLengthSheetContent(
    turnSeconds: Int,
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
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = stringResource(R.string.settings_title),
                style = MaterialTheme.typography.titleLarge,
                color = TextPrimary
            )
            Text(
                text = stringResource(R.string.settings_seconds, turnSeconds),
                style = MaterialTheme.typography.titleLarge.copy(fontFeatureSettings = "tnum"),
                color = TextPrimary,
                fontWeight = FontWeight.Bold
            )
        }
        Text(
            text = stringResource(R.string.settings_subtitle),
            style = MaterialTheme.typography.bodyMedium,
            color = TextSecondary
        )

        Spacer(Modifier.height(16.dp))

        Slider(
            value = turnSeconds.toFloat(),
            onValueChange = { onTurnSecondsChange(it.roundToInt()) },
            valueRange = TimerViewModel.MIN_TURN_SECONDS.toFloat()..TimerViewModel.MAX_TURN_SECONDS.toFloat(),
            steps = TimerViewModel.MAX_TURN_SECONDS - TimerViewModel.MIN_TURN_SECONDS - 1,
            colors = SliderDefaults.colors(
                thumbColor = HighlightCool,
                activeTrackColor = HighlightCool,
                inactiveTrackColor = Border,
                activeTickColor = HighlightCool.copy(alpha = 0f),
                inactiveTickColor = Border.copy(alpha = 0f)
            )
        )
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(
                stringResource(R.string.settings_seconds, TimerViewModel.MIN_TURN_SECONDS),
                color = TextSecondary,
                style = MaterialTheme.typography.labelLarge
            )
            Text(
                stringResource(R.string.settings_seconds, TimerViewModel.MAX_TURN_SECONDS),
                color = TextSecondary,
                style = MaterialTheme.typography.labelLarge
            )
        }

        Spacer(Modifier.height(16.dp))

        Button(
            onClick = onDone,
            modifier = Modifier
                .fillMaxWidth()
                .height(56.dp),
            shape = RoundedCornerShape(16.dp),
            colors = ButtonDefaults.buttonColors(
                containerColor = HighlightCool,
                contentColor = Background
            )
        ) {
            Text(
                stringResource(R.string.settings_done),
                fontWeight = FontWeight.Bold
            )
        }
    }
}

private fun formatTime(totalSeconds: Int): String {
    val m = totalSeconds / 60
    val s = totalSeconds % 60
    return "%d:%02d".format(m, s)
}
