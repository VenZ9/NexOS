package dev.nexos.desktop

import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.unit.DpSize
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dev.nexos.apps.NexAppType
import dev.nexos.data.preferences.UserPreferences
import dev.nexos.data.preferences.UserPreferencesRepository
import dev.nexos.data.repository.FileRepository
import dev.nexos.domain.model.NexFile
import dev.nexos.domain.model.WallpaperTheme
import dev.nexos.runtime.RuntimeProvider
import dev.nexos.windows.WindowInstance
import dev.nexos.windows.WindowManager
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.io.File

data class ContextMenuState(
    val isVisible: Boolean = false,
    val position: Offset = Offset.Zero
)

class DesktopViewModel(
    val preferencesRepository: UserPreferencesRepository,
    val fileRepository: FileRepository,
    val runtimeProvider: RuntimeProvider,
    val windowManager: WindowManager = WindowManager()
) : ViewModel() {

    val preferences: StateFlow<UserPreferences> = preferencesRepository.userPreferencesFlow
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = UserPreferences()
        )

    val windows: StateFlow<List<WindowInstance>> = windowManager.windows

    private val _isStartMenuOpen = MutableStateFlow(false)
    val isStartMenuOpen: StateFlow<Boolean> = _isStartMenuOpen.asStateFlow()

    private val _isSystemTrayFlyoutOpen = MutableStateFlow(false)
    val isSystemTrayFlyoutOpen: StateFlow<Boolean> = _isSystemTrayFlyoutOpen.asStateFlow()

    private val _contextMenuState = MutableStateFlow(ContextMenuState())
    val contextMenuState: StateFlow<ContextMenuState> = _contextMenuState.asStateFlow()

    fun toggleStartMenu() {
        _isStartMenuOpen.value = !_isStartMenuOpen.value
        if (_isStartMenuOpen.value) {
            _isSystemTrayFlyoutOpen.value = false
            _contextMenuState.value = ContextMenuState(isVisible = false)
        }
    }

    fun toggleSystemTrayFlyout() {
        _isSystemTrayFlyoutOpen.value = !_isSystemTrayFlyoutOpen.value
        if (_isSystemTrayFlyoutOpen.value) {
            _isStartMenuOpen.value = false
            _contextMenuState.value = ContextMenuState(isVisible = false)
        }
    }

    fun dismissAllPopups() {
        _isStartMenuOpen.value = false
        _isSystemTrayFlyoutOpen.value = false
        _contextMenuState.value = ContextMenuState(isVisible = false)
    }

    fun openContextMenu(position: Offset) {
        dismissAllPopups()
        _contextMenuState.value = ContextMenuState(isVisible = true, position = position)
    }

    fun openApp(appType: NexAppType, customTitle: String? = null, params: Map<String, String> = emptyMap()) {
        dismissAllPopups()
        windowManager.openWindow(appType = appType, customTitle = customTitle, params = params)
    }

    fun openFile(file: NexFile) {
        dismissAllPopups()
        if (file.isWindowsExe) {
            openApp(
                appType = NexAppType.WINDOWS_EXE_RUNNER,
                customTitle = "Windows Host: ${file.name}",
                params = mapOf("filePath" to file.path)
            )
        } else if (file.isTextDocument) {
            openApp(
                appType = NexAppType.TEXT_EDITOR,
                customTitle = file.name,
                params = mapOf("filePath" to file.path)
            )
        } else {
            // Default to File Manager or Text Editor
            openApp(
                appType = NexAppType.TEXT_EDITOR,
                customTitle = file.name,
                params = mapOf("filePath" to file.path)
            )
        }
    }

    fun handleTaskbarWindowClick(window: WindowInstance) {
        if (window.isFocused && window.state != dev.nexos.windows.WindowState.MINIMIZED) {
            windowManager.minimizeWindow(window.id)
        } else {
            windowManager.focusWindow(window.id)
        }
    }

    fun focusWindow(windowId: String) = windowManager.focusWindow(windowId)
    fun minimizeWindow(windowId: String) = windowManager.minimizeWindow(windowId)
    fun maximizeToggleWindow(windowId: String) = windowManager.toggleMaximize(windowId)
    fun closeWindow(windowId: String) = windowManager.closeWindow(windowId)
    fun moveWindow(windowId: String, delta: Offset, maxWidth: Float, maxHeight: Float) =
        windowManager.moveWindow(windowId, delta, maxWidth, maxHeight)
    fun resizeWindow(windowId: String, newSize: DpSize) =
        windowManager.resizeWindow(windowId, newSize)
    fun updateWindowTitle(windowId: String, newTitle: String) =
        windowManager.updateTitle(windowId, newTitle)
}
