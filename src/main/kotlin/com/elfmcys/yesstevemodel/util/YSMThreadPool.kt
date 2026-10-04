@file:Suppress("unused")

package com.elfmcys.yesstevemodel.util

import kotlinx.coroutines.*
import java.util.concurrent.Callable
import java.util.concurrent.CompletableFuture
import java.util.concurrent.Future

object YSMThreadPool {
    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.Default)
    private val ioScope = CoroutineScope(SupervisorJob() + Dispatchers.IO)
    private val syncScope = CoroutineScope(SupervisorJob() + Dispatchers.IO.limitedParallelism(8))

    @JvmStatic
    fun submit(runnable: Runnable): Future<*> {
        val future = CompletableFuture<Unit>()
        scope.launch {
            runCatching {
                runnable.run()
                future.complete(Unit)
            }.onFailure {
                future.completeExceptionally(it)
            }
        }
        return future
    }

    @JvmStatic
    fun <T> submitCallable(callable: Callable<T>): Future<T> {
        val future = CompletableFuture<T>()
        scope.launch {
            runCatching {
                future.complete(callable.call())
            }.onFailure {
                future.completeExceptionally(it)
            }
        }
        return future
    }

    @JvmStatic
    fun submitSync(runnable: Runnable): Future<*> {
        val future = CompletableFuture<Unit>()
        syncScope.launch {
            runCatching {
                runnable.run()
                future.complete(Unit)
            }.onFailure {
                future.completeExceptionally(it)
            }
        }
        return future
    }

    fun launch(block: suspend CoroutineScope.() -> Unit): Job = scope.launch(block = block)

    fun launchIO(block: suspend CoroutineScope.() -> Unit): Job = ioScope.launch(block = block)

    fun launchSync(block: suspend CoroutineScope.() -> Unit): Job = syncScope.launch(block = block)

    @JvmStatic
    fun awaitTermination(i: Long): Boolean {
        return runCatching {
            Thread.sleep(i)
            true
        }.getOrDefault(false)
    }

    @JvmStatic
    fun awaitTermination(i: Int): Boolean = awaitTermination(i.toLong())
}