package com.example.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Shapes
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp

private val EduDarkColorScheme = darkColorScheme(
    primary = Violet400,
    onPrimary = Navy950,
    primaryContainer = Color(0xFF2E1065),
    onPrimaryContainer = Violet100,
    secondary = Teal500,
    onSecondary = Navy950,
    secondaryContainer = Color(0xFF134E4A),
    onSecondaryContainer = Teal100,
    tertiary = Blue500,
    onTertiary = Color.White,
    background = Slate950,
    onBackground = Slate100,
    surface = Navy900,
    onSurface = Slate100,
    surfaceVariant = Navy800,
    onSurfaceVariant = Slate300,
    outline = Slate700,
    outlineVariant = Slate800,
    error = Rose500,
    onError = Color.White,
    errorContainer = Color(0xFF4C0519),
    onErrorContainer = Rose100
)

private val EduLightColorScheme = lightColorScheme(
    primary = Violet600,
    onPrimary = Color.White,
    primaryContainer = Violet100,
    onPrimaryContainer = Color(0xFF4C1D95),
    secondary = Teal600,
    onSecondary = Color.White,
    secondaryContainer = Teal100,
    onSecondaryContainer = Color(0xFF115E59),
    tertiary = Blue600,
    onTertiary = Color.White,
    background = Lavender50,
    onBackground = Slate900,
    surface = Color.White,
    onSurface = Slate900,
    surfaceVariant = Lavender100,
    onSurfaceVariant = Slate600,
    outline = Slate200,
    outlineVariant = Slate100,
    error = Rose600,
    onError = Color.White,
    errorContainer = Rose100,
    onErrorContainer = Color(0xFF881337)
)

val EduMetricsShapes = Shapes(
    small = RoundedCornerShape(10.dp),
    medium = RoundedCornerShape(16.dp),
    large = RoundedCornerShape(20.dp),
    extraLarge = RoundedCornerShape(24.dp)
)

@Composable
fun EduMetricsTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit
) {
    val colorScheme = if (darkTheme) EduDarkColorScheme else EduLightColorScheme

    MaterialTheme(
        colorScheme = colorScheme,
        typography = EduMetricsTypography,
        shapes = EduMetricsShapes,
        content = content
    )
}
