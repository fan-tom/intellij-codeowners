package com.github.fantom.codeowners.reference

import com.github.benmanes.caffeine.cache.Caffeine
import com.github.fantom.codeowners.services.PatternCache
import com.intellij.openapi.Disposable
import com.intellij.openapi.application.ApplicationManager
import com.intellij.openapi.project.Project
import com.intellij.openapi.vfs.VirtualFile
import com.intellij.openapi.vfs.VirtualFileListener
import com.intellij.openapi.vfs.VirtualFileManager
import com.intellij.openapi.vfs.newvfs.BulkFileListener
import com.intellij.openapi.vfs.newvfs.events.*
import java.util.concurrent.TimeUnit

/**
 * Cache that retrieves matching files using given [Pattern].
 * Cache population happened on demand in the background.
 * The cache eviction happen in the following cases:
 * * by using [VirtualFileListener] to handle filesystem changes
 * and clean cache if needed for the specific pattern parts.
 * * after entries have been expired: entries becomes expired if no read/write operations happened with the
 * corresponding key during some amount of time (10 minutes).
 * * after project dispose
 */
typealias AtAnyLevel = Boolean
typealias DirOnly = Boolean

internal class CodeownersPatternsMatchedFilesCache(private val project: Project) : Disposable {
    private val cacheByPrefix =
        Caffeine.newBuilder()
            .expireAfterAccess(10, TimeUnit.MINUTES)
            .build<Triple<CharSequence, AtAnyLevel, DirOnly>, Collection<VirtualFile>>() // CharSequence as a key to be able to lookup by substring without instantiation

    init {
        ApplicationManager.getApplication().messageBus.connect(this)
            .subscribe(
                VirtualFileManager.VFS_CHANGES,
                object : BulkFileListener {
                    override fun after(events: List<VFileEvent>) {
                        if (cacheByPrefix.estimatedSize() == 0L) {
                            return
                        }

                        for (event in events) {
                            if (event is VFileCreateEvent ||
                                event is VFileDeleteEvent ||
                                event is VFileCopyEvent
                            ) {
                                cleanupCache(event.path)
                            } else if (event is VFilePropertyChangeEvent && event.isRename) {
                                cleanupCache(event.oldPath)
                                cleanupCache(event.path)
                            } else if (event is VFileMoveEvent) {
                                cleanupCache(event.oldPath)
                                cleanupCache(event.path)
                            }
                        }
                    }

                    private fun cleanupCache(path: String) {
                        val cacheMap = cacheByPrefix.asMap()
                        val globCache = PatternCache.getInstance(project)
                        for (key in cacheMap.keys) {
                            val regex = globCache.getOrCreatePrefixRegex(key.first, key.second, key.third)
                            if (regex.containsMatchIn(path)) {
                                cacheMap.remove(key)
                            }
                        }
                    }
                }
            )
    }

    override fun dispose() {
        cacheByPrefix.invalidateAll()
    }

    fun getFilesByPrefix(prefix: CharSequence, atAnyLevel: Boolean, dirOnly: Boolean): Collection<VirtualFile> {
        return cacheByPrefix.getIfPresent(Triple(prefix, atAnyLevel, dirOnly)) ?: emptyList()
    }

    fun addFilesByPrefix(prefix: CharSequence, atAnyLevel: AtAnyLevel, dirOnly: DirOnly, files: Collection<VirtualFile>) {
        cacheByPrefix.put(Triple(prefix, atAnyLevel, dirOnly), files)
    }

    companion object {
        @JvmStatic
        fun getInstance(project: Project): CodeownersPatternsMatchedFilesCache {
            return project.getService(CodeownersPatternsMatchedFilesCache::class.java)
        }
    }
}
