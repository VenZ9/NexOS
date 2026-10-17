package dev.nexos.apps.texteditor

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dev.nexos.data.repository.FileRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.io.File

data class TextEditorUiState(
    val filePath: String? = null,
    val fileName: String = "Untitled.txt",
    val content: String = "",
    val originalContent: String = "",
    val isModified: Boolean = false,
    val wordWrap: Boolean = true,
    val lineCount: Int = 1,
    val charCount: Int = 0,
    val isSaving: Boolean = false,
    val statusMessage: String? = null
)

class TextEditorViewModel(
    private val fileRepository: FileRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(TextEditorUiState())
    val uiState: StateFlow<TextEditorUiState> = _uiState.asStateFlow()

    fun loadFromFile(file: File) {
        viewModelScope.launch {
            val result = fileRepository.readTextFile(file)
            result.onSuccess { text ->
                _uiState.value = _uiState.value.copy(
                    filePath = file.absolutePath,
                    fileName = file.name,
                    content = text,
                    originalContent = text,
                    isModified = false,
                    lineCount = text.lines().size,
                    charCount = text.length,
                    statusMessage = "Loaded ${file.name}"
                )
            }.onFailure {
                _uiState.value = _uiState.value.copy(
                    statusMessage = "Error opening file: ${it.message}"
                )
            }
        }
    }

    fun updateContent(newContent: String) {
        val isModified = newContent != _uiState.value.originalContent
        _uiState.value = _uiState.value.copy(
            content = newContent,
            isModified = isModified,
            lineCount = newContent.lines().size,
            charCount = newContent.length
        )
    }

    fun toggleWordWrap() {
        _uiState.value = _uiState.value.copy(wordWrap = !_uiState.value.wordWrap)
    }

    fun save(onSaved: (String) -> Unit) {
        val currentPath = _uiState.value.filePath
        if (currentPath == null) {
            // Untitled: save to Documents by default
            val target = File(fileRepository.documentsDir, _uiState.value.fileName)
            saveToFile(target, onSaved)
        } else {
            saveToFile(File(currentPath), onSaved)
        }
    }

    fun saveToFile(file: File, onSaved: (String) -> Unit) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isSaving = true)
            val result = fileRepository.writeTextFile(file, _uiState.value.content)
            result.onSuccess {
                _uiState.value = _uiState.value.copy(
                    filePath = file.absolutePath,
                    fileName = file.name,
                    originalContent = _uiState.value.content,
                    isModified = false,
                    isSaving = false,
                    statusMessage = "Saved to ${file.name}"
                )
                onSaved(file.name)
            }.onFailure {
                _uiState.value = _uiState.value.copy(
                    isSaving = false,
                    statusMessage = "Save failed: ${it.message}"
                )
            }
        }
    }

    fun resetNewDocument() {
        _uiState.value = TextEditorUiState()
    }
}
