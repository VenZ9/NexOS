package dev.nexos.windows.components

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.CropSquare
import androidx.compose.material.icons.filled.FilterNone
import androidx.compose.material.icons.filled.Remove
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import dev.nexos.ui.theme.TextMutedDark
import dev.nexos.ui.theme.TextPrimaryDark
import dev.nexos.ui.theme.WindowDarkTitleBar
import dev.nexos.ui.theme.WindowInactiveTitleBar

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun WindowTitleBar(
    title: String,
    icon: ImageVector,
    isFocused: Boolean,
    isMaximized: Boolean,
    onDrag: (Offset) -> Unit,
    onMinimize: () -> Unit,
    onMaximizeToggle: () -> Unit,
    onClose: () -> Unit,
    modifier: Modifier = Modifier
) {
    val titleBarBg = if (isFocused) WindowDarkTitleBar else WindowInactiveTitleBar
    val titleColor = if (isFocused) TextPrimaryDark else TextMutedDark

    Row(
        modifier = modifier
            .fillMaxWidth()
            .height(34.dp)
            .background(titleBarBg)
            .pointerInput(isMaximized) {
                if (!isMaximized) {
                    detectDragGestures { change, dragAmount ->
                        change.consume()
                        onDrag(dragAmount)
                    }
                }
            }
            .combinedClickable(
                onClick = {},
                onDoubleClick = onMaximizeToggle
            )
            .padding(start = 10.dp, end = 2.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        // App Icon
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = if (isFocused) MaterialTheme.colorScheme.primary else TextMutedDark,
            modifier = Modifier.size(16.dp)
        )

        Spacer(modifier = Modifier.width(8.dp))

        // Title Text
        Text(
            text = title,
            style = MaterialTheme.typography.bodyMedium.copy(
                fontWeight = if (isFocused) FontWeight.Medium else FontWeight.Normal,
                fontSize = 12.sp
            ),
            color = titleColor,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
            modifier = Modifier.weight(1f)
        )

        // Action Buttons: Minimize, Maximize/Restore, Close
        Row(verticalAlignment = Alignment.CenterVertically) {
            // Minimize
            IconButton(
                onClick = onMinimize,
                modifier = Modifier.size(32.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.Remove,
                    contentDescription = "Minimize",
                    tint = TextPrimaryDark.copy(alpha = 0.8f),
                    modifier = Modifier.size(14.dp)
                )
            }

            // Maximize / Restore
            IconButton(
                onClick = onMaximizeToggle,
                modifier = Modifier.size(32.dp)
            ) {
                Icon(
                    imageVector = if (isMaximized) Icons.Default.FilterNone else Icons.Default.CropSquare,
                    contentDescription = if (isMaximized) "Restore" else "Maximize",
                    tint = TextPrimaryDark.copy(alpha = 0.8f),
                    modifier = Modifier.size(13.dp)
                )
            }

            // Close
            Box(
                modifier = Modifier
                    .size(32.dp)
                    .background(Color.Transparent, RoundedCornerShape(2.dp)),
                contentAlignment = Alignment.Center
            ) {
                IconButton(
                    onClick = onClose,
                    modifier = Modifier.size(32.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Close,
                        contentDescription = "Close",
                        tint = TextPrimaryDark,
                        modifier = Modifier.size(15.dp)
                    )
                }
            }
        }
    }
}
