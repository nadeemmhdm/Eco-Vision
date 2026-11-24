package org.ecovision.app.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val DarkColorScheme = darkColorScheme(
    primary = EcoEmerald500,
    onPrimary = Color.Black,
    primaryContainer = EcoEmerald600,
    onPrimaryContainer = Color.White,
    secondary = LeafCoinGold,
    onSecondary = Color.Black,
    secondaryContainer = Color(0xFF78350F),
    onSecondaryContainer = LeafCoinGoldLight,
    background = Slate900,
    onBackground = Color.White,
    surface = Slate850,
    onSurface = Color.White,
    surfaceVariant = Slate800,
    onSurfaceVariant = Slate400,
    outline = CardBorderBright,
    error = CoralRed,
    onError = Color.White
)

private val LightColorScheme = lightColorScheme(
    primary = EcoEmerald600,
    onPrimary = Color.White,
    primaryContainer = EcoEmerald100,
    onPrimaryContainer = EcoEmerald600,
    secondary = LeafCoinGold,
    onSecondary = Color.White,
    background = Color(0xFFF8FAFC),
    onBackground = Slate900,
    surface = Color.White,
    onSurface = Slate900,
    surfaceVariant = Slate100,
    onSurfaceVariant = Slate600,
    outline = Slate200,
    error = CoralRed,
    onError = Color.White
)

@Composable
fun EcoVisionTheme(
    darkTheme: Boolean = true, // Default to sleek eco-dark aesthetic
    content: @Composable () -> Unit
) {
    val colorScheme = if (darkTheme) DarkColorScheme else LightColorScheme

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}
