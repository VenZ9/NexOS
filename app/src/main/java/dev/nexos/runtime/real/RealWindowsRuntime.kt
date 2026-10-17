package dev.nexos.runtime.real

import android.content.Context
import android.os.Build
import dev.nexos.runtime.InstalledApp
import dev.nexos.runtime.PeFileInspector
import dev.nexos.runtime.PeMetadata
import dev.nexos.runtime.RuntimeState
import dev.nexos.runtime.RuntimeStatus
import dev.nexos.runtime.WindowsRuntime
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.withContext
import java.io.File
import java.io.FileOutputStream
import java.util.UUID

class RealWindowsRuntime(private val context: Context) : WindowsRuntime {

    override val runtimeName: String = "Wine 9.x / Box64 Runtime"
    override val isMock: Boolean = false

    private val _state = MutableStateFlow<RuntimeState>(RuntimeState.NotInstalled)
    override val state: StateFlow<RuntimeState> = _state.asStateFlow()

    private var lastErrorMessage: String? = null
    private val activeProcesses = mutableMapOf<String, Process>()

    // Isolated runtime directories inside app-private internal storage
    val runtimeRootDir: File by lazy { File(context.filesDir, "runtime") }
    val installersDir: File by lazy { File(runtimeRootDir, "installers") }
    val winePrefixDir: File by lazy { File(runtimeRootDir, "prefixes/default") }
    val driveCDir: File by lazy { File(winePrefixDir, "drive_c") }
    val programFilesDir: File by lazy { File(driveCDir, "Program Files") }
    val wineBinDir: File by lazy { File(runtimeRootDir, "wine/bin") }
    val box64BinDir: File by lazy { File(runtimeRootDir, "box64") }

    init {
        ensureDirectoriesExist()
    }

    private fun ensureDirectoriesExist() {
        installersDir.mkdirs()
        programFilesDir.mkdirs()
        wineBinDir.mkdirs()
        box64BinDir.mkdirs()
    }

    override suspend fun detectInstallation(): Boolean = withContext(Dispatchers.IO) {
        val wineBinary = File(wineBinDir, "wine")
        val box64Binary = File(box64BinDir, "box64")
        wineBinary.exists() && wineBinary.canExecute() && box64Binary.exists() && box64Binary.canExecute()
    }

    override suspend fun initialize(): Result<RuntimeStatus> = withContext(Dispatchers.IO) {
        _state.value = RuntimeState.Initializing(0.1f, "Probing host architecture and native drivers...")
        val status = getStatus()

        if (!status.isAvailable) {
            val missingSummary = status.missingComponents.joinToString(", ")
            lastErrorMessage = "Missing required components: $missingSummary"
            _state.value = RuntimeState.Error(
                title = "Wine/Box64 Runtime Not Configured",
                message = "The real Windows runtime environment requires native Wine and Box64 container packages.",
                details = "Missing components: $missingSummary.\nInstalled location: ${runtimeRootDir.absolutePath}\n\nNote: You can switch to the Mock Simulator in Settings to test all desktop workflows while real container packages are deployed."
            )
            return@withContext Result.failure(IllegalStateException(lastErrorMessage))
        }

        _state.value = RuntimeState.Initializing(0.7f, "Initializing Wine prefix container...")
        ensureDirectoriesExist()

        _state.value = RuntimeState.Ready(status)
        Result.success(status)
    }

    override suspend fun getStatus(): RuntimeStatus = withContext(Dispatchers.IO) {
        val isArm64 = Build.SUPPORTED_ABIS.any { it.contains("arm64") }
        val wineBinary = File(wineBinDir, "wine")
        val box64Binary = File(box64BinDir, "box64")

        val wineInstalled = wineBinary.exists() && wineBinary.canExecute()
        val box64Installed = box64Binary.exists() && box64Binary.canExecute()

        // Check Vulkan availability via system library probe
        val vulkanLib = File("/system/lib64/libvulkan.so")
        val hasVulkan = vulkanLib.exists() || File("/vendor/lib64/hw/vulkan.adreno.so").exists() || File("/system/lib/libvulkan.so").exists()

        val missing = mutableListOf<String>()
        if (!isArm64) {
            missing.add("ARM64 (aarch64) Host CPU Required")
        }
        if (!wineInstalled) {
            missing.add("Wine 9.x glibc container binaries")
        }
        if (!box64Installed) {
            missing.add("Box64 dynamic binary translator")
        }

        val totalUsedBytes = calculateStorage(runtimeRootDir)

        RuntimeStatus(
            engineName = runtimeName,
            version = "Wine 9.17 / Box64 0.2.8",
            isAvailable = wineInstalled && box64Installed && isArm64,
            isMock = false,
            architecture = if (isArm64) "aarch64 (ARM64)" else Build.CPU_ABI,
            wineInstalled = wineInstalled,
            box64Installed = box64Installed,
            vulkanAvailable = hasVulkan,
            missingComponents = missing,
            storageUsedBytes = totalUsedBytes,
            activeProcessesCount = activeProcesses.size,
            bottlePrefixPath = winePrefixDir.absolutePath
        )
    }

    override suspend fun listInstalledApplications(): List<InstalledApp> = withContext(Dispatchers.IO) {
        val apps = mutableListOf<InstalledApp>()

        // 1. Scan installers directory
        installersDir.listFiles()?.filter { it.isFile && (it.extension.equals("exe", true) || it.extension.equals("msi", true)) }?.forEach { file ->
            val pe = PeFileInspector.inspect(file)
            apps.add(
                InstalledApp(
                    id = "installer_${file.name}",
                    name = file.nameWithoutExtension.replace('_', ' ').replace('-', ' ').capitalizeWords(),
                    executablePath = file.absolutePath,
                    installedDate = file.lastModified(),
                    architecture = if (pe.isValidPe) pe.machineName else "Unknown PE",
                    sizeBytes = file.length(),
                    sha256 = pe.sha256Checksum,
                    isMock = false,
                    commandLineArgs = ""
                )
            )
        }

        // 2. Scan virtual C:\Program Files directory
        programFilesDir.listFiles()?.filter { it.isDirectory }?.forEach { appFolder ->
            val exes = appFolder.walkTopDown().filter { it.isFile && it.extension.equals("exe", true) }.take(3).toList()
            for (exe in exes) {
                val pe = PeFileInspector.inspect(exe)
                apps.add(
                    InstalledApp(
                        id = "prog_${appFolder.name}_${exe.name}",
                        name = "${appFolder.name} - ${exe.nameWithoutExtension}",
                        executablePath = exe.absolutePath,
                        installedDate = exe.lastModified(),
                        architecture = if (pe.isValidPe) pe.machineName else "x86_64",
                        sizeBytes = exe.length(),
                        sha256 = pe.sha256Checksum,
                        isMock = false,
                        commandLineArgs = ""
                    )
                )
            }
        }

        apps
    }

    override suspend fun importInstaller(sourceFile: File, destinationName: String?): Result<PeMetadata> = withContext(Dispatchers.IO) {
        if (!sourceFile.exists()) {
            return@withContext Result.failure(IllegalArgumentException("Source file does not exist."))
        }

        val targetName = (destinationName ?: sourceFile.name).sanitizeFileName()
        val destFile = File(installersDir, targetName)

        try {
            sourceFile.inputStream().use { input ->
                FileOutputStream(destFile).use { output ->
                    input.copyTo(output)
                }
            }

            val metadata = PeFileInspector.inspect(destFile)
            if (!metadata.isValidPe) {
                // If not valid PE, keep file but report honest inspection warning
                lastErrorMessage = metadata.errorMessage ?: "Not a valid Windows PE binary."
            }
            Result.success(metadata)
        } catch (e: Exception) {
            lastErrorMessage = "Import failed: ${e.message}"
            Result.failure(e)
        }
    }

    override suspend fun installApplication(installerPath: String, appName: String): Result<InstalledApp> = withContext(Dispatchers.IO) {
        val file = File(installerPath)
        if (!file.exists()) {
            return@withContext Result.failure(IllegalArgumentException("Installer file not found at: $installerPath"))
        }

        val status = getStatus()
        if (!status.canLaunchRealWindowsBinaries) {
            val reason = "Cannot execute installer: Wine/Box64 native components are not installed. (${status.missingComponents.joinToString()})"
            lastErrorMessage = reason
            _state.value = RuntimeState.Error(
                title = "Execution Blocked",
                message = reason,
                details = "Target: ${file.name}\nSize: ${file.length()} bytes\nSwitch to the Mock Simulator in Settings to test installer flows."
            )
            return@withContext Result.failure(IllegalStateException(reason))
        }

        // Real container execution would run: box64 wine <installerPath>
        // When real rootfs is present, we spawn the container process safely without shell injection
        Result.failure(UnsupportedOperationException("Container rootfs bootstrap in progress."))
    }

    override suspend fun launchApplication(appId: String): Result<Int> = withContext(Dispatchers.IO) {
        val apps = listInstalledApplications()
        val targetApp = apps.find { it.id == appId }
            ?: return@withContext Result.failure(IllegalArgumentException("Application not found: $appId"))

        val status = getStatus()
        if (!status.canLaunchRealWindowsBinaries) {
            val err = "Real execution unavailable: Wine 9.x container is not installed. (${status.missingComponents.joinToString()})"
            lastErrorMessage = err
            _state.value = RuntimeState.Error(
                title = "Cannot Launch Windows Binary",
                message = err,
                details = "Application: ${targetApp.name}\nPath: ${targetApp.executablePath}\nArch: ${targetApp.architecture}\n\nNexOS will not fake successful execution. Use Settings to toggle Mock Simulator for UI testing."
            )
            return@withContext Result.failure(IllegalStateException(err))
        }

        Result.failure(UnsupportedOperationException("Container runtime pending initial rootfs download."))
    }

    override suspend fun stopApplication(appId: String): Result<Unit> = withContext(Dispatchers.IO) {
        val process = activeProcesses.remove(appId)
        if (process != null) {
            try {
                process.destroy()
                _state.value = RuntimeState.Stopped
                Result.success(Unit)
            } catch (e: Exception) {
                Result.failure(e)
            }
        } else {
            Result.success(Unit)
        }
    }

    override suspend fun deleteApplication(appId: String): Result<Unit> = withContext(Dispatchers.IO) {
        val apps = listInstalledApplications()
        val targetApp = apps.find { it.id == appId }
            ?: return@withContext Result.failure(IllegalArgumentException("Application not found: $appId"))

        val file = File(targetApp.executablePath)
        if (file.exists()) {
            file.delete()
        }
        Result.success(Unit)
    }

    override fun getLastError(): String? = lastErrorMessage

    private fun calculateStorage(dir: File): Long {
        if (!dir.exists()) return 0L
        return dir.walkTopDown().filter { it.isFile }.map { it.length() }.sum()
    }

    private fun String.sanitizeFileName(): String =
        this.replace("[^a-zA-Z0-9._-]".toRegex(), "_")

    private fun String.capitalizeWords(): String =
        split(" ").joinToString(" ") { it.replaceFirstChar { char -> char.uppercase() } }
}
