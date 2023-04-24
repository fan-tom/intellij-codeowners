package com.github.fantom.codeowners.util

import com.intellij.concurrency.JobScheduler
import com.intellij.openapi.Disposable
import com.intellij.openapi.project.DumbAwareRunnable
import java.util.concurrent.ScheduledFuture
import java.util.concurrent.TimeUnit

/**
 * Debounced runnable class that allows to run command just once in case it was triggered too often.
 */
abstract class Debounced<T>(private val delay: Int) : DumbAwareRunnable, Disposable {

    /** Timer that depends on the given [.delay] value. */
    private var timer: ScheduledFuture<*>? = null

    /** Wrapper run() method to invoke [.timer] properly. */
    override fun run() {
        run(null)
    }

    /** Wrapper run() method to invoke [.timer] properly. */
    fun run(argument: T?) {
        timer?.cancel(false)
        timer = JobScheduler.getScheduler().schedule(
            DumbAwareRunnable { task(argument) },
            delay.toLong(),
            TimeUnit.MILLISECONDS
        )
    }

    /** Task to run in debounce way. */
    protected abstract fun task(argument: T?)

    override fun dispose() {
        timer?.cancel(true)
    }
}
