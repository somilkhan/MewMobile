package com.nuvio.app.features.cloudstream

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertFailsWith
import kotlin.test.assertTrue

class CloudStreamPackageInspectorTest {
    @Test
    fun inspectsSafeCs3CentralDirectory() {
        val archive = fakeZip("manifest.json", "classes.dex")
        val inspection = CloudStreamPackageInspector.inspect(archive)
        assertTrue(inspection.containsManifest)
        assertTrue(inspection.containsDex)
        assertEquals(2, inspection.entries.size)
    }

    @Test
    fun rejectsPackageAboveConfiguredDownloadLimit() {
        assertFailsWith<IllegalArgumentException> {
            CloudStreamPackageInspector.validateDownloadSize(size = 11, maxBytes = 10)
        }
    }

    @Test
    fun acceptsPackageAtConfiguredDownloadLimit() {
        CloudStreamPackageInspector.validateDownloadSize(size = 10, maxBytes = 10)
    }

    @Test
    fun acceptsAValidDexHeader() {
        val header = byteArrayOf(
            'd'.code.toByte(), 'e'.code.toByte(), 'x'.code.toByte(), '\n'.code.toByte(),
            '0'.code.toByte(), '3'.code.toByte(), '5'.code.toByte(), 0,
        )
        assertTrue(CloudStreamPackageInspector.hasValidDexMagic(header))
    }

    @Test
    fun rejectsInvalidOrTruncatedDexHeaders() {
        assertFalse(CloudStreamPackageInspector.hasValidDexMagic("not-dex!".encodeToByteArray()))
        assertFalse(CloudStreamPackageInspector.hasValidDexMagic(byteArrayOf(1, 2, 3)))
    }

    @Test
    fun rejectsPathTraversal() {
        val archive = fakeZip("manifest.json", "classes.dex", "../escape.txt")
        assertFailsWith<IllegalArgumentException> {
            CloudStreamPackageInspector.inspect(archive)
        }
    }

    @Test
    fun rejectsPackageWithoutDex() {
        assertFailsWith<IllegalArgumentException> {
            CloudStreamPackageInspector.inspect(fakeZip("manifest.json"))
        }
    }

    private fun fakeZip(vararg names: String): ByteArray {
        val centralEntries = names.map { name ->
            byteArrayOf(0x50, 0x4b, 0x01, 0x02) +
                ByteArray(16) +
                intLe(1) +
                intLe(1) +
                shortLe(name.encodeToByteArray().size) +
                shortLe(0) +
                shortLe(0) +
                ByteArray(12) +
                name.encodeToByteArray()
        }
        val central = centralEntries.fold(ByteArray(0)) { acc, entry -> acc + entry }
        val end = byteArrayOf(0x50, 0x4b, 0x05, 0x06) +
            shortLe(0) + shortLe(0) +
            shortLe(names.size) + shortLe(names.size) +
            intLe(central.size) + intLe(0) + shortLe(0)
        return central + end
    }

    private fun shortLe(value: Int): ByteArray = byteArrayOf(value.toByte(), (value ushr 8).toByte())

    private fun intLe(value: Int): ByteArray = byteArrayOf(
        value.toByte(),
        (value ushr 8).toByte(),
        (value ushr 16).toByte(),
        (value ushr 24).toByte(),
    )
}

