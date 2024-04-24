package com.github.fantom.codeowners.services

import com.github.fantom.codeowners.util.Glob
import com.intellij.openapi.Disposable
import com.intellij.openapi.components.Service
import com.intellij.openapi.components.service
import dk.brics.automaton.Automaton
import dk.brics.automaton.RegExp
import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.ConcurrentMap

/**
 * Component that prepares patterns for glob/regex statements and cache them.
 */
@Service(Service.Level.APP)
class PatternCache : Disposable {
    private val PREFIX_TO_REGEX_CACHE: ConcurrentMap<Triple<CharSequence, Boolean, Boolean>, Regex> = ConcurrentHashMap()

    private val GLOB_TO_AUTOMATON_CACHE: ConcurrentMap<String, Automaton> = ConcurrentHashMap()

    override fun dispose() {
        clearCache()
    }

    private fun clearCache() {
        PREFIX_TO_REGEX_CACHE.clear()
        GLOB_TO_AUTOMATON_CACHE.clear()
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

    fun getOrCreateGlobRegexes2(glob: String): Automaton {
        return GLOB_TO_AUTOMATON_CACHE.computeIfAbsent(glob) {
                RegExp(
                    Glob.createDregex(it, acceptChildren = false, supportSquareBrackets = false)
                ).toAutomaton()
            }
    }

    companion object {
        fun getInstance() = service<PatternCache>()
    }
}
