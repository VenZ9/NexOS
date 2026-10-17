package dev.nexos.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val BaseDarkColorScheme = darkColorScheme(
    primary = WindowsBlue,
    onPrimary = Color.White,
    primaryContainer = WindowsBlueDark,
    onPrimaryContainer = Color.White,
    secondary = WindowsBlueLight,
    onSecondary = Color.Black,
    background = DesktopDarkBg,
    onBackground = TextPrimaryDark,
    surface = WindowDarkBg,
    onSurface = TextPrimaryDark,
    surfaceVariant = WindowDarkTitleBar,
    onSurfaceVariant = TextSecondaryDark,
    outline = WindowBorder,
    error = StatusError,
    onError = Color.White
)

@Composable
fun NexOSTheme(
    accentColor: Color = WindowsBlue,
    content: @Composable () -> Unit
) {
    val colorScheme = BaseDarkColorScheme.copy(
        primary = accentColor,
        secondary = accentColor.copy(alpha = 0.8f)
    )

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}
