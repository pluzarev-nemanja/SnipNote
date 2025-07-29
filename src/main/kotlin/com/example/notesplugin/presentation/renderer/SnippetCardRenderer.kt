package com.example.notesplugin.presentation.renderer

import com.example.notesplugin.domain.model.Snippet
import com.example.notesplugin.presentation.component.RoundedLabel
import com.example.notesplugin.presentation.util.CodeLanguage
import com.intellij.openapi.editor.colors.EditorColorsManager
import com.intellij.openapi.fileTypes.FileTypeManager
import com.intellij.openapi.project.Project
import com.intellij.ui.EditorTextField
import com.intellij.ui.JBColor
import com.intellij.util.ui.JBUI
import java.awt.*
import javax.swing.*

class SnippetCardRenderer(
    project: Project,
    private val onEditClick: (Snippet) -> Unit
) : ListCellRenderer<Snippet> {

    private var snippetContentEditor: EditorTextField = EditorTextField(
        "",
        project,
        FileTypeManager.getInstance().getFileTypeByExtension(CodeLanguage.KOTLIN.fileExtension)
    ).apply {
        addSettingsProvider { editor ->
            editor.isOneLineMode = false
            editor.setCaretEnabled(true)
            editor.colorsScheme = EditorColorsManager.getInstance().globalScheme
            editor.settings.apply {
                isLineNumbersShown = true
                isFoldingOutlineShown = true
                isUseSoftWraps = true
                isViewer = false
            }
        }
    }

    override fun getListCellRendererComponent(
        list: JList<out Snippet>,
        value: Snippet,
        index: Int,
        isSelected: Boolean,
        cellHasFocus: Boolean
    ): Component {
        val panel = JPanel(BorderLayout()).apply {
            border = JBUI.Borders.empty(8)
            background = if (isSelected) JBColor.LIGHT_GRAY else JBColor.WHITE
        }

        val headerPanel = JPanel(BorderLayout()).apply {
            background = panel.background
        }

        val titleLabel = JLabel(value.title).apply {
            font = Font("Segoe UI", Font.BOLD, 14)
        }

        val languageLabel = RoundedLabel().apply {
            text = value.languageName
            background = value.languageColor
            foreground = Color.WHITE
            font = Font("Segoe UI", Font.PLAIN, 12)
            horizontalAlignment = SwingConstants.CENTER
        }

        val editButton = JButton("Edit").apply {
            font = Font("Segoe UI", Font.PLAIN, 12)
            preferredSize = Dimension(80, 28)
            addActionListener { onEditClick(value) }
        }

        val rightPanel = JPanel(FlowLayout(FlowLayout.RIGHT, 6, 0)).apply {
            background = panel.background
            add(languageLabel)
            add(editButton)
        }

        headerPanel.add(titleLabel, BorderLayout.WEST)
        headerPanel.add(rightPanel, BorderLayout.EAST)

        val descriptionArea = JTextArea().apply {
            text = value.content
            font = Font("Segoe UI", Font.PLAIN, 12)
            lineWrap = true
            wrapStyleWord = true
            rows = 8
            isEditable = false
            isFocusable = false
            border = null
            background = panel.background
        }

        panel.add(headerPanel, BorderLayout.NORTH)
        panel.add(descriptionArea, BorderLayout.CENTER)

        return panel
    }
}