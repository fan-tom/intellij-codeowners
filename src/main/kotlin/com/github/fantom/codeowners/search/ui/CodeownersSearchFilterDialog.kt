package com.github.fantom.codeowners.search.ui

import com.github.fantom.codeowners.CodeownersManager
import com.github.fantom.codeowners.indexing.CodeownersEntryOccurrence
import com.github.fantom.codeowners.search.Filter
import com.intellij.openapi.components.service
import com.intellij.openapi.project.Project
import com.intellij.openapi.ui.ComboBox
import com.intellij.openapi.ui.DialogWrapper
import com.intellij.ui.CollectionComboBoxModel
import com.intellij.ui.ScrollPaneFactory
import java.awt.BorderLayout
import java.awt.event.ItemEvent
import javax.swing.BorderFactory
import javax.swing.JComponent
import javax.swing.JPanel

class CodeownersSearchFilterDialog(
    project: Project,
    codeownersFiles: List<CodeownersEntryOccurrence>, // must be not empty
) : DialogWrapper(project) {
    private var contentPane: JPanel
    private var codeownersFileChooser = CodeownersFileChooser(codeownersFiles)

    private var orsPanel: OrsPanel

    var result: Pair<CodeownersEntryOccurrence, List<List<Filter>>>? = null
    private set

    private val manager = project.service<CodeownersManager>()

    private fun getOwners() = codeownersFileChooser
        .getChosenFile()
        .let(manager::getMentionedOwners)

    private fun createOrsPanel(): JPanel {
        val container = JPanel().apply { layout = BorderLayout() }
        container.add(orsPanel, BorderLayout.NORTH)
        return container
    }

    init {
        orsPanel = OrsPanel(getOwners())

        val scrollPanel = ScrollPaneFactory.createScrollPane(createOrsPanel())

        codeownersFileChooser.addItemListener {
            if (it.stateChange == ItemEvent.SELECTED) {
                orsPanel = OrsPanel(getOwners())
                scrollPanel.setViewportView(createOrsPanel())
                scrollPanel.revalidate()
                scrollPanel.repaint()
            }
        }

        // setup content panel
        contentPane = JPanel().also { cp ->
            cp.layout = BorderLayout() // to not let nested panels stretch vertically

            cp.add(JPanel()
                .also {
                    it.layout = BorderLayout() // to make combobox stretch horizontally
                    it.border = BorderFactory.createTitledBorder("Codeowners file")
                    it.add(codeownersFileChooser)
                },
                BorderLayout.NORTH,
            )
            cp.add(scrollPanel, BorderLayout.CENTER)
        }

        init()
    }

    override fun doOKAction() {
        result = Pair((codeownersFileChooser.getChosenFile()), orsPanel.getOrs())
        super.doOKAction()
    }

    override fun createCenterPanel(): JComponent {
        return contentPane
    }
}

private class CodeownersFileChooser(
    codeownersFiles: List<CodeownersEntryOccurrence>,
): ComboBox<CodeownersFileChooser.CodeownersEntryOccurrenceWrapper>() {
    init {
        toolTipText = "Select a CODEOWNERS file used to calculate ownership"
        model = CollectionComboBoxModel(codeownersFiles.map(::CodeownersEntryOccurrenceWrapper))
    }

    fun getChosenFile() = (selectedItem as CodeownersEntryOccurrenceWrapper).codeownersEntryOccurrence

    class CodeownersEntryOccurrenceWrapper(val codeownersEntryOccurrence: CodeownersEntryOccurrence) {
        override fun toString(): String {
            return codeownersEntryOccurrence.url
        }
    }
}
