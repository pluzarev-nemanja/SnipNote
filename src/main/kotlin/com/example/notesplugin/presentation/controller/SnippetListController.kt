package com.example.notesplugin.presentation.controller

import com.example.notesplugin.domain.model.Snippet

class SnippetListController {

    private var allSnippets: List<Snippet> = emptyList()

    private var currentSearchText: String = ""
    private var currentLanguage: String? = null

    fun setSnippets(snippets: List<Snippet>) {
        allSnippets = snippets
    }

    fun updateSearchText(text: String) {
        currentSearchText = text
    }

    fun updateLanguage(language: String?) {
        currentLanguage = language
    }

    fun filter(searchText: String = currentSearchText): List<Snippet> {
        updateSearchText(searchText)

        return allSnippets.filter { snippet ->
            val matchesSearch = snippet.title.contains(searchText, ignoreCase = true) ||
                    snippet.content.contains(searchText, ignoreCase = true)

            val matchesLanguage = currentLanguage == null ||
                    currentLanguage.equals("All", ignoreCase = true) ||
                    snippet.languageName.equals(currentLanguage, ignoreCase = true)

            matchesSearch && matchesLanguage
        }
    }
}
