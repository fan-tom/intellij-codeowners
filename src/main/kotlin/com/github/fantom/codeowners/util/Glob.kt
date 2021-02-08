package com.github.fantom.codeowners.util

import com.github.fantom.codeowners.language.psi.CodeownersEntry
import com.intellij.openapi.util.text.StringUtil
import com.intellij.openapi.vfs.VfsUtil
import com.intellij.openapi.vfs.VirtualFile
import com.intellij.openapi.vfs.VirtualFileVisitor
import com.jetbrains.rd.util.concurrentMapOf
import com.github.fantom.codeowners.services.CodeownersMatcher
import com.github.fantom.codeowners.util.Utils.getRelativePath
import java.util.regex.Pattern
import java.util.regex.PatternSyntaxException

/**
 * Glob util class that prepares glob statements or searches for content using glob rules.
 */
object Glob {

    /**
     * Finds for [VirtualFile] list using glob rule in given root directory.
     *
     * @param root  root directory
     * @param entry ignore entry
     * @return search result
     */
    fun findOne(root: VirtualFile, entry: CodeownersEntry, matcher: CodeownersMatcher) =
        find(root, listOf(entry), matcher, false)[entry]?.firstOrNull()

    /**
     * Finds for [VirtualFile] list using glob rule in given root directory.
     *
     * @param root          root directory
     * @param entries       codeowners entries
     * @param includeNested attach children to the search result
     * @return search result
     */
    fun find(root: VirtualFile, entries: List<CodeownersEntry>, matcher: CodeownersMatcher, includeNested: Boolean) =
        concurrentMapOf<CodeownersEntry, MutableList<VirtualFile>>().apply {
            val map = concurrentMapOf<CodeownersEntry, Pattern>()

            entries.forEach {
                this[it] = mutableListOf()
                createPattern(it)?.let { pattern ->
                    map[it] = pattern
                }
            }

            val visitor = object : VirtualFileVisitor<Map<CodeownersEntry, Pattern?>>(NO_FOLLOW_SYMLINKS) {
                @Suppress("ReturnCount")
                override fun visitFile(file: VirtualFile): Boolean {
                    if (root == file) {
                        return true
                    }
                    val current = mutableMapOf<CodeownersEntry, Pattern?>()
                    val path = getRelativePath(root, file)
                    if (currentValue.isEmpty() || path == null) {
                        return false
                    }

                    currentValue.forEach { (key, value) ->
                        var matches = false
                        if (value == null || matcher.match(value, path)) {
                            matches = true
                            get(key)?.add(file)
                        }
                        current[key] = value.takeIf { !includeNested || !matches }
                    }

                    setValueForChildren(current)
                    return true
                }
            }

            visitor.setValueForChildren(map)
            VfsUtil.visitChildrenRecursively(root, visitor)
        }

    /**
     * Finds for [VirtualFile] paths list using glob rule in given root directory.
     *
     * @param root          root directory
     * @param entries       ignore entry
     * @param includeNested attach children to the search result
     * @return search result
     */
    fun findAsPaths(root: VirtualFile, entries: List<CodeownersEntry>, matcher: CodeownersMatcher, includeNested: Boolean) =
        find(root, entries, matcher, includeNested).mapValues { (_, value) ->
            value
                .asSequence()
                .map { getRelativePath(root, it) }
                .filterNotNull()
                .toSet()
        }

    /**
     * Creates regex [Pattern] using [CodeownersEntry].
     *
     * @param entry          [CodeownersEntry]
     * @param acceptChildren Matches directory children
     * @return regex [Pattern]
     */
    fun createPattern(entry: CodeownersEntry, acceptChildren: Boolean = false) = createPattern(entry.value, acceptChildren)

    /**
     * Creates regex [Pattern] using glob rule.
     *
     * @param rule   rule value
     * @param syntax rule syntax
     * @return regex [Pattern]
     */
    fun createPattern(rule: String, acceptChildren: Boolean = false) =
        getPattern(createRegex(rule, acceptChildren))

    /**
     * Converts regex string to [Pattern] with caching.
     *
     * @param regex regex to convert
     * @return [Pattern] instance or null if invalid
     */
    fun getPattern(regex: String) = try {
        Pattern.compile(regex)
    } catch (e: PatternSyntaxException) {
        null
    }

    /**
     * Creates regex [String] using glob rule.
     *
     * @param glob           rule
     * @param acceptChildren Matches directory children
     * @return regex [String]
     */
    @Suppress("ComplexMethod", "LongMethod", "NestedBlockDepth")
    fun createRegex(glob: String, acceptChildren: Boolean): String = glob.trim { it <= ' ' }.let {
        val sb = StringBuilder("^")
        var escape = false
        var star = false
        var doubleStar = false
        var beginIndex = 0

        if (StringUtil.startsWith(it, Constants.DOUBLESTAR)) {
            sb.append("(?:[^/]*?/)*")
            beginIndex = 2
            doubleStar = true
        } else if (StringUtil.startsWith(it, "*/")) {
            sb.append("[^/]*")
            beginIndex = 1
            star = true
        } else if (StringUtil.equals(Constants.STAR, it)) {
            sb.append(".*")
        } else if (StringUtil.startsWithChar(it, '*')) {
            sb.append(".*?")
        } else if (StringUtil.startsWithChar(it, '/')) {
            beginIndex = 1
        } else {
            val slashes = StringUtil.countChars(it, '/')
            if (slashes == 0 || slashes == 1 && StringUtil.endsWithChar(it, '/')) {
                sb.append("(?:[^/]*?/)*")
            }
        }

        val chars = it.substring(beginIndex).toCharArray()
        chars.forEach { ch ->
            if (doubleStar) {
                doubleStar = false
                if (ch == '/') {
                    sb.append("(?:[^/]*/)*?")
                    return@forEach
                } else {
                    sb.append("[^/]*?")
                }
            }
            if (ch == '*') {
                when {
                    escape -> {
                        sb.append("\\*")
                        star = false
                        escape = star
                    }
                    star -> {
                        val prev = if (sb.isNotEmpty()) sb[sb.length - 1] else '\u0000'
                        if (prev == '\u0000' || prev == '^' || prev == '/') {
                            doubleStar = true
                        } else {
                            sb.append("[^/]*?")
                        }
                        star = false
                    }
                    else -> {
                        star = true
                    }
                }
                return@forEach
            } else if (star) {
                sb.append("[^/]*?")
                star = false
            }
            when (ch) {
                '\\' -> {
                    if (escape) {
                        sb.append("\\\\")
                    }
                    escape = !escape
                }
                '?' ->
                    if (escape) {
                        sb.append("\\?")
                        escape = false
                    } else {
                        sb.append('.')
                    }
//                '[' -> {
//                    if (escape) {
//                        sb.append('\\')
//                        escape = false
//                    } else {
//                        bracket = true
//                    }
//                    sb.append(ch)
//                }
//                ']' -> {
//                    if (!bracket) {
//                        sb.append('\\')
//                    }
//                    sb.append(ch)
//                    bracket = false
//                    escape = false
//                }
                '.', '(', ')', '{', '}', '[', ']', '+', '|', '^', '$', '@', '%' -> {
                    sb.append('\\')
                    sb.append(ch)
                    escape = false
                }
                else -> {
                    escape = false
                    sb.append(ch)
                }
            }
        }
        when {
            StringUtil.endsWithChar(sb, '/') -> when {
                star -> sb.append("[^/]+") // or *
//                doubleStar -> sb.append(".+")
//                else -> if (acceptChildren) sb.append("[^/]*")
                else -> sb.append(".*")
            }
            star || doubleStar -> sb.append("[^/]*/?")
            else -> sb.append(if (acceptChildren) "(?:/.*)?" else "/?")
        }
//        if (star || doubleStar) {
//            if (StringUtil.endsWithChar(sb, '/')) {
//                if (doubleStar) {
//                    sb.append(".+")
//                } else {
//                    sb.append("[^/]*") // or +
//                }
//            } else {
//                sb.append("[^/]*/?")
//            }
//        } else {
//            if (StringUtil.endsWithChar(sb, '/')) {
//                if (acceptChildren) {
//                    sb.append("[^/]*")
//                }
//            } else {
//                sb.append(if (acceptChildren) "(?:/.*)?" else "/?")
//            }
//        }
        sb.append('$')
        return sb.toString()
    }
}