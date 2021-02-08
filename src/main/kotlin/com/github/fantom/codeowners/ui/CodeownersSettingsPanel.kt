package com.github.fantom.codeowners.ui

import com.intellij.openapi.Disposable
import javax.swing.JCheckBox
import javax.swing.JPanel

/**
 * UI form for [CodeownersSettings] edition.
 */
@Suppress("MagicNumber")
class CodeownersSettingsPanel : Disposable {

    /** The parent panel for the form. */
    var panel: JPanel? = null

    /** Form element for CodeownersSettings#missingGitignore. */
    private lateinit var missingCodeownersCheckBox: JCheckBox
//
//    /** Templates list panel. */
//    private lateinit var templatesListPanel: TemplatesListPanel
//
//    /** Enable ignored file status coloring. */
//    private lateinit var ignoredFileStatusCheckBox: JCheckBox

    /** Defines if new content should be inserted at the cursor's position or at the document end. */
    private lateinit var insertAtCursorCheckBox: JCheckBox
//
//    /** Splitter element. */
//    private lateinit var templatesSplitter: Splitter
//
//    /** File types scroll panel with table. */
//    private lateinit var languagesPanel: JScrollPane
//
//    /** Settings table. */
//    private lateinit var languagesTable: JBTable
//
//    /** Enable unignore files group. */
//    private lateinit var unignoreFiles: JCheckBox
//
//    /** Inform about editing ignored file. */
//    private lateinit var notifyCodeownersdEditingCheckBox: JCheckBox
//
//    /** Editor panel element. */
//    private lateinit var editorPanel: EditorPanel

//    companion object {
//        const val NAME_COLUMN = 0
//        const val NEW_FILE_COLUMN = 1
//        const val ENABLE_COLUMN = 2
//    }

//    private fun createUIComponents() {
//        templatesListPanel = TemplatesListPanel()
//        editorPanel = EditorPanel().apply {
//            preferredSize = Dimension(Int.MAX_VALUE, 200)
//        }
//        templatesSplitter = Splitter(false, 0.3f).apply {
//            firstComponent = templatesListPanel
//            secondComponent = editorPanel
//        }
//        languagesTable = JBTable().apply {
//            model = LanguagesTableModel()
//            selectionModel.selectionMode = ListSelectionModel.SINGLE_SELECTION
//            columnSelectionAllowed = false
//            rowHeight = 22
//            columnModel.getColumn(NEW_FILE_COLUMN).apply {
//                cellEditor = BooleanTableCellEditor()
//                cellRenderer = BooleanTableCellRenderer()
//            }
//            columnModel.getColumn(ENABLE_COLUMN).apply {
//                cellEditor = BooleanTableCellEditor()
//                cellRenderer = object : BooleanTableCellRenderer() {
//                    override fun getTableCellRendererComponent(
//                        table: JTable,
//                        value: Any,
//                        isSel: Boolean,
//                        hasFocus: Boolean,
//                        row: Int,
//                        column: Int,
//                    ): Component {
//                        val editable = table.isCellEditable(row, column)
//                        val newValue = if (editable) value else null
//                        return super.getTableCellRendererComponent(table, newValue, isSel, hasFocus, row, column)
//                    }
//                }
//            }
//            preferredScrollableViewportSize = Dimension(-1, rowHeight * CodeownersBundle.LANGUAGES.size / 2)
//            isStriped = true
//            border = JBUI.Borders.empty()
//            dragEnabled = false
//            setShowGrid(false)
//        }
//        languagesPanel = ScrollPaneFactory.createScrollPane(languagesTable)
//    }

    override fun dispose() {
//        if (!editorPanel.preview.isDisposed) {
//            EditorFactory.getInstance().releaseEditor(editorPanel.preview)
//        }
    }

    var missingCodeowners
        get() = missingCodeownersCheckBox.isSelected
        set(selected) {
            missingCodeownersCheckBox.isSelected = selected
        }
//
//    var ignoredFileStatus
//        get() = ignoredFileStatusCheckBox.isSelected
//        set(selected) {
//            ignoredFileStatusCheckBox.isSelected = selected
//        }

//    var userTemplates: List<UserTemplate>
//        get() = templatesListPanel.list
//        set(userTemplates) {
//            templatesListPanel.resetForm(userTemplates)
//        }

    var insertAtCursor
        get() = insertAtCursorCheckBox.isSelected
        set(selected) {
            insertAtCursorCheckBox.isSelected = selected
        }
//
//    var unignoreActions
//        get() = unignoreFiles.isSelected
//        set(selected) {
//            unignoreFiles.isSelected = selected
//        }
//
//    var notifyCodeownersdEditing
//        get() = notifyCodeownersdEditingCheckBox.isSelected
//        set(selected) {
//            notifyCodeownersdEditingCheckBox.isSelected = selected
//        }

//    val languagesSettings: LanguagesTableModel
//        get() = languagesTable.model as LanguagesTableModel
//
//    /** Extension for the CRUD list panel. */
//    open inner class TemplatesListPanel : AddEditDeleteListPanel<UserTemplate>(null, ArrayList()) {
//
//        override fun customizeDecorator(decorator: ToolbarDecorator) {
//            super.customizeDecorator(decorator)
//            val group = DefaultActionGroup().apply {
//                addSeparator()
//                add(
//                    object : AnAction(
//                        message("action.importTemplates"),
//                        message("action.importTemplates.description"),
//                        AllIcons.Actions.Install
//                    ) {
//                        override fun actionPerformed(event: AnActionEvent) {
//                            val descriptor: FileChooserDescriptor = object : FileChooserDescriptor(true, false, true, false, true, false) {
//                                override fun isFileVisible(file: VirtualFile, showHiddenFiles: Boolean) =
//                                    super.isFileVisible(file, showHiddenFiles) &&
//                                        (file.isDirectory || file.extension == "xml" || file.fileType === FileTypes.ARCHIVE)
//
//                                override fun isFileSelectable(file: VirtualFile) = file.fileType === XmlFileType.INSTANCE
//                            }.apply {
//                                description = message("action.importTemplates.wrapper.description")
//                                title = message("action.importTemplates.wrapper")
//                                putUserData(
//                                    LangDataKeys.MODULE_CONTEXT,
//                                    LangDataKeys.MODULE.getData(event.dataContext)
//                                )
//                            }
//
//                            FileChooser.chooseFile(descriptor, templatesListPanel, null, null)?.let { file ->
//                                try {
//                                    val element = JDOMUtil.load(file.inputStream)
//                                    val templates = CodeownersSettings.loadTemplates(element)
//                                    templates.forEach { myListModel.addElement(it) }
//                                    Messages.showInfoMessage(
//                                        templatesListPanel,
//                                        message("action.importTemplates.success", templates.size),
//                                        message("action.exportTemplates.success.title")
//                                    )
//                                    return
//                                } catch (e: IOException) {
//                                    e.printStackTrace()
//                                } catch (e: JDOMException) {
//                                    e.printStackTrace()
//                                }
//                            }
//                            Messages.showErrorDialog(templatesListPanel, message("action.importTemplates.error"))
//                        }
//                    }
//                )
//                add(
//                    object : AnAction(
//                        message("action.exportTemplates"),
//                        message("action.exportTemplates.description"),
//                        AllIcons.ToolbarDecorator.Export
//                    ) {
//                        override fun actionPerformed(event: AnActionEvent) {
//                            FileChooserFactory.getInstance().createSaveFileDialog(
//                                FileSaverDescriptor(
//                                    message("action.exportTemplates.wrapper"),
//                                    "",
//                                    "xml"
//                                ),
//                                templatesListPanel
//                            ).save(null as VirtualFile?, null)?.let { wrapper ->
//                                val items = currentItems
//                                val document = Document(CodeownersSettings.createTemplatesElement(items))
//                                try {
//                                    JDOMUtil.writeDocument(document, wrapper.file, Constants.NEWLINE)
//                                    Messages.showInfoMessage(
//                                        templatesListPanel,
//                                        message("action.exportTemplates.success", items.size),
//                                        message("action.exportTemplates.success.title")
//                                    )
//                                } catch (e: IOException) {
//                                    Messages.showErrorDialog(
//                                        templatesListPanel,
//                                        message("action.exportTemplates.error")
//                                    )
//                                }
//                            }
//                        }
//
//                        override fun update(e: AnActionEvent) {
//                            e.presentation.isEnabled = currentItems.isNotEmpty()
//                        }
//                    }
//                )
//            }
//            decorator.setActionGroup(group)
//        }
//
//        override fun findItemToAdd() = showEditDialog(UserTemplate())
//
//        private fun showEditDialog(initialValue: UserTemplate): UserTemplate? {
//            Messages.showInputDialog(
//                this,
//                message("settings.userTemplates.dialogDescription"),
//                message("settings.userTemplates.dialogTitle"),
//                Messages.getQuestionIcon(),
//                initialValue.name,
//                object : InputValidatorEx {
//                    override fun checkInput(inputString: String) = !StringUtil.isEmpty(inputString)
//
//                    override fun canClose(inputString: String) = !StringUtil.isEmpty(inputString)
//
//                    override fun getErrorText(inputString: String) =
//                        message("settings.userTemplates.dialogError").takeUnless { checkInput(inputString) }
//                }
//            )?.let {
//                initialValue.name = it
//            }
//
//            return initialValue.takeUnless { initialValue.isEmpty }
//        }
//
//        fun resetForm(userTemplates: List<UserTemplate>) {
//            myListModel.clear()
//            userTemplates.forEach { (name, content) ->
//                myListModel.addElement(UserTemplate(name, content))
//            }
//        }
//
//        override fun editSelectedItem(item: UserTemplate) = showEditDialog(item)
//
//        val list
//            get() = myListModel.elements().toList()
//
//        /**
//         * Updates editor component with given content.
//         *
//         * @param content new content
//         */
//        fun updateContent(content: String?) {
//            currentItem?.content = content ?: ""
//        }
//
//        private val currentItem: UserTemplate?
//            get() {
//                val index = myList.selectedIndex
//                return if (index == -1) {
//                    null
//                } else myListModel[index]
//            }
//
//        /**
//         * Returns selected [CodeownersSettings.UserTemplate] elements.
//         *
//         * @return [CodeownersSettings.UserTemplate] list
//         */
//        val currentItems
//            get() = myList.selectedIndices.indices.map { list[it] }
//
//        /** Constructs CRUD panel with list listener for editor updating. */
//        init {
//            myList.addListSelectionListener {
//                val enabled = myListModel.size() > 0
//                editorPanel.isEnabled = enabled
//                if (enabled) {
//                    editorPanel.setContent(currentItem?.content ?: "")
//                }
//            }
//        }
//    }
//
//    /** Editor panel class that displays document editor or label if no template is selected. */
//    private inner class EditorPanel : JPanel(BorderLayout()) {
////        /** Preview editor. */
////        val preview: Editor
//
//        /** `No templates is selected` label. */
//        private val label = JBLabel(message("settings.userTemplates.noTemplateSelected"), JBLabel.CENTER)
////
////        /** Preview document. */
////        private val previewDocument = EditorFactory.getInstance().createDocument("")
//
//        /**
//         * Shows or hides label and editor.
//         *
//         * @param enabled if true shows editor, else shows label
//         */
//        override fun setEnabled(enabled: Boolean) {
//            if (enabled) {
//                remove(label)
////                add(preview.component)
//            } else {
//                add(label)
////                remove(preview.component)
//            }
//            revalidate()
//            repaint()
//        }
////
////        /**
////         * Sets new content to the editor component.
////         *
////         * @param content new content
////         */
////        fun setContent(content: String) {
////            ApplicationManager.getApplication().runWriteAction {
////                CommandProcessor.getInstance()
////                    .runUndoTransparentAction { previewDocument.replaceString(0, previewDocument.textLength, content) }
////            }
////        }
//
//        /** Constructor that creates document editor, empty content label. */
//        init {
////            preview = createPreviewEditor(previewDocument, null, false).apply {
////                document.addDocumentListener(
////                    object : DocumentListener {
////                        override fun documentChanged(event: DocumentEvent) {
////                            templatesListPanel.updateContent(event.document.text)
////                        }
////                    }
////                )
////            }
//            isEnabled = false
//        }
//    }
//
//    /** Languages table helper class. */
//    class LanguagesTableModel : AbstractTableModel() {
//        val settings = CodeownersLanguagesSettings()
//
//        private val columnNames = arrayOf(
//            message("settings.languagesSettings.table.name"),
//            message("settings.languagesSettings.table.newFile"),
//            message("settings.languagesSettings.table.enable")
//        )
//
//        private val columnClasses = arrayOf(
//            String::class.java,
//            Boolean::class.java,
//            Boolean::class.java
//        )
//
//        override fun getRowCount() = settings.size
//
//        override fun getColumnCount() = columnNames.size
//
//        override fun getColumnName(column: Int) = columnNames[column]
//
//        override fun getColumnClass(columnIndex: Int) = columnClasses[columnIndex]
//
//        override fun isCellEditable(row: Int, column: Int) =
//            column > 0 || (column == 2 && settings.keys.toList()[row]?.let(CodeownersBundle::isExcludedFromHighlighting) ?: false)
//
//        override fun getValueAt(row: Int, column: Int): Any {
//            val language = settings.keys.toList()[row] ?: return false
//            val data = settings[language]
//            return when (column) {
//                NAME_COLUMN -> language.id
//                NEW_FILE_COLUMN -> getBoolean(CodeownersLanguagesSettings.KEY.NEW_FILE, data)
//                ENABLE_COLUMN -> getBoolean(CodeownersLanguagesSettings.KEY.ENABLE, data)
//                else -> throw IllegalArgumentException()
//            }
//        }
//
//        private fun getBoolean(key: CodeownersLanguagesSettings.KEY, data: TreeMap<CodeownersLanguagesSettings.KEY, Any>?) =
//            data?.get(key).toString().toBoolean()
//
//        override fun setValueAt(value: Any, row: Int, column: Int) {
//            val language = settings.keys.toList()[row]
//            val data = settings[language]
//            when (column) {
//                NEW_FILE_COLUMN -> {
//                    data?.set(CodeownersLanguagesSettings.KEY.NEW_FILE, value)
//                    return
//                }
//                ENABLE_COLUMN -> {
//                    data?.set(CodeownersLanguagesSettings.KEY.ENABLE, value)
//                    return
//                }
//            }
//            throw IllegalArgumentException()
//        }
//
//        fun update(settings: CodeownersLanguagesSettings) {
//            this.settings.apply {
//                clear()
//                putAll(settings)
//            }
//        }
//
//        fun equalSettings(settings: CodeownersLanguagesSettings) = this.settings == settings
//    }
}
