package com.example.notesplugin.toolwindow

import com.example.notesplugin.domain.model.Snippet
import com.example.notesplugin.facade.SnippetLabelFacade
import com.example.notesplugin.presentation.component.SavedNotesPanel
import com.example.notesplugin.presentation.util.CodeLanguage
import com.example.notesplugin.presentation.util.CodeLanguageColors
import com.example.notesplugin.presentation.util.PanelTabs
import com.example.notesplugin.service.SnippetService
import com.example.notesplugin.util.MyNotifier
import com.intellij.icons.AllIcons
import com.intellij.notification.NotificationType
import com.intellij.openapi.command.WriteCommandAction
import com.intellij.openapi.editor.colors.EditorColorsManager
import com.intellij.openapi.editor.ex.EditorEx
import com.intellij.openapi.editor.highlighter.EditorHighlighterFactory
import com.intellij.openapi.fileEditor.FileEditorManager
import com.intellij.openapi.fileTypes.FileTypeManager
import com.intellij.openapi.fileTypes.UnknownFileType
import com.intellij.openapi.ide.CopyPasteManager
import com.intellij.openapi.project.Project
import com.intellij.openapi.roots.ProjectRootManager
import com.intellij.openapi.ui.ComboBox
import com.intellij.openapi.ui.SimpleToolWindowPanel
import com.intellij.openapi.vfs.VfsUtilCore
import com.intellij.openapi.vfs.VirtualFile
import com.intellij.ui.ColoredListCellRenderer
import com.intellij.ui.EditorTextField
import com.intellij.ui.components.JBLabel
import com.intellij.ui.components.JBPanel
import com.intellij.ui.components.JBTextField
import com.intellij.util.ui.JBUI
import java.awt.BorderLayout
import java.awt.FlowLayout
import java.awt.Toolkit
import java.awt.datatransfer.DataFlavor
import java.awt.datatransfer.StringSelection
import javax.swing.*

class SnipNoteToolWindow(private val project: Project) : SimpleToolWindowPanel(true, true) {

    private val facade = SnippetLabelFacade(project)
    private lateinit var snippetContentEditor: EditorTextField
    private val titleField = JBTextField()
    private val panel = JBPanel<JBPanel<*>>(BorderLayout())
    private val saveButton = JButton("Save")
    private val copyButton = JButton("Copy")
    private val pasteButton = JButton("Paste")
    private val insertToEditorButton = JButton("Insert to Editor")
    private val languageComboBox = ComboBox(CodeLanguage.entries.toTypedArray())
    private lateinit var savedNotesPanel: SavedNotesPanel

    init {
        setupLanguageComboBox()
        setupEditor()
        setupButtons()
        setupMainPanel()
        setupTabs()
        initListeners()
    }

    private fun setupLanguageComboBox() {
        languageComboBox.renderer = object : ColoredListCellRenderer<CodeLanguage>() {
            override fun customizeCellRenderer(
                list: JList<out CodeLanguage>,
                value: CodeLanguage?,
                index: Int,
                selected: Boolean,
                hasFocus: Boolean
            ) {
                value?.let {
                    append(it.displayName)
                }
            }
        }
        languageComboBox.selectedItem = detectProjectPrimaryLanguage(project)
    }

    private fun setupEditor() {
        snippetContentEditor = EditorTextField(
            "",
            project,
            FileTypeManager.getInstance().getFileTypeByExtension(CodeLanguage.KOTLIN.fileExtension)
        ).apply {
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
    }

    private fun setupButtons() {
        val buttonPanel = JBPanel<JBPanel<*>>(FlowLayout(FlowLayout.RIGHT)).apply {
            add(insertToEditorButton)
            add(copyButton)
            add(pasteButton)
            add(saveButton)
        }
        panel.add(buttonPanel, BorderLayout.SOUTH)
    }

    private fun setupMainPanel() {
        val titlePanel = JPanel(BorderLayout(5, 0)).apply {
            add(JBLabel("Title:").apply {
                border = JBUI.Borders.emptyLeft(10)
            }, BorderLayout.WEST)
            add(titleField, BorderLayout.CENTER)
            add(languageComboBox, BorderLayout.EAST)
        }

        panel.add(titlePanel, BorderLayout.NORTH)
        panel.add(JScrollPane(snippetContentEditor), BorderLayout.CENTER)
    }

    private fun setupTabs() {
        val tabs = JTabbedPane()
        tabs.addTab(PanelTabs.EDITOR.title, AllIcons.Actions.Commit, panel)

        savedNotesPanel = SavedNotesPanel(facade) { snippet ->
            tabs.selectedIndex = PanelTabs.EDITOR.ordinal
            updateEditorTab(snippet)
        }
        tabs.addTab(PanelTabs.SAVED_NOTES.title, AllIcons.Actions.MenuPaste, savedNotesPanel)

        tabs.addChangeListener {
            if (tabs.selectedIndex == PanelTabs.SAVED_NOTES.ordinal) {
                savedNotesPanel.refreshList()
            }
        }

        setContent(tabs)
    }

    private fun updateEditorTab(snippet: Snippet) {
        titleField.text = snippet.title
        snippetContentEditor.text = snippet.content
        CodeLanguage.entries.find { it.displayName == snippet.languageName }?.let {
            languageComboBox.selectedItem = it
            updateEditorHighlighter(it)
        }
        saveButton.text = "Update"
    }

    private fun initListeners() {
        languageComboBox.addActionListener {
            val selectedLanguage = languageComboBox.selectedItem as? CodeLanguage ?: return@addActionListener
            updateEditorHighlighter(selectedLanguage)
        }

        saveButton.addActionListener {
            val title = titleField.text
            if (title.isBlank()) {
                MyNotifier.showNotification(
                    project,
                    message = "Snippet title cannot be empty.",
                    type = NotificationType.ERROR
                )
                return@addActionListener
            }
            val selectedLanguage = languageComboBox.selectedItem as? CodeLanguage
            facade.addSnippet(
                Snippet(
                    title = title,
                    content = snippetContentEditor.text,
                    languageName = selectedLanguage?.displayName ?: "Unknown",
                    languageColor = CodeLanguageColors
                        .getColor(selectedLanguage ?: CodeLanguage.KOTLIN)
                        .let { "#%02x%02x%02x".format(it.red, it.green, it.blue) })
            )
            savedNotesPanel.refreshList()
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

    private fun updateEditorHighlighter(language: CodeLanguage) {
        val fileType = FileTypeManager.getInstance().getFileTypeByExtension(language.fileExtension)
        val editorHighlighter = EditorHighlighterFactory.getInstance()
            .createEditorHighlighter(project, fileType)

        (snippetContentEditor.editor as? EditorEx)?.apply {
            highlighter = editorHighlighter
        } ?: println("⚠️ No syntax highlighter found for: ${language.fileExtension}. Falling back to plain text.")
    }

    fun detectProjectPrimaryLanguage(project: Project): CodeLanguage {
        val fileTypeManager = FileTypeManager.getInstance()
        val allFiles = mutableListOf<VirtualFile>()
        ProjectRootManager.getInstance(project).contentRoots.forEach { root ->
            VfsUtilCore.iterateChildrenRecursively(root, null) {
                if (!it.isDirectory) allFiles.add(it)
                true
            }
        }
        val fileTypeCount = allFiles
            .map { it.fileType }
            .groupingBy { it }
            .eachCount()

        val mostUsedFileType = fileTypeCount
            .filter { it.key !is UnknownFileType }
            .maxByOrNull { it.value }
            ?.key ?: return CodeLanguage.KOTLIN

        return CodeLanguage.entries.find {
            fileTypeManager.getFileTypeByExtension(it.fileExtension) == mostUsedFileType
        } ?: CodeLanguage.KOTLIN
    }

    fun getContentPanel(): JComponent = this
}