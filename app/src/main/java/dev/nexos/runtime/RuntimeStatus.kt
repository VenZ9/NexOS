package dev.nexos.runtime

data class RuntimeStatus(
    val engineName: String,
    val version: String,
    val isAvailable: Boolean,
    val isMock: Boolean,
    val architecture: String,
    val wineInstalled: Boolean,
    val box64Installed: Boolean,
    val vulkanAvailable: Boolean,
    val missingComponents: List<String> = emptyList(),
    val storageUsedBytes: Long = 0L,
    val activeProcessesCount: Int = 0,
    val bottlePrefixPath: String = ""
) {
    val canLaunchRealWindowsBinaries: Boolean
        get() = !isMock && isAvailable && wineInstalled && box64Installed

    fun formattedStorage(): String {
        val mb = storageUsedBytes / (1024.0 * 1024.0)
        return if (mb >= 1024.0) {
            String.format("%.2f GB", mb / 1024.0)
        } else {
            String.format("%.1f MB", mb)
        }
    }
}
