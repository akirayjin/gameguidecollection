package com.akirayjin.gameguide.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val DarkColorScheme = darkColorScheme(
    primary = VitaBlue,
    onPrimary = VitaTextPrimary,
    secondary = VitaGold,
    background = VitaNavyDark,
    surface = VitaSurfaceDark,
    onBackground = VitaTextPrimary,
    onSurface = VitaTextPrimary,
    outline = VitaCardBorder
)

private val LightColorScheme = lightColorScheme(
    primary = VitaBlue,
    onPrimary = Color.White,
    secondary = VitaGold,
    background = Color(0xFFF0F2F5),
    surface = Color(0xFFFFFFFF),
    onBackground = Color(0xFF1F2328),
    onSurface = Color(0xFF1F2328),
    outline = Color(0xFFD0D7DE)
)

@Composable
fun GameGuideTheme(
    darkTheme: Boolean = true,
    content: @Composable () -> Unit
) {
    val colorScheme = if (darkTheme) DarkColorScheme else LightColorScheme
    MaterialTheme(
        colorScheme = colorScheme,
        content = content
    )
}
