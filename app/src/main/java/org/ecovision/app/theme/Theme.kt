package org.ecovision.app.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

enum class ThemeMode {
    SYSTEM, // Auto-detect based on Android system setting
    LIGHT,  // Force crisp Light Theme
    DARK    // Force sleek Eco Dark Theme
}

private val DarkColorScheme = darkColorScheme(
    primary = EcoEmerald500,
    onPrimary = Color.Black,
    primaryContainer = EcoEmerald900,
    onPrimaryContainer = EcoEmerald100,
    secondary = LeafCoinGold,
    onSecondary = Color.Black,
    secondaryContainer = Color(0xFF78350F),
    onSecondaryContainer = LeafCoinGoldLight,
    background = Slate950,
    onBackground = Color.White,
    surface = Slate900,
    onSurface = Color.White,
    surfaceVariant = Slate850,
    onSurfaceVariant = Slate300,
    outline = CardBorderBright,
    outlineVariant = CardBorder,
    error = CoralRed,
    onError = Color.White
)

private val LightColorScheme = lightColorScheme(
    primary = EcoEmerald700,
    onPrimary = Color.White,
    primaryContainer = EcoEmerald100,
    onPrimaryContainer = EcoEmerald900,
    secondary = Color(0xFFD97706),
    onSecondary = Color.White,
    secondaryContainer = Color(0xFFFEF3C7),
    onSecondaryContainer = Color(0xFF92400E),
    background = Color(0xFFF8FAFC),
    onBackground = Slate900,
    surface = Color.White,
    onSurface = Slate900,
    surfaceVariant = Color(0xFFF1F5F9),
    onSurfaceVariant = Slate600,
    outline = Slate300,
    outlineVariant = Slate200,
    error = CoralRed,
    onError = Color.White
)

@Composable
fun EcoVisionTheme(
    themeMode: ThemeMode = ThemeMode.SYSTEM,
    content: @Composable () -> Unit
) {
    val isSystemDark = isSystemInDarkTheme()
    val isDark = when (themeMode) {
        ThemeMode.SYSTEM -> isSystemDark
        ThemeMode.LIGHT -> false
        ThemeMode.DARK -> true
    }

    val colorScheme = if (isDark) DarkColorScheme else LightColorScheme

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}
