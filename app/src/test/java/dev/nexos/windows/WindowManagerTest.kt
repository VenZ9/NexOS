package dev.nexos.windows

import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.unit.DpSize
import androidx.compose.ui.unit.dp
import dev.nexos.apps.NexAppType
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

class WindowManagerTest {

    private lateinit var windowManager: WindowManager

    @Before
    fun setUp() {
        windowManager = WindowManager()
    }

    @Test
    fun testOpenWindow_createsFocusedWindow() {
        val windowId = windowManager.openWindow(NexAppType.FILE_MANAGER)
        val windows = windowManager.windows.value

        assertEquals(1, windows.size)
        val window = windows.first()
        assertEquals(windowId, window.id)
        assertEquals(NexAppType.FILE_MANAGER, window.appType)
        assertTrue(window.isFocused)
        assertEquals(WindowState.NORMAL, window.state)
    }

    @Test
    fun testSingleInstance_focusesExistingInsteadOfDuplicating() {
        val firstId = windowManager.openWindow(NexAppType.SETTINGS)
        val secondId = windowManager.openWindow(NexAppType.SETTINGS)

        val windows = windowManager.windows.value
        assertEquals(1, windows.size)
        assertEquals(firstId, secondId)
    }

    @Test
    fun testMultiInstance_allowsMultipleWindows() {
        val id1 = windowManager.openWindow(NexAppType.TEXT_EDITOR)
        val id2 = windowManager.openWindow(NexAppType.TEXT_EDITOR)

        val windows = windowManager.windows.value
        assertEquals(2, windows.size)
        assertTrue(windows.find { it.id == id2 }!!.isFocused)
        assertFalse(windows.find { it.id == id1 }!!.isFocused)
    }

    @Test
    fun testMinimizeAndRestore() {
        val id = windowManager.openWindow(NexAppType.FILE_MANAGER)
        windowManager.minimizeWindow(id)

        var window = windowManager.windows.value.first()
        assertEquals(WindowState.MINIMIZED, window.state)
        assertFalse(window.isFocused)

        windowManager.restoreWindow(id)
        window = windowManager.windows.value.first()
        assertEquals(WindowState.NORMAL, window.state)
        assertTrue(window.isFocused)
    }

    @Test
    fun testMaximizeToggle() {
        val id = windowManager.openWindow(NexAppType.FILE_MANAGER)

        windowManager.toggleMaximize(id)
        var window = windowManager.windows.value.first()
        assertEquals(WindowState.MAXIMIZED, window.state)

        windowManager.toggleMaximize(id)
        window = windowManager.windows.value.first()
        assertEquals(WindowState.NORMAL, window.state)
    }

    @Test
    fun testCloseWindow_focusesNextTopmost() {
        val id1 = windowManager.openWindow(NexAppType.TEXT_EDITOR)
        val id2 = windowManager.openWindow(NexAppType.FILE_MANAGER)

        windowManager.closeWindow(id2)
        val windows = windowManager.windows.value
        assertEquals(1, windows.size)
        assertEquals(id1, windows.first().id)
        assertTrue(windows.first().isFocused)
    }

    @Test
    fun testMoveWindow_clampedToBounds() {
        val id = windowManager.openWindow(NexAppType.FILE_MANAGER, initialPosition = Offset(100f, 100f))

        // Move by delta within bounds
        windowManager.moveWindow(id, Offset(50f, 50f), maxWidth = 1000f, maxHeight = 800f)
        var window = windowManager.windows.value.first()
        assertEquals(150f, window.position.x, 0.01f)
        assertEquals(150f, window.position.y, 0.01f)

        // Negative move clamped to 0
        windowManager.moveWindow(id, Offset(-500f, -500f), maxWidth = 1000f, maxHeight = 800f)
        window = windowManager.windows.value.first()
        assertEquals(0f, window.position.x, 0.01f)
        assertEquals(0f, window.position.y, 0.01f)
    }

    @Test
    fun testResizeWindow_respectsMinimumSize() {
        val id = windowManager.openWindow(NexAppType.FILE_MANAGER)
        val window = windowManager.windows.value.first()

        // Try resizing smaller than minSize
        windowManager.resizeWindow(id, DpSize(100.dp, 50.dp))
        val resized = windowManager.windows.value.first()

        assertEquals(window.minSize.width, resized.size.width)
        assertEquals(window.minSize.height, resized.size.height)
    }
}
