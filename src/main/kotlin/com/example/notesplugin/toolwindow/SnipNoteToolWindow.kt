package com.example.notesplugin.toolwindow

import com.example.notesplugin.domain.model.Snippet
import com.example.notesplugin.service.SnippetService
import com.intellij.openapi.command.WriteCommandAction
import com.intellij.openapi.editor.colors.EditorColorsManager
import com.intellij.openapi.fileEditor.FileEditorManager
import com.intellij.openapi.fileTypes.FileTypeManager
import com.intellij.openapi.ide.CopyPasteManager
import com.intellij.openapi.project.Project
import com.intellij.openapi.ui.Messages
import com.intellij.openapi.ui.SimpleToolWindowPanel
import com.intellij.ui.EditorTextField
import com.intellij.ui.components.JBList
import com.intellij.ui.components.JBPanel
import com.intellij.ui.components.JBScrollPane
import com.intellij.util.ui.JBUI
import java.awt.BorderLayout
import java.awt.FlowLayout
import java.awt.Toolkit
import java.awt.datatransfer.DataFlavor
import java.awt.datatransfer.StringSelection
import javax.swing.*
import javax.swing.event.ListSelectionEvent
import javax.swing.event.ListSelectionListener

class SnipNoteToolWindow(private val project: Project) : SimpleToolWindowPanel(true, true) {

    private val snippetService = project.getService(SnippetService::class.java)

    private val snippetListModel = DefaultListModel<Snippet>()
    private val snippetList = JBList(snippetListModel)

    private val snippetTitleField = JTextField()
    private lateinit var snippetContentEditor: EditorTextField // Renamed for clarity

    private val saveButton = JButton("Save Changes")
    private val deleteButton = JButton("Delete")
    private val copyButton = JButton("Copy to Clipboard")
    private val pasteIntoEditorButton = JButton("Paste into Editor")
    private val pasteIntoSnippetEditorButton = JButton("Paste Raw") // New button for pasting into snippetContentEditor

    init {
        // --- UI Setup ---
        // Main content panel, which will hold the split pane
        val mainContentPanel = JBPanel<JBPanel<*>>(BorderLayout()) // Use JBPanel and BorderLayout for the main content

        // Snippet List Panel
        val listPanel = JBPanel<JBPanel<*>>(BorderLayout()).apply {
            border = JBUI.Borders.empty(5)
            add(JBScrollPane(snippetList), BorderLayout.CENTER)
        }

        // Snippet Detail Panel
        val detailPanel = JBPanel<JBPanel<*>>(BorderLayout()).apply { // Use BorderLayout for detailPanel
            border = JBUI.Borders.empty(5)

            val titlePanel = JBPanel<JBPanel<*>>(FlowLayout(FlowLayout.LEFT)).apply {
                add(JLabel("Title:"))
                add(snippetTitleField.apply { preferredSize = JBUI.size(200, 25) })
            }
            add(titlePanel, BorderLayout.NORTH) // Title at the top of detailPanel

            snippetContentEditor =
                EditorTextField("", project, FileTypeManager.getInstance().getFileTypeByExtension("kt")).apply {
                    addSettingsProvider { editor ->
                        editor.isOneLineMode = false
                        editor.setCaretEnabled(true)
                        val colorsScheme = EditorColorsManager.getInstance().globalScheme
                        editor.colorsScheme = colorsScheme
                        editor.settings.apply {
                            isLineNumbersShown = true
                            isFoldingOutlineShown = true
                            isUseSoftWraps = true
                            isViewer = false
                        }
                    }
                }
            add(snippetContentEditor, BorderLayout.CENTER)

            val buttonPanel = JBPanel<JBPanel<*>>(FlowLayout(FlowLayout.RIGHT)).apply {
                add(pasteIntoEditorButton)
                add(copyButton)
                add(saveButton)
                add(deleteButton)
                add(pasteIntoSnippetEditorButton)
            }
            add(buttonPanel, BorderLayout.SOUTH)
        }

        val splitPane = JSplitPane(JSplitPane.HORIZONTAL_SPLIT, listPanel, detailPanel).apply {
            dividerLocation = 200 // Initial divider position
        }

        mainContentPanel.add(splitPane, BorderLayout.CENTER)

        setContent(mainContentPanel)

        initListeners()
        loadSnippets()
        clearSnippetDetails()
    }

    private fun initListeners() {
        snippetList.addListSelectionListener(object : ListSelectionListener {
            override fun valueChanged(e: ListSelectionEvent?) {
                if (!e?.valueIsAdjusting!!) { // Only react when selection has settled
                    val selectedSnippet = snippetList.selectedValue
                    if (selectedSnippet != null) {
                        displaySnippet(selectedSnippet)
                    } else {
                        clearSnippetDetails()
                    }
                }
            }
        })

        saveButton.addActionListener {
            saveChangesToSelectedSnippet()
        }

        deleteButton.addActionListener {
            deleteSelectedSnippet()
        }

        copyButton.addActionListener { // Copies content from snippetContentEditor to system clipboard
            val textToCopy = snippetContentEditor.text
            if (textToCopy.isNotBlank()) {
                val stringSelection = StringSelection(textToCopy)
                CopyPasteManager.getInstance().setContents(stringSelection)
                Messages.showInfoMessage(project, "Snippet content copied to clipboard.", "SnipNote")
            } else {
                Messages.showInfoMessage(project, "No content to copy.", "SnipNote")
            }
        }

        pasteIntoEditorButton.addActionListener { // Pastes selected snippet content into current active IDE editor
            pasteSelectedSnippetIntoEditor()
        }

        pasteIntoSnippetEditorButton.addActionListener { // Pastes raw text from system clipboard into snippetContentEditor
            val clipboard = Toolkit.getDefaultToolkit().systemClipboard
            if (clipboard.isDataFlavorAvailable(DataFlavor.stringFlavor)) {
                try {
                    val data = clipboard.getData(DataFlavor.stringFlavor) as String
                    WriteCommandAction.runWriteCommandAction(project) {
                        snippetContentEditor.document.setText(data) // Set text directly, bypassing smart paste
                    }
                    Messages.showInfoMessage(project, "Text pasted into snippet content.", "SnipNote")
                } catch (e: Exception) {
                    Messages.showErrorDialog(project, "Failed to paste from clipboard: ${e.message}", "Paste Error")
                }
            } else {
                Messages.showInfoMessage(project, "Clipboard does not contain plain text.", "SnipNote")
            }
        }
    }

    private fun loadSnippets() {
        snippetListModel.clear()
        snippetService.state.snippets.forEach { snippetListModel.addElement(it) }
        // Select the first snippet if available
        if (!snippetListModel.isEmpty) {
            snippetList.setSelectedValue(snippetListModel.getElementAt(0), true) // Select first and scroll
        }
    }

    private fun displaySnippet(snippet: Snippet) {
        snippetTitleField.text = snippet.title
        WriteCommandAction.runWriteCommandAction(project) {
            snippetContentEditor.text = snippet.content
        }

        saveButton.isEnabled = true
        deleteButton.isEnabled = true
        copyButton.isEnabled = true
        pasteIntoEditorButton.isEnabled = true
        pasteIntoSnippetEditorButton.isEnabled = true // Enable this button too
    }

    private fun clearSnippetDetails() {
        snippetTitleField.text = ""
        WriteCommandAction.runWriteCommandAction(project) {
            snippetContentEditor.text = ""
        }
        saveButton.isEnabled = false
        deleteButton.isEnabled = false
        copyButton.isEnabled = false
        pasteIntoEditorButton.isEnabled = false
        pasteIntoSnippetEditorButton.isEnabled = false // Disable this button too
    }

    private fun saveChangesToSelectedSnippet() {
        val selectedIndex = snippetList.selectedIndex
        if (selectedIndex != -1) {
            val originalSnippet = snippetListModel.getElementAt(selectedIndex)
            val updatedTitle = snippetTitleField.text
            val updatedContent = snippetContentEditor.text

            if (updatedTitle.isBlank()) {
                Messages.showErrorDialog(project, "Snippet title cannot be empty.", "Error")
                return
            }

            val updatedSnippet = Snippet(updatedTitle, updatedContent)
            snippetService.updateSnippet(originalSnippet, updatedSnippet)
            snippetListModel.setElementAt(updatedSnippet, selectedIndex)
            snippetList.setSelectedValue(updatedSnippet, true) // Re-select to refresh display
            Messages.showInfoMessage(project, "Snippet saved successfully.", "SnipNote")
        }
    }

    private fun deleteSelectedSnippet() {
        val selectedIndex = snippetList.selectedIndex
        if (selectedIndex != -1) {
            val snippetToDelete = snippetListModel.getElementAt(selectedIndex)
            if (Messages.showYesNoDialog(
                    project,
                    "Are you sure you want to delete '${snippetToDelete.title}'?",
                    "Delete Snippet",
                    Messages.getQuestionIcon()
                ) == Messages.YES
            ) {
                snippetService.deleteSnippet(snippetToDelete)
                snippetListModel.remove(selectedIndex)
                clearSnippetDetails()
                Messages.showInfoMessage(project, "Snippet deleted.", "SnipNote")
            }
        }
    }

    private fun pasteSelectedSnippetIntoEditor() {
        val selectedSnippet = snippetList.selectedValue
        if (selectedSnippet != null) {
            val editor = FileEditorManager.getInstance(project).selectedTextEditor
            val document = editor?.document

            if (editor != null && document != null) {
                WriteCommandAction.runWriteCommandAction(project) {
                    val caret = editor.caretModel.primaryCaret
                    document.insertString(caret.offset, selectedSnippet.content)
                }
            } else {
                Messages.showWarningDialog(project, "No active editor found to paste into.", "SnipNote")
            }
        } else {
            Messages.showInfoMessage(project, "No snippet selected to paste.", "SnipNote")
        }
    }


    fun getContentPanel(): JComponent {
        return this
    }

    fun addSnippet(title: String, content: String) {
        val newSnippet = Snippet(title, content)
        snippetService.addSnippet(newSnippet)
        snippetListModel.addElement(newSnippet)
        snippetList.setSelectedValue(newSnippet, true) // Select and display the new snippet
        Messages.showInfoMessage(project, "New snippet '$title' added.", "SnipNote")
    }
}