package dev.nexos.apps.systeminfo

import android.app.ActivityManager
import android.content.Context
import android.os.Build
import android.os.Environment
import android.os.StatFs
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dev.nexos.domain.model.SystemSpecs
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.File

class SystemInfoViewModel(
    private val context: Context
) : ViewModel() {

    private val _specs = MutableStateFlow<SystemSpecs?>(null)
    val specs: StateFlow<SystemSpecs?> = _specs.asStateFlow()

    init {
        loadSpecs()
    }

    fun loadSpecs() {
        viewModelScope.launch {
            val systemSpecs = gatherSystemSpecs()
            _specs.value = systemSpecs
        }
    }

    private suspend fun gatherSystemSpecs(): SystemSpecs = withContext(Dispatchers.IO) {
        val actManager = context.getSystemService(Context.ACTIVITY_SERVICE) as ActivityManager
        val memInfo = ActivityManager.MemoryInfo()
        actManager.getMemoryInfo(memInfo)

        val statFs = StatFs(Environment.getDataDirectory().path)
        val blockSize = statFs.blockSizeLong
        val totalBlocks = statFs.blockCountLong
        val availableBlocks = statFs.availableBlocksLong

        val totalStorageBytes = totalBlocks * blockSize
        val freeStorageBytes = availableBlocks * blockSize

        val appDataBytes = calculateDirSize(context.filesDir)

        val is64Bit = Build.SUPPORTED_64_BIT_ABIS.isNotEmpty()
        val hasVulkan = File("/system/lib64/libvulkan.so").exists() || File("/system/lib/libvulkan.so").exists()

        SystemSpecs(
            osName = "Android",
            androidVersion = Build.VERSION.RELEASE,
            apiLevel = Build.VERSION.SDK_INT,
            securityPatch = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) Build.VERSION.SECURITY_PATCH else "N/A",
            deviceManufacturer = Build.MANUFACTURER.replaceFirstChar { it.uppercase() },
            deviceModel = Build.MODEL,
            hardwareBoard = Build.BOARD,
            cpuAbi = Build.CPU_ABI,
            supportedAbis = Build.SUPPORTED_ABIS.toList(),
            cpuCores = Runtime.getRuntime().availableProcessors(),
            totalRamBytes = memInfo.totalMem,
            availableRamBytes = memInfo.availMem,
            internalStorageTotalBytes = totalStorageBytes,
            internalStorageFreeBytes = freeStorageBytes,
            appDataUsedBytes = appDataBytes,
            is64Bit = is64Bit,
            hasVulkan = hasVulkan,
            nexOsVersion = "1.0.0 (Fluent Build 2026.10)"
        )
    }

    private fun calculateDirSize(dir: File): Long {
        if (!dir.exists()) return 0L
        return dir.walkTopDown().filter { it.isFile }.map { it.length() }.sum()
    }
}
