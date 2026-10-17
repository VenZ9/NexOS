package dev.nexos.data.repository

import android.content.Context
import android.net.Uri
import android.provider.OpenableColumns
import dev.nexos.domain.model.NexFile
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File
import java.io.FileOutputStream
import java.util.UUID

class FileRepository(private val context: Context) {

    val nexOsDriveDir: File by lazy { File(context.filesDir, "NexOS_Drive") }
    val virtualCDir: File by lazy { File(nexOsDriveDir, "C") }
    val documentsDir: File by lazy { File(nexOsDriveDir, "Documents") }
    val downloadsDir: File by lazy { File(nexOsDriveDir, "Downloads") }
    val installersDir: File by lazy { File(nexOsDriveDir, "Installers") }

    init {
        initializeVirtualFileSystem()
    }

    private fun initializeVirtualFileSystem() {
        virtualCDir.mkdirs()
        documentsDir.mkdirs()
        downloadsDir.mkdirs()
        installersDir.mkdirs()

        // Create initial starter documents if not already present
        val welcomeFile = File(documentsDir, "Welcome to NexOS.txt")
        if (!welcomeFile.exists()) {
            welcomeFile.writeText(
                """
                Welcome to NexOS for Android!
                =============================
                
                NexOS brings a refined Windows-inspired desktop environment to Android devices.
                
                Features:
                - Multi-window desktop manager with drag, resize, minimize, maximize, and restore.
                - Taskbar with Start menu, quick search, system tray, and live clock.
                - Built-in apps: File Manager, Text Editor, App Library, Settings, System Information, and Runtime Manager.
                - Real Storage Access Framework (SAF) integration for secure external file access.
                - Isolated Windows .exe compatibility layer architecture powered by Wine and Box64.
                
                Getting Started:
                - Open File Manager to browse virtual C: drive or pick an external folder via SAF.
                - Import Windows installers (.exe / .msi) into isolated storage via the "Import .exe" button.
                - Configure wallpaper and taskbar preferences in Settings.
                
                Enjoy desktop computing on Android!
                """.trimIndent()
            )
        }

        val runtimeNoticeFile = File(documentsDir, "Windows Compatibility Notice.txt")
        if (!runtimeNoticeFile.exists()) {
            runtimeNoticeFile.writeText(
                """
                Windows .exe Compatibility Architecture
                ========================================
                
                Running x86 / x64 Windows applications on Android requires translating both
                CPU instructions (Box64/Box86 dynamic binary translation) and Windows system
                APIs (Wine compatibility layer).
                
                Security Notice:
                Untrusted Windows executables can contain malicious payloads. NexOS isolates
                all imported binaries inside private application storage and inspects Portable
                Executable (PE) headers, COFF machine architectures, and SHA-256 signatures before
                processing.
                
                For complete technical architecture and license audits, open Runtime Manager.
                """.trimIndent()
            )
        }
    }

    suspend fun listFiles(directory: File): List<NexFile> = withContext(Dispatchers.IO) {
        if (!directory.exists() || !directory.isDirectory) {
            return@withContext emptyList()
        }

        val files = directory.listFiles() ?: return@withContext emptyList()
        files.map { file ->
            NexFile(
                id = file.absolutePath,
                name = file.name,
                path = file.absolutePath,
                uri = null,
                isDirectory = file.isDirectory,
                sizeBytes = if (file.isDirectory) 0L else file.length(),
                lastModified = file.lastModified()
            )
        }.sortedWith(compareBy({ !it.isDirectory }, { it.name.lowercase() }))
    }

    suspend fun readTextFile(file: File): Result<String> = withContext(Dispatchers.IO) {
        try {
            if (!file.exists()) {
                return@withContext Result.failure(IllegalArgumentException("File does not exist: ${file.absolutePath}"))
            }
            Result.success(file.readText())
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun writeTextFile(file: File, content: String): Result<Unit> = withContext(Dispatchers.IO) {
        try {
            file.parentFile?.mkdirs()
            file.writeText(content)
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun createNewDocument(parentDir: File, name: String, content: String = ""): Result<NexFile> = withContext(Dispatchers.IO) {
        try {
            val validName = if (name.endsWith(".txt")) name else "$name.txt"
            val file = File(parentDir, validName)
            file.writeText(content)
            Result.success(
                NexFile(
                    id = file.absolutePath,
                    name = file.name,
                    path = file.absolutePath,
                    isDirectory = false,
                    sizeBytes = file.length(),
                    lastModified = file.lastModified()
                )
            )
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun createFolder(parentDir: File, folderName: String): Result<File> = withContext(Dispatchers.IO) {
        try {
            val dir = File(parentDir, folderName)
            if (dir.mkdirs() || dir.exists()) {
                Result.success(dir)
            } else {
                Result.failure(IllegalStateException("Could not create folder: $folderName"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun deleteFile(file: File): Result<Boolean> = withContext(Dispatchers.IO) {
        try {
            if (file.isDirectory) {
                Result.success(file.deleteRecursively())
            } else {
                Result.success(file.delete())
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun importFromUri(uri: Uri, targetFolder: File): Result<File> = withContext(Dispatchers.IO) {
        try {
            val displayName = getFileNameFromUri(uri) ?: "imported_file_${System.currentTimeMillis()}"
            val sanitized = displayName.replace("[^a-zA-Z0-9._-]".toRegex(), "_")
            val destFile = File(targetFolder, sanitized)

            context.contentResolver.openInputStream(uri)?.use { inputStream ->
                FileOutputStream(destFile).use { outputStream ->
                    inputStream.copyTo(outputStream)
                }
            } ?: return@withContext Result.failure(IllegalStateException("Failed to open input stream for URI: $uri"))

            Result.success(destFile)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    private fun getFileNameFromUri(uri: Uri): String? {
        var name: String? = null
        if (uri.scheme == "content") {
            val cursor = context.contentResolver.query(uri, arrayOf(OpenableColumns.DISPLAY_NAME), null, null, null)
            cursor?.use {
                if (it.moveToFirst()) {
                    name = it.getString(it.getColumnIndexOrThrow(OpenableColumns.DISPLAY_NAME))
                }
            }
        }
        if (name == null) {
            name = uri.path?.substringAfterLast('/')
        }
        return name
    }
}
