package com.example.notesplugin.domain.model

import java.awt.Color

data class Snippet(
    var title: String = "",
    var content: String = "",
    var languageName: String = "",
    var languageColor: String = "#888888",
    var labels: List<String> = emptyList()
) {
    companion object {
        fun getColor(languageColor: String): Color = Color.decode(languageColor)
    }
}
