package dev.nexos.desktop.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Divider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.zIndex
import dev.nexos.ui.theme.ContextMenuBg
import dev.nexos.ui.theme.ContextMenuBorder
import dev.nexos.ui.theme.TextMutedDark
import dev.nexos.ui.theme.TextPrimaryDark
import kotlin.math.roundToInt

data class ContextMenuItem(
    val title: String,
    val icon: ImageVector? = null,
    val isDivider: Boolean = false,
    val onClick: () -> Unit = {}
)

@Composable
fun ContextMenu(
    position: Offset,
    items: List<ContextMenuItem>,
    onDismiss: () -> Unit,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .offset { IntOffset(position.x.roundToInt(), position.y.roundToInt()) }
            .zIndex(9999f)
            .shadow(16.dp, RoundedCornerShape(8.dp))
            .clip(RoundedCornerShape(8.dp))
            .background(ContextMenuBg)
            .border(1.dp, ContextMenuBorder, RoundedCornerShape(8.dp))
            .padding(vertical = 4.dp)
            .width(190.dp)
    ) {
        Column {
            items.forEach { item ->
                if (item.isDivider) {
                    Divider(
                        color = ContextMenuBorder.copy(alpha = 0.5f),
                        modifier = Modifier.padding(vertical = 4.dp)
                    )
                } else {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable {
                                item.onClick()
                                onDismiss()
                            }
                            .padding(horizontal = 12.dp, vertical = 7.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        item.icon?.let { icon ->
                            Icon(
                                imageVector = icon,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(modifier = Modifier.width(10.dp))
                        }
                        Text(
                            text = item.title,
                            style = MaterialTheme.typography.bodyMedium,
                            color = TextPrimaryDark,
                            fontSize = 12.sp
                        )
                    }
                }
            }
        }
    }
}
