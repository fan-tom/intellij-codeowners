package com.github.fantom.codeowners.highlighter

import com.github.fantom.codeowners.CodeownersBundle
import com.github.fantom.codeowners.lang.kind.bitbucket.highlighter.CodeownersHighlighter
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
        @NonNls private val SAMPLE_CODEOWNERS_PATH = "/sample.codeowners"
        @NonNls private val DISPLAY_NAME = CodeownersBundle.message("codeowners.colorSettings.displayName")
        private val SAMPLE_CODEOWNERS = loadSampleCodeowners()
        private val DESCRIPTORS = arrayOf(
            AttributesDescriptor(CodeownersBundle.message("highlighter.header"), CodeownersHighlighterColors.HEADER),
            AttributesDescriptor(CodeownersBundle.message("highlighter.section"), CodeownersHighlighterColors.SECTION),
            AttributesDescriptor(CodeownersBundle.message("highlighter.comment"), CodeownersHighlighterColors.COMMENT),
            AttributesDescriptor(CodeownersBundle.message("highlighter.negation"), CodeownersHighlighterColors.NEGATION),
//            AttributesDescriptor(CodeownersBundle.message("highlighter.brackets"), CodeownersHighlighterColors.BRACKET),
            AttributesDescriptor(CodeownersBundle.message("highlighter.slash"), CodeownersHighlighterColors.SLASH),
            AttributesDescriptor(CodeownersBundle.message("highlighter.value"), CodeownersHighlighterColors.VALUE),
            AttributesDescriptor(CodeownersBundle.message("highlighter.configName"), CodeownersHighlighterColors.CONFIG_NAME),
            AttributesDescriptor(CodeownersBundle.message("highlighter.configValue"), CodeownersHighlighterColors.CONFIG_VALUE),
            AttributesDescriptor(CodeownersBundle.message("highlighter.unused"), CodeownersHighlighterColors.UNUSED),
        )

        /**
         * Loads sample CODEOWNERS file
         *
         * @return the text loaded from [.SAMPLE_CODEOWNERS_PATH]
         *
         * @see .getDemoText
         * @see .SAMPLE_CODEOWNERS_PATH
         * @see .SAMPLE_CODEOWNERS
         */
        private fun loadSampleCodeowners() = Resources.getResourceContent(SAMPLE_CODEOWNERS_PATH) ?: ""
    }

    override fun getIcon(): Icon? = null

    // we use bitbucket file type because it is more feature-rich
    override fun getHighlighter() = CodeownersHighlighter()

    override fun getDemoText() = SAMPLE_CODEOWNERS

    override fun getAdditionalHighlightingTagToDescriptorMap(): Map<String, TextAttributesKey>? = null

    override fun getAttributeDescriptors() = DESCRIPTORS

    override fun getColorDescriptors(): Array<ColorDescriptor> = ColorDescriptor.EMPTY_ARRAY

    override fun getDisplayName(): String = DISPLAY_NAME
}
