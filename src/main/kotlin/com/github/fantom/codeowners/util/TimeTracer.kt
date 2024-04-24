package com.github.fantom.codeowners.util

import com.github.fantom.codeowners.util.TimeTracer.LogEntry.Nested
import com.intellij.openapi.diagnostic.Logger
import com.intellij.openapi.util.Key
import org.apache.commons.lang3.time.StopWatch
import java.util.concurrent.ConcurrentLinkedQueue

class TimeTracerStub(private val upper: TimeTracer) {
    fun start(name: String): TimeTracer {
        val tracer = upper.nested(name)
        tracer.start()
        return tracer
    }
}

class TimeTracer(val name: String, private val isRoot: Boolean = false) : AutoCloseable {
    sealed class LogEntry {
        abstract val elapsedNs: Long
        class Log(val name: String, private val ns: Long) : LogEntry() {
            override val elapsedNs: Long
                get() = ns

            override fun toString(): String {
                return "$name took $ns ns\n"
            }
        }
        class Nested(private val tracer: TimeTracer) : LogEntry() {
            override val elapsedNs: Long
                get() = tracer.nanoTime

            override fun toString() = "${tracer}\n"
        }
    }
    private val logs: ConcurrentLinkedQueue<LogEntry> = ConcurrentLinkedQueue()

    private val sw = StopWatch()

    fun start() {
        sw.start()
    }

    private fun stop() {
        sw.stop()
    }

    val nanoTime
        get() = sw.nanoTime

    fun nested(name: String): TimeTracer {
        val nestedTracer = TimeTracer(name)
        logs.add(Nested(nestedTracer))
        return nestedTracer
    }

    fun nested() = TimeTracerStub(this)

    override fun toString(): String {
        val sb = StringBuilder("$name took ${sw.nanoTime} ns\n")
        logs.forEach {
            sb.append(it.toString().prependIndent("  "))
        }
        return sb.toString()
    }

    override fun close() {
        stop()
        if (isRoot) {
            logger.trace(toString())
        }
    }

    companion object {
        val logger = Logger.getInstance(TimeTracer::class.java)

        fun <T> wrap(name: String, f: (TimeTracer) -> T): T {
            val tracer = TimeTracer(name, true)
            tracer.start()
            return tracer.use {
                f(it)
            }
        }
    }
}

object TimeTracerKey : Key<TimeTracer>("TimeTracer")

inline fun <T> withNullableCloseable(closeable: AutoCloseable?, f: () -> T): T {
    return if (closeable == null) {
        f()
    } else {
        closeable.use { f() }
    }
}
