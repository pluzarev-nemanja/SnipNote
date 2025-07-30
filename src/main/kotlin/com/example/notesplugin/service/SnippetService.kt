package com.example.notesplugin.service

import com.example.notesplugin.domain.model.Snippet
import com.intellij.openapi.components.PersistentStateComponent
import com.intellij.openapi.components.Service
import com.intellij.openapi.components.State
import com.intellij.openapi.components.Storage
import com.intellij.openapi.project.Project
import com.intellij.util.xmlb.XmlSerializerUtil
import java.util.concurrent.CopyOnWriteArrayList

@Service(Service.Level.PROJECT)
@State(
    name = "SnipNoteSnippets",
    storages = [Storage("snipnote-snippets.xml")]
)
class SnippetService : PersistentStateComponent<SnippetService.State> {

    class State {
        var snippets: MutableList<Snippet> = CopyOnWriteArrayList()
    }

    private var myState: State = State()


    override fun getState(): SnippetService.State = myState

    override fun loadState(state: SnippetService.State) {
        println("LOADED SNIPPETS: ${state.snippets}")
        XmlSerializerUtil.copyBean(state, this.myState)
    }

    fun addSnippet(snippet: Snippet) {
        myState.snippets.add(snippet)
    }

    fun deleteSnippet(snippet: Snippet) {
        myState.snippets.remove(snippet)
    }

    fun updateSnippet(original: Snippet, updated: Snippet) {
        val index = myState.snippets.indexOf(original)
        if (index != -1) {
            myState.snippets[index] = updated
        }
    }

    companion object {
        fun getInstance(project: Project): SnippetService {
            return project.getService(SnippetService::class.java)
        }
    }
}