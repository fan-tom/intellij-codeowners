package com.github.fantom.codeowners.command

import com.intellij.openapi.command.WriteCommandAction
import com.intellij.openapi.project.Project
import com.intellij.openapi.util.ThrowableComputable

abstract class CommandAction<T>(private val project: Project) {

    @Throws(Throwable::class)
    protected abstract fun compute(): T

    @Throws(Throwable::class)
    fun execute(): T = WriteCommandAction.writeCommandAction(project).compute(ThrowableComputable(this::compute))
}
