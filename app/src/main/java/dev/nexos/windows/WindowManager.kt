package dev.nexos.windows

import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.unit.DpSize
import androidx.compose.ui.unit.dp
import dev.nexos.apps.NexAppType
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import java.util.UUID

class WindowManager {

    private val _windows = MutableStateFlow<List<WindowInstance>>(emptyList())
    val windows: StateFlow<List<WindowInstance>> = _windows.asStateFlow()

    private var zIndexCounter = 1.0f

    fun openWindow(
        appType: NexAppType,
        customTitle: String? = null,
        params: Map<String, String> = emptyMap(),
        preferredSize: DpSize? = null,
        initialPosition: Offset? = null
    ): String {
        val currentList = _windows.value

        // Check if single-instance app is already open
        if (appType.isSingleInstance) {
            val existing = currentList.find { it.appType == appType }
            if (existing != null) {
                focusWindow(existing.id)
                if (existing.state == WindowState.MINIMIZED) {
                    restoreWindow(existing.id)
                }
                return existing.id
            }
        }

        val windowId = "${appType.name.lowercase()}_${UUID.randomUUID().toString().take(8)}"
        zIndexCounter += 1.0f

        val cascadeIndex = currentList.size % 8
        val defaultX = (40f + cascadeIndex * 30f)
        val defaultY = (40f + cascadeIndex * 26f)

        val size = preferredSize ?: when (appType) {
            NexAppType.FILE_MANAGER -> DpSize(640.dp, 440.dp)
            NexAppType.TEXT_EDITOR -> DpSize(580.dp, 420.dp)
            NexAppType.SETTINGS -> DpSize(540.dp, 460.dp)
            NexAppType.APP_LIBRARY -> DpSize(600.dp, 420.dp)
            NexAppType.SYSTEM_INFO -> DpSize(480.dp, 440.dp)
            NexAppType.RUNTIME_MANAGER -> DpSize(620.dp, 460.dp)
            NexAppType.ABOUT_NEXOS -> DpSize(440.dp, 360.dp)
            NexAppType.WINDOWS_EXE_RUNNER -> DpSize(600.dp, 440.dp)
        }

        val newWindow = WindowInstance(
            id = windowId,
            title = customTitle ?: appType.defaultTitle,
            appType = appType,
            position = initialPosition ?: Offset(defaultX, defaultY),
            size = size,
            state = WindowState.NORMAL,
            zIndex = zIndexCounter,
            isFocused = true,
            params = params
        )

        // Unfocus other windows and append new window
        _windows.value = currentList.map { it.copy(isFocused = false) } + newWindow
        return windowId
    }

    fun focusWindow(windowId: String) {
        val currentList = _windows.value
        val target = currentList.find { it.id == windowId } ?: return

        zIndexCounter += 1.0f
        _windows.value = currentList.map { window ->
            if (window.id == windowId) {
                val newState = if (window.state == WindowState.MINIMIZED) WindowState.NORMAL else window.state
                window.copy(
                    zIndex = zIndexCounter,
                    isFocused = true,
                    state = newState
                )
            } else {
                window.copy(isFocused = false)
            }
        }
    }

    fun closeWindow(windowId: String) {
        val currentList = _windows.value
        val filtered = currentList.filterNot { it.id == windowId }

        // If the closed window was focused, focus the next topmost non-minimized window
        val nextFocusTarget = filtered
            .filter { it.state != WindowState.MINIMIZED }
            .maxByOrNull { it.zIndex }

        _windows.value = filtered.map { window ->
            if (nextFocusTarget != null && window.id == nextFocusTarget.id) {
                window.copy(isFocused = true)
            } else {
                window.copy(isFocused = false)
            }
        }
    }

    fun minimizeWindow(windowId: String) {
        val currentList = _windows.value
        val nextFocus = currentList
            .filter { it.id != windowId && it.state != WindowState.MINIMIZED }
            .maxByOrNull { it.zIndex }

        _windows.value = currentList.map { window ->
            when (window.id) {
                windowId -> window.copy(state = WindowState.MINIMIZED, isFocused = false)
                nextFocus?.id -> window.copy(isFocused = true)
                else -> window.copy(isFocused = false)
            }
        }
    }

    fun maximizeWindow(windowId: String) {
        zIndexCounter += 1.0f
        _windows.value = _windows.value.map { window ->
            if (window.id == windowId) {
                window.copy(
                    state = WindowState.MAXIMIZED,
                    isFocused = true,
                    zIndex = zIndexCounter
                )
            } else {
                window.copy(isFocused = false)
            }
        }
    }

    fun restoreWindow(windowId: String) {
        zIndexCounter += 1.0f
        _windows.value = _windows.value.map { window ->
            if (window.id == windowId) {
                window.copy(
                    state = WindowState.NORMAL,
                    isFocused = true,
                    zIndex = zIndexCounter
                )
            } else {
                window.copy(isFocused = false)
            }
        }
    }

    fun toggleMaximize(windowId: String) {
        val current = _windows.value.find { it.id == windowId } ?: return
        if (current.state == WindowState.MAXIMIZED) {
            restoreWindow(windowId)
        } else {
            maximizeWindow(windowId)
        }
    }

    fun moveWindow(windowId: String, delta: Offset, maxWidth: Float, maxHeight: Float) {
        _windows.value = _windows.value.map { window ->
            if (window.id == windowId && window.state == WindowState.NORMAL) {
                val newX = (window.position.x + delta.x).coerceIn(0f, (maxWidth - 120f).coerceAtLeast(0f))
                val newY = (window.position.y + delta.y).coerceIn(0f, (maxHeight - 60f).coerceAtLeast(0f))
                window.copy(position = Offset(newX, newY))
            } else {
                window
            }
        }
    }

    fun resizeWindow(windowId: String, newSize: DpSize) {
        _windows.value = _windows.value.map { window ->
            if (window.id == windowId && window.state == WindowState.NORMAL) {
                val clampedWidth = newSize.width.coerceAtLeast(window.minSize.width)
                val clampedHeight = newSize.height.coerceAtLeast(window.minSize.height)
                window.copy(size = DpSize(clampedWidth, clampedHeight))
            } else {
                window
            }
        }
    }

    fun updateTitle(windowId: String, newTitle: String) {
        _windows.value = _windows.value.map { window ->
            if (window.id == windowId) window.copy(title = newTitle) else window
        }
    }

    fun getFocusedWindow(): WindowInstance? = _windows.value.find { it.isFocused }
}
