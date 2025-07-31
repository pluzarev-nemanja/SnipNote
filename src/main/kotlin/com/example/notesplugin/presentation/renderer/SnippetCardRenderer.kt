package com.example.notesplugin.presentation.renderer

import com.example.notesplugin.domain.model.Snippet
import com.example.notesplugin.presentation.component.RoundedLabel
import com.intellij.openapi.project.Project
import com.intellij.ui.JBColor
import com.intellij.util.ui.JBUI
import java.awt.*
import javax.swing.*
import javax.swing.border.LineBorder

class SnippetCardRenderer(
    private val onEditClick: (Snippet) -> Unit
) : ListCellRenderer<Snippet> {

    override fun getListCellRendererComponent(
        list: JList<out Snippet>,
        value: Snippet,
        index: Int,
        isSelected: Boolean,
        cellHasFocus: Boolean
    ): Component {
        val panel = JPanel(BorderLayout(0, 6)).apply {
            border = JBUI.Borders.empty(10, 12)
            background = if (isSelected) JBColor(0xF0F6FF, 0x2B2B2B) else JBColor.WHITE
        }

        val titleLabel = JLabel(value.title).apply {
            font = Font("Segoe UI", Font.BOLD, 15)
            foreground = if (isSelected) Snippet.getColor(value.languageColor) else JBColor(0x111827, 0xE5E7EB)
        }

        val languageLabel = RoundedLabel().apply {
            text = value.languageName
            background = Snippet.getColor(value.languageColor)
            foreground = Color.WHITE
            font = Font("Segoe UI", Font.PLAIN, 11)
            horizontalAlignment = SwingConstants.CENTER
            border = JBUI.Borders.empty(2, 6)
        }

        val editButton = JButton("✏ Edit").apply {
            font = Font("Segoe UI", Font.PLAIN, 12)
            background = JBColor(0xE5E7EB, 0x3C3F41)
            border = LineBorder(JBColor(0xD1D5DB, 0x555555))
            preferredSize = Dimension(80, 28)
            isFocusPainted = false
            addActionListener { onEditClick(value) }
        }

        val codePreview = JTextArea().apply {
            isEditable = false
            isOpaque = true
            font = Font("Monospaced", Font.ITALIC, 12)
            lineWrap = true
            wrapStyleWord = true
            background = if (isSelected) JBColor(0xF0F6FF, 0x2B2B2B) else JBColor.WHITE
            border = JBUI.Borders.empty(4)
        }

        val rightPanel = JPanel(FlowLayout(FlowLayout.RIGHT, 8, 0)).apply {
            background = panel.background
            add(languageLabel)
            add(editButton)
        }

        val headerPanel = JPanel(BorderLayout()).apply {
            background = panel.background
            add(titleLabel, BorderLayout.WEST)
            add(rightPanel, BorderLayout.EAST)
        }

        codePreview.text = formatQuotedPreview(value)

        panel.add(headerPanel, BorderLayout.NORTH)
        panel.add(codePreview, BorderLayout.CENTER)

        panel.border = BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(
                if (isSelected) Snippet.getColor(value.languageColor) else JBColor(0xE5E7EB, 0x555555), 1
            ),
            JBUI.Borders.empty(8)
        )

        return panel
    }

    private fun formatQuotedPreview(snippet: Snippet, maxLines: Int = 4): String {
        val lines = snippet.content.lines()
        val preview = lines.take(maxLines).joinToString("\n")
        val isTruncated = lines.size > maxLines
        return "“$preview${if (isTruncated) "\n..." else ""}”"
    }
}