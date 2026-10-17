package dev.nexos.runtime.mock

import dev.nexos.runtime.InstalledApp
import dev.nexos.runtime.PeMetadata
import dev.nexos.runtime.RuntimeState
import dev.nexos.runtime.RuntimeStatus
import dev.nexos.runtime.WindowsRuntime
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.withContext
import java.io.File
import java.util.UUID

class MockWindowsRuntime : WindowsRuntime {

    override val runtimeName: String = "NexOS Mock Windows Simulator [DEV ONLY]"
    override val isMock: Boolean = true

    private val _state = MutableStateFlow<RuntimeState>(RuntimeState.NotInstalled)
    override val state: StateFlow<RuntimeState> = _state.asStateFlow()

    private var lastErrorMessage: String? = null
    private val installedApps = mutableListOf<InstalledApp>()
    private var runningAppId: String? = null

    init {
        // Pre-populate with realistic mock Windows applications for UI development
        installedApps.add(
            InstalledApp(
                id = "mock_7zip",
                name = "7-Zip File Manager [Mock]",
                executablePath = "C:\\Program Files\\7-Zip\\7zFM.exe",
                installedDate = System.currentTimeMillis() - 86400000L * 2,
                architecture = "x64 (64-bit AMD/Intel)",
                sizeBytes = 2_450_000L,
                sha256 = "e3b0c44298fc1c149afbf4c8996fb92427ae41e4649b934ca495991b7852b855",
                isMock = true
            )
        )
        installedApps.add(
            InstalledApp(
                id = "mock_notepadpp",
                name = "Notepad++ Editor [Mock]",
                executablePath = "C:\\Program Files\\Notepad++\\notepad++.exe",
                installedDate = System.currentTimeMillis() - 86400000L * 5,
                architecture = "x86 (32-bit Intel/AMD)",
                sizeBytes = 5_800_000L,
                sha256 = "ca978112ca1bbdcafac231b39a23dc4da786eff8147c4e72b9807785afee48bb",
                isMock = true
            )
        )
        installedApps.add(
            InstalledApp(
                id = "mock_foobar2000",
                name = "foobar2000 Audio Player [Mock]",
                executablePath = "C:\\Program Files\\foobar2000\\foobar2000.exe",
                installedDate = System.currentTimeMillis() - 86400000L * 10,
                architecture = "x86 (32-bit Intel/AMD)",
                sizeBytes = 8_200_000L,
                sha256 = "88d4266fd4e6338d13b845fcf289579d209c897823b9217da3e161936f031589",
                isMock = true
            )
        )
    }

    override suspend fun detectInstallation(): Boolean = true

    override suspend fun initialize(): Result<RuntimeStatus> = withContext(Dispatchers.Default) {
        _state.value = RuntimeState.Initializing(0.2f, "[MOCK] Initializing simulated Wine prefix...")
        delay(300)
        _state.value = RuntimeState.Initializing(0.6f, "[MOCK] Loading virtual x86/x64 instruction emulator...")
        delay(300)
        _state.value = RuntimeState.Initializing(0.9f, "[MOCK] Readying simulated DirectX/Vulkan pipeline...")
        delay(200)

        val status = getStatus()
        _state.value = RuntimeState.Ready(status)
        Result.success(status)
    }

    override suspend fun getStatus(): RuntimeStatus = RuntimeStatus(
        engineName = runtimeName,
        version = "Simulator v1.0.0 [Mock]",
        isAvailable = true,
        isMock = true,
        architecture = "Simulated x86_64 on ARM64",
        wineInstalled = true,
        box64Installed = true,
        vulkanAvailable = true,
        missingComponents = emptyList(),
        storageUsedBytes = 16_450_000L,
        activeProcessesCount = if (runningAppId != null) 1 else 0,
        bottlePrefixPath = "Virtual://NexOS/SimulatedPrefix/drive_c"
    )

    override suspend fun listInstalledApplications(): List<InstalledApp> = installedApps.toList()

    override suspend fun importInstaller(sourceFile: File, destinationName: String?): Result<PeMetadata> = withContext(Dispatchers.Default) {
        delay(400)
        val name = destinationName ?: sourceFile.name
        val meta = PeMetadata(
            isValidPe = true,
            machineType = PeMetadata.IMAGE_FILE_MACHINE_AMD64,
            machineName = "x64 (64-bit AMD/Intel)",
            subsystem = PeMetadata.IMAGE_SUBSYSTEM_WINDOWS_GUI,
            subsystemName = "Windows GUI [Simulated]",
            is64Bit = true,
            sizeBytes = if (sourceFile.exists()) sourceFile.length() else 12_500_000L,
            sha256Checksum = "simulated_sha256_" + UUID.randomUUID().toString().replace("-", "")
        )
        Result.success(meta)
    }

    override suspend fun installApplication(installerPath: String, appName: String): Result<InstalledApp> = withContext(Dispatchers.Default) {
        _state.value = RuntimeState.Initializing(0.3f, "[MOCK] Unpacking simulated installer: $appName...")
        delay(500)
        _state.value = RuntimeState.Initializing(0.7f, "[MOCK] Writing simulated registry entries...")
        delay(400)

        val app = InstalledApp(
            id = "mock_" + UUID.randomUUID().toString().take(8),
            name = "$appName [Mock]",
            executablePath = "C:\\Program Files\\$appName\\$appName.exe",
            installedDate = System.currentTimeMillis(),
            architecture = "x64 (64-bit AMD/Intel)",
            sizeBytes = 15_400_000L,
            sha256 = "mock_hash_" + System.currentTimeMillis(),
            isMock = true
        )
        installedApps.add(0, app)
        _state.value = RuntimeState.Ready(getStatus())
        Result.success(app)
    }

    override suspend fun launchApplication(appId: String): Result<Int> = withContext(Dispatchers.Default) {
        val app = installedApps.find { it.id == appId }
            ?: return@withContext Result.failure(IllegalArgumentException("Application not found: $appId"))

        runningAppId = appId
        val fakePid = 1000 + (Math.random() * 8000).toInt()
        _state.value = RuntimeState.Running(app, fakePid)
        Result.success(fakePid)
    }

    override suspend fun stopApplication(appId: String): Result<Unit> = withContext(Dispatchers.Default) {
        if (runningAppId == appId) {
            runningAppId = null
            _state.value = RuntimeState.Stopped
        }
        Result.success(Unit)
    }

    override suspend fun deleteApplication(appId: String): Result<Unit> = withContext(Dispatchers.Default) {
        installedApps.removeAll { it.id == appId }
        if (runningAppId == appId) {
            runningAppId = null
            _state.value = RuntimeState.Stopped
        }
        Result.success(Unit)
    }

    override fun getLastError(): String? = lastErrorMessage
}
