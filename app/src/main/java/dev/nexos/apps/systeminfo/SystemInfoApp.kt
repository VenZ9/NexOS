package dev.nexos.apps.systeminfo

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
import androidx.compose.material.icons.filled.DeveloperBoard
import androidx.compose.material.icons.filled.Laptop
import androidx.compose.material.icons.filled.Memory
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Storage
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Divider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import dev.nexos.ui.theme.EmeraldAccent
import dev.nexos.ui.theme.StatusSuccess
import dev.nexos.ui.theme.StatusWarning
import dev.nexos.ui.theme.TextMutedDark
import dev.nexos.ui.theme.TextPrimaryDark
import dev.nexos.ui.theme.TextSecondaryDark
import dev.nexos.ui.theme.WindowBorder
import dev.nexos.ui.theme.WindowDarkBg
import dev.nexos.ui.theme.WindowDarkTitleBar

@Composable
fun SystemInfoApp(
    viewModel: SystemInfoViewModel,
    modifier: Modifier = Modifier
) {
    val specsState by viewModel.specs.collectAsState()

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(WindowDarkBg)
            .padding(14.dp)
            .verticalScroll(rememberScrollState())
    ) {
        // Header
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = Icons.Default.Laptop,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.primary,
                modifier = Modifier.size(36.dp)
            )

            Spacer(modifier = Modifier.width(12.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = specsState?.let { "${it.deviceManufacturer} ${it.deviceModel}" } ?: "System Properties",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold,
                    color = TextPrimaryDark
                )
                Text(
                    text = specsState?.nexOsVersion ?: "NexOS 1.0.0",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.secondary
                )
            }

            IconButton(onClick = { viewModel.loadSpecs() }, modifier = Modifier.size(28.dp)) {
                Icon(Icons.Default.Refresh, contentDescription = "Refresh", tint = TextPrimaryDark, modifier = Modifier.size(16.dp))
            }
        }

        Spacer(modifier = Modifier.height(14.dp))

        if (specsState == null) {
            Box(modifier = Modifier.fillMaxWidth().height(150.dp), contentAlignment = Alignment.Center) {
                CircularProgressIndicator(modifier = Modifier.size(28.dp), strokeWidth = 2.dp)
            }
        } else {
            val specs = specsState!!

            // Card 1: Processor & Architecture
            SpecsSectionCard(
                title = "Processor & Architecture",
                icon = Icons.Default.Memory
            ) {
                SpecRow("Primary ABI", specs.cpuAbi)
                SpecRow("Supported ABIs", specs.supportedAbis.joinToString(", "))
                SpecRow("CPU Cores", "${specs.cpuCores} active execution cores")
                SpecRow("64-Bit Host Architecture", if (specs.is64Bit) "Yes (aarch64)" else "32-Bit (ARMv7)")
                SpecRow("Hardware Board", specs.hardwareBoard)
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Card 2: Memory & Storage
            SpecsSectionCard(
                title = "Memory & Storage",
                icon = Icons.Default.Storage
            ) {
                SpecRow("Installed System RAM", specs.formattedTotalRam())
                SpecRow("Available System RAM", specs.formattedAvailableRam())
                SpecRow("Internal Storage Total", specs.formattedStorageTotal())
                SpecRow("Internal Storage Free", specs.formattedStorageFree())
                SpecRow("NexOS Virtual Drive Used", specs.formattedAppDataUsed())
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Card 3: Android Platform & Graphics
            SpecsSectionCard(
                title = "Operating System & Graphics",
                icon = Icons.Default.DeveloperBoard
            ) {
                SpecRow("OS Version", "${specs.osName} ${specs.androidVersion} (API ${specs.apiLevel})")
                SpecRow("Security Patch", specs.securityPatch)
                SpecRow("Vulkan Hardware Support", if (specs.hasVulkan) "Available (Vulkan 1.1+)" else "Fallback / OpenGLES only")
                SpecRow("Windows Translation Strategy", "Wine 9.x + Box64 Dynamic Translation")
            }
        }
    }
}

@Composable
private fun SpecsSectionCard(
    title: String,
    icon: ImageVector,
    content: @Composable () -> Unit
) {
    Card(
        colors = CardDefaults.cardColors(containerColor = WindowDarkTitleBar),
        shape = RoundedCornerShape(8.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(12.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(icon, contentDescription = null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(16.dp))
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = title,
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.Medium,
                    color = TextPrimaryDark
                )
            }

            Spacer(modifier = Modifier.height(8.dp))

            content()
        }
    }
}

@Composable
private fun SpecRow(label: String, value: String) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 3.dp),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.bodySmall,
            color = TextSecondaryDark,
            modifier = Modifier.weight(0.45f)
        )
        Text(
            text = value,
            style = MaterialTheme.typography.bodySmall,
            fontWeight = FontWeight.Medium,
            color = TextPrimaryDark,
            modifier = Modifier.weight(0.55f)
        )
    }
}
