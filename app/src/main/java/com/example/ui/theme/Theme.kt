package com.example.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable

private val DarkHorrorColorScheme =
  darkColorScheme(
    primary = BloodCrimson,
    onPrimary = SpectralWhite,
    secondary = BloodGlow,
    onSecondary = SpectralWhite,
    background = VoidBlack,
    onBackground = SpectralWhite,
    surface = DeepCrypt,
    onSurface = SpectralWhite,
    surfaceVariant = CryptCard,
    onSurfaceVariant = MutedAsh,
    error = BloodCrimson,
    onError = SpectralWhite
  )

@Composable
fun MyApplicationTheme(
  darkTheme: Boolean = true,
  dynamicColor: Boolean = false,
  content: @Composable () -> Unit,
) {
  MaterialTheme(
    colorScheme = DarkHorrorColorScheme,
    typography = HorrorTypography,
    content = content
  )
}
