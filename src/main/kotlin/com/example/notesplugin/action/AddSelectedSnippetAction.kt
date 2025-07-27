package com.example.notesplugin.action

import com.example.notesplugin.toolwindow.SnipNoteToolWindow
import com.intellij.openapi.actionSystem.AnAction
import com.intellij.openapi.actionSystem.AnActionEvent
import com.intellij.openapi.actionSystem.CommonDataKeys
import com.intellij.openapi.ui.Messages
import com.intellij.openapi.wm.ToolWindowManager

class AddSelectedSnippetAction : AnAction() {

    override fun actionPerformed(e: AnActionEvent) {
        val project = e.project ?: return
        val editor = e.getData(CommonDataKeys.EDITOR) ?: return
        val document = editor.document
        val primaryCaret = editor.caretModel.primaryCaret

        val selectedText = primaryCaret.selectedText

        if (selectedText.isNullOrBlank()) {
            Messages.showMessageDialog(
                project,
                "No text selected to add as a snippet.",
                "SnipNote",
                Messages.getInformationIcon()
            )
            return
        }

        val snippetTitle = Messages.showInputDialog(
            project,
            "Enter a title for the new snippet:",
            "Add to SnipNote",
            Messages.getQuestionIcon(),
            "New Snippet",
            null
        )

        require(!snippetTitle.isNullOrBlank()) { "Snippet title must not be null or blank." }

        val toolWindow = ToolWindowManager.getInstance(project).getToolWindow("SnipNote")
        toolWindow?.contentManager?.contents?.forEach { content ->
        }
    }

    override fun update(e: AnActionEvent) {
        val project = e.project
        val editor = e.getData(CommonDataKeys.EDITOR)
        val hasSelection = editor?.caretModel?.primaryCaret?.hasSelection() ?: false

        e.presentation.isEnabledAndVisible = project != null && hasSelection
    }
}