package dev.nexos.domain.model

import android.net.Uri

data class NexFile(
    val id: String,
    val name: String,
    val path: String,
    val uri: Uri? = null,
    val isDirectory: Boolean = false,
    val sizeBytes: Long = 0L,
    val lastModified: Long = System.currentTimeMillis(),
    val extension: String = name.substringAfterLast('.', "").lowercase(),
    val mimeType: String = ""
) {
    val isWindowsExe: Boolean
        get() = extension in listOf("exe", "msi", "bat", "cmd")

    val isTextDocument: Boolean
        get() = extension in listOf("txt", "log", "ini", "md", "json", "xml", "cfg", "conf", "csv", "kt", "java")

    val isImage: Boolean
        get() = extension in listOf("png", "jpg", "jpeg", "webp", "gif", "bmp")

    fun formattedSize(): String {
        if (isDirectory) return "--"
        val kb = sizeBytes / 1024.0
        val mb = kb / 1024.0
        val gb = mb / 1024.0
        return when {
            gb >= 1.0 -> String.format("%.2f GB", gb)
            mb >= 1.0 -> String.format("%.2f MB", mb)
            kb >= 1.0 -> String.format("%.1f KB", kb)
            else -> "$sizeBytes B"
        }
    }
}
