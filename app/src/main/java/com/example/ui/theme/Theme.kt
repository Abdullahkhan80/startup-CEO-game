package com.example.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable


private val DarkColorScheme = darkColorScheme(
  primary = Indigo400,
  secondary = Indigo500,
  tertiary = Amber400,
  background = BgSlate,
  surface = BgSlate,
  onPrimary = TextWhite,
  onSecondary = TextWhite,
  onBackground = TextWhite,
  onSurface = TextWhite
)

@Composable
fun MyApplicationTheme(
  content: @Composable () -> Unit,
) {
  MaterialTheme(
    colorScheme = DarkColorScheme,
    typography = Typography,
    content = content
  )
}
