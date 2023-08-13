package com.github.fantom.codeowners.grouping.changes

import com.intellij.openapi.vcs.FilePath
import com.intellij.openapi.vfs.VirtualFile
import com.intellij.openapi.vfs.VirtualFileSystem
import com.intellij.testFramework.LightVirtualFile

/**
 * A [VirtualFile] implementation that represents moved/renamed file
 * Minimal required set of properties is overridden
 *
 * @param path previous path of the file (moved from)
 * @param currentFile moved/renamed file itself, in its current state
 */
class MovedVirtualFile(
    private val path: FilePath,
    private val currentFile: VirtualFile,
) : LightVirtualFile(path.name, path.fileType, "", path.charset, 0) {
    init {
        isWritable = false
    }

    override fun getParent(): VirtualFile? {
        return path.virtualFileParent ?: path.parentPath?.let{ MovedVirtualDir(it, fileSystem) }
    }

    override fun getPath() = path.path

    /** File system must be the same as of the [currentFile]'s */
    override fun getFileSystem() = currentFile.fileSystem
}

/**
 * A [VirtualFile] implementation that represents moved/renamed directory
 * Minimal required set of properties is overridden
 * This class must be instantiated only from itself and from [MovedVirtualFile]
 *
 * @param path previous path of the directory (moved from)
 * @param vfs file system of the files, moved otgether with this directory
 */
private class MovedVirtualDir(
    private val path: FilePath,
    private val vfs: VirtualFileSystem,
): LightVirtualFile(path.name) {
    init {
        isWritable = false
    }

    override fun getParent(): VirtualFile? {
        return path.virtualFileParent ?: path.parentPath?.let{ MovedVirtualDir(it, vfs) }
    }

    override fun getPath() = path.path

    override fun getFileSystem() = vfs

    override fun isDirectory() = true
}
