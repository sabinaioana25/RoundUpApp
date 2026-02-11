package com.example.roundupapp.ui.theme

import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext

private val DarkColorScheme = darkColorScheme(
  primary = PrimaryDark,
  onPrimary = Color(0xFF002366),
  primaryContainer = PrimaryContainerDark,
  onPrimaryContainer = Color(0xFFDCE7FF),
  
  secondary = SecondaryDark,
  onSecondary = Color(0xFF003544),
  secondaryContainer = SecondaryContainerDark,
  onSecondaryContainer = Color(0xFFCFF9FE),
  
  background = SurfaceDark,
  onBackground = Color(0xFFE8E8E8),
  
  surface = SurfaceDark,
  onSurface = Color(0xFFE8E8E8),
  surfaceVariant = SurfaceVariantDark,
  onSurfaceVariant = Color(0xFFCACACA)
)

private val LightColorScheme = lightColorScheme(
  primary = Primary,
  onPrimary = Color.White,
  primaryContainer = PrimaryContainer,
  onPrimaryContainer = Color(0xFF001B3F),
  
  secondary = Secondary,
  onSecondary = Color.White,
  secondaryContainer = SecondaryContainer,
  onSecondaryContainer = Color(0xFF001F26),
  
  background = Color(0xFFFFFFFF),
  onBackground = Color(0xFF1A1A1A),
  
  surface = SurfaceLight,
  onSurface = Color(0xFF1A1A1A),
  surfaceVariant = SurfaceVariantLight,
  onSurfaceVariant = Color(0xFF45464F)
)

@Composable
fun RoundUpAppTheme(
  darkTheme: Boolean = isSystemInDarkTheme(),
  dynamicColor: Boolean = false,
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
  
  Surface(
    modifier = Modifier.fillMaxSize(),
    color = colorScheme.background
  ) {
    MaterialTheme(
      colorScheme = colorScheme,
      typography = Typography,
      content = content
    )
  }
}
