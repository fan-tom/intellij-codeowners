package com.github.fantom.codeowners.reference

import com.github.benmanes.caffeine.cache.Cache
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
import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.TimeUnit

/**
 * Cache that retrieves matching files using given glob prefix, taking at an level/dir only into account.
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
typealias Root = String

internal class CodeownersPatternsMatchedFilesCache(private val project: Project) : Disposable {
    private val cacheByPrefix = ConcurrentHashMap<Root, Cache<Triple<CharSequence, AtAnyLevel, DirOnly>, Collection<VirtualFile>>>()

    init {
        ApplicationManager.getApplication().messageBus.connect(this)
            .subscribe(
                VirtualFileManager.VFS_CHANGES,
                object : BulkFileListener {
                    override fun after(events: List<VFileEvent>) {
                        if (cacheByPrefix.isEmpty()) {
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
                        val caches = cacheByPrefix.filterKeys {
                            path.startsWith(it)
                        }
                        // in practice there should be only one cache for given path
                        caches.forEach { (root, cache) ->
                            val relativePath = path.removePrefix(root).let {
                                // TODO check if this condition can be false
                                if (!it.endsWith('/')) {
                                    StringBuilder(it).append('/') // glob compiled to regex always contains trailing slash
                                } else {
                                    it
                                }
                            }
                            val cacheMap = cache.asMap()
                            val globCache = PatternCache.getInstance(project)
                            for (key in cacheMap.keys) {
                                // TODO think about how to take the fact that path may point to a file into account.
                                // In this case we shouldn't assume that atAnyLevel glob may point
                                // to some subtree of this path and so shouldn't invalidate such a glob
                                val regex = globCache.getOrCreatePrefixRegex(key.first, key.second, key.third)
                                val match = regex.find(relativePath) ?: continue
                                // if relative path matched only partially, it means
                                // it points to a tree that is not covered by this glob,
                                // even if they have common prefix
                                if (match.range.last >= relativePath.indices.last) {
                                    cacheMap.remove(key)
                                }
                            }
                        }
                    }
                }
            )
    }

    override fun dispose() {
        cacheByPrefix.values.forEach { it.invalidateAll() }
        cacheByPrefix.clear()
    }

    private fun getCache(context: String) = cacheByPrefix.computeIfAbsent(context) {
        Caffeine.newBuilder()
            .expireAfterAccess(10, TimeUnit.MINUTES)
            .build() // CharSequence as a key to be able to lookup by substring without instantiation
    }

    fun getFilesByPrefix(context: String, prefix: CharSequence, atAnyLevel: Boolean, dirOnly: Boolean): Collection<VirtualFile> {
        return getCache(context)
            .getIfPresent(Triple(prefix, atAnyLevel, dirOnly)) ?: emptyList()
    }

    fun addFilesByPrefix(context: String, prefix: CharSequence, atAnyLevel: AtAnyLevel, dirOnly: DirOnly, files: Collection<VirtualFile>) {
        getCache(context).put(Triple(prefix, atAnyLevel, dirOnly), files)
    }

    companion object {
        @JvmStatic
        fun getInstance(project: Project): CodeownersPatternsMatchedFilesCache {
            return project.getService(CodeownersPatternsMatchedFilesCache::class.java)
        }
    }
}
