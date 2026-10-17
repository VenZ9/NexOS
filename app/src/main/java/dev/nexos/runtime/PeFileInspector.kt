package dev.nexos.runtime

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File
import java.io.FileInputStream
import java.io.InputStream
import java.nio.ByteBuffer
import java.nio.ByteOrder
import java.security.MessageDigest

object PeFileInspector {

    private const val MAX_FILE_SIZE_BYTES = 2L * 1024L * 1024L * 1024L // 2 GB limit

    suspend fun inspect(file: File): PeMetadata = withContext(Dispatchers.IO) {
        if (!file.exists() || !file.isFile) {
            return@withContext PeMetadata(
                isValidPe = false,
                errorMessage = "File does not exist or is not a regular file."
            )
        }

        val size = file.length()
        if (size > MAX_FILE_SIZE_BYTES) {
            return@withContext PeMetadata(
                isValidPe = false,
                sizeBytes = size,
                errorMessage = "File exceeds maximum supported size (2 GB limit)."
            )
        }

        if (size < 0x40) {
            return@withContext PeMetadata(
                isValidPe = false,
                sizeBytes = size,
                errorMessage = "File is too small to contain a valid DOS/PE header."
            )
        }

        try {
            val sha256 = computeSha256(file)

            FileInputStream(file).use { input ->
                inspectStream(input, size, sha256)
            }
        } catch (e: Exception) {
            PeMetadata(
                isValidPe = false,
                sizeBytes = size,
                errorMessage = "PE Inspection failed: ${e.message}"
            )
        }
    }

    suspend fun inspectStream(input: InputStream, totalSize: Long, sha256: String = ""): PeMetadata = withContext(Dispatchers.IO) {
        val headerBuffer = ByteArray(4096)
        val read = input.read(headerBuffer)
        if (read < 0x40) {
            return@withContext PeMetadata(
                isValidPe = false,
                sizeBytes = totalSize,
                sha256Checksum = sha256,
                errorMessage = "Stream contains insufficient data for PE header."
            )
        }

        // Check DOS Header MZ magic (0x4D, 0x5A)
        if (headerBuffer[0] != 'M'.code.toByte() || headerBuffer[1] != 'Z'.code.toByte()) {
            return@withContext PeMetadata(
                isValidPe = false,
                sizeBytes = totalSize,
                sha256Checksum = sha256,
                errorMessage = "Missing DOS signature (MZ). Not a valid Windows executable."
            )
        }

        // Offset to PE Header is at 0x3C (e_lfanew)
        val eLfanew = ByteBuffer.wrap(headerBuffer, 0x3C, 4).order(ByteOrder.LITTLE_ENDIAN).int
        if (eLfanew < 0x40 || eLfanew + 24 > read) {
            return@withContext PeMetadata(
                isValidPe = false,
                sizeBytes = totalSize,
                sha256Checksum = sha256,
                errorMessage = "Invalid PE header offset (e_lfanew: $eLfanew)."
            )
        }

        // Verify PE signature: 'P', 'E', 0, 0
        if (headerBuffer[eLfanew] != 'P'.code.toByte() ||
            headerBuffer[eLfanew + 1] != 'E'.code.toByte() ||
            headerBuffer[eLfanew + 2] != 0.toByte() ||
            headerBuffer[eLfanew + 3] != 0.toByte()
        ) {
            return@withContext PeMetadata(
                isValidPe = false,
                sizeBytes = totalSize,
                sha256Checksum = sha256,
                errorMessage = "Missing PE signature."
            )
        }

        // COFF Header starts at eLfanew + 4
        val coffOffset = eLfanew + 4
        val machine = ByteBuffer.wrap(headerBuffer, coffOffset, 2).order(ByteOrder.LITTLE_ENDIAN).short.toInt() and 0xFFFF
        val sizeOfOptionalHeader = ByteBuffer.wrap(headerBuffer, coffOffset + 16, 2).order(ByteOrder.LITTLE_ENDIAN).short.toInt() and 0xFFFF

        val machineName = when (machine) {
            PeMetadata.IMAGE_FILE_MACHINE_I386 -> "x86 (32-bit Intel/AMD)"
            PeMetadata.IMAGE_FILE_MACHINE_AMD64 -> "x64 (64-bit AMD/Intel)"
            PeMetadata.IMAGE_FILE_MACHINE_ARMNT -> "ARM (32-bit)"
            PeMetadata.IMAGE_FILE_MACHINE_ARM64 -> "ARM64 (64-bit)"
            else -> String.format("Unknown Machine (0x%04X)", machine)
        }

        var is64Bit = (machine == PeMetadata.IMAGE_FILE_MACHINE_AMD64 || machine == PeMetadata.IMAGE_FILE_MACHINE_ARM64)
        var subsystem = 0
        var subsystemName = "Unknown"

        // Optional Header
        if (sizeOfOptionalHeader > 0 && coffOffset + 20 + sizeOfOptionalHeader <= read) {
            val optOffset = coffOffset + 20
            val magic = ByteBuffer.wrap(headerBuffer, optOffset, 2).order(ByteOrder.LITTLE_ENDIAN).short.toInt() and 0xFFFF
            is64Bit = (magic == 0x020b) // PE32+

            // Subsystem offset in Optional Header: 68 bytes from start of optional header
            if (sizeOfOptionalHeader >= 70) {
                subsystem = ByteBuffer.wrap(headerBuffer, optOffset + 68, 2).order(ByteOrder.LITTLE_ENDIAN).short.toInt() and 0xFFFF
                subsystemName = when (subsystem) {
                    PeMetadata.IMAGE_SUBSYSTEM_WINDOWS_GUI -> "Windows GUI (Desktop Application)"
                    PeMetadata.IMAGE_SUBSYSTEM_WINDOWS_CUI -> "Windows CUI (Command Line Application)"
                    1 -> "Native Driver"
                    7 -> "POSIX CUI"
                    9 -> "Windows CE GUI"
                    10 -> "EFI Application"
                    else -> "Subsystem $subsystem"
                }
            }
        }

        PeMetadata(
            isValidPe = true,
            machineType = machine,
            machineName = machineName,
            subsystem = subsystem,
            subsystemName = subsystemName,
            is64Bit = is64Bit,
            sizeBytes = totalSize,
            sha256Checksum = sha256
        )
    }

    private fun computeSha256(file: File): String {
        val digest = MessageDigest.getInstance("SHA-256")
        FileInputStream(file).use { fis ->
            val buffer = ByteArray(8192)
            var bytesRead: Int
            while (fis.read(buffer).also { bytesRead = it } != -1) {
                digest.update(buffer, 0, bytesRead)
            }
        }
        val hash = digest.digest()
        val hexString = StringBuilder()
        for (b in hash) {
            val hex = Integer.toHexString(0xff and b.toInt())
            if (hex.length == 1) hexString.append('0')
            hexString.append(hex)
        }
        return hexString.toString()
    }
}
