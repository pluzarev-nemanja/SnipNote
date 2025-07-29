package com.example.notesplugin.domain.model

import com.intellij.ui.JBColor
import java.awt.Color

data class Snippet(
    val title: String = "",
    val content: String = "",
    val languageName: String = "",
    val languageColor: Color = JBColor.GRAY
)
