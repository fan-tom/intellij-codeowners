package com.github.fantom.codeowners.services

import com.github.fantom.codeowners.util.Glob
import com.intellij.openapi.Disposable
import com.intellij.openapi.project.Project
import com.intellij.openapi.util.Disposer
import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.ConcurrentMap

/**
 * Component that prepares patterns for glob/regex statements and cache them.
 */
class PatternCache(project: Project) : Disposable {
    private val GLOB_TO_REGEX_CACHE: ConcurrentMap<Triple<CharSequence, Boolean, Boolean>, Regex> = ConcurrentHashMap()

    init {
        Disposer.register(project, this)
    }

    override fun dispose() {
        clearCache()
    }

    private fun clearCache() {
        GLOB_TO_REGEX_CACHE.clear()
    }

    fun createRelativePattern(text: CharSequence, caseSensitive: Boolean): Regex {
        // TODO cache?
        return Regex(Glob.createFragmentRegex(text), if (caseSensitive) setOf() else setOf(RegexOption.IGNORE_CASE))
    }

    fun getOrCreatePrefixRegex(prefixGlob: CharSequence, atAnyLevel: Boolean, dirOnly: Boolean): Regex {
        val key = Triple(prefixGlob, atAnyLevel, dirOnly)
        return GLOB_TO_REGEX_CACHE.computeIfAbsent(key) {
            Glob.createPrefixRegex(it.first, it.second, it.third)
        }
    }

    companion object {
        fun getInstance(project: Project): PatternCache {
            return project.getService(PatternCache::class.java)
        }
    }
}
