package dev.nexos.apps.about

import androidx.compose.foundation.background
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
import androidx.compose.material.icons.filled.Laptop
import androidx.compose.material3.Button
import androidx.compose.material3.Divider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import dev.nexos.ui.theme.TextMutedDark
import dev.nexos.ui.theme.TextPrimaryDark
import dev.nexos.ui.theme.TextSecondaryDark
import dev.nexos.ui.theme.WindowBorder
import dev.nexos.ui.theme.WindowDarkBg

@Composable
fun AboutApp(
    onClose: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .background(WindowDarkBg)
            .padding(16.dp)
    ) {
        // Logo & Title
        Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(
                imageVector = Icons.Default.Laptop,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.primary,
                modifier = Modifier.size(42.dp)
            )

            Spacer(modifier = Modifier.width(14.dp))

            Column {
                Text(
                    text = "NexOS",
                    style = MaterialTheme.typography.headlineMedium,
                    fontWeight = FontWeight.Bold,
                    color = TextPrimaryDark
                )
                Text(
                    text = "Native Android Desktop & Windows Compatibility Layer",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.secondary
                )
            }
        }

        Spacer(modifier = Modifier.height(14.dp))
        Divider(color = WindowBorder)
        Spacer(modifier = Modifier.height(14.dp))

        Text(
            text = "Version: 1.0.0 (Release 2026.10)\n" +
                    "Runtime: Kotlin 2.0 / Jetpack Compose / Material 3\n" +
                    "Compatibility Target: Wine 9.x + Box64 Dynamic Translation",
            style = MaterialTheme.typography.bodySmall,
            color = TextSecondaryDark,
            lineHeight = 18.sp
        )

        Spacer(modifier = Modifier.height(14.dp))

        Text(
            text = "Security Notice:",
            style = MaterialTheme.typography.titleSmall,
            fontWeight = FontWeight.Medium,
            color = TextPrimaryDark
        )

        Spacer(modifier = Modifier.height(4.dp))

        Text(
            text = "Imported Windows executables (.exe, .msi) run in isolated app storage. Untrusted binaries are checked for PE signatures, COFF architecture, and SHA-256 integrity before execution.",
            style = MaterialTheme.typography.bodySmall,
            color = TextMutedDark,
            lineHeight = 16.sp
        )

        Spacer(modifier = Modifier.weight(1f))

        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Spacer(modifier = Modifier.weight(1f))
            Button(
                onClick = onClose,
                shape = RoundedCornerShape(4.dp),
                modifier = Modifier.height(30.dp)
            ) {
                Text("OK", fontSize = 12.sp)
            }
        }
    }
}
