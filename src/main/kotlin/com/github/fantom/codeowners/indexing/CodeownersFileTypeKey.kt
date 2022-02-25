package com.github.fantom.codeowners.indexing

import com.github.fantom.codeowners.file.type.CodeownersFileType

/**
 * Decorator for [CodeownersFileType] to provide less unique hashcode when used with [CodeownersFilesIndex].
 */
class CodeownersFileTypeKey(val type: CodeownersFileType) {

//    override fun equals(other: Any?) = other is CodeownersFileTypeKey && other.type.languageName == type.languageName
//
//    override fun hashCode() = type.languageName.hashCode()
}
