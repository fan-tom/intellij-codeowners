package com.github.fantom.codeowners

import com.intellij.openapi.project.Project
import com.intellij.openapi.util.Disposer
import com.intellij.openapi.wm.StatusBarWidget
import com.intellij.openapi.wm.impl.status.widget.StatusBarEditorBasedWidgetFactory
import org.jetbrains.annotations.Nls

class CodeownersBarWidgetFactory : StatusBarEditorBasedWidgetFactory() {
    override fun getId() = "CodeownersPanel"

    @Nls
    override fun getDisplayName() = CodeownersBundle.message("status.bar.codeowners.widget.name")

    override
    fun createWidget(project: Project) = CodeownersBarPanel(project)

    override fun disposeWidget(widget: StatusBarWidget) {
        Disposer.dispose(widget)
    }
}
