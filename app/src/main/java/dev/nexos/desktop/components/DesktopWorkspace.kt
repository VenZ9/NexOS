package dev.nexos.desktop.components

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.dp
import dev.nexos.apps.AppDescriptor
import dev.nexos.apps.AppRegistry
import dev.nexos.apps.NexAppType
import dev.nexos.windows.WindowInstance
import dev.nexos.windows.components.WindowFrame

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun DesktopWorkspace(
    windows: List<WindowInstance>,
    showShortcuts: Boolean,
    onAppOpen: (NexAppType) -> Unit,
    onWindowFocus: (String) -> Unit,
    onWindowDrag: (String, Offset, Float, Float) -> Unit,
    onWindowResize: (String, androidx.compose.ui.unit.DpSize) -> Unit,
    onWindowMinimize: (String) -> Unit,
    onWindowMaximizeToggle: (String) -> Unit,
    onWindowClose: (String) -> Unit,
    onDesktopContextMenu: (Offset) -> Unit,
    onDismissPopups: () -> Unit,
    windowContentProvider: @Composable (WindowInstance) -> Unit,
    modifier: Modifier = Modifier
) {
    var selectedAppType by remember { mutableStateOf<NexAppType?>(null) }
    val density = LocalDensity.current

    BoxWithConstraints(
        modifier = modifier
            .fillMaxSize()
            .pointerInput(Unit) {
                detectTapGestures(
                    onTap = {
                        selectedAppType = null
                        onDismissPopups()
                    },
                    onLongPress = { offset ->
                        onDesktopContextMenu(offset)
                    }
                )
            }
    ) {
        val maxWidthPx = with(density) { maxWidth.toPx() }
        val maxHeightPx = with(density) { maxHeight.toPx() }

        // Desktop Shortcuts Grid (Left-aligned column)
        if (showShortcuts) {
            Column(
                modifier = Modifier
                    .padding(start = 12.dp, top = 12.dp)
            ) {
                AppRegistry.builtInApps.filter { it.showOnDesktop }.forEach { app ->
                    DesktopShortcutView(
                        app = app,
                        isSelected = selectedAppType == app.type,
                        onClick = {
                            selectedAppType = app.type
                            onDismissPopups()
                        },
                        onDoubleClick = {
                            selectedAppType = null
                            onAppOpen(app.type)
                        },
                        onLongClick = {
                            selectedAppType = app.type
                            onDesktopContextMenu(Offset(80f, 100f))
                        }
                    )
                }
            }
        }

        // Render Open Windows
        windows.forEach { window ->
            val appDesc = AppRegistry.getDescriptor(window.appType)

            WindowFrame(
                window = window,
                icon = appDesc.icon,
                onFocus = { onWindowFocus(window.id) },
                onDrag = { delta -> onWindowDrag(window.id, delta, maxWidthPx, maxHeightPx) },
                onResize = { newSize -> onWindowResize(window.id, newSize) },
                onMinimize = { onWindowMinimize(window.id) },
                onMaximizeToggle = { onWindowMaximizeToggle(window.id) },
                onClose = { onWindowClose(window.id) }
            ) {
                windowContentProvider(window)
            }
        }
    }
}
