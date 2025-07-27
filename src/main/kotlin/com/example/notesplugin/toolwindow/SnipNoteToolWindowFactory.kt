package com.example.notesplugin.toolwindow

import com.intellij.openapi.project.Project
import com.intellij.openapi.wm.ToolWindow
import com.intellij.openapi.wm.ToolWindowFactory
import com.intellij.ui.content.ContentFactory

class SnipNoteToolWindowFactory : ToolWindowFactory {

    override fun createToolWindowContent(project: Project, toolWindow: ToolWindow) {
        toolWindow.contentManager.addContent(
            ContentFactory.getInstance().createContent(
                SnipNoteToolWindow(project).getContentPanel(),
                "Edit Note",
                false
            )
        )
    }
}