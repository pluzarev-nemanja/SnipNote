package com.example.notesplugin.presentation.renderer

import com.example.notesplugin.domain.model.Snippet
import java.awt.*
import javax.swing.*
import javax.swing.border.EmptyBorder
import kotlin.math.abs
import kotlin.random.Random

class SnippetCardRenderer : ListCellRenderer<Snippet> {
    private val panel = JPanel(BorderLayout()).apply {
        border = EmptyBorder(8, 8, 8, 8)
        background = Color.WHITE
    }
    private val titleLabel = JLabel().apply {
        font = Font("Segoe UI", Font.BOLD, 14)
    }
    private val previewLabel = JLabel().apply {
        font = Font("Segoe UI", Font.PLAIN, 12)
        foreground = Color(0x555555)
    }

    override fun getListCellRendererComponent(
        list: JList<out Snippet>,
        value: Snippet,
        index: Int,
        isSelected: Boolean,
        cellHasFocus: Boolean
    ): Component {
        panel.removeAll()
        val avatar = createCircleIcon()
        avatar.alignmentY = Component.TOP_ALIGNMENT
        val headerPanel = JPanel(BorderLayout()).apply {
            isOpaque = false
            add(avatar, BorderLayout.WEST)
            add(titleLabel, BorderLayout.CENTER)
        }
        val textPanel = JPanel()
            .apply {
                layout = BoxLayout(this, BoxLayout.Y_AXIS)
                background = panel.background
                add(titleLabel.apply { text = value.title })
                add(Box.createVerticalStrut(4))
                val preview = value.content.lines().firstOrNull()?.take(60)?.let { "$it…" } ?: ""
                previewLabel.text = "<html><body style='width:200px;'>$preview</body></html>"
                add(previewLabel)
            }

        panel.add(textPanel, BorderLayout.CENTER)

        panel.border = BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(Color(0xDDDDDD)),
            EmptyBorder(8, 8, 8, 8)
        )

        if (isSelected) {
            panel.background = list.selectionBackground
            textPanel.background = list.selectionBackground
            titleLabel.foreground = list.selectionForeground
            previewLabel.foreground = list.selectionForeground
        } else {
            panel.background = Color.WHITE
            textPanel.background = Color.WHITE
            titleLabel.foreground = Color(0x222222)
            previewLabel.foreground = Color(0x555555)
        }

        return panel
    }
    private fun createCircleIcon(): JComponent {
        return object : JComponent() {
            private val color = Color(
                (50..200).random(),
                (50..200).random(),
                (50..200).random()
            )

            override fun getPreferredSize(): Dimension = Dimension(12, 12)

            override fun paintComponent(g: Graphics) {
                super.paintComponent(g)
                val g2 = g as Graphics2D
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON)
                g2.color = color
                g2.fillOval(0, 0, width, height)
            }
        }
    }
}