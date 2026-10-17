package dev.nexos.windows.components

import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.DpSize
import androidx.compose.ui.unit.dp

@Composable
fun ResizeHandles(
    currentSize: DpSize,
    onResize: (DpSize) -> Unit,
    modifier: Modifier = Modifier
) {
    val density = LocalDensity.current

    Box(modifier = modifier) {
        // Right Edge handle
        Box(
            modifier = Modifier
                .align(Alignment.CenterEnd)
                .width(10.dp)
                .fillMaxHeight()
                .pointerInput(currentSize) {
                    detectDragGestures { change, dragAmount ->
                        change.consume()
                        val deltaWidthDp = with(density) { dragAmount.x.toDp() }
                        val newWidth = currentSize.width + deltaWidthDp
                        onResize(DpSize(newWidth, currentSize.height))
                    }
                }
        )

        // Bottom Edge handle
        Box(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .height(10.dp)
                .fillMaxWidth()
                .pointerInput(currentSize) {
                    detectDragGestures { change, dragAmount ->
                        change.consume()
                        val deltaHeightDp = with(density) { dragAmount.y.toDp() }
                        val newHeight = currentSize.height + deltaHeightDp
                        onResize(DpSize(currentSize.width, newHeight))
                    }
                }
        )

        // Bottom-Right Corner handle
        Box(
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .size(16.dp)
                .background(Color.Transparent)
                .pointerInput(currentSize) {
                    detectDragGestures { change, dragAmount ->
                        change.consume()
                        val deltaWidthDp = with(density) { dragAmount.x.toDp() }
                        val deltaHeightDp = with(density) { dragAmount.y.toDp() }
                        val newWidth = currentSize.width + deltaWidthDp
                        val newHeight = currentSize.height + deltaHeightDp
                        onResize(DpSize(newWidth, newHeight))
                    }
                }
        )
    }
}
