package dev.nexos.apps

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Apps
import androidx.compose.material.icons.filled.Description
import androidx.compose.material.icons.filled.Folder
import androidx.compose.material.icons.filled.HelpOutline
import androidx.compose.material.icons.filled.Layers
import androidx.compose.material.icons.filled.Memory
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Settings
import androidx.compose.ui.graphics.vector.ImageVector

data class AppDescriptor(
    val type: NexAppType,
    val name: String,
    val icon: ImageVector,
    val description: String,
    val isPinnedToTaskbar: Boolean = false,
    val showOnDesktop: Boolean = true
)

object AppRegistry {

    val builtInApps: List<AppDescriptor> = listOf(
        AppDescriptor(
            type = NexAppType.FILE_MANAGER,
            name = "File Manager",
            icon = Icons.Default.Folder,
            description = "Browse user storage via SAF, manage virtual C: drive, and import .exe packages.",
            isPinnedToTaskbar = true,
            showOnDesktop = true
        ),
        AppDescriptor(
            type = NexAppType.TEXT_EDITOR,
            name = "Text Editor",
            icon = Icons.Default.Description,
            description = "Lightweight Windows-style Notepad editor for plain-text and configuration files.",
            isPinnedToTaskbar = true,
            showOnDesktop = true
        ),
        AppDescriptor(
            type = NexAppType.APP_LIBRARY,
            name = "App Library",
            icon = Icons.Default.Apps,
            description = "Installed applications catalog and Windows executable inspector.",
            isPinnedToTaskbar = true,
            showOnDesktop = true
        ),
        AppDescriptor(
            type = NexAppType.RUNTIME_MANAGER,
            name = "Runtime Manager",
            icon = Icons.Default.Layers,
            description = "Wine 9.x / Box64 compatibility status, diagnostic tools, and PE validator.",
            isPinnedToTaskbar = true,
            showOnDesktop = true
        ),
        AppDescriptor(
            type = NexAppType.SYSTEM_INFO,
            name = "System Information",
            icon = Icons.Default.Memory,
            description = "Hardware specifications, CPU architecture, memory, and Android platform diagnostics.",
            isPinnedToTaskbar = false,
            showOnDesktop = true
        ),
        AppDescriptor(
            type = NexAppType.SETTINGS,
            name = "Settings",
            icon = Icons.Default.Settings,
            description = "Personalize wallpaper, customize taskbar, and configure compatibility layer.",
            isPinnedToTaskbar = true,
            showOnDesktop = true
        ),
        AppDescriptor(
            type = NexAppType.ABOUT_NEXOS,
            name = "About NexOS",
            icon = Icons.Default.HelpOutline,
            description = "Version, design philosophy, open-source credits, and security notices.",
            isPinnedToTaskbar = false,
            showOnDesktop = true
        )
    )

    fun getDescriptor(type: NexAppType): AppDescriptor {
        return builtInApps.find { it.type == type } ?: AppDescriptor(
            type = type,
            name = type.defaultTitle,
            icon = Icons.Default.PlayArrow,
            description = "External application"
        )
    }
}
