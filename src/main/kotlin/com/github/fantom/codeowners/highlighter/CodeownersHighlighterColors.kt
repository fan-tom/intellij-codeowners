package com.github.fantom.codeowners.highlighter

import com.intellij.openapi.editor.colors.TextAttributesKey

/**
 * Contains highlighter attributes definitions.
 */
object CodeownersHighlighterColors {
    /** Default style for regular comment started with # */
    val COMMENT = TextAttributesKey.createTextAttributesKey("CODEOWNERS.COMMENT")

    /** Default style for section comment started with ## */
    val SECTION = TextAttributesKey.createTextAttributesKey("CODEOWNERS.SECTION")

    /** Default style for header comment started with ### */
    val HEADER = TextAttributesKey.createTextAttributesKey("CODEOWNERS.HEADER")

    /** Default style for negation element - ! in the beginning of the entry */
    val NEGATION = TextAttributesKey.createTextAttributesKey("CODEOWNERS.NEGATION")

    /** Default style for bracket element - [] in the entry */
//    val BRACKET = TextAttributesKey.createTextAttributesKey("CODEOWNERS.BRACKET")

    /** Default style for slash - / in the entry */
    val SLASH = TextAttributesKey.createTextAttributesKey("CODEOWNERS.SLASH")

    /** Default style for value element - part of entry */
    val VALUE = TextAttributesKey.createTextAttributesKey("CODEOWNERS.VALUE")

    /** Default style for value element - part of entry */
    val NAME = TextAttributesKey.createTextAttributesKey("CODEOWNERS.NAME")

    val CONFIG_NAME = TextAttributesKey.createTextAttributesKey("CODEOWNERS.CONFIG_NAME")
    val CONFIG_VALUE = TextAttributesKey.createTextAttributesKey("CODEOWNERS.CONFIG_VALUE")

    /** Default style for unused entry */
    val UNUSED = TextAttributesKey.createTextAttributesKey("CODEOWNERS.UNUSED_ENTRY")
}
