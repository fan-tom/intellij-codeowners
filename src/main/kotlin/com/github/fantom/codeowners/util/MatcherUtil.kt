package com.github.fantom.codeowners.util

import java.util.regex.Pattern

/**
 * Util class to speed up and limit regex operation on the files paths.
 */
class MatcherUtil private constructor() {

    companion object {
        /**
         * Checks if given path contains all of the path parts.
         *
         * @param parts that should be contained in path
         * @param path  to check
         * @return path contains all parts
         */
        @Suppress("ReturnCount")
        fun matchAllParts(parts: Array<String>, path: String): Boolean {
            var index = -1
            parts.forEach {
                index = path.indexOf(it, index)
                if (index == -1) {
                    return false
                }
            }
            return true
        }

        /**
         * Extracts alphanumeric parts from  [Pattern].
         *
         * @param pattern to handle
         * @return extracted parts
         */
        fun getParts(pattern: Pattern?): Array<String> {
            if (pattern == null) {
                return emptyArray()
            }
            val parts: MutableList<String> = ArrayList()
            val sPattern = pattern.toString()
            var part = StringBuilder()
            for (i in sPattern.indices) {
                val ch = sPattern[i]
                if (Character.isLetterOrDigit(ch)) {
                    part.append(sPattern[i])
                } else if (part.isNotEmpty()) {
                    parts.add(part.toString())
                    part = StringBuilder()
                }
            }
            return parts.toTypedArray()
        }
    }
}
