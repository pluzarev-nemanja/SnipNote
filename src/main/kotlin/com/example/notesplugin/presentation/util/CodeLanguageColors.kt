package com.example.notesplugin.presentation.util

import com.intellij.ui.JBColor
import java.awt.Color

object CodeLanguageColors {
    private val defaultColor = JBColor(0x777777, 0x999999)

    val colors: Map<CodeLanguage, Color> = mapOf(
        CodeLanguage.KOTLIN to JBColor(0x7F52FF, 0x9B6BFF),
        CodeLanguage.JAVA to JBColor(0xF89820, 0xFFB347),
        CodeLanguage.PYTHON to JBColor(0x3776AB, 0x6C8CD5),
        CodeLanguage.JAVASCRIPT to JBColor(0xF7DF1E, 0xFCE87C),
        CodeLanguage.TYPESCRIPT to JBColor(0x3178C6, 0x6FA6FF),
        CodeLanguage.HTML to JBColor(0xE44D26, 0xF07154),
        CodeLanguage.CSS to JBColor(0x264DE4, 0x4A6CF7),
        CodeLanguage.SCSS to JBColor(0xCC6699, 0xE58EBF),
        CodeLanguage.JSON to JBColor(0x292929, 0x5A5A5A),
        CodeLanguage.XML to JBColor(0x0060AC, 0x3B7DC1),
        CodeLanguage.YAML to JBColor(0xFFCD00, 0xFFE066),
        CodeLanguage.MARKDOWN to JBColor(0x083FA1, 0x3B5DBB),
        CodeLanguage.SQL to JBColor(0xF29111, 0xFFB347),
        CodeLanguage.SHELL to JBColor(0x4EAA25, 0x79C95A),
        CodeLanguage.BASH to JBColor(0x4EAA25, 0x79C95A),
        CodeLanguage.C to JBColor(0xA8B9CC, 0xC4D0E8),
        CodeLanguage.CPP to JBColor(0x00599C, 0x4C92C9),
        CodeLanguage.CSHARP to JBColor(0x239120, 0x4EB94D),
        CodeLanguage.RUST to JBColor(0xDEA584, 0xD7A06F),
        CodeLanguage.GO to JBColor(0x00ADD8, 0x4CC3D9),
        CodeLanguage.RUBY to JBColor(0xCC342D, 0xE57373),
        CodeLanguage.PHP to JBColor(0x8993BE, 0xB3BCE6),
        CodeLanguage.SWIFT to JBColor(0xFA7343, 0xFF9B7A),
        CodeLanguage.OBJECTIVE_C to JBColor(0x438EFF, 0x7FA9FF),
        CodeLanguage.PERL to JBColor(0x39457E, 0x6473B9),
        CodeLanguage.LUA to JBColor(0x000080, 0x5050A5),
        CodeLanguage.GROOVY to JBColor(0x4298B8, 0x7EBFD3),
        CodeLanguage.DART to JBColor(0x00B4AB, 0x5CE0DD),
        CodeLanguage.HASKELL to JBColor(0x5E5086, 0x8A7FC8),
        CodeLanguage.SCALA to JBColor(0xDC322F, 0xE57373),
        CodeLanguage.R to JBColor(0x276DC3, 0x6496F5)
    )

    fun getColor(language: CodeLanguage): Color {
        return colors[language] ?: defaultColor
    }
}