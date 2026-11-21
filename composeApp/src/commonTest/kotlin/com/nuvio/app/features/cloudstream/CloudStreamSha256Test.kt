package com.nuvio.app.features.cloudstream

import kotlin.test.Test
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class CloudStreamSha256Test {
    @Test
    fun packageWithoutExpectedHashIsNotVerified() {
        assertFalse(isCloudStreamPackageVerified(null, "package".encodeToByteArray()))
    }

    @Test
    fun matchingSha256HashVerifiesPackage() {
        val bytes = "mew-cloudstream".encodeToByteArray()
        val hash = CloudStreamFileHash.parse("sha256-${sha256Hex(bytes)}")

        assertTrue(isCloudStreamPackageVerified(hash, bytes))
    }

    @Test
    fun mismatchedSha256HashDoesNotVerifyPackage() {
        val bytes = "mew-cloudstream".encodeToByteArray()
        val hash = CloudStreamFileHash.parse("sha256-${"0".repeat(64)}")

        assertFalse(isCloudStreamPackageVerified(hash, bytes))
    }
}
