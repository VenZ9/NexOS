package dev.nexos.desktop

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Description
import androidx.compose.material.icons.filled.FileUpload
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Layers
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Wallpaper
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import dev.nexos.apps.NexAppType
import dev.nexos.apps.about.AboutApp
import dev.nexos.apps.applibrary.AppLibraryApp
import dev.nexos.apps.applibrary.AppLibraryViewModel
import dev.nexos.apps.filemanager.FileManagerApp
import dev.nexos.apps.filemanager.FileManagerViewModel
import dev.nexos.apps.runtimemanager.RuntimeManagerApp
import dev.nexos.apps.runtimemanager.RuntimeManagerViewModel
import dev.nexos.apps.settings.SettingsApp
import dev.nexos.apps.settings.SettingsViewModel
import dev.nexos.apps.systeminfo.SystemInfoApp
import dev.nexos.apps.systeminfo.SystemInfoViewModel
import dev.nexos.apps.texteditor.TextEditorApp
import dev.nexos.apps.texteditor.TextEditorViewModel
import dev.nexos.apps.windowsrunner.WindowsExeRunnerApp
import dev.nexos.desktop.components.ContextMenu
import dev.nexos.desktop.components.ContextMenuItem
import dev.nexos.desktop.components.DesktopWorkspace
import dev.nexos.desktop.components.StartMenu
import dev.nexos.desktop.components.SystemTrayFlyout
import dev.nexos.desktop.components.Taskbar
import dev.nexos.desktop.components.WallpaperView

@Composable
fun DesktopScreen(
    viewModel: DesktopViewModel,
    modifier: Modifier = Modifier
) {
    val prefs by viewModel.preferences.collectAsState()
    val windows by viewModel.windows.collectAsState()
    val isStartMenuOpen by viewModel.isStartMenuOpen.collectAsState()
    val isFlyoutOpen by viewModel.isSystemTrayFlyoutOpen.collectAsState()
    val contextMenuState by viewModel.contextMenuState.collectAsState()
    val activeRuntime by viewModel.runtimeProvider.activeRuntime.collectAsState()

    Box(
        modifier = modifier
            .fillMaxSize()
            .windowInsetsPadding(WindowInsets.safeDrawing)
    ) {
        // Desktop Wallpaper
        WallpaperView(
            theme = prefs.wallpaperTheme,
            customUriString = prefs.customWallpaperUri
        )

        // Workspace and Windows
        Column(modifier = Modifier.fillMaxSize()) {
            DesktopWorkspace(
                windows = windows,
                showShortcuts = prefs.showDesktopIcons,
                onAppOpen = { appType -> viewModel.openApp(appType) },
                onWindowFocus = { id -> viewModel.focusWindow(id) },
                onWindowDrag = { id, delta, maxW, maxH -> viewModel.moveWindow(id, delta, maxW, maxH) },
                onWindowResize = { id, size -> viewModel.resizeWindow(id, size) },
                onWindowMinimize = { id -> viewModel.minimizeWindow(id) },
                onWindowMaximizeToggle = { id -> viewModel.maximizeToggleWindow(id) },
                onWindowClose = { id -> viewModel.closeWindow(id) },
                onDesktopContextMenu = { pos -> viewModel.openContextMenu(pos) },
                onDismissPopups = { viewModel.dismissAllPopups() },
                windowContentProvider = { window ->
                    when (window.appType) {
                        NexAppType.FILE_MANAGER -> {
                            val fmVm = remember(window.id) { FileManagerViewModel(viewModel.fileRepository) }
                            FileManagerApp(
                                viewModel = fmVm,
                                onOpenFile = { file -> viewModel.openFile(file) },
                                onLaunchExe = { file -> viewModel.openFile(file) }
                            )
                        }
                        NexAppType.TEXT_EDITOR -> {
                            val editorVm = remember(window.id) { TextEditorViewModel(viewModel.fileRepository) }
                            TextEditorApp(
                                viewModel = editorVm,
                                initialFilePath = window.params["filePath"],
                                onTitleChanged = { newTitle -> viewModel.updateWindowTitle(window.id, newTitle) }
                            )
                        }
                        NexAppType.SETTINGS -> {
                            val settingsVm = remember(window.id) {
                                SettingsViewModel(viewModel.preferencesRepository, viewModel.runtimeProvider)
                            }
                            SettingsApp(viewModel = settingsVm)
                        }
                        NexAppType.APP_LIBRARY -> {
                            val appLibVm = remember(window.id) { AppLibraryViewModel(viewModel.runtimeProvider) }
                            AppLibraryApp(
                                viewModel = appLibVm,
                                onOpenBuiltIn = { appType -> viewModel.openApp(appType) }
                            )
                        }
                        NexAppType.SYSTEM_INFO -> {
                            val sysInfoVm = remember(window.id) {
                                SystemInfoViewModel(viewModel.preferencesRepository.run {
                                    // context access
                                    dev.nexos.NexApplication.instance
                                })
                            }
                            SystemInfoApp(viewModel = sysInfoVm)
                        }
                        NexAppType.RUNTIME_MANAGER -> {
                            val runtimeVm = remember(window.id) { RuntimeManagerViewModel(viewModel.runtimeProvider) }
                            RuntimeManagerApp(viewModel = runtimeVm)
                        }
                        NexAppType.ABOUT_NEXOS -> {
                            AboutApp(onClose = { viewModel.closeWindow(window.id) })
                        }
                        NexAppType.WINDOWS_EXE_RUNNER -> {
                            val filePath = window.params["filePath"] ?: ""
                            WindowsExeRunnerApp(
                                filePath = filePath,
                                runtimeProvider = viewModel.runtimeProvider,
                                onClose = { viewModel.closeWindow(window.id) }
                            )
                        }
                    }
                },
                modifier = Modifier.weight(1f)
            )

            // Bottom Taskbar
            Taskbar(
                windows = windows,
                alignment = prefs.taskbarAlignment,
                isStartMenuOpen = isStartMenuOpen,
                isFlyoutOpen = isFlyoutOpen,
                isMockRuntime = activeRuntime.isMock,
                onStartButtonClick = { viewModel.toggleStartMenu() },
                onTrayClick = { viewModel.toggleSystemTrayFlyout() },
                onAppClick = { type -> viewModel.openApp(type) },
                onWindowClick = { win -> viewModel.handleTaskbarWindowClick(win) }
            )
        }

        // Floating Start Menu
        StartMenu(
            isOpen = isStartMenuOpen,
            alignment = prefs.taskbarAlignment,
            onAppClick = { type -> viewModel.openApp(type) },
            onDismiss = { viewModel.dismissAllPopups() },
            onOpenSettings = { viewModel.openApp(NexAppType.SETTINGS) }
        )

        // Floating Quick Settings / System Tray Flyout
        SystemTrayFlyout(
            isOpen = isFlyoutOpen,
            isMockRuntime = activeRuntime.isMock,
            onOpenSettings = { viewModel.openApp(NexAppType.SETTINGS) },
            onOpenRuntimeManager = { viewModel.openApp(NexAppType.RUNTIME_MANAGER) },
            onDismiss = { viewModel.dismissAllPopups() }
        )

        // Desktop Context Menu
        if (contextMenuState.isVisible) {
            val contextMenuItems = listOf(
                ContextMenuItem(
                    title = "New Text Document",
                    icon = Icons.Default.Description,
                    onClick = { viewModel.openApp(NexAppType.TEXT_EDITOR) }
                ),
                ContextMenuItem(
                    title = "Import .exe / .msi",
                    icon = Icons.Default.FileUpload,
                    onClick = { viewModel.openApp(NexAppType.FILE_MANAGER) }
                ),
                ContextMenuItem(isDivider = true),
                ContextMenuItem(
                    title = "Change Wallpaper",
                    icon = Icons.Default.Wallpaper,
                    onClick = { viewModel.openApp(NexAppType.SETTINGS) }
                ),
                ContextMenuItem(
                    title = "Runtime Manager",
                    icon = Icons.Default.Layers,
                    onClick = { viewModel.openApp(NexAppType.RUNTIME_MANAGER) }
                ),
                ContextMenuItem(
                    title = "System Information",
                    icon = Icons.Default.Info,
                    onClick = { viewModel.openApp(NexAppType.SYSTEM_INFO) }
                )
            )

            ContextMenu(
                position = contextMenuState.position,
                items = contextMenuItems,
                onDismiss = { viewModel.dismissAllPopups() }
            )
        }
    }
}
