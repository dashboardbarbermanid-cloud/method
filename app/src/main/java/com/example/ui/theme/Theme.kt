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

private val DarkColorScheme =
  darkColorScheme(
    primary = AccentLime,
    secondary = AccentLime,
    tertiary = AccentLime,
    background = MatteBlack,
    surface = Color(0xFF151515),
    surfaceVariant = Color(0xFF222222),
    onPrimary = Color.Black,
    onSecondary = Color.Black,
    onBackground = PureWhite,
    onSurface = PureWhite,
    onSurfaceVariant = DarkMuted,
    error = SoftError
  )

private val LightColorScheme =
  lightColorScheme(
    primary = MatteBlack,
    secondary = AccentLime,
    tertiary = ElectricTeal,
    background = PureWhite,
    surface = Color(0xFFF3F4F1),
    surfaceVariant = Color(0xFFEBECE8),
    onPrimary = PureWhite,
    onSecondary = MatteBlack,
    onBackground = MatteBlack,
    onSurface = MatteBlack,
    onSurfaceVariant = Color(0xFF555653),
    error = SoftError
  )

@Composable
fun MyApplicationTheme(
  darkTheme: Boolean = true, // Force a premium Dark theme by default for that streetwear look
  dynamicColor: Boolean = false, // Keep the bespoke storefront theme intact for visual consistency
  content: @Composable () -> Unit,
) {
  val colorScheme = if (darkTheme) DarkColorScheme else LightColorScheme

  MaterialTheme(colorScheme = colorScheme, typography = Typography, content = content)
}
