package com.elfmcys.yesstevemodel.util

import kotlinx.coroutines.runBlocking
import java.util.concurrent.atomic.AtomicInteger
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class YSMThreadPoolTest {

    @Test
    fun testLaunchAndAsync() = runBlocking {
        val counter = AtomicInteger(0)
        val job = YSMThreadPool.launch {
            counter.incrementAndGet()
        }
        job.join()
        assertEquals(1, counter.get())

        val deferred = YSMThreadPool.async {
            40 + 2
        }
        val result = deferred.await()
        assertEquals(42, result)
    }

    @Test
    fun testIOOperations() = runBlocking {
        val counter = AtomicInteger(0)
        val job = YSMThreadPool.launchIO {
            counter.addAndGet(10)
        }
        job.join()
        assertEquals(10, counter.get())

        val deferred = YSMThreadPool.asyncIO {
            "io-test-result"
        }
        assertEquals("io-test-result", deferred.await())
    }

    @Test
    fun testSyncOperations() = runBlocking {
        val counter = AtomicInteger(0)
        val job = YSMThreadPool.launchSync {
            counter.addAndGet(100)
        }
        job.join()
        assertEquals(100, counter.get())

        val deferred = YSMThreadPool.asyncSync {
            "sync-test-result"
        }
        assertEquals("sync-test-result", deferred.await())
    }

    @Test
    fun testAsyncExceptionHandling() = runBlocking {
        val deferred = YSMThreadPool.async {
            throw IllegalStateException("Test exception")
        }
        val result = runCatching {
            deferred.await()
        }
        assertTrue(result.isFailure)
        assertTrue(result.exceptionOrNull() is IllegalStateException)
    }
}
