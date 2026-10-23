package com.example.ui.theme

import android.app.Activity
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalView
import androidx.core.view.WindowCompat

private val PubgDarkColorScheme = darkColorScheme(
    primary = PubgOrange,
    onPrimary = CharcoalBlack,
    primaryContainer = PubgOrangeGlow,
    onPrimaryContainer = PubgOrangeBright,
    secondary = PubgGold,
    onSecondary = CharcoalBlack,
    secondaryContainer = DarkSurfaceVariant,
    onSecondaryContainer = PubgGold,
    tertiary = GlacierCyan,
    onTertiary = CharcoalBlack,
    background = CharcoalBlack,
    onBackground = TextPrimary,
    surface = DarkSurface,
    onSurface = TextPrimary,
    surfaceVariant = DarkSurfaceVariant,
    onSurfaceVariant = TextSecondary,
    outline = DarkCardBorder,
    error = AceRed
)

@Composable
fun MyApplicationTheme(
    darkTheme: Boolean = true, // Gaming theme defaults to dark
    dynamicColor: Boolean = false, // Keep branded esports colors
    content: @Composable () -> Unit
) {
    val colorScheme = PubgDarkColorScheme
    val view = LocalView.current

    if (!view.isInEditMode) {
        SideEffect {
            val window = (view.context as? Activity)?.window
            if (window != null) {
                window.statusBarColor = CharcoalBlack.toArgb()
                window.navigationBarColor = CharcoalBlack.toArgb()
                WindowCompat.getInsetsController(window, view).isAppearanceLightStatusBars = false
                WindowCompat.getInsetsController(window, view).isAppearanceLightNavigationBars = false
            }
        }
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}
