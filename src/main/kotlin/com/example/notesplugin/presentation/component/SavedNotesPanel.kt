package com.example.notesplugin.presentation.component

import com.example.notesplugin.domain.model.Snippet
import com.example.notesplugin.presentation.renderer.SnippetCardRenderer
import com.example.notesplugin.service.SnippetService
import com.intellij.openapi.project.Project
import java.awt.*
import java.awt.event.MouseAdapter
import java.awt.event.MouseEvent
import javax.swing.*

class SavedNotesPanel(
    private val project: Project,
    private val onEditSnippet: (Snippet) -> Unit
) : JPanel(BorderLayout()) {

    private val listModel = DefaultListModel<Snippet>()

    private val snippetList = JList(listModel).apply {
        selectionMode = ListSelectionModel.SINGLE_SELECTION
        visibleRowCount = -1
        fixedCellHeight = -1
        cellRenderer = SnippetCardRenderer()
    }

    private val emptyLabel = JLabel("📭 No snippets saved yet.").apply {
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

    init {
        add(container, BorderLayout.CENTER)
        snippetList.addMouseListener(object : MouseAdapter() {
            override fun mouseClicked(e: MouseEvent) {
                if (e.clickCount == 2 && !e.isConsumed) {
                    e.consume()
                    val selectedSnippet = snippetList.selectedValue
                    if (selectedSnippet != null) {
                        onEditSnippet(selectedSnippet)
                    }
                }
            }
        })
    }

    fun refreshList() {
        val snippets = SnippetService.getInstance(project).state.snippets
        if (snippets.isEmpty()) {
            listModel.clear()
            cardLayout.show(container, "empty")
        } else {
            listModel.clear()
            snippets.forEach { listModel.addElement(it) }
            cardLayout.show(container, "list")
        }

        revalidate()
        repaint()
    }

    override fun addNotify() {
        super.addNotify()
        refreshList()
    }
}