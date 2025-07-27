package com.example.notesplugin.presentation.component

import com.example.notesplugin.domain.model.Snippet
import com.example.notesplugin.service.SnippetService
import com.intellij.openapi.project.Project
import java.awt.BorderLayout
import javax.swing.*

class SavedNotesPanel(
    private val project: Project,
    private val onSnippetSelected: (Snippet) -> Unit
) : JPanel(BorderLayout()) {

    private val listModel = DefaultListModel<Snippet>()
    private val snippetList = JList(listModel).apply {
        cellRenderer = object : JLabel(), ListCellRenderer<Snippet> {
            override fun getListCellRendererComponent(
                list: JList<out Snippet>,
                value: Snippet,
                index: Int,
                isSelected: Boolean,
                cellHasFocus: Boolean
            ): java.awt.Component {
                text = value.title
                background = if (isSelected) list.selectionBackground else list.background
                foreground = if (isSelected) list.selectionForeground else list.foreground
                isOpaque = true
                return this
            }
        }
    }
    private val emptyLabel = JLabel("📭 No snippets saved yet.").apply {
        horizontalAlignment = JLabel.CENTER
        verticalAlignment = JLabel.CENTER
    }
    private val listScrollPane = JScrollPane(snippetList)

    init {
        snippetList.addListSelectionListener {
            if (!it.valueIsAdjusting) {
                snippetList.selectedValue?.let(onSnippetSelected)
            }
        }
    }

    fun refreshList() {
        listModel.clear()
        remove(listScrollPane)
        remove(emptyLabel)
        val snippets = SnippetService.getInstance(project).state.snippets
        if (snippets.isEmpty()) {
            remove(listScrollPane)
            add(emptyLabel, BorderLayout.CENTER)
        } else {
            remove(emptyLabel)
            listModel.addAll(snippets)
            add(listScrollPane, BorderLayout.CENTER)
        }

        revalidate()
        repaint()
    }
    override fun addNotify() {
        super.addNotify()
        refreshList()
    }
}