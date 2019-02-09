package org.watsi.device.testutils

import java.util.concurrent.Callable
import java.util.concurrent.ExecutorService
import java.util.concurrent.Future
import java.util.concurrent.TimeUnit

/**
 * Helper class for scenarios when we want to run tasks synchronously
 *
 * e.g. ensuring HTTP requests run synchronously in an integration test
 *
 * ref: https://github.com/square/retrofit/issues/1259
 */
class SynchronousExecutorService : ExecutorService {

    override fun isShutdown(): Boolean = false
    override fun isTerminated(): Boolean = false
    override fun shutdown() {}
    override fun shutdownNow(): List<Runnable>? = null
    override fun awaitTermination(timeout: Long, unit: TimeUnit): Boolean = false
    override fun <T> submit(task: Callable<T>): Future<T>? = null
    override fun <T> submit(task: Runnable, result: T): Future<T>? = null
    override fun submit(task: Runnable): Future<*>? = null
    override fun <T> invokeAll(tasks: Collection<Callable<T>>): List<Future<T>>? = null
    override fun <T> invokeAll(tasks: Collection<Callable<T>>, timeout: Long, unit: TimeUnit): List<Future<T>>? = null
    override fun <T> invokeAny(tasks: Collection<Callable<T>>): T? = null
    override fun <T> invokeAny(tasks: Collection<Callable<T>>, timeout: Long, unit: TimeUnit): T? = null
    override fun execute(command: Runnable) {
        command.run()
    }
}
