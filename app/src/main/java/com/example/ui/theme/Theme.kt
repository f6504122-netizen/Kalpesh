package com.example.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val KevSpaceDarkColorScheme = darkColorScheme(
    primary = KevSpacePrimary,
    onPrimary = Color.White,
    secondary = KevSpaceSecondary,
    onSecondary = Color.Black,
    tertiary = KevSpaceAccent,
    onTertiary = Color.White,
    background = KevSpaceDarkBg,
    onBackground = KevSpaceText,
    surface = KevSpaceSurface,
    onSurface = KevSpaceText,
    surfaceVariant = KevSpaceCard,
    onSurfaceVariant = KevSpaceTextMuted,
)

private val KevSpaceLightColorScheme = lightColorScheme(
    primary = KevSpacePrimaryDark,
    onPrimary = Color.White,
    secondary = KevSpaceSecondary,
    onSecondary = Color.White,
    tertiary = KevSpaceAccent,
    background = Color(0xFFF1F5F9),
    onBackground = Color(0xFF0F172A),
    surface = Color.White,
    onSurface = Color(0xFF0F172A),
    surfaceVariant = Color(0xFFE2E8F0),
    onSurfaceVariant = Color(0xFF475569)
)

@Composable
fun KevSpaceTheme(
    darkTheme: Boolean = true,
    content: @Composable () -> Unit
) {
    val colorScheme = if (darkTheme) KevSpaceDarkColorScheme else KevSpaceLightColorScheme
    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}

@Composable
fun MyApplicationTheme(
    darkTheme: Boolean = true,
    dynamicColor: Boolean = false,
    content: @Composable () -> Unit
) {
    KevSpaceTheme(darkTheme = darkTheme, content = content)
}
