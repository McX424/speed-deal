package com.mcx424.speeddeal.ui.theme

import androidx.compose.material3.Typography
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp

// System sans (Roboto on most Android phones).
private val Sans = FontFamily.Default

/** Huge countdown: bold, tabular (fixed-width) figures so digits don't jitter. */
val TimerTextStyle = TextStyle(
    fontFamily = Sans,
    fontWeight = FontWeight.Bold,
    fontSize = 148.sp,
    lineHeight = 148.sp,
    letterSpacing = (-2).sp,
    fontFeatureSettings = "tnum"
)

/** Spaced capitals used for the "SPEED" wordmark. */
val WordmarkTextStyle = TextStyle(
    fontFamily = Sans,
    fontWeight = FontWeight.Black,
    fontSize = 28.sp,
    letterSpacing = 14.sp
)

val Typography = Typography(
    titleLarge = TextStyle(
        fontFamily = Sans,
        fontWeight = FontWeight.SemiBold,
        fontSize = 22.sp
    ),
    titleMedium = TextStyle(
        fontFamily = Sans,
        fontWeight = FontWeight.Medium,
        fontSize = 16.sp
    ),
    bodyLarge = TextStyle(
        fontFamily = Sans,
        fontWeight = FontWeight.Normal,
        fontSize = 16.sp
    ),
    bodyMedium = TextStyle(
        fontFamily = Sans,
        fontWeight = FontWeight.Normal,
        fontSize = 14.sp
    ),
    labelLarge = TextStyle(
        fontFamily = Sans,
        fontWeight = FontWeight.SemiBold,
        fontSize = 14.sp,
        letterSpacing = 0.5.sp
    ),
    labelMedium = TextStyle(
        fontFamily = Sans,
        fontWeight = FontWeight.SemiBold,
        fontSize = 13.sp,
        letterSpacing = 2.sp
    )
)
