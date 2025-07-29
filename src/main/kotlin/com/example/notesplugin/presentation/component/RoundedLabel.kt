package com.example.notesplugin.presentation.component

import com.intellij.ui.JBColor
import java.awt.Dimension
import java.awt.Graphics
import java.awt.Graphics2D
import java.awt.RenderingHints
import javax.swing.JLabel

class RoundedLabel : JLabel() {

    var cornerRadius = 12

    override fun paintComponent(g: Graphics) {
        val g2 = g.create() as Graphics2D
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON)
        g2.color = background
        g2.fillRoundRect(0, 0, width, height, cornerRadius, cornerRadius)
        super.paintComponent(g)
        g2.dispose()
    }

    override fun getPreferredSize(): Dimension {
        val size = super.getPreferredSize()
        size.width += cornerRadius
        size.height += cornerRadius / 2
        return size
    }

    init {
        isOpaque = false
        foreground = JBColor.WHITE
        horizontalAlignment = CENTER
        verticalAlignment = CENTER
        border = javax.swing.BorderFactory.createEmptyBorder(4, 8, 4, 8)
    }
}