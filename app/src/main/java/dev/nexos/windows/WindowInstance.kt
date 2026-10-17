package dev.nexos.windows

import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.DpSize
import androidx.compose.ui.unit.dp
import dev.nexos.apps.NexAppType

data class WindowInstance(
    val id: String,
    val title: String,
    val appType: NexAppType,
    val position: Offset = Offset(60f, 60f),
    val size: DpSize = DpSize(620.dp, 440.dp),
    val minSize: DpSize = DpSize(320.dp, 240.dp),
    val state: WindowState = WindowState.NORMAL,
    val zIndex: Float = 1.0f,
    val isFocused: Boolean = true,
    val params: Map<String, String> = emptyMap()
) {
    val isMinimized: Boolean
        get() = state == WindowState.MINIMIZED

    val isMaximized: Boolean
        get() = state == WindowState.MAXIMIZED

    val isNormal: Boolean
        get() = state == WindowState.NORMAL
}
