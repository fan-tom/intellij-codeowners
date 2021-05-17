package com.github.fantom.codeowners

import com.intellij.openapi.module.Module
import com.intellij.openapi.project.ModuleListener
import com.intellij.openapi.project.Project
import com.intellij.openapi.roots.ModuleRootEvent
import com.intellij.openapi.roots.ModuleRootListener
import com.intellij.util.Function

/**
 * Wrapper for common listeners.
 */
class CommonRunnableListeners(private val task: Runnable) : CodeownersManager.RefreshStatusesListener, ModuleRootListener, ModuleListener {

    override fun refresh() = task.run()

    override fun beforeRootsChange(event: ModuleRootEvent) = Unit

    override fun rootsChanged(event: ModuleRootEvent) = task.run()

    override fun moduleAdded(project: Project, module: Module) = task.run()

    override fun beforeModuleRemoved(project: Project, module: Module) = Unit

    override fun moduleRemoved(project: Project, module: Module) = task.run()

    override fun modulesRenamed(project: Project, modules: MutableList<out Module>, oldNameProvider: Function<in Module, String>) = task.run()
}
