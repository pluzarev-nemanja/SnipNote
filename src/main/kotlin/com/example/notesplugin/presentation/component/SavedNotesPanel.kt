package com.example.notesplugin.presentation.component

import com.example.notesplugin.domain.model.Snippet
import com.example.notesplugin.facade.SnippetLabelFacade
import com.example.notesplugin.presentation.controller.SnippetListController
import com.example.notesplugin.presentation.renderer.SnippetCardRenderer
import com.example.notesplugin.presentation.util.CodeLanguage
import com.example.notesplugin.service.SnippetService
import com.intellij.icons.AllIcons
import com.intellij.openapi.project.Project
import com.intellij.openapi.ui.ComboBox
import com.intellij.ui.ColoredListCellRenderer
import java.awt.*
import java.awt.event.FocusAdapter
import java.awt.event.FocusEvent
import java.awt.event.MouseAdapter
import java.awt.event.MouseEvent
import javax.swing.*

class SavedNotesPanel(
    private val facade: SnippetLabelFacade,
    private val onEditSnippet: (Snippet) -> Unit
) : JPanel(BorderLayout()) {

    private val listModel = DefaultListModel<Snippet>()
    private val controller = SnippetListController()

    private val snippetList = JList(listModel).apply {
        selectionMode = ListSelectionModel.SINGLE_SELECTION
        visibleRowCount = -1
        fixedCellHeight = -1
        cellRenderer = SnippetCardRenderer()
    }

    private val emptyLabel = JLabel("📭 No snippets found.").apply {
        horizontalAlignment = SwingConstants.CENTER
        verticalAlignment = SwingConstants.CENTER
        font = Font("Segoe UI", Font.PLAIN, 14)
    }

    private val scrollPane = JScrollPane(snippetList).apply {
        border = BorderFactory.createEmptyBorder()
        verticalScrollBar.unitIncrement = 16
    }

    private val cardLayout = CardLayout()
    private val container = JPanel(cardLayout).apply {
        add(emptyLabel, "empty")
        add(scrollPane, "list")
    }

    private val searchField = JTextField().apply {
        toolTipText = "Search snippets..."
    }
    val searchIconLabel = JLabel(AllIcons.Actions.Search).apply {
        border = BorderFactory.createEmptyBorder(0, 4, 0, 4)
    }
    private val allowedLanguages = listOf("All") + CodeLanguage.entries.map { it.displayName }
    private val languageComboBox = ComboBox(allowedLanguages.toTypedArray())

    init {
        val searchWithIconPanel = JPanel(BorderLayout()).apply {
            add(searchField, BorderLayout.CENTER)
            add(searchIconLabel, BorderLayout.EAST)
        }

        val leftPanel = JPanel()
        leftPanel.layout = BoxLayout(leftPanel, BoxLayout.Y_AXIS)
        leftPanel.add(searchWithIconPanel)

        val rightPanel = JPanel()
        rightPanel.layout = BoxLayout(rightPanel, BoxLayout.Y_AXIS)
        rightPanel.border = BorderFactory.createEmptyBorder(0, 10, 0, 0)
        languageComboBox.maximumSize = Dimension(150, languageComboBox.preferredSize.height)
        rightPanel.add(languageComboBox)

        val topPanel = JPanel()
        topPanel.layout = BoxLayout(topPanel, BoxLayout.X_AXIS)
        topPanel.border = BorderFactory.createEmptyBorder(8, 8, 8, 8)
        topPanel.add(leftPanel)
        topPanel.add(Box.createHorizontalGlue())
        topPanel.add(rightPanel)

        add(topPanel, BorderLayout.NORTH)
        add(container, BorderLayout.CENTER)

        snippetList.addMouseListener(object : MouseAdapter() {
            override fun mouseClicked(e: MouseEvent) {
                if (e.clickCount == 2 && !e.isConsumed) {
                    e.consume()
                    snippetList.selectedValue?.let { onEditSnippet(it) }
                }
            }
        })
        searchField.document.addDocumentListener(object : javax.swing.event.DocumentListener {
            override fun insertUpdate(e: javax.swing.event.DocumentEvent) = applyFilter()
            override fun removeUpdate(e: javax.swing.event.DocumentEvent) = applyFilter()
            override fun changedUpdate(e: javax.swing.event.DocumentEvent) = applyFilter()
        })

        setupLanguageComboBox()
    }

    private fun setupLanguageComboBox() {
        languageComboBox.renderer = object : ColoredListCellRenderer<String>() {
            override fun customizeCellRenderer(
                list: JList<out String>,
                value: String?,
                index: Int,
                selected: Boolean,
                hasFocus: Boolean
            ) {
                append(value ?: "")
            }
        }
        languageComboBox.addActionListener {
            val selectedLanguage = languageComboBox.selectedItem as? String
            controller.updateLanguage(if (selectedLanguage == "All") null else selectedLanguage)
            applyFilter()
        }
    }

    private fun applyFilter() {
        val query = searchField.text
        val filtered = controller.filter(query)
        listModel.clear()
        if (filtered.isEmpty()) {
            cardLayout.show(container, "empty")
        } else {
            filtered.forEach { listModel.addElement(it) }
            cardLayout.show(container, "list")
        }
    }

    fun refreshList() {
        val snippets = facade.getAllSnippets()
        controller.setSnippets(snippets)
        applyFilter()
        revalidate()
        repaint()
    }

    override fun addNotify() {
        super.addNotify()
        refreshList()
    }
}