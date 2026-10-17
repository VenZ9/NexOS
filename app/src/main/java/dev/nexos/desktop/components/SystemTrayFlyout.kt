package dev.nexos.desktop.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.BatteryFull
import androidx.compose.material.icons.filled.BrightnessMedium
import androidx.compose.material.icons.filled.Layers
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.VolumeUp
import androidx.compose.material.icons.filled.Wifi
import androidx.compose.material3.Divider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.zIndex
import dev.nexos.ui.theme.EmeraldAccent
import dev.nexos.ui.theme.FlyoutBg
import dev.nexos.ui.theme.FlyoutBorder
import dev.nexos.ui.theme.PurpleAccent
import dev.nexos.ui.theme.TextMutedDark
import dev.nexos.ui.theme.TextPrimaryDark
import dev.nexos.ui.theme.TextSecondaryDark
import dev.nexos.ui.theme.WindowBorder

@Composable
fun SystemTrayFlyout(
    isOpen: Boolean,
    isMockRuntime: Boolean,
    onOpenSettings: () -> Unit,
    onOpenRuntimeManager: () -> Unit,
    onDismiss: () -> Unit,
    modifier: Modifier = Modifier
) {
    if (!isOpen) return

    var volume by remember { mutableFloatStateOf(0.75f) }
    var brightness by remember { mutableFloatStateOf(0.85f) }

    Box(
        modifier = modifier
            .fillMaxSize()
            .clickable(onClick = onDismiss),
        contentAlignment = Alignment.BottomEnd
    ) {
        Column(
            modifier = Modifier
                .padding(bottom = 52.dp, end = 12.dp)
                .width(320.dp)
                .zIndex(999f)
                .shadow(24.dp, RoundedCornerShape(10.dp))
                .clip(RoundedCornerShape(10.dp))
                .background(FlyoutBg)
                .border(1.dp, FlyoutBorder, RoundedCornerShape(10.dp))
                .clickable(enabled = false) {}
                .padding(14.dp)
        ) {
            // Header / Quick Toggles
            Text(
                text = "Quick Settings",
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.SemiBold,
                color = TextPrimaryDark
            )

            Spacer(modifier = Modifier.height(10.dp))

            // Quick Status Tiles
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                // Wi-Fi Tile
                QuickTile(
                    title = "Connected",
                    subtitle = "Wi-Fi 6",
                    icon = Icons.Default.Wifi,
                    isActive = true,
                    modifier = Modifier.weight(1f)
                )

                // Battery Tile
                QuickTile(
                    title = "88%",
                    subtitle = "Battery OK",
                    icon = Icons.Default.BatteryFull,
                    isActive = true,
                    modifier = Modifier.weight(1f)
                )
            }

            Spacer(modifier = Modifier.height(14.dp))

            // Volume Slider
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Default.VolumeUp, contentDescription = null, tint = TextPrimaryDark, modifier = Modifier.size(18.dp))
                Spacer(modifier = Modifier.width(10.dp))
                Slider(
                    value = volume,
                    onValueChange = { volume = it },
                    modifier = Modifier.weight(1f),
                    colors = SliderDefaults.colors(thumbColor = MaterialTheme.colorScheme.primary, activeTrackColor = MaterialTheme.colorScheme.primary)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text("${(volume * 100).toInt()}%", style = MaterialTheme.typography.bodySmall, color = TextSecondaryDark, fontSize = 10.sp)
            }

            // Brightness Slider
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Default.BrightnessMedium, contentDescription = null, tint = TextPrimaryDark, modifier = Modifier.size(18.dp))
                Spacer(modifier = Modifier.width(10.dp))
                Slider(
                    value = brightness,
                    onValueChange = { brightness = it },
                    modifier = Modifier.weight(1f),
                    colors = SliderDefaults.colors(thumbColor = MaterialTheme.colorScheme.primary, activeTrackColor = MaterialTheme.colorScheme.primary)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text("${(brightness * 100).toInt()}%", style = MaterialTheme.typography.bodySmall, color = TextSecondaryDark, fontSize = 10.sp)
            }

            Spacer(modifier = Modifier.height(8.dp))
            Divider(color = WindowBorder.copy(alpha = 0.5f))
            Spacer(modifier = Modifier.height(10.dp))

            // Runtime Status & Shortcut
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(6.dp))
                    .background(Color(0xFF18181C))
                    .clickable {
                        onOpenRuntimeManager()
                        onDismiss()
                    }
                    .padding(8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(Icons.Default.Layers, contentDescription = null, tint = if (isMockRuntime) PurpleAccent else EmeraldAccent, modifier = Modifier.size(18.dp))
                Spacer(modifier = Modifier.width(8.dp))
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = if (isMockRuntime) "Mock Simulator Active" else "Real Wine 9.x Layer",
                        style = MaterialTheme.typography.bodySmall,
                        fontWeight = FontWeight.Medium,
                        color = TextPrimaryDark
                    )
                    Text(
                        text = "Click to open Runtime Manager",
                        style = MaterialTheme.typography.labelSmall,
                        color = TextMutedDark,
                        fontSize = 9.sp
                    )
                }
            }
        }
    }
}

@Composable
private fun QuickTile(
    title: String,
    subtitle: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    isActive: Boolean,
    modifier: Modifier = Modifier
) {
    val bg = if (isActive) MaterialTheme.colorScheme.primary.copy(alpha = 0.25f) else Color(0xFF1E1E24)

    Row(
        modifier = modifier
            .clip(RoundedCornerShape(6.dp))
            .background(bg)
            .border(1.dp, if (isActive) MaterialTheme.colorScheme.primary.copy(alpha = 0.5f) else WindowBorder, RoundedCornerShape(6.dp))
            .padding(8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(icon, contentDescription = null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(18.dp))
        Spacer(modifier = Modifier.width(8.dp))
        Column {
            Text(title, style = MaterialTheme.typography.bodySmall, fontWeight = FontWeight.Medium, color = TextPrimaryDark, fontSize = 11.sp)
            Text(subtitle, style = MaterialTheme.typography.labelSmall, color = TextMutedDark, fontSize = 9.sp)
        }
    }
}
