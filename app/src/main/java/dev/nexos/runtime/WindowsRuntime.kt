package dev.nexos.runtime

import kotlinx.coroutines.flow.StateFlow
import java.io.File

interface WindowsRuntime {
    val runtimeName: String
    val isMock: Boolean

    val state: StateFlow<RuntimeState>

    suspend fun detectInstallation(): Boolean

    suspend fun initialize(): Result<RuntimeStatus>

    suspend fun getStatus(): RuntimeStatus

    suspend fun listInstalledApplications(): List<InstalledApp>

    suspend fun importInstaller(sourceFile: File, destinationName: String? = null): Result<PeMetadata>

    suspend fun installApplication(installerPath: String, appName: String): Result<InstalledApp>

    suspend fun launchApplication(appId: String): Result<Int>

    suspend fun stopApplication(appId: String): Result<Unit>

    suspend fun deleteApplication(appId: String): Result<Unit>

    fun getLastError(): String?
}
