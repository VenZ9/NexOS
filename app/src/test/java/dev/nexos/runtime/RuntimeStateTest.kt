package dev.nexos.runtime

import dev.nexos.runtime.mock.MockWindowsRuntime
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

class RuntimeStateTest {

    private lateinit var mockRuntime: MockWindowsRuntime

    @Before
    fun setUp() {
        mockRuntime = MockWindowsRuntime()
    }

    @Test
    fun testMockRuntime_reportsMockState() = runBlocking {
        assertTrue(mockRuntime.isMock)
        val status = mockRuntime.getStatus()
        assertTrue(status.isMock)
        assertEquals(0, status.activeProcessesCount)
    }

    @Test
    fun testMockRuntime_listsPrepopulatedApps() = runBlocking {
        val apps = mockRuntime.listInstalledApplications()
        assertTrue(apps.isNotEmpty())
        assertTrue(apps.any { it.name.contains("7-Zip") })
    }

    @Test
    fun testMockRuntime_launchAndStopApplication() = runBlocking {
        val apps = mockRuntime.listInstalledApplications()
        val targetApp = apps.first()

        val launchResult = mockRuntime.launchApplication(targetApp.id)
        assertTrue(launchResult.isSuccess)
        val pid = launchResult.getOrThrow()
        assertTrue(pid > 0)

        val currentState = mockRuntime.state.value
        assertTrue(currentState is RuntimeState.Running)
        assertEquals((currentState as RuntimeState.Running).app.id, targetApp.id)

        val stopResult = mockRuntime.stopApplication(targetApp.id)
        assertTrue(stopResult.isSuccess)
        assertTrue(mockRuntime.state.value is RuntimeState.Stopped)
    }

    @Test
    fun testMockRuntime_installApplication() = runBlocking {
        val initialCount = mockRuntime.listInstalledApplications().size
        val installResult = mockRuntime.installApplication("C:\\Installers\\setup.exe", "VLC Player")

        assertTrue(installResult.isSuccess)
        val newApp = installResult.getOrThrow()
        assertTrue(newApp.name.contains("VLC Player"))

        val newCount = mockRuntime.listInstalledApplications().size
        assertEquals(initialCount + 1, newCount)
    }
}
