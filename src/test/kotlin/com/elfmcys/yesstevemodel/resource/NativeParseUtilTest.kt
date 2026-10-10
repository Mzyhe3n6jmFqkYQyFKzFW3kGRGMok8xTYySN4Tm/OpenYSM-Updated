package com.elfmcys.yesstevemodel.resource

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.runBlocking
import kotlin.test.Test
import kotlin.test.assertNull

class NativeParseUtilTest {
    @Test
    fun testParseNativeWithNullOrEmpty() {
        assertNull(NativeParseUtil.parseNative(null, "test_null"))
        assertNull(NativeParseUtil.parseNative(ByteArray(0), "test_empty"))
    }

    @Test
    fun testParseNativeWithCorruptDataReturnsNullGracefully() {
        val corruptData = byteArrayOf(0x01, 0x02, 0x03, 0x04, 0x05)
        val result = NativeParseUtil.parseNative(corruptData, "test_corrupt")
        assertNull(result)
    }

    @Test
    fun testParseNativeConcurrentSafety() = runBlocking {
        (1..10).map { i ->
            async(Dispatchers.Default) {
                val dummyData = "corrupt_data_$i".toByteArray()
                val result = NativeParseUtil.parseNative(dummyData, "test_concurrent_$i")
                assertNull(result)
            }
        }.awaitAll()
    }
}
