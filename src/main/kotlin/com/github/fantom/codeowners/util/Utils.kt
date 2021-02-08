package com.github.fantom.codeowners.util

import com.intellij.openapi.module.Module
import com.intellij.openapi.module.ModuleManager
import com.intellij.openapi.project.Project
import com.intellij.openapi.roots.ModuleRootManager
import com.intellij.openapi.util.text.StringUtil
import com.intellij.openapi.vfs.VfsUtilCore
import com.intellij.openapi.vfs.VirtualFile

/**
 * [Utils] class that contains various methods.
 */
object Utils {

    /**
     * Gets relative path of given @{link VirtualFile} and root directory.
     *
     * @param directory root directory
     * @param file      file to get it's path
     * @return relative path
     */
    fun getRelativePath(directory: VirtualFile, file: VirtualFile) =
        VfsUtilCore.getRelativePath(file, directory, '/')?.let {
            it + ('/'.takeIf { file.isDirectory } ?: "")
        }

    /**
     * Searches for the module in the project that contains given file.
     *
     * @param file    file
     * @param project project
     * @return module containing passed file or null
     */
    fun getModuleForFile(file: VirtualFile, project: Project): Module? =
        ModuleManager.getInstance(project).modules.find { it.moduleContentScope.contains(file) }

    fun getModuleRootForFile(file: VirtualFile, project: Project) = getModuleForFile(file, project)?.let { module ->
        ModuleRootManager.getInstance(module).contentRoots.first()?.takeIf { it.isDirectory }
    }

    /**
     * Checks if file is in project directory.
     *
     * @param file    file
     * @param project project
     * @return file is under directory
     */
    fun isInProject(file: VirtualFile, project: Project) =
        getModuleForFile(file, project) != null || StringUtil.startsWith(file.url, "temp://")
}
