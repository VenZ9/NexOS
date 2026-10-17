package dev.nexos.runtime

data class PeMetadata(
    val isValidPe: Boolean,
    val machineType: Int = 0,
    val machineName: String = "Unknown",
    val subsystem: Int = 0,
    val subsystemName: String = "Unknown",
    val is64Bit: Boolean = false,
    val sizeBytes: Long = 0L,
    val sha256Checksum: String = "",
    val errorMessage: String? = null
) {
    val isExecutable: Boolean
        get() = isValidPe && errorMessage == null

    val isArmCompatible: Boolean
        get() = machineType == IMAGE_FILE_MACHINE_ARM64 || machineType == IMAGE_FILE_MACHINE_ARMNT

    val requiresTranslation: Boolean
        get() = machineType == IMAGE_FILE_MACHINE_I386 || machineType == IMAGE_FILE_MACHINE_AMD64

    companion object {
        const val IMAGE_FILE_MACHINE_I386 = 0x014c
        const val IMAGE_FILE_MACHINE_AMD64 = 0x8664
        const val IMAGE_FILE_MACHINE_ARMNT = 0x01c4
        const val IMAGE_FILE_MACHINE_ARM64 = 0xaa64

        const val IMAGE_SUBSYSTEM_WINDOWS_GUI = 2
        const val IMAGE_SUBSYSTEM_WINDOWS_CUI = 3
    }
}
