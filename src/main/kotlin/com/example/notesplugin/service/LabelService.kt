package com.example.notesplugin.service

import com.example.notesplugin.domain.model.Label
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
class LabelService : PersistentStateComponent<LabelService.State> {

    class State {
        var labels: MutableList<Label> = CopyOnWriteArrayList()
    }
    companion object {
        fun getInstance(project: Project): LabelService =
            project.getService(LabelService::class.java)
    }

    private var myState: State = State()

    override fun getState(): State = myState

    override fun loadState(state: State) {
        XmlSerializerUtil.copyBean(state, this.myState)
    }

    private val labels = mutableListOf<Label>()

    fun getLabels(): List<Label> = labels.toList()

    fun addLabel(label: Label) {
        if(labels.none { it.name == label.name}) {
            labels.add(label)
        }
    }

    fun deleteLabel(labelName: String) {
        labels.removeIf { it.name == labelName }
    }

    fun updateLabel(oldName: String,newLabel: Label) {
        val index = labels.indexOfFirst { it.name == oldName }
        if (index != -1) labels[index] = newLabel
    }

}