package com.example.notesplugin.toolwindow

import com.example.notesplugin.domain.model.Snippet
import com.example.notesplugin.service.SnippetService
import com.example.notesplugin.util.MyNotifier
import com.intellij.notification.NotificationType
import com.intellij.openapi.command.WriteCommandAction
import com.intellij.openapi.editor.colors.EditorColorsManager
import com.intellij.openapi.fileEditor.FileEditorManager
import com.intellij.openapi.fileTypes.FileTypeManager
import com.intellij.openapi.ide.CopyPasteManager
import com.intellij.openapi.project.Project
import com.intellij.openapi.ui.SimpleToolWindowPanel
import com.intellij.ui.EditorTextField
import com.intellij.ui.components.JBPanel
import com.intellij.util.ui.JBUI
import java.awt.BorderLayout
import java.awt.FlowLayout
import java.awt.Toolkit
import java.awt.datatransfer.DataFlavor
import java.awt.datatransfer.StringSelection
import javax.swing.*

class SnipNoteToolWindow(private val project: Project) : SimpleToolWindowPanel(true, true) {

    private val snippetService = project.getService(SnippetService::class.java)

    private val snippetTitleField = JTextField()
    private var snippetContentEditor: EditorTextField

    private val saveButton = JButton("Save")
    private val copyButton = JButton("Copy")
    private val pasteButton = JButton("Paste")
    private val insertToEditorButton = JButton("Insert to Editor")

    init {
        val panel = JBPanel<JBPanel<*>>(BorderLayout())

        val titlePanel = JBPanel<JBPanel<*>>(FlowLayout(FlowLayout.LEFT)).apply {
            add(JLabel("Title:"))
            add(snippetTitleField.apply { preferredSize = JBUI.size(200, 25) })
        }

        snippetContentEditor =
            EditorTextField("", project, FileTypeManager.getInstance().getFileTypeByExtension("kt")).apply {
                addSettingsProvider { editor ->
                    editor.isOneLineMode = false
                    editor.setCaretEnabled(true)
                    editor.colorsScheme = EditorColorsManager.getInstance().globalScheme
                    editor.settings.apply {
                        isLineNumbersShown = true
                        isFoldingOutlineShown = true
                        isUseSoftWraps = true
                        isViewer = false
                    }
                }
            }

        val buttonPanel = JBPanel<JBPanel<*>>(FlowLayout(FlowLayout.RIGHT)).apply {
            add(insertToEditorButton)
            add(copyButton)
            add(pasteButton)
            add(saveButton)
        }

        panel.add(titlePanel, BorderLayout.NORTH)
        panel.add(JScrollPane(snippetContentEditor), BorderLayout.CENTER)
        panel.add(buttonPanel, BorderLayout.SOUTH)

        setContent(panel)
        initListeners()
    }

    private fun initListeners() {
        saveButton.addActionListener {
            val title = snippetTitleField.text
            val content = snippetContentEditor.text

            if (title.isBlank()) {
                MyNotifier.showNotification(
                    project,
                    message = "Snippet title cannot be empty.",
                    type = NotificationType.ERROR
                )
                return@addActionListener
            }

            val newSnippet = Snippet(title, content)
            snippetService.addSnippet(newSnippet)
            MyNotifier.showNotification(project, message = "Snippet saved.", type = NotificationType.INFORMATION)
        }

        copyButton.addActionListener {
            val text = snippetContentEditor.text
            if (text.isNotBlank()) {
                CopyPasteManager.getInstance().setContents(StringSelection(text))
                MyNotifier.showNotification(
                    project,
                    message = "Copied to clipboard.",
                    type = NotificationType.INFORMATION
                )
            }
        }

        pasteButton.addActionListener {
            val clipboard = Toolkit.getDefaultToolkit().systemClipboard
            if (clipboard.isDataFlavorAvailable(DataFlavor.stringFlavor)) {
                val data = clipboard.getData(DataFlavor.stringFlavor) as? String
                if (data != null) {
                    WriteCommandAction.runWriteCommandAction(project) {
                        snippetContentEditor.document.setText(data)
                    }
                }
            }
        }

        insertToEditorButton.addActionListener {
            val editor = FileEditorManager.getInstance(project).selectedTextEditor
            val document = editor?.document
            if (editor != null && document != null) {
                WriteCommandAction.runWriteCommandAction(project) {
                    val caret = editor.caretModel.primaryCaret
                    document.insertString(caret.offset, snippetContentEditor.text)
                }
            } else {
                MyNotifier.showNotification(
                    project,
                    message = "No active editor found.",
                    type = NotificationType.ERROR
                )
            }
        }
    }

    fun getContentPanel(): JComponent = this
}