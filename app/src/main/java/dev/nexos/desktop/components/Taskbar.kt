package dev.nexos.desktop.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.BatteryFull
import androidx.compose.material.icons.filled.GridView
import androidx.compose.material.icons.filled.VolumeUp
import androidx.compose.material.icons.filled.Wifi
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.zIndex
import dev.nexos.apps.AppDescriptor
import dev.nexos.apps.AppRegistry
import dev.nexos.apps.NexAppType
import dev.nexos.domain.model.TaskbarAlignment
import dev.nexos.ui.theme.EmeraldAccent
import dev.nexos.ui.theme.PurpleAccent
import dev.nexos.ui.theme.TaskbarBorder
import dev.nexos.ui.theme.TaskbarDarkBg
import dev.nexos.ui.theme.TaskbarItemActive
import dev.nexos.ui.theme.TextMutedDark
import dev.nexos.ui.theme.TextPrimaryDark
import dev.nexos.ui.theme.TextSecondaryDark
import dev.nexos.windows.WindowInstance
import dev.nexos.windows.WindowState
import kotlinx.coroutines.delay
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun Taskbar(
    windows: List<WindowInstance>,
    alignment: TaskbarAlignment,
    isStartMenuOpen: Boolean,
    isFlyoutOpen: Boolean,
    isMockRuntime: Boolean,
    onStartButtonClick: () -> Unit,
    onTrayClick: () -> Unit,
    onAppClick: (NexAppType) -> Unit,
    onWindowClick: (WindowInstance) -> Unit,
    modifier: Modifier = Modifier
) {
    var currentTime by remember { mutableStateOf("") }
    var currentDate by remember { mutableStateOf("") }

    LaunchedEffect(Unit) {
        val timeFormat = SimpleDateFormat("h:mm a", Locale.getDefault())
        val dateFormat = SimpleDateFormat("M/d/yyyy", Locale.getDefault())
        while (true) {
            val now = Date()
            currentTime = timeFormat.format(now)
            currentDate = dateFormat.format(now)
            delay(1000)
        }
    }

    val pinnedApps = remember { AppRegistry.builtInApps.filter { it.isPinnedToTaskbar } }

    Row(
        modifier = modifier
            .fillMaxWidth()
            .height(44.dp)
            .background(TaskbarDarkBg)
            .border(1.dp, TaskbarBorder)
            .padding(horizontal = 4.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Left side (when Left alignment): Start button is here
        if (alignment == TaskbarAlignment.LEFT) {
            StartButton(isOpen = isStartMenuOpen, onClick = onStartButtonClick)
            Spacer(modifier = Modifier.width(4.dp))
        }

        // Center / App Icons Container
        val appsRowArrangement = if (alignment == TaskbarAlignment.CENTER) Arrangement.Center else Arrangement.Start
        Box(
            modifier = Modifier
                .weight(1f)
                .fillMaxHeight(),
            contentAlignment = if (alignment == TaskbarAlignment.CENTER) Alignment.Center else Alignment.CenterStart
        ) {
            Row(
                modifier = Modifier
                    .horizontalScroll(rememberScrollState()),
                horizontalArrangement = appsRowArrangement,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // When Center alignment: Start button is the first icon in the centered cluster
                if (alignment == TaskbarAlignment.CENTER) {
                    StartButton(isOpen = isStartMenuOpen, onClick = onStartButtonClick)
                    Spacer(modifier = Modifier.width(4.dp))
                }

                // Pinned Applications
                pinnedApps.forEach { app ->
                    val openInstances = windows.filter { it.appType == app.type }
                    val isRunning = openInstances.isNotEmpty()
                    val isFocused = openInstances.any { it.isFocused && it.state != WindowState.MINIMIZED }

                    TaskbarAppItem(
                        app = app,
                        isRunning = isRunning,
                        isFocused = isFocused,
                        onClick = {
                            if (isRunning) {
                                val top = openInstances.maxByOrNull { it.zIndex } ?: openInstances.first()
                                onWindowClick(top)
                            } else {
                                onAppClick(app.type)
                            }
                        }
                    )
                }

                // Windows that are open but not pinned
                val unpinnedWindows = windows.filter { win -> pinnedApps.none { it.type == win.appType } }
                unpinnedWindows.distinctBy { it.appType }.forEach { win ->
                    val appDesc = AppRegistry.getDescriptor(win.appType)
                    val instances = windows.filter { it.appType == win.appType }
                    val isFocused = instances.any { it.isFocused && it.state != WindowState.MINIMIZED }

                    TaskbarAppItem(
                        app = appDesc,
                        isRunning = true,
                        isFocused = isFocused,
                        onClick = {
                            val top = instances.maxByOrNull { it.zIndex } ?: win
                            onWindowClick(top)
                        }
                    )
                }
            }
        }

        // System Tray (Right Side)
        Row(
            modifier = Modifier
                .clip(RoundedCornerShape(6.dp))
                .clickable(onClick = onTrayClick)
                .padding(horizontal = 6.dp, vertical = 3.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Runtime Mode Badge
            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(3.dp))
                    .background(if (isMockRuntime) PurpleAccent.copy(alpha = 0.2f) else EmeraldAccent.copy(alpha = 0.2f))
                    .padding(horizontal = 5.dp, vertical = 2.dp)
            ) {
                Text(
                    text = if (isMockRuntime) "MOCK" else "REAL",
                    color = if (isMockRuntime) PurpleAccent else EmeraldAccent,
                    fontSize = 9.sp,
                    fontWeight = FontWeight.Bold
                )
            }

            Spacer(modifier = Modifier.width(8.dp))

            Icon(Icons.Default.Wifi, contentDescription = "Wi-Fi", tint = TextPrimaryDark, modifier = Modifier.size(15.dp))
            Spacer(modifier = Modifier.width(6.dp))

            Icon(Icons.Default.VolumeUp, contentDescription = "Volume", tint = TextPrimaryDark, modifier = Modifier.size(15.dp))
            Spacer(modifier = Modifier.width(6.dp))

            Icon(Icons.Default.BatteryFull, contentDescription = "Battery", tint = TextPrimaryDark, modifier = Modifier.size(15.dp))

            Spacer(modifier = Modifier.width(10.dp))

            // Clock & Date
            Column(horizontalAlignment = Alignment.End) {
                Text(
                    text = currentTime,
                    style = MaterialTheme.typography.bodySmall.copy(fontSize = 11.sp, fontWeight = FontWeight.Medium),
                    color = TextPrimaryDark
                )
                Text(
                    text = currentDate,
                    style = MaterialTheme.typography.bodySmall.copy(fontSize = 9.sp),
                    color = TextMutedDark
                )
            }
        }
    }
}

@Composable
private fun StartButton(
    isOpen: Boolean,
    onClick: () -> Unit
) {
    val bg = if (isOpen) TaskbarItemActive else Color.Transparent

    Box(
        modifier = Modifier
            .size(36.dp)
            .clip(RoundedCornerShape(6.dp))
            .background(bg)
            .clickable(onClick = onClick),
        contentAlignment = Alignment.Center
    ) {
        // Fluent 4-Pane Windows Logo
        Column(
            modifier = Modifier.size(18.dp),
            verticalArrangement = Arrangement.spacedBy(2.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Row(horizontalArrangement = Arrangement.spacedBy(2.dp)) {
                Box(modifier = Modifier.size(7.dp).background(Color(0xFF0078D4), RoundedCornerShape(1.dp)))
                Box(modifier = Modifier.size(7.dp).background(Color(0xFF2B88D8), RoundedCornerShape(1.dp)))
            }
            Row(horizontalArrangement = Arrangement.spacedBy(2.dp)) {
                Box(modifier = Modifier.size(7.dp).background(Color(0xFF0063B1), RoundedCornerShape(1.dp)))
                Box(modifier = Modifier.size(7.dp).background(Color(0xFF60CDFF), RoundedCornerShape(1.dp)))
            }
        }
    }
}

@Composable
private fun TaskbarAppItem(
    app: AppDescriptor,
    isRunning: Boolean,
    isFocused: Boolean,
    onClick: () -> Unit
) {
    val bg = when {
        isFocused -> TaskbarItemActive
        isRunning -> TaskbarDarkBg.copy(alpha = 0.5f)
        else -> Color.Transparent
    }

    Column(
        modifier = Modifier
            .width(40.dp)
            .height(36.dp)
            .clip(RoundedCornerShape(6.dp))
            .background(bg)
            .clickable(onClick = onClick)
            .padding(top = 4.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Icon(
            imageVector = app.icon,
            contentDescription = app.name,
            tint = if (isFocused) MaterialTheme.colorScheme.primary else Color.White,
            modifier = Modifier.size(20.dp)
        )

        Spacer(modifier = Modifier.height(2.dp))

        // Running Indicator Pill/Dot
        if (isRunning) {
            val indicatorWidth = if (isFocused) 14.dp else 5.dp
            val indicatorColor = if (isFocused) MaterialTheme.colorScheme.primary else TextMutedDark

            Box(
                modifier = Modifier
                    .width(indicatorWidth)
                    .height(3.dp)
                    .clip(CircleShape)
                    .background(indicatorColor)
            )
        } else {
            Spacer(modifier = Modifier.height(3.dp))
        }
    }
}
