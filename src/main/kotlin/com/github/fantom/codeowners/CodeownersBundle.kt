package com.github.fantom.codeowners

import com.github.fantom.codeowners.lang.CodeownersLanguage
import com.github.fantom.codeowners.lang.kind.bitbucket.BitbucketLanguage
import com.github.fantom.codeowners.lang.kind.github.GithubLanguage
import com.intellij.AbstractBundle
import org.jetbrains.annotations.NonNls
import org.jetbrains.annotations.PropertyKey
import java.util.ArrayList
import java.util.ResourceBundle

/**
 * [ResourceBundle]/localization utils for the CODEOWNERS support plugin.
 */
object CodeownersBundle : AbstractBundle("messages.CodeownersBundle") {

    @NonNls
    const val BUNDLE_NAME = "messages.CodeownersBundle"

    private val BUNDLE = ResourceBundle.getBundle(BUNDLE_NAME)

    val LANGUAGES = CodeownersLanguages(listOf(GithubLanguage.INSTANCE, BitbucketLanguage.INSTANCE))

    /**
     * Loads a [String] from the [.BUNDLE] [ResourceBundle].
     *
     * @param key    the key of the resource
     * @param params the optional parameters for the specific resource
     * @return the [String] value or `null` if no resource found for the key
     */
    fun message(@PropertyKey(resourceBundle = BUNDLE_NAME) key: String, vararg params: Any?) = message(BUNDLE, key, *params)

    /**
     * Loads a [String] from the [.BUNDLE] [ResourceBundle].
     *
     * @param key    the key of the resource
     * @param params the optional parameters for the specific resource
     * @return the [String] value or `null` if no resource found for the key
     */
    fun messagePointer(@PropertyKey(resourceBundle = BUNDLE_NAME) key: String, vararg params: Any?) = getLazyMessage(key, *params)

    /**
     * Simple [ArrayList] with method to find [CodeownersLanguage] by its name.
     */
    class CodeownersLanguages(languages: List<CodeownersLanguage>) : ArrayList<CodeownersLanguage>(languages) {

        operator fun get(id: String) = find { id == it.id }
    }
}
