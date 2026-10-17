package dev.nexos.domain.model

data class SystemSpecs(
    val osName: String,
    val androidVersion: String,
    val apiLevel: Int,
    val securityPatch: String,
    val deviceManufacturer: String,
    val deviceModel: String,
    val hardwareBoard: String,
    val cpuAbi: String,
    val supportedAbis: List<String>,
    val cpuCores: Int,
    val totalRamBytes: Long,
    val availableRamBytes: Long,
    val internalStorageTotalBytes: Long,
    val internalStorageFreeBytes: Long,
    val appDataUsedBytes: Long,
    val is64Bit: Boolean,
    val hasVulkan: Boolean,
    val nexOsVersion: String
) {
    fun formattedTotalRam(): String = formatBytes(totalRamBytes)
    fun formattedAvailableRam(): String = formatBytes(availableRamBytes)
    fun formattedStorageTotal(): String = formatBytes(internalStorageTotalBytes)
    fun formattedStorageFree(): String = formatBytes(internalStorageFreeBytes)
    fun formattedAppDataUsed(): String = formatBytes(appDataUsedBytes)

    companion object {
        fun formatBytes(bytes: Long): String {
            if (bytes <= 0) return "0 MB"
            val gb = bytes / (1024.0 * 1024.0 * 1024.0)
            val mb = bytes / (1024.0 * 1024.0)
            return if (gb >= 1.0) {
                String.format("%.2f GB", gb)
            } else {
                String.format("%.1f MB", mb)
            }
        }
    }
}
