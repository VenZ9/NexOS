package dev.nexos.data.preferences

import dev.nexos.domain.model.TaskbarAlignment
import dev.nexos.domain.model.WallpaperTheme

data class UserPreferences(
    val wallpaperTheme: WallpaperTheme = WallpaperTheme.FLUENT_DARK,
    val customWallpaperUri: String = "",
    val accentColorHex: Long = 0xFF0078D4,
    val taskbarAlignment: TaskbarAlignment = TaskbarAlignment.CENTER,
    val autoHideTaskbar: Boolean = false,
    val showDesktopIcons: Boolean = true,
    val activeRuntimeType: String = "REAL",
    val strictPeValidation: Boolean = true
)
