package com.github.fantom.codeowners.util

import java.io.InputStream
import java.util.Scanner

/**
 * [Resources] util class that contains methods that work on plugin resources.
 */
object Resources {

    /**
     * Reads resource file and returns its content as a String.
     *
     * @param path Resource path
     * @return Content
     */
    fun getResourceContent(path: String) = convertStreamToString(Resources::class.java.getResourceAsStream(path))

    /**
     * Converts InputStream resource to String.
     *
     * @param inputStream Input stream
     * @return Content
     */
    private fun convertStreamToString(inputStream: InputStream?) =
        inputStream?.let { stream -> Scanner(stream).useDelimiter("\\A").takeIf { it.hasNext() }?.next() ?: "" }
}
