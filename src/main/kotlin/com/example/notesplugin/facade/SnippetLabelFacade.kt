package com.example.notesplugin.facade

import com.example.notesplugin.domain.model.Label
import com.example.notesplugin.domain.model.Snippet
import com.example.notesplugin.service.LabelService
import com.example.notesplugin.service.SnippetService
import com.intellij.openapi.project.Project

class SnippetLabelFacade(project: Project) {

    private val snippetService = SnippetService.getInstance(project)
    private val labelService = LabelService.getInstance(project)

    /** Snippet operations **/
    fun addSnippet(snippet: Snippet) {
        snippetService.addSnippet(snippet)
    }

    fun updateSnippet(original: Snippet, updated: Snippet) {
        snippetService.updateSnippet(original, updated)
    }

    fun deleteSnippet(snippet: Snippet) {
        snippetService.deleteSnippet(snippet)
    }

    fun getAllSnippets(): List<Snippet> = snippetService.state.snippets.toList()

    /** Label operations **/
    fun getAllLabels(): List<Label> = labelService.state.labels.toList()

    fun addLabel(label: Label) {
        labelService.addLabel(label)
    }

    fun updateLabel(oldName: String, newLabel: Label) {
        labelService.updateLabel(oldName, newLabel)
    }

    fun deleteLabel(labelName: String) {
        val labels = getAllLabels().map { it.name }
        if (labelName !in labels) return
        labelService.deleteLabel(labelName)
        removeLabelFromSnippets(labelName)
    }

    /** Utility **/
    fun getSnippetsByLabel(labelName: String): List<Snippet> =
        getAllSnippets().filter { snippet -> labelName in snippet.labels }

    private fun removeLabelFromSnippets(labelName: String) {
        val snippets = getAllSnippets()
        snippets.forEach { snippet ->
            if (labelName in snippet.labels) {
                val updatedLabels = snippet.labels.filterNot { it == labelName }
                val updatedSnippet = snippet.copy(labels = updatedLabels)
                snippetService.updateSnippet(snippet, updatedSnippet)
            }
        }
    }

}