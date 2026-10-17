package dev.nexos.apps

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Test

class AppRegistryTest {

    @Test
    fun testBuiltInApps_containsEssentialApps() {
        val apps = AppRegistry.builtInApps
        assertTrue(apps.isNotEmpty())

        val appTypes = apps.map { it.type }
        assertTrue(appTypes.contains(NexAppType.FILE_MANAGER))
        assertTrue(appTypes.contains(NexAppType.TEXT_EDITOR))
        assertTrue(appTypes.contains(NexAppType.SETTINGS))
        assertTrue(appTypes.contains(NexAppType.APP_LIBRARY))
        assertTrue(appTypes.contains(NexAppType.SYSTEM_INFO))
        assertTrue(appTypes.contains(NexAppType.RUNTIME_MANAGER))
        assertTrue(appTypes.contains(NexAppType.ABOUT_NEXOS))
    }

    @Test
    fun testGetDescriptor_returnsCorrectDescriptor() {
        val desc = AppRegistry.getDescriptor(NexAppType.FILE_MANAGER)
        assertEquals("File Manager", desc.name)
        assertNotNull(desc.icon)
    }

    @Test
    fun testSingleInstance_flagsCorrect() {
        assertTrue(NexAppType.SETTINGS.isSingleInstance)
        assertTrue(NexAppType.SYSTEM_INFO.isSingleInstance)
        assertTrue(NexAppType.RUNTIME_MANAGER.isSingleInstance)
        assertFalse(NexAppType.FILE_MANAGER.isSingleInstance)
        assertFalse(NexAppType.TEXT_EDITOR.isSingleInstance)
    }
}
