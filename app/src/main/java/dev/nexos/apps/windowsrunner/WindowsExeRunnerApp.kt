package dev.nexos.apps.windowsrunner

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
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
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.Stop
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import dev.nexos.runtime.PeFileInspector
import dev.nexos.runtime.PeMetadata
import dev.nexos.runtime.RuntimeProvider
import dev.nexos.runtime.RuntimeStatus
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
import java.io.File

@Composable
fun WindowsExeRunnerApp(
    filePath: String,
    runtimeProvider: RuntimeProvider,
    onClose: () -> Unit,
    modifier: Modifier = Modifier
) {
    var peMetadata by remember { mutableStateOf<PeMetadata?>(null) }
    var isInspecting by remember { mutableStateOf(true) }
    var runtimeStatus by remember { mutableStateOf<RuntimeStatus?>(null) }
    var runningPid by remember { mutableStateOf<Int?>(null) }
    var executionError by remember { mutableStateOf<String?>(null) }

    val activeRuntime = runtimeProvider.activeRuntime.collectAsState().value
    val file = remember(filePath) { File(filePath) }

    LaunchedEffect(filePath) {
        isInspecting = true
        if (file.exists()) {
            peMetadata = PeFileInspector.inspect(file)
        }
        runtimeStatus = activeRuntime.getStatus()
        isInspecting = false
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(WindowDarkBg)
            .padding(14.dp)
            .verticalScroll(rememberScrollState())
    ) {
        // Target Executable Header
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = Icons.Default.PlayArrow,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.primary,
                modifier = Modifier.size(36.dp)
            )

            Spacer(modifier = Modifier.width(12.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = file.name,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold,
                    color = TextPrimaryDark
                )
                Text(
                    text = file.absolutePath,
                    style = MaterialTheme.typography.bodySmall,
                    color = TextMutedDark,
                    fontSize = 10.sp
                )
            }

            if (runningPid != null) {
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(4.dp))
                        .background(StatusSuccess.copy(alpha = 0.2f))
                        .padding(horizontal = 8.dp, vertical = 3.dp)
                ) {
                    Text("PID: $runningPid", color = StatusSuccess, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                }
            }
        }

        Spacer(modifier = Modifier.height(14.dp))

        if (isInspecting) {
            Box(modifier = Modifier.fillMaxWidth().height(100.dp), contentAlignment = Alignment.Center) {
                CircularProgressIndicator(modifier = Modifier.size(28.dp), strokeWidth = 2.dp)
            }
        } else {
            val pe = peMetadata

            // Card 1: Binary PE Analysis
            Card(
                colors = CardDefaults.cardColors(containerColor = WindowDarkTitleBar),
                shape = RoundedCornerShape(8.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(12.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.Security, contentDescription = null, tint = StatusInfo, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Portable Executable (PE) Inspection", style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.Medium, color = TextPrimaryDark)
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    if (pe == null || !pe.isValidPe) {
                        Text(
                            text = "Invalid or corrupted Windows PE header: ${pe?.errorMessage ?: "Not a PE binary"}",
                            style = MaterialTheme.typography.bodySmall,
                            color = StatusError
                        )
                    } else {
                        DetailRow("Architecture Target", pe.machineName)
                        DetailRow("Binary Subsystem", pe.subsystemName)
                        DetailRow("64-Bit Binary", if (pe.is64Bit) "Yes (PE32+)" else "No (PE32)")
                        DetailRow("File Size", "${pe.sizeBytes} bytes")
                        DetailRow("SHA-256 Checksum", pe.sha256Checksum.take(24) + "...")
                    }
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Card 2: Runtime Compatibility Verdict
            Card(
                colors = CardDefaults.cardColors(containerColor = WindowDarkTitleBar),
                shape = RoundedCornerShape(8.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(12.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        val icon = if (runtimeStatus?.canLaunchRealWindowsBinaries == true || activeRuntime.isMock) Icons.Default.CheckCircle else Icons.Default.Warning
                        val tint = if (runtimeStatus?.canLaunchRealWindowsBinaries == true || activeRuntime.isMock) StatusSuccess else StatusWarning

                        Icon(icon, contentDescription = null, tint = tint, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Runtime Execution Environment", style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.Medium, color = TextPrimaryDark)
                    }

                    Spacer(modifier = Modifier.height(6.dp))

                    Text(
                        text = "Active Engine: ${activeRuntime.runtimeName}",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.secondary
                    )

                    if (!activeRuntime.isMock && runtimeStatus?.canLaunchRealWindowsBinaries != true) {
                        Spacer(modifier = Modifier.height(6.dp))
                        Text(
                            text = "Real execution is blocked: Wine 9.x glibc container is not installed. NexOS does not fake execution. Switch to Mock Simulator in Settings to test UI flows.",
                            style = MaterialTheme.typography.bodySmall,
                            color = StatusWarning,
                            fontSize = 11.sp
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            // Execution Controls
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                if (runningPid == null) {
                    Button(
                        onClick = {
                            if (activeRuntime.isMock) {
                                runningPid = 1420 + (Math.random() * 5000).toInt()
                                executionError = null
                            } else {
                                executionError = "Wine 9.x container binaries missing. Please configure container packages or switch to Mock Simulator in Settings."
                            }
                        },
                        shape = RoundedCornerShape(4.dp),
                        modifier = Modifier.weight(1f),
                        colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary)
                    ) {
                        Icon(Icons.Default.PlayArrow, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("Launch Executable", fontSize = 12.sp)
                    }
                } else {
                    Button(
                        onClick = {
                            runningPid = null
                        },
                        shape = RoundedCornerShape(4.dp),
                        modifier = Modifier.weight(1f),
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFEF4444))
                    ) {
                        Icon(Icons.Default.Stop, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("Terminate Process", fontSize = 12.sp)
                    }
                }
            }

            executionError?.let { err ->
                Spacer(modifier = Modifier.height(10.dp))
                Text(
                    text = err,
                    style = MaterialTheme.typography.bodySmall,
                    color = StatusWarning,
                    fontSize = 11.sp
                )
            }
        }
    }
}

@Composable
private fun DetailRow(label: String, value: String) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 2.dp),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(label, style = MaterialTheme.typography.bodySmall, color = TextSecondaryDark)
        Text(value, style = MaterialTheme.typography.bodySmall, fontWeight = FontWeight.Medium, color = TextPrimaryDark)
    }
}
