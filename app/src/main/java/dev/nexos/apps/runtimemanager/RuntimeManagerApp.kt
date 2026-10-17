package dev.nexos.apps.runtimemanager

import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.BugReport
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Error
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Layers
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import dev.nexos.ui.theme.EmeraldAccent
import dev.nexos.ui.theme.PurpleAccent
import dev.nexos.ui.theme.StatusError
import dev.nexos.ui.theme.StatusInfo
import dev.nexos.ui.theme.StatusSuccess
import dev.nexos.ui.theme.StatusWarning
import dev.nexos.ui.theme.TextMutedDark
import dev.nexos.ui.theme.TextPrimaryDark
import dev.nexos.ui.theme.TextSecondaryDark
import dev.nexos.ui.theme.WindowBorder
import dev.nexos.ui.theme.WindowDarkBg
import dev.nexos.ui.theme.WindowDarkTitleBar

@Composable
fun RuntimeManagerApp(
    viewModel: RuntimeManagerViewModel,
    modifier: Modifier = Modifier
) {
    val state by viewModel.uiState.collectAsState()
    val status = state.status

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(WindowDarkBg)
            .padding(14.dp)
            .verticalScroll(rememberScrollState())
    ) {
        // Engine Status Banner
        Card(
            colors = CardDefaults.cardColors(containerColor = WindowDarkTitleBar),
            shape = RoundedCornerShape(8.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(modifier = Modifier.padding(14.dp)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.Layers,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(32.dp)
                    )

                    Spacer(modifier = Modifier.width(12.dp))

                    Column(modifier = Modifier.weight(1f)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                text = status?.engineName ?: "Windows Compatibility Runtime",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.SemiBold,
                                color = TextPrimaryDark
                            )

                            Spacer(modifier = Modifier.width(8.dp))

                            if (state.isMock) {
                                Box(
                                    modifier = Modifier
                                        .clip(RoundedCornerShape(3.dp))
                                        .background(PurpleAccent.copy(alpha = 0.2f))
                                        .padding(horizontal = 6.dp, vertical = 2.dp)
                                ) {
                                    Text("Mock Simulator", color = PurpleAccent, fontSize = 9.sp, fontWeight = FontWeight.Bold)
                                }
                            }
                        }

                        Text(
                            text = status?.version ?: "Probing...",
                            style = MaterialTheme.typography.bodySmall,
                            color = TextSecondaryDark
                        )
                    }

                    // Engine Mode Switcher Button
                    OutlinedButton(
                        onClick = {
                            if (state.isMock) {
                                viewModel.switchRuntime("REAL")
                            } else {
                                viewModel.switchRuntime("MOCK")
                            }
                        },
                        shape = RoundedCornerShape(4.dp),
                        modifier = Modifier.height(28.dp)
                    ) {
                        Text(
                            text = if (state.isMock) "Switch to Real" else "Switch to Mock",
                            fontSize = 11.sp
                        )
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(14.dp))

        // Checklist of Components
        Text("Component Health Checklist", style = MaterialTheme.typography.titleMedium, color = TextPrimaryDark)
        Spacer(modifier = Modifier.height(6.dp))

        Card(
            colors = CardDefaults.cardColors(containerColor = WindowDarkTitleBar),
            shape = RoundedCornerShape(8.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(modifier = Modifier.padding(12.dp)) {
                ComponentHealthRow(
                    name = "ARM64 Host Processor",
                    description = "Required to execute Box64 dynamic translation",
                    isHealthy = status?.architecture?.contains("64") == true,
                    statusText = status?.architecture ?: "Unknown"
                )

                ComponentHealthRow(
                    name = "Vulkan Driver Acceleration",
                    description = "Required for DXVK / Turnip GPU translation",
                    isHealthy = status?.vulkanAvailable == true,
                    statusText = if (status?.vulkanAvailable == true) "Available" else "Fallback"
                )

                ComponentHealthRow(
                    name = "Box64 Dynamic Translator",
                    description = "Translates x86_64 CISC instructions to ARM64 RISC",
                    isHealthy = status?.box64Installed == true || state.isMock,
                    statusText = if (status?.box64Installed == true) "Installed" else if (state.isMock) "Simulated" else "Missing"
                )

                ComponentHealthRow(
                    name = "Wine 9.x POSIX Translation",
                    description = "Translates Win32 / Win64 system calls to POSIX",
                    isHealthy = status?.wineInstalled == true || state.isMock,
                    statusText = if (status?.wineInstalled == true) "Installed" else if (state.isMock) "Simulated" else "Missing"
                )

                ComponentHealthRow(
                    name = "Isolated Wine Prefix (Virtual C:)",
                    description = "App-private drive container for registry & libraries",
                    isHealthy = true,
                    statusText = "Initialized"
                )
            }
        }

        Spacer(modifier = Modifier.height(14.dp))

        // Diagnostics Runner Section
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text("Engine Diagnostics", style = MaterialTheme.typography.titleMedium, color = TextPrimaryDark)

            Button(
                onClick = { viewModel.runDiagnostics() },
                enabled = !state.isRunningDiagnostics,
                shape = RoundedCornerShape(4.dp),
                modifier = Modifier.height(28.dp),
                colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary)
            ) {
                if (state.isRunningDiagnostics) {
                    CircularProgressIndicator(modifier = Modifier.size(12.dp), strokeWidth = 1.5.dp, color = Color.White)
                    Spacer(modifier = Modifier.width(6.dp))
                }
                Text("Run Diagnostics", fontSize = 11.sp)
            }
        }

        Spacer(modifier = Modifier.height(6.dp))

        // Diagnostic Console Output
        Card(
            colors = CardDefaults.cardColors(containerColor = Color(0xFF101014)),
            shape = RoundedCornerShape(6.dp),
            modifier = Modifier
                .fillMaxWidth()
                .border(1.dp, WindowBorder, RoundedCornerShape(6.dp))
        ) {
            Column(modifier = Modifier.padding(10.dp)) {
                if (state.diagnosticLog.isEmpty()) {
                    Text(
                        text = "Click 'Run Diagnostics' to probe device ABI, Vulkan, and Wine container health.",
                        style = MaterialTheme.typography.bodySmall.copy(fontFamily = FontFamily.Monospace),
                        color = TextMutedDark,
                        fontSize = 11.sp
                    )
                } else {
                    state.diagnosticLog.forEach { logLine ->
                        val color = when {
                            logLine.contains("READY") -> StatusSuccess
                            logLine.contains("OFFLINE") || logLine.contains("Missing") -> StatusWarning
                            else -> TextSecondaryDark
                        }
                        Text(
                            text = logLine,
                            style = MaterialTheme.typography.bodySmall.copy(fontFamily = FontFamily.Monospace),
                            color = color,
                            fontSize = 11.sp
                        )
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(14.dp))

        // Architecture & Licensing Audit Notice
        Card(
            colors = CardDefaults.cardColors(containerColor = WindowDarkTitleBar),
            shape = RoundedCornerShape(8.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(modifier = Modifier.padding(12.dp)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.Info, contentDescription = null, tint = StatusInfo, modifier = Modifier.size(18.dp))
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Upstream Architecture & Redistribution Audit", style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.Medium, color = TextPrimaryDark)
                }

                Spacer(modifier = Modifier.height(6.dp))

                Text(
                    text = "• Wine is licensed under LGPL v2.1+. Box64 is licensed under MIT. DXVK is licensed under Zlib/Libpng.\n" +
                            "• A complete Wine + Box64 glibc rootfs container requires 500 MB – 1.2 GB of disk storage, which cannot be bundled directly into a standard lightweight APK.\n" +
                            "• Real rendering requires an internal X11 / Wayland surface bridge rendered into an Android SurfaceView.\n" +
                            "• NexOS strictly adheres to honest reporting: Windows execution is only performed when real container binaries are present.",
                    style = MaterialTheme.typography.bodySmall,
                    color = TextSecondaryDark,
                    fontSize = 11.sp,
                    lineHeight = 16.sp
                )
            }
        }
    }
}

@Composable
private fun ComponentHealthRow(
    name: String,
    description: String,
    isHealthy: Boolean,
    statusText: String
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 5.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            imageVector = if (isHealthy) Icons.Default.CheckCircle else Icons.Default.Warning,
            contentDescription = null,
            tint = if (isHealthy) StatusSuccess else StatusWarning,
            modifier = Modifier.size(16.dp)
        )

        Spacer(modifier = Modifier.width(8.dp))

        Column(modifier = Modifier.weight(1f)) {
            Text(name, style = MaterialTheme.typography.bodyMedium, color = TextPrimaryDark, fontWeight = FontWeight.Medium)
            Text(description, style = MaterialTheme.typography.bodySmall, color = TextMutedDark, fontSize = 10.sp)
        }

        Box(
            modifier = Modifier
                .clip(RoundedCornerShape(3.dp))
                .background(if (isHealthy) StatusSuccess.copy(alpha = 0.15f) else StatusWarning.copy(alpha = 0.15f))
                .padding(horizontal = 6.dp, vertical = 2.dp)
        ) {
            Text(
                text = statusText,
                color = if (isHealthy) StatusSuccess else StatusWarning,
                fontSize = 10.sp,
                fontWeight = FontWeight.Bold
            )
        }
    }
}
