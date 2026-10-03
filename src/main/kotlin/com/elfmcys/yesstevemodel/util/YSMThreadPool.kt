package com.elfmcys.yesstevemodel.util

import java.util.concurrent.Callable
import java.util.concurrent.Future
import java.util.concurrent.LinkedBlockingQueue
import java.util.concurrent.ThreadPoolExecutor
import java.util.concurrent.TimeUnit
import kotlin.math.max

object YSMThreadPool {
    private val EXECUTOR: ThreadPoolExecutor = ThreadPoolExecutor(
        max(2, Runtime.getRuntime().availableProcessors() / 2),
        max(2, Runtime.getRuntime().availableProcessors() / 2),
        30L,
        TimeUnit.SECONDS,
        LinkedBlockingQueue()
    ) { runnable ->
        Thread(runnable, "YSM Worker").apply {
            priority = 5
            isDaemon = true
        }
    }

    private val SYNC_EXECUTOR: ThreadPoolExecutor = ThreadPoolExecutor(
        2,
        4,
        30L,
        TimeUnit.SECONDS,
        LinkedBlockingQueue()
    ) { runnable ->
        Thread(runnable, "YSM Sync").apply {
            priority = 7
            isDaemon = true
        }
    }

    @JvmStatic
    fun submit(runnable: Runnable): Future<*> {
        return EXECUTOR.submit(runnable)
    }

    @JvmStatic
    fun <T> submitCallable(callable: Callable<T>): Future<T> {
        return EXECUTOR.submit(callable)
    }

    @JvmStatic
    fun submitSync(runnable: Runnable): Future<*> {
        return SYNC_EXECUTOR.submit(runnable)
    }

    @JvmStatic
    fun awaitTermination(i: Long): Boolean {
        return runCatching {
            Thread.sleep(i)
            true
        }.getOrDefault(false)
    }

    @JvmStatic
    fun awaitTermination(i: Int): Boolean {
        return awaitTermination(i.toLong())
    }
}