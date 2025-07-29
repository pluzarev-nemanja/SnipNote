package com.example.notesplugin.presentation.component

import com.example.notesplugin.domain.model.Snippet
import com.example.notesplugin.presentation.renderer.SnippetCardRenderer
import com.example.notesplugin.service.SnippetService
import com.intellij.openapi.project.Project
import java.awt.BorderLayout
import java.awt.CardLayout
import javax.swing.*

class SavedNotesPanel(
    private val project: Project,
    private val onSnippetSelected: (Snippet) -> Unit
) : JPanel(BorderLayout()) {

    private val listModel = DefaultListModel<Snippet>()
    private val snippetList = JList(listModel).apply {
        cellRenderer = SnippetCardRenderer(project) { snippet ->
            onSnippetSelected(snippet)
        }
    }
    private val emptyLabel = JLabel("📭 No snippets saved yet.").apply {
        horizontalAlignment = JLabel.CENTER
        verticalAlignment = JLabel.CENTER
    }
    private val listScrollPane = JScrollPane(snippetList)
    private val cardLayout = CardLayout()
    private val container = JPanel(cardLayout)

    init {
        container.add(emptyLabel, "empty")
        container.add(listScrollPane, "list")
        add(container, BorderLayout.CENTER)
    }

    fun refreshList() {
        val snippets = SnippetService.getInstance(project).state.snippets

        if (snippets.isEmpty()) {
            cardLayout.show(container, "empty")
        } else {
            listModel.clear()
            snippets.forEach { listModel.addElement(it) }
            cardLayout.show(container, "list")
        }
    }
}