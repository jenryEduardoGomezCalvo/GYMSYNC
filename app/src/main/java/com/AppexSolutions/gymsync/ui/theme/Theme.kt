package com.AppexSolutions.gymsync.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalContext
import android.app.Activity
import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext

private val DarkColorScheme = darkColorScheme(
    primary = ElectricBlue,
    onPrimary = TextPrimary,
    primaryContainer = NavyBlue,
    onPrimaryContainer = TextPrimary,

    secondary = BrightBlue,
    onSecondary = TextPrimary,
    secondaryContainer = SteelBlue,
    onSecondaryContainer = TextSecondary,

    tertiary = SuccessGreen,
    onTertiary = TextPrimary,

    error = ErrorRed,
    onError = TextPrimary,
    errorContainer = Color(0xFF7F1D1D),
    onErrorContainer = TextPrimary,

    background = NavyBlue,
    onBackground = TextPrimary,
    surface = SurfaceDark,
    onSurface = TextPrimary,
    surfaceVariant = CardBackground,
    onSurfaceVariant = TextSecondary,

    outline = SteelBlue,
    outlineVariant = SteelBlue,

    scrim = DarkNavy
)

private val LightColorScheme = lightColorScheme(
    primary = NavyBlue,
    onPrimary = TextPrimary,
    primaryContainer = SteelBlue,
    onPrimaryContainer = TextPrimary,

    secondary = ElectricBlue,
    onSecondary = TextPrimary,
    secondaryContainer = BrightBlue,
    onSecondaryContainer = NavyBlue,

    tertiary = SuccessGreen,
    onTertiary = TextPrimary,

    error = ErrorRed,
    onError = TextPrimary,
    errorContainer = Color(0xFFFEE2E2),
    onErrorContainer = Color(0xFF991B1B),

    background = Color(0xFFF8FAFC),
    onBackground = NavyBlue,
    surface = TextPrimary,
    onSurface = NavyBlue,
    surfaceVariant = Color(0xFFE2E8F0),
    onSurfaceVariant = SteelBlue,

    outline = SteelBlue,
    outlineVariant = Color(0xFFCBD5E1),

    scrim = Color(0xFF000000).copy(alpha = 0.32f)
)

@Composable
fun GymSyncTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    // Dynamic color is available on Android 12+
    dynamicColor: Boolean = true,
    content: @Composable () -> Unit
) {
    val colorScheme = when {
        dynamicColor && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S -> {
            val context = LocalContext.current
            if (darkTheme) dynamicDarkColorScheme(context) else dynamicLightColorScheme(context)
        }

        darkTheme -> DarkColorScheme
        else -> LightColorScheme
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}