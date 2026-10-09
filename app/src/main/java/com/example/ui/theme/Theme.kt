package com.example.ui.theme

import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext

private val DarkColorScheme = darkColorScheme(
    primary = PrimaryGold,
    onPrimary = Slate950,
    primaryContainer = PrimaryGoldDark,
    onPrimaryContainer = Color.White,
    secondary = ProfitGreen,
    onSecondary = Slate950,
    secondaryContainer = ProfitGreenDark,
    onSecondaryContainer = Color.White,
    tertiary = Color(0xFF38BDF8),
    onTertiary = Slate950,
    background = Slate950,
    onBackground = Slate50,
    surface = SurfaceDark,
    onSurface = Slate50,
    surfaceVariant = CardDark,
    onSurfaceVariant = Slate200,
    outline = BorderDark,
    outlineVariant = Slate700,
    error = ExpenseRed,
    onError = Color.White
)

private val LightColorScheme = lightColorScheme(
    primary = PrimaryGoldDark,
    onPrimary = Color.White,
    primaryContainer = PrimaryGoldLight,
    onPrimaryContainer = Slate900,
    secondary = ProfitGreenDark,
    onSecondary = Color.White,
    secondaryContainer = ProfitGreenLight,
    onSecondaryContainer = Slate900,
    tertiary = Color(0xFF0284C7),
    onTertiary = Color.White,
    background = BackgroundLight,
    onBackground = Slate900,
    surface = SurfaceLight,
    onSurface = Slate900,
    surfaceVariant = Slate100,
    onSurfaceVariant = Slate700,
    outline = BorderLight,
    outlineVariant = Slate200,
    error = ExpenseRed,
    onError = Color.White
)

@Composable
fun MyApplicationTheme(
    darkTheme: Boolean = true, // Default to sleek dark mode matching culinary pro app
    dynamicColor: Boolean = false, // Keep intentional curated palette
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
