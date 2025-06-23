package com.example.sweetspot.ui.theme

import android.app.Activity
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
    primary = Color(0xFFFFA726),
    onPrimary = Color.Black,
    primaryContainer = Color(0xFFFFA726),
    onPrimaryContainer = Color.Black,
    secondary = Color(0xFFBB86FC),
    onSecondary = Color.Black,
    secondaryContainer = Color(0xFF6200EE),
    onSecondaryContainer = Color.White,
    background = Color.Black,
    onBackground = Color.White,
    surface = Color.Black,
    onSurface = Color.White,
    outline = Color(0xFFFFA726)
)

private val LightColorScheme = lightColorScheme(
    primary = Color(0xFFFFA726),
    onPrimary = Color.White,
    primaryContainer = Color(0xFFFFCC80),
    onPrimaryContainer = Color.Black,
    secondary = Color(0xFF6200EE),
    onSecondary = Color.White,
    secondaryContainer = Color(0xFFBB86FC),
    onSecondaryContainer = Color.Black,
    background = Color.White,
    onBackground = Color.Black,
    surface = Color.White,
    onSurface = Color.Black,
    outline = Color(0xFFFFA726)
)

@Composable
fun SweetspotTheme(
    isDarkTheme: Boolean,
    content: @Composable () -> Unit
) {
    val colorScheme = if (isDarkTheme) {
        DarkColorScheme
    } else {
        LightColorScheme
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}
