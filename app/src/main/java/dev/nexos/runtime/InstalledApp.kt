package dev.nexos.runtime

data class InstalledApp(
    val id: String,
    val name: String,
    val executablePath: String,
    val iconPath: String? = null,
    val installedDate: Long = System.currentTimeMillis(),
    val architecture: String = "x86_64",
    val sizeBytes: Long = 0L,
    val sha256: String = "",
    val isMock: Boolean = false,
    val commandLineArgs: String = ""
) {
    fun formattedSize(): String {
        val mb = sizeBytes / (1024.0 * 1024.0)
        return if (mb >= 1.0) {
            String.format("%.1f MB", mb)
        } else {
            String.format("%d KB", sizeBytes / 1024)
        }
    }
}
