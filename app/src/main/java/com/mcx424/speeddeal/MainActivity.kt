package com.mcx424.speeddeal

import android.graphics.Color
import android.os.Bundle
import android.view.WindowManager
import androidx.activity.ComponentActivity
import androidx.activity.SystemBarStyle
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Surface
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import com.mcx424.speeddeal.ui.SpeedDealScreen
import com.mcx424.speeddeal.ui.theme.SpeedDealTheme

class MainActivity : ComponentActivity() {

    private val viewModel: TimerViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        // Dark status + navigation bars with light icons, matching the #0A0A0A background.
        val barColor = Color.parseColor("#0A0A0A")
        enableEdgeToEdge(
            statusBarStyle = SystemBarStyle.dark(barColor),
            navigationBarStyle = SystemBarStyle.dark(barColor)
        )
        setContent {
            val state by viewModel.uiState.collectAsState()

            LaunchedEffect(state.isActive) {
                if (state.isActive) {
                    window.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
                } else {
                    window.clearFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
                }
            }

            SpeedDealTheme {
                Surface(modifier = Modifier.fillMaxSize()) {
                    SpeedDealScreen(
                        state = state,
                        onTurnSecondsChange = viewModel::setTurnSeconds,
                        onStartStop = viewModel::toggleStartStop,
                        onReset = viewModel::reset
                    )
                }
            }
        }
    }
}
