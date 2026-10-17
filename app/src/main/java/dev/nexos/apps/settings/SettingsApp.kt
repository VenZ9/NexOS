package dev.nexos.apps.settings

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.ColorLens
import androidx.compose.material.icons.filled.DesktopWindows
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Layers
import androidx.compose.material.icons.filled.Wallpaper
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import dev.nexos.domain.model.TaskbarAlignment
import dev.nexos.domain.model.WallpaperTheme
import dev.nexos.ui.theme.AmberAccent
import dev.nexos.ui.theme.EmeraldAccent
import dev.nexos.ui.theme.PurpleAccent
import dev.nexos.ui.theme.StatusWarning
import dev.nexos.ui.theme.TextMutedDark
import dev.nexos.ui.theme.TextPrimaryDark
import dev.nexos.ui.theme.TextSecondaryDark
import dev.nexos.ui.theme.WindowBorder
import dev.nexos.ui.theme.WindowDarkBg
import dev.nexos.ui.theme.WindowDarkTitleBar
import dev.nexos.ui.theme.WindowsBlue

@Composable
fun SettingsApp(
    viewModel: SettingsViewModel,
    modifier: Modifier = Modifier
) {
    val prefs by viewModel.preferences.collectAsState()
    var selectedCategory by remember { mutableStateOf("Personalization") }

    val imagePickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.OpenDocument()
    ) { uri: Uri? ->
        uri?.let { viewModel.setCustomWallpaperUri(it.toString()) }
    }

    Row(
        modifier = modifier
            .fillMaxSize()
            .background(WindowDarkBg)
    ) {
        // Left Categories Sidebar
        Column(
            modifier = Modifier
                .width(170.dp)
                .fillMaxHeight()
                .background(WindowDarkTitleBar)
                .border(1.dp, WindowBorder.copy(alpha = 0.5f))
                .padding(vertical = 12.dp)
        ) {
            Text(
                text = "Settings",
                style = MaterialTheme.typography.titleMedium,
                color = TextPrimaryDark,
                modifier = Modifier.padding(horizontal = 14.dp, vertical = 6.dp)
            )

            Spacer(modifier = Modifier.height(8.dp))

            SettingsCategoryItem(
                title = "Personalization",
                icon = Icons.Default.ColorLens,
                isSelected = selectedCategory == "Personalization",
                onClick = { selectedCategory = "Personalization" }
            )

            SettingsCategoryItem(
                title = "Taskbar & Desktop",
                icon = Icons.Default.DesktopWindows,
                isSelected = selectedCategory == "Taskbar & Desktop",
                onClick = { selectedCategory = "Taskbar & Desktop" }
            )

            SettingsCategoryItem(
                title = "Compatibility Runtime",
                icon = Icons.Default.Layers,
                isSelected = selectedCategory == "Compatibility Runtime",
                onClick = { selectedCategory = "Compatibility Runtime" }
            )

            SettingsCategoryItem(
                title = "About NexOS",
                icon = Icons.Default.Info,
                isSelected = selectedCategory == "About NexOS",
                onClick = { selectedCategory = "About NexOS" }
            )
        }

        // Right Detail View
        Column(
            modifier = Modifier
                .weight(1f)
                .fillMaxHeight()
                .padding(16.dp)
                .verticalScroll(rememberScrollState())
        ) {
            when (selectedCategory) {
                "Personalization" -> {
                    Text("Personalization", style = MaterialTheme.typography.headlineMedium, color = TextPrimaryDark)
                    Text("Customize desktop wallpapers, theme colors, and visual styles", style = MaterialTheme.typography.bodyMedium, color = TextMutedDark)

                    Spacer(modifier = Modifier.height(16.dp))

                    // Wallpapers Section
                    Text("Desktop Wallpaper", style = MaterialTheme.typography.titleMedium, color = TextPrimaryDark)
                    Spacer(modifier = Modifier.height(8.dp))

                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        WallpaperTheme.values().forEach { theme ->
                            WallpaperOptionCard(
                                theme = theme,
                                isSelected = prefs.wallpaperTheme == theme,
                                onClick = {
                                    if (theme == WallpaperTheme.CUSTOM_IMAGE) {
                                        imagePickerLauncher.launch(arrayOf("image/*"))
                                    } else {
                                        viewModel.setWallpaperTheme(theme)
                                    }
                                }
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(20.dp))

                    // Accent Colors Section
                    Text("Accent Color", style = MaterialTheme.typography.titleMedium, color = TextPrimaryDark)
                    Spacer(modifier = Modifier.height(8.dp))

                    Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                        AccentColorSwatch(WindowsBlue, prefs.accentColorHex == 0xFF0078D4L) { viewModel.setAccentColor(0xFF0078D4L) }
                        AccentColorSwatch(PurpleAccent, prefs.accentColorHex == 0xFF8B5CF6L) { viewModel.setAccentColor(0xFF8B5CF6L) }
                        AccentColorSwatch(EmeraldAccent, prefs.accentColorHex == 0xFF10B981L) { viewModel.setAccentColor(0xFF10B981L) }
                        AccentColorSwatch(AmberAccent, prefs.accentColorHex == 0xFFF59E0BL) { viewModel.setAccentColor(0xFFF59E0BL) }
                    }
                }

                "Taskbar & Desktop" -> {
                    Text("Taskbar & Desktop", style = MaterialTheme.typography.headlineMedium, color = TextPrimaryDark)
                    Text("Configure layout, alignments, and shortcuts", style = MaterialTheme.typography.bodyMedium, color = TextMutedDark)

                    Spacer(modifier = Modifier.height(16.dp))

                    // Alignment
                    Card(
                        colors = CardDefaults.cardColors(containerColor = WindowDarkTitleBar),
                        shape = RoundedCornerShape(8.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(modifier = Modifier.padding(14.dp)) {
                            Text("Taskbar Alignment", style = MaterialTheme.typography.titleMedium, color = TextPrimaryDark)
                            Spacer(modifier = Modifier.height(8.dp))
                            Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                                Button(
                                    onClick = { viewModel.setTaskbarAlignment(TaskbarAlignment.CENTER) },
                                    shape = RoundedCornerShape(6.dp),
                                    colors = androidx.compose.material3.ButtonDefaults.buttonColors(
                                        containerColor = if (prefs.taskbarAlignment == TaskbarAlignment.CENTER) MaterialTheme.colorScheme.primary else WindowDarkBg
                                    )
                                ) {
                                    Text("Center (Modern)")
                                }

                                Button(
                                    onClick = { viewModel.setTaskbarAlignment(TaskbarAlignment.LEFT) },
                                    shape = RoundedCornerShape(6.dp),
                                    colors = androidx.compose.material3.ButtonDefaults.buttonColors(
                                        containerColor = if (prefs.taskbarAlignment == TaskbarAlignment.LEFT) MaterialTheme.colorScheme.primary else WindowDarkBg
                                    )
                                ) {
                                    Text("Left (Classic)")
                                }
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    // Show Desktop Icons
                    Card(
                        colors = CardDefaults.cardColors(containerColor = WindowDarkTitleBar),
                        shape = RoundedCornerShape(8.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(
                            modifier = Modifier.padding(14.dp).fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text("Show Desktop Shortcuts", style = MaterialTheme.typography.titleMedium, color = TextPrimaryDark)
                                Text("Display application icons on the desktop grid", style = MaterialTheme.typography.bodySmall, color = TextMutedDark)
                            }
                            Switch(
                                checked = prefs.showDesktopIcons,
                                onCheckedChange = { viewModel.setShowDesktopIcons(it) }
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    // Auto Hide Taskbar
                    Card(
                        colors = CardDefaults.cardColors(containerColor = WindowDarkTitleBar),
                        shape = RoundedCornerShape(8.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(
                            modifier = Modifier.padding(14.dp).fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text("Automatically Hide Taskbar", style = MaterialTheme.typography.titleMedium, color = TextPrimaryDark)
                                Text("Reveal taskbar when hovering near the screen bottom", style = MaterialTheme.typography.bodySmall, color = TextMutedDark)
                            }
                            Switch(
                                checked = prefs.autoHideTaskbar,
                                onCheckedChange = { viewModel.setAutoHideTaskbar(it) }
                            )
                        }
                    }
                }

                "Compatibility Runtime" -> {
                    Text("Windows Runtime Configuration", style = MaterialTheme.typography.headlineMedium, color = TextPrimaryDark)
                    Text("Select execution mode and configure security verification", style = MaterialTheme.typography.bodyMedium, color = TextMutedDark)

                    Spacer(modifier = Modifier.height(16.dp))

                    // Active Runtime Engine Selector
                    Card(
                        colors = CardDefaults.cardColors(containerColor = WindowDarkTitleBar),
                        shape = RoundedCornerShape(8.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(modifier = Modifier.padding(14.dp)) {
                            Text("Active Execution Engine", style = MaterialTheme.typography.titleMedium, color = TextPrimaryDark)
                            Spacer(modifier = Modifier.height(8.dp))

                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clip(RoundedCornerShape(6.dp))
                                    .background(if (prefs.activeRuntimeType == "REAL") MaterialTheme.colorScheme.primary.copy(alpha = 0.2f) else WindowDarkBg)
                                    .clickable { viewModel.setActiveRuntimeType("REAL") }
                                    .padding(10.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column(modifier = Modifier.weight(1f)) {
                                    Text("Wine 9.x + Box64 Container (Production Target)", style = MaterialTheme.typography.bodyMedium, color = TextPrimaryDark, fontWeight = androidx.compose.ui.text.font.FontWeight.Medium)
                                    Text("Genuine Wine and Box64 dynamic binary translation. Probes native binaries and reports honest system status.", style = MaterialTheme.typography.bodySmall, color = TextSecondaryDark)
                                }
                                if (prefs.activeRuntimeType == "REAL") {
                                    Icon(Icons.Default.Check, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                                }
                            }

                            Spacer(modifier = Modifier.height(8.dp))

                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clip(RoundedCornerShape(6.dp))
                                    .background(if (prefs.activeRuntimeType == "MOCK") MaterialTheme.colorScheme.primary.copy(alpha = 0.2f) else WindowDarkBg)
                                    .clickable { viewModel.setActiveRuntimeType("MOCK") }
                                    .padding(10.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column(modifier = Modifier.weight(1f)) {
                                    Text("NexOS Simulator [Mock - Dev Mode]", style = MaterialTheme.typography.bodyMedium, color = TextPrimaryDark, fontWeight = androidx.compose.ui.text.font.FontWeight.Medium)
                                    Text("Simulates installation and process lifecycle for UI testing without downloading large native rootfs containers.", style = MaterialTheme.typography.bodySmall, color = TextSecondaryDark)
                                }
                                if (prefs.activeRuntimeType == "MOCK") {
                                    Icon(Icons.Default.Check, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                                }
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    // Strict PE Validation
                    Card(
                        colors = CardDefaults.cardColors(containerColor = WindowDarkTitleBar),
                        shape = RoundedCornerShape(8.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(
                            modifier = Modifier.padding(14.dp).fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text("Strict PE Header & Hash Verification", style = MaterialTheme.typography.titleMedium, color = TextPrimaryDark)
                                Text("Inspect DOS signatures, COFF machine architectures, and calculate SHA-256 before any execution", style = MaterialTheme.typography.bodySmall, color = TextMutedDark)
                            }
                            Switch(
                                checked = prefs.strictPeValidation,
                                onCheckedChange = { viewModel.setStrictPeValidation(it) }
                            )
                        }
                    }
                }

                "About NexOS" -> {
                    Text("About NexOS", style = MaterialTheme.typography.headlineMedium, color = TextPrimaryDark)
                    Text("Version 1.0.0 (Release 2026.10)", style = MaterialTheme.typography.bodyMedium, color = TextMutedDark)

                    Spacer(modifier = Modifier.height(16.dp))

                    Card(
                        colors = CardDefaults.cardColors(containerColor = WindowDarkTitleBar),
                        shape = RoundedCornerShape(8.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(modifier = Modifier.padding(14.dp)) {
                            Text("Philosophy & Architecture", style = MaterialTheme.typography.titleMedium, color = TextPrimaryDark)
                            Spacer(modifier = Modifier.height(6.dp))
                            Text(
                                "NexOS is built natively in 100% Kotlin with Jetpack Compose. It includes zero backend dependencies, no tracking, no subscriptions, and strictly uses scoped Android Storage Access Framework APIs.",
                                style = MaterialTheme.typography.bodySmall,
                                color = TextSecondaryDark
                            )

                            Spacer(modifier = Modifier.height(12.dp))

                            Text("Windows Compatibility Layer", style = MaterialTheme.typography.titleMedium, color = TextPrimaryDark)
                            Spacer(modifier = Modifier.height(6.dp))
                            Text(
                                "Compatible with x86/x64 Windows executables via Wine and Box64 dynamic binary translation. Container environments run isolated in private application storage.",
                                style = MaterialTheme.typography.bodySmall,
                                color = TextSecondaryDark
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun SettingsCategoryItem(
    title: String,
    icon: ImageVector,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    val bg = if (isSelected) MaterialTheme.colorScheme.primary.copy(alpha = 0.25f) else Color.Transparent

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(4.dp))
            .background(bg)
            .clickable(onClick = onClick)
            .padding(horizontal = 12.dp, vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(icon, contentDescription = null, tint = if (isSelected) MaterialTheme.colorScheme.primary else TextSecondaryDark, modifier = Modifier.size(17.dp))
        Spacer(modifier = Modifier.width(10.dp))
        Text(title, style = MaterialTheme.typography.bodySmall, color = TextPrimaryDark, maxLines = 1)
    }
}

@Composable
private fun WallpaperOptionCard(
    theme: WallpaperTheme,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    val border = if (isSelected) WindowBorder.copy(alpha = 0.8f) else Color.Transparent
    val bg = if (isSelected) MaterialTheme.colorScheme.primary.copy(alpha = 0.15f) else WindowDarkTitleBar

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(6.dp))
            .background(bg)
            .border(if (isSelected) 1.5.dp else 1.dp, if (isSelected) MaterialTheme.colorScheme.primary else WindowBorder, RoundedCornerShape(6.dp))
            .clickable(onClick = onClick)
            .padding(10.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(Icons.Default.Wallpaper, contentDescription = null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(20.dp))
        Spacer(modifier = Modifier.width(10.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text(theme.displayName, style = MaterialTheme.typography.bodyMedium, color = TextPrimaryDark, fontWeight = androidx.compose.ui.text.font.FontWeight.Medium)
            Text(theme.description, style = MaterialTheme.typography.bodySmall, color = TextMutedDark)
        }
        if (isSelected) {
            Icon(Icons.Default.Check, contentDescription = null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(18.dp))
        }
    }
}

@Composable
private fun AccentColorSwatch(
    color: Color,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    Box(
        modifier = Modifier
            .size(36.dp)
            .clip(CircleShape)
            .background(color)
            .clickable(onClick = onClick)
            .border(if (isSelected) 3.dp else 1.dp, if (isSelected) Color.White else WindowBorder, CircleShape),
        contentAlignment = Alignment.Center
    ) {
        if (isSelected) {
            Icon(Icons.Default.Check, contentDescription = null, tint = Color.White, modifier = Modifier.size(16.dp))
        }
    }
}
