package dev.nexos.windows.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.unit.DpSize
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.zIndex
import dev.nexos.ui.theme.WindowActiveBorder
import dev.nexos.ui.theme.WindowBorder
import dev.nexos.ui.theme.WindowDarkBg
import dev.nexos.windows.WindowInstance
import dev.nexos.windows.WindowState
import kotlin.math.roundToInt

@Composable
fun WindowFrame(
    window: WindowInstance,
    icon: ImageVector,
    onFocus: () -> Unit,
    onDrag: (Offset) -> Unit,
    onResize: (DpSize) -> Unit,
    onMinimize: () -> Unit,
    onMaximizeToggle: () -> Unit,
    onClose: () -> Unit,
    content: @Composable () -> Unit
) {
    if (window.state == WindowState.MINIMIZED) return

    val isMaximized = window.state == WindowState.MAXIMIZED
    val shape = if (isMaximized) RoundedCornerShape(0.dp) else RoundedCornerShape(8.dp)
    val borderColor = if (window.isFocused) WindowActiveBorder else WindowBorder
    val elevation = if (window.isFocused) 16.dp else 6.dp

    val frameModifier = if (isMaximized) {
        Modifier
            .fillMaxSize()
            .zIndex(window.zIndex)
    } else {
        Modifier
            .offset { IntOffset(window.position.x.roundToInt(), window.position.y.roundToInt()) }
            .size(window.size)
            .zIndex(window.zIndex)
            .shadow(elevation, shape)
    }

    Box(
        modifier = frameModifier
            .clip(shape)
            .background(WindowDarkBg)
            .border(1.dp, borderColor, shape)
            .pointerInput(Unit) {
                detectTapGestures {
                    onFocus()
                }
            }
    ) {
        Column(modifier = Modifier.fillMaxSize()) {
            // TitleBar
            WindowTitleBar(
                title = window.title,
                icon = icon,
                isFocused = window.isFocused,
                isMaximized = isMaximized,
                onDrag = onDrag,
                onMinimize = onMinimize,
                onMaximizeToggle = onMaximizeToggle,
                onClose = onClose
            )

            // Window Content Area
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f)
                    .background(WindowDarkBg)
            ) {
                content()
            }
        }

        // Resize handles (only when not maximized)
        if (!isMaximized) {
            ResizeHandles(
                currentSize = window.size,
                onResize = onResize,
                modifier = Modifier.fillMaxSize()
            )
        }
    }
}
