package com.github.fantom.codeowners.command

/**
 * Command action that appends specified file to rules list.
 */
// class AppendFileCommandAction(
//    private val project: Project,
//    private val file: PsiFile,
//    private val content: MutableSet<String>,
//    private val ignoreDuplicates: Boolean = false,
//    private val ignoreComments: Boolean = false,
// ) : CommandAction<PsiFile?>(project) {
//
//    /**
//     * Adds [.content] to the given [.file]. Checks if file contains content and sends a notification.
//     *
//     * @return previously provided file
//     */
//    @Suppress("ComplexMethod", "LongMethod", "NestedBlockDepth")
//    override fun compute(): PsiFile {
//        if (content.isEmpty()) {
//            return file
//        }
//        val manager = PsiDocumentManager.getInstance(project)
//        manager.getDocument(file)?.let { document ->
//            val insertAtCursor = CodeownersSettings.getInstance().insertAtCursor
//            var offset = document.textLength
//
//            file.acceptChildren(
//                object : com.github.fantom.codeowners.lang.kind.github.psi.CodeownersVisitor() {
//                    override fun visitEntry(entry: com.github.fantom.codeowners.lang.kind.github.psi.CodeownersEntry) {
//                        val moduleDir = Utils.getModuleRootForFile(file.virtualFile, project)
//                        if (content.contains(entry.text) && moduleDir != null) {
//                            Notify.show(
//                                project,
//                                CodeownersBundle.message("action.appendFile.entryExists", entry.text),
//                                CodeownersBundle.message(
//                                    "action.appendFile.entryExists.in",
//                                    Utils.getRelativePath(moduleDir, file.virtualFile)
//                                ),
//                                NotificationType.WARNING
//                            )
//                            content.remove(entry.text)
//                        }
//                    }
//                }
//            )
//
//            if (insertAtCursor) {
//                EditorFactory.getInstance().getEditors(document).firstOrNull()?.let { editor ->
//                    editor.selectionModel.selectionStartPosition?.let { position ->
//                        offset = document.getLineStartOffset(position.line)
//                    }
//                }
//            }
//
//            content.forEach { it ->
//                var entry = it
//
//                if (ignoreDuplicates) {
//                    val currentLines = document.text.split(Constants.NEWLINE).filter {
//                        it.isNotEmpty() && !it.startsWith(Constants.HASH)
//                    }.toMutableList()
//                    val entryLines = it.split(Constants.NEWLINE).toMutableList()
//                    val iterator = entryLines.iterator()
//
//                    while (iterator.hasNext()) {
//                        val line = iterator.next().trim { it <= ' ' }
//                        if (line.isEmpty() || line.startsWith(Constants.HASH)) {
//                            continue
//                        }
//                        if (currentLines.contains(line)) {
//                            iterator.remove()
//                        } else {
//                            currentLines.add(line)
//                        }
//                    }
//                    entry = StringUtil.join(entryLines, Constants.NEWLINE)
//                }
//                if (ignoreComments) {
//                    val entryLines = it.split(Constants.NEWLINE).toMutableList()
//                    val iterator = entryLines.iterator()
//                    while (iterator.hasNext()) {
//                        val line = iterator.next().trim { it <= ' ' }
//                        if (line.isEmpty() || line.startsWith(Constants.HASH)) {
//                            iterator.remove()
//                        }
//                    }
//                    entry = StringUtil.join(entryLines, Constants.NEWLINE)
//                }
//
//                entry = StringUtil.replace(entry, "\r", "")
//                if (!StringUtil.isEmpty(entry)) {
//                    entry += Constants.NEWLINE
//                }
//
//                if (!insertAtCursor && !document.text.endsWith(Constants.NEWLINE) && !StringUtil.isEmpty(entry)) {
//                    entry = Constants.NEWLINE + entry
//                }
//
//                document.insertString(offset, entry)
//                offset += entry.length
//            }
//
//            manager.commitDocument(document)
//        }
//        return file
//    }
// }
