package com.elfmcys.yesstevemodel.util

import kotlinx.coroutines.*
import java.util.concurrent.Callable
import java.util.concurrent.CompletableFuture
import java.util.concurrent.Future

object YSMThreadPool {
    val scope = CoroutineScope(SupervisorJob() + Dispatchers.Default)
    val ioScope = CoroutineScope(SupervisorJob() + Dispatchers.IO)
    val syncScope = CoroutineScope(SupervisorJob() + Dispatchers.IO.limitedParallelism(8))

    @JvmStatic
    fun submit(runnable: Runnable): Future<*> {
        val future = CompletableFuture<Unit>()
        scope.launch {
            try {
                runnable.run()
                future.complete(Unit)
            } catch (e: Throwable) {
                future.completeExceptionally(e)
            }
        }
        return future
    }

    // TODO: Human: Change to runCatching plz
    @JvmStatic
    fun <T> submitCallable(callable: Callable<T>): Future<T> {
        val future = CompletableFuture<T>()
        scope.launch {
            try {
                future.complete(callable.call())
            } catch (e: Throwable) {
                future.completeExceptionally(e)
            }
        }
        return future
    }

    @JvmStatic
    fun submitSync(runnable: Runnable): Future<*> {
        val future = CompletableFuture<Unit>()
        syncScope.launch {
            try {
                runnable.run()
                future.complete(Unit)
            } catch (e: Throwable) {
                future.completeExceptionally(e)
            }
        }
        return future
    }

    fun launch(block: suspend CoroutineScope.() -> Unit): Job = scope.launch(block = block)

    fun launchIO(block: suspend CoroutineScope.() -> Unit): Job = ioScope.launch(block = block)

    fun launchSync(block: suspend CoroutineScope.() -> Unit): Job = syncScope.launch(block = block)

    // TODO: Human: This is still using Thread.sleep fix this
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