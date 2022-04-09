package com.github.fantom.codeowners.services

import com.github.fantom.codeowners.util.Glob
import com.intellij.openapi.Disposable
import com.intellij.openapi.project.Project
import com.intellij.openapi.util.Disposer
import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.ConcurrentMap
import java.util.regex.Pattern
import java.util.regex.PatternSyntaxException

/**
 * Component that prepares patterns for glob/regex statements and cache them.
 */
class PatternCache(project: Project) : Disposable {
    /**
     * Cache map that holds processed regex statements to the glob rules.
     */
    private val GLOBS_CACHE: ConcurrentMap<String, String> = ConcurrentHashMap()

    /**
     * Cache map that holds compiled regex.
     */
    private val PATTERNS_CACHE: ConcurrentMap<String, Pattern> = ConcurrentHashMap()

    init {
        Disposer.register(project, this)
    }

    /**
     * Creates regex [Pattern] using glob rule.
     *
     * @param rule           rule value
     * @param acceptChildren Matches directory children
     * @return regex [Pattern]
     */
    @JvmOverloads
    fun createPattern(rule: String, acceptChildren: Boolean = false): Pattern? {
        val regex = getRegex(rule, acceptChildren)
        return getOrCreatePattern(regex)
    }

    /**
     * Returns regex string basing on the rule and provided syntax.
     *
     * @param rule           rule value
     * @param syntax         rule syntax
     * @param acceptChildren Matches directory children
     * @return regex string
     */
    fun getRegex(rule: String, acceptChildren: Boolean): String {
        return createRegex(rule, acceptChildren)
    }

    /**
     * Converts regex string to [Pattern] with caching.
     *
     * @param regex regex to convert
     * @return [Pattern] instance or null if invalid
     */
    fun getOrCreatePattern(regex: String): Pattern? {
        return try {
            PATTERNS_CACHE.computeIfAbsent(regex) {
                Pattern.compile(regex)
            }
        } catch (e: PatternSyntaxException) {
            null
        }
    }

    fun getPattern(regex: String): Pattern? {
        return PATTERNS_CACHE[regex]
    }

    /**
     * Creates regex [String] using glob rule.
     *
     * @param glob           rule
     * @param acceptChildren Matches directory children
     * @return regex [String]
     */
    private fun createRegex(glob: String, acceptChildren: Boolean): String {
        var glob = glob
        glob = glob.trim { it <= ' ' }
        return GLOBS_CACHE.computeIfAbsent(glob) {
            Glob.createRegex(glob, acceptChildren, false) // TODO supportSquareBrackets, what to do with it?
        }
    }

    override fun dispose() {
        clearCache()
    }

    fun clearCache() {
        GLOBS_CACHE.clear()
        PATTERNS_CACHE.clear()
    }

    companion object {
        fun getInstance(project: Project): PatternCache {
            return project.getService(PatternCache::class.java)
        }
    }
}
