package com.github.fantom.codeowners.services

import com.github.fantom.codeowners.lang.CodeownersFile
import com.github.fantom.codeowners.util.Glob
import com.intellij.openapi.Disposable
import com.intellij.openapi.project.Project
import com.intellij.openapi.util.Disposer
import com.intellij.openapi.util.Key
import dregex.Universe
import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.ConcurrentMap
import kotlin.text.Regex
import dregex.Regex as Dregex

/**
 * Component that prepares patterns for glob/regex statements and cache them.
 */
class PatternCache(project: Project) : Disposable {
    private val PREFIX_TO_REGEX_CACHE: ConcurrentMap<Triple<CharSequence, Boolean, Boolean>, Regex> = ConcurrentHashMap()

    private val GLOB_TO_REGEX_CACHE: ConcurrentMap<String, Dregex> = ConcurrentHashMap()

    init {
        Disposer.register(project, this)
    }

    override fun dispose() {
        clearCache()
    }

    private fun clearCache() {
        PREFIX_TO_REGEX_CACHE.clear()
    }

    fun createRelativePattern(text: CharSequence, caseSensitive: Boolean): Regex {
        // TODO cache?
        return Regex(Glob.createFragmentRegex(text), if (caseSensitive) setOf() else setOf(RegexOption.IGNORE_CASE))
    }

    fun getOrCreatePrefixRegex(prefixGlob: CharSequence, atAnyLevel: Boolean, dirOnly: Boolean): Regex {
        val key = Triple(prefixGlob, atAnyLevel, dirOnly)
        return PREFIX_TO_REGEX_CACHE.computeIfAbsent(key) {
            Glob.createPrefixRegex(it.first, it.second, it.third)
        }
    }

    private fun compileDregexInUniverse(regex: String, universe: Universe): Dregex {
        val parsedRegex = Dregex.parse(regex)
        return Dregex.compileParsed(parsedRegex, universe)
    }

    fun getOrCreateGlobRegexes(codeownersFile: CodeownersFile, pattern: String): Dregex {
        return GLOB_TO_REGEX_CACHE.computeIfAbsent(pattern) {
            val regex: String = Glob.createDregex(it, false, false)
            val universe = codeownersFile.getUserData(universeKey)
            val compiledRegex = if (universe == null) {
                val compiledRegex = Dregex.compile(regex)
                val newUniverse = compiledRegex.universe()
                // putIfAbsent to protect from concurrent overrides
                val existingUniverse = codeownersFile.putUserDataIfAbsent(universeKey, newUniverse)
                // concurrent modification happened
                if (existingUniverse != newUniverse) {
                    compileDregexInUniverse(regex, existingUniverse)
                }
                compiledRegex
            } else {
                compileDregexInUniverse(regex, universe)
            }
            compiledRegex
        }
    }

    fun getOrCreateGlobRegexes(codeownersFile: CodeownersFile, patterns: List<String>): List<Dregex> {
        val dregexes = Dregex.compile(
            patterns.map {
                Glob.createDregex(it, false, false)
            }
        )
        return dregexes
    }

    companion object {
        private val universeKey: Key<Universe> = Key.create("CODEOWNERS-UNIVERSE")
        fun getInstance(project: Project): PatternCache {
            return project.getService(PatternCache::class.java)
        }
    }
}
