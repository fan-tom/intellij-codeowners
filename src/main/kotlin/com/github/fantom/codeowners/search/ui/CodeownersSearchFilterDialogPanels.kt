package com.github.fantom.codeowners.search.ui

import com.github.fantom.codeowners.OwnersSet
import com.github.fantom.codeowners.indexing.OwnerString
import com.github.fantom.codeowners.search.Filter
import com.github.fantom.codeowners.search.ui.FilterConditionType.*
import com.intellij.openapi.ui.ComboBox
import com.intellij.ui.CheckBoxList
import com.intellij.ui.EnumComboBoxModel
import com.intellij.ui.ScrollPaneFactory
import com.intellij.ui.SimpleListCellRenderer
import com.intellij.ui.components.JBCheckBox
import java.awt.BorderLayout
import java.awt.event.ItemEvent
import javax.swing.*

class OrsPanel(owners: OwnersSet) : JPanel() {
    private val andsPanels = mutableListOf<AndsPanel>()

    private var andsPanelsPanel: JPanel // contains ands panels and (+ or) button

    private val addAndsButton: JButton

    init {
        layout = BoxLayout(this, BoxLayout.Y_AXIS) // we will have an unpredictable number of children
        border = BorderFactory.createTitledBorder("Ors")

        val andsPanel = createAndRegisterAndsPanel(owners)
        this.add(andsPanel)

        andsPanelsPanel = JPanel().apply {
            layout = BoxLayout(this, BoxLayout.Y_AXIS) // we will have an unpredictable number of children
        }
        andsPanelsPanel.add(andsPanel, BorderLayout.NORTH)

        add(andsPanelsPanel, BorderLayout.NORTH)

        addAndsButton = createAndRegisterAddAndsButton(owners)
        add(addAndsButton, BorderLayout.SOUTH)
    }

    private fun createAndRegisterAndsPanel(owners: OwnersSet): AndsPanel {
        val andsPanel = AndsPanel(owners)
        andsPanels.add(andsPanel)
        return andsPanel
    }

    private fun createAndRegisterAddAndsButton(owners: OwnersSet): JButton {
        val addAndsButton = JButton("+ (or)")

        addAndsButton.addActionListener {
            val andsPanel = createAndRegisterAndsPanel(owners)

            val removeButton = JButton("x (remove)")
            removeButton.addActionListener {
                andsPanels.remove(andsPanel)

                andsPanelsPanel.remove(andsPanel)
                andsPanelsPanel.revalidate()
                andsPanelsPanel.repaint()
            }
            andsPanel.add(removeButton, BorderLayout.SOUTH)

            andsPanelsPanel.add(andsPanel)
            andsPanelsPanel.revalidate()
            andsPanelsPanel.repaint()
        }

        return addAndsButton
    }

    fun getOrs(): List<List<Filter>> = andsPanels.map(AndsPanel::getAnds)
}

class AndsPanel(owners: OwnersSet) : JPanel() {
    private val filterPanels = mutableListOf<PrimitiveFilterPanel>()

    private var filtersPanel: JPanel // contains filter panels and + (and) button

    private val addFilterButton: JButton

    init {
        layout = BorderLayout() // don't let children stretch vertically
        border = BorderFactory.createTitledBorder("Ands")

        val filterPanel = createAndRegisterFilterPanel(owners)

        filtersPanel = JPanel().apply {
            layout = BoxLayout(this, BoxLayout.Y_AXIS) // we will have an unpredictable number of children
        }
        filtersPanel.add(filterPanel, BorderLayout.NORTH)

        add(filtersPanel, BorderLayout.NORTH)

        addFilterButton = createAndRegisterAddFilterButton(owners)
        add(addFilterButton, BorderLayout.CENTER)
    }

    private fun createAndRegisterFilterPanel(owners: OwnersSet): PrimitiveFilterPanel {
        val filterPanel = PrimitiveFilterPanel(owners)
        filterPanels.add(filterPanel)
        return filterPanel
    }

    private fun createAndRegisterAddFilterButton(owners: OwnersSet): JButton {
        val addFilterButton = JButton("+ (and)")

        addFilterButton.addActionListener {
            val filterPanel = createAndRegisterFilterPanel(owners)

            val removeButton = JButton("x (remove)")
            removeButton.addActionListener {
                filterPanels.remove(filterPanel)

                filtersPanel.remove(filterPanel)
                filtersPanel.revalidate()
                filtersPanel.repaint()
            }
            filterPanel.add(removeButton)

            filtersPanel.add(filterPanel, BorderLayout.NORTH)
            filtersPanel.revalidate()
            filtersPanel.repaint()
        }

        return addFilterButton
    }

    fun getAnds(): List<Filter> = filterPanels.map(PrimitiveFilterPanel::getFilter)
}

class PrimitiveFilterPanel(
    owners: OwnersSet,
) : JPanel() {
    private val negationCheckbox = JBCheckBox("Not")

    private val filterConditionComboBox = ComboBox(EnumComboBoxModel(FilterConditionType::class.java))

    private val selectedOwnersIdxs = mutableSetOf<Int>()

    private val ownersCheckBoxList = CheckBoxList<OwnerString>().apply {
        setItems(owners.sorted(), null)
        setCheckBoxListListener { idx, enabled ->
            if (enabled) {
                selectedOwnersIdxs.add(idx)
            } else {
                selectedOwnersIdxs.remove(idx)
            }
        }
    }

    private val ownersCheckBoxListScroll: JScrollPane

    init {
        layout = BoxLayout(this, BoxLayout.X_AXIS) // we have 2/3/4 children, placed in a row

        ownersCheckBoxListScroll = ScrollPaneFactory.createScrollPane(ownersCheckBoxList).also {
            // hide until user chooses compatible filter condition
            it.isVisible = false
        }

        filterConditionComboBox.renderer = SimpleListCellRenderer.create("", FilterConditionType::title)
        filterConditionComboBox.toolTipText = (filterConditionComboBox.selectedItem as FilterConditionType).description
        filterConditionComboBox.addItemListener { e ->
            if (e.stateChange == ItemEvent.SELECTED) {
                val item = e.item as FilterConditionType
                ownersCheckBoxListScroll.isVisible = !item.parameterless
                filterConditionComboBox.toolTipText = item.description
            }
        }

        // don't let combobox stretch vertically because of scroll pane
        val container = JPanel().also {
            it.layout = BoxLayout(it, BoxLayout.X_AXIS) // place checkbox and combobox in a row, let them stretch horizontally
            it.add(negationCheckbox)
            it.add(filterConditionComboBox)
        }

        val borderPanel = JPanel().also {
            it.layout = BorderLayout()
            it.add(container, BorderLayout.NORTH) // place panel with checkbox and combobox at the top of the panel, don't let it stretch vertically
        }

        add(borderPanel)
        add(ownersCheckBoxListScroll)
    }

    fun getFilter(): Filter {
        val condition = when (filterConditionComboBox.selectedItem as FilterConditionType) {
            Unowned -> Filter.Condition.Unowned
            OwnershipReset -> Filter.Condition.OwnershipReset
            OwnedByAnyOf -> Filter.Condition.OwnedByAnyOf(selectedOwnersIdxs.map { ownersCheckBoxList.getItemAt(it)!! }.toSet())
            OwnedByAllOf -> Filter.Condition.OwnedByAllOf(selectedOwnersIdxs.map { ownersCheckBoxList.getItemAt(it)!! }.toSet())
            OwnedByExactly -> Filter.Condition.OwnedByExactly(selectedOwnersIdxs.map { ownersCheckBoxList.getItemAt(it)!! }.toSet())
        }

        return if (negationCheckbox.isSelected) {
            Filter.Not(condition)
        } else {
            condition
        }
    }
}

private enum class FilterConditionType(
    val parameterless: Boolean,
    val title: String,
    val description: String,
) {
    Unowned(true, "Unowned", "Files with no defined ownership"),
    OwnershipReset(true, "Explicitly unowned", "Files with reset ownership"),
    OwnedByAnyOf(false, "Owned by any of", "Files, owned by any of selected owners"),
    OwnedByAllOf(false, "Owned by all of", "Files, owned by all selected owners"),
    OwnedByExactly(false, "Owned by exactly", "Files, owned by only selected owners, nobody else"),
    ;

    companion object {
        // for compile-time check, that we covered all filter types here
        @Suppress("unused")
        fun fromDomain(condition: Filter.Condition): FilterConditionType {
            return when (condition) {
                Filter.Condition.Unowned -> Unowned
                Filter.Condition.OwnershipReset -> OwnershipReset
                is Filter.Condition.OwnedByAnyOf -> OwnedByAnyOf
                is Filter.Condition.OwnedByAllOf -> OwnedByAllOf
                is Filter.Condition.OwnedByExactly -> OwnedByExactly
            }
        }
    }
}
