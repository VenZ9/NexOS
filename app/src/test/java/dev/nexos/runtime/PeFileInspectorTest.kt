package dev.nexos.runtime

import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Test
import java.io.ByteArrayInputStream
import java.nio.ByteBuffer
import java.nio.ByteOrder

class PeFileInspectorTest {

    @Test
    fun testInspectStream_detectsValidPeHeader() = runBlocking {
        // Construct a minimal valid PE header buffer
        val buffer = ByteArray(512)
        // 1. DOS Header MZ
        buffer[0] = 'M'.code.toByte()
        buffer[1] = 'Z'.code.toByte()

        // 2. e_lfanew at 0x3C points to offset 0x80
        val eLfanew = 0x80
        ByteBuffer.wrap(buffer, 0x3C, 4).order(ByteOrder.LITTLE_ENDIAN).putInt(eLfanew)

        // 3. PE signature: 'P', 'E', 0, 0
        buffer[eLfanew] = 'P'.code.toByte()
        buffer[eLfanew + 1] = 'E'.code.toByte()
        buffer[eLfanew + 2] = 0
        buffer[eLfanew + 3] = 0

        // 4. COFF Header: Machine = AMD64 (0x8664) at eLfanew + 4
        val coffOffset = eLfanew + 4
        ByteBuffer.wrap(buffer, coffOffset, 2).order(ByteOrder.LITTLE_ENDIAN).putShort(PeMetadata.IMAGE_FILE_MACHINE_AMD64.toShort())

        // 5. Size of Optional Header = 240 bytes at coffOffset + 16
        ByteBuffer.wrap(buffer, coffOffset + 16, 2).order(ByteOrder.LITTLE_ENDIAN).putShort(240.toShort())

        // 6. Optional Header magic = 0x020b (PE32+) at coffOffset + 20
        val optOffset = coffOffset + 20
        ByteBuffer.wrap(buffer, optOffset, 2).order(ByteOrder.LITTLE_ENDIAN).putShort(0x020b.toShort())

        // 7. Subsystem = 2 (Windows GUI) at optOffset + 68
        ByteBuffer.wrap(buffer, optOffset + 68, 2).order(ByteOrder.LITTLE_ENDIAN).putShort(2.toShort())

        val inputStream = ByteArrayInputStream(buffer)
        val result = PeFileInspector.inspectStream(inputStream, totalSize = buffer.size.toLong())

        assertTrue(result.isValidPe)
        assertTrue(result.is64Bit)
        assertEquals(PeMetadata.IMAGE_FILE_MACHINE_AMD64, result.machineType)
        assertTrue(result.machineName.contains("64-bit"))
        assertEquals(2, result.subsystem)
        assertTrue(result.subsystemName.contains("Windows GUI"))
    }

    @Test
    fun testInspectStream_rejectsNonPeFile() = runBlocking {
        val nonPeText = "Hello, world! This is a simple text file that contains more than sixty-four bytes of ASCII text, so it passes the minimum size threshold and tests the MZ signature check."
        val nonPeBuffer = nonPeText.toByteArray()
        val inputStream = ByteArrayInputStream(nonPeBuffer)
        val result = PeFileInspector.inspectStream(inputStream, totalSize = nonPeBuffer.size.toLong())

        assertFalse(result.isValidPe)
        assertNotNull(result.errorMessage)
        assertTrue(result.errorMessage!!.contains("Missing DOS signature"))
    }

    @Test
    fun testInspectStream_rejectsTruncatedFile() = runBlocking {
        val shortBuffer = byteArrayOf('M'.code.toByte(), 'Z'.code.toByte(), 0x01, 0x02)
        val inputStream = ByteArrayInputStream(shortBuffer)
        val result = PeFileInspector.inspectStream(inputStream, totalSize = shortBuffer.size.toLong())

        assertFalse(result.isValidPe)
        assertNotNull(result.errorMessage)
    }
}
