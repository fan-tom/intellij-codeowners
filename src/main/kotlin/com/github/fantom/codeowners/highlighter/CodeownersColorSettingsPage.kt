package com.github.fantom.codeowners.highlighter

import com.github.fantom.codeowners.CodeownersBundle
import com.github.fantom.codeowners.util.Resources
import com.intellij.openapi.editor.colors.TextAttributesKey
import com.intellij.openapi.options.colors.AttributesDescriptor
import com.intellij.openapi.options.colors.ColorDescriptor
import com.intellij.openapi.options.colors.ColorSettingsPage
import org.jetbrains.annotations.NonNls
import javax.swing.Icon

/**
 * [ColorSettingsPage] that allows to modify color scheme.
 */
class CodeownersColorSettingsPage : ColorSettingsPage {

    companion object {
        @NonNls private val SAMPLE_GITIGNORE_PATH = "/sample.gitignore"
        @NonNls private val DISPLAY_NAME = CodeownersBundle.message("ignore.colorSettings.displayName")
        private val SAMPLE_GITIGNORE = loadSampleGitignore()
        private val DESCRIPTORS = arrayOf(
            AttributesDescriptor(CodeownersBundle.message("highlighter.header"), CodeownersHighlighterColors.HEADER),
            AttributesDescriptor(CodeownersBundle.message("highlighter.section"), CodeownersHighlighterColors.SECTION),
            AttributesDescriptor(CodeownersBundle.message("highlighter.comment"), CodeownersHighlighterColors.COMMENT),
            AttributesDescriptor(CodeownersBundle.message("highlighter.negation"), CodeownersHighlighterColors.NEGATION),
            AttributesDescriptor(CodeownersBundle.message("highlighter.brackets"), CodeownersHighlighterColors.BRACKET),
            AttributesDescriptor(CodeownersBundle.message("highlighter.slash"), CodeownersHighlighterColors.SLASH),
            AttributesDescriptor(CodeownersBundle.message("highlighter.syntax"), CodeownersHighlighterColors.SYNTAX),
            AttributesDescriptor(CodeownersBundle.message("highlighter.value"), CodeownersHighlighterColors.VALUE),
            AttributesDescriptor(CodeownersBundle.message("highlighter.unused"), CodeownersHighlighterColors.UNUSED)
        )

        /**
         * Loads sample .gitignore file
         *
         * @return the text loaded from [.SAMPLE_GITIGNORE_PATH]
         *
         * @see .getDemoText
         * @see .SAMPLE_GITIGNORE_PATH
         * @see .SAMPLE_GITIGNORE
         */
        private fun loadSampleGitignore() = Resources.getResourceContent(SAMPLE_GITIGNORE_PATH) ?: ""
    }

    override fun getIcon(): Icon? = null

    override fun getHighlighter() = CodeownersHighlighter(null)

    override fun getDemoText() = SAMPLE_GITIGNORE

    override fun getAdditionalHighlightingTagToDescriptorMap(): Map<String, TextAttributesKey>? = null

    override fun getAttributeDescriptors() = DESCRIPTORS

    override fun getColorDescriptors(): Array<ColorDescriptor> = ColorDescriptor.EMPTY_ARRAY

    override fun getDisplayName(): String = DISPLAY_NAME
}
