package dev.nexos.windows

import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.unit.DpSize

sealed class WindowAction {
    data class Focus(val windowId: String) : WindowAction()
    data class Minimize(val windowId: String) : WindowAction()
    data class Maximize(val windowId: String) : WindowAction()
    data class Restore(val windowId: String) : WindowAction()
    data class ToggleMaximize(val windowId: String) : WindowAction()
    data class Close(val windowId: String) : WindowAction()
    data class Move(val windowId: String, val delta: Offset) : WindowAction()
    data class Resize(val windowId: String, val newSize: DpSize) : WindowAction()
    data class UpdateTitle(val windowId: String, val newTitle: String) : WindowAction()
}
