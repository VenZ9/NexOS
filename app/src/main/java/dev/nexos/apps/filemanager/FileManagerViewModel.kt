package dev.nexos.apps.filemanager

import android.net.Uri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dev.nexos.data.repository.FileRepository
import dev.nexos.domain.model.NexFile
import dev.nexos.runtime.PeFileInspector
import dev.nexos.runtime.PeMetadata
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.io.File
import java.util.Stack

data class FileManagerUiState(
    val currentPath: String = "",
    val items: List<NexFile> = emptyList(),
    val filteredItems: List<NexFile> = emptyList(),
    val searchQuery: String = "",
    val isGridView: Boolean = false,
    val selectedItem: NexFile? = null,
    val isLoading: Boolean = false,
    val errorMessage: String? = null,
    val infoMessage: String? = null,
    val inspectedPe: PeMetadata? = null,
    val canGoBack: Boolean = false
)

class FileManagerViewModel(
    private val fileRepository: FileRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(FileManagerUiState())
    val uiState: StateFlow<FileManagerUiState> = _uiState.asStateFlow()

    private var currentDir: File = fileRepository.nexOsDriveDir
    private val backStack = Stack<File>()

    init {
        loadDirectory(fileRepository.nexOsDriveDir)
    }

    fun loadDirectory(dir: File) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true, errorMessage = null)
            val files = fileRepository.listFiles(dir)
            currentDir = dir
            _uiState.value = _uiState.value.copy(
                currentPath = dir.absolutePath,
                items = files,
                filteredItems = filterFiles(files, _uiState.value.searchQuery),
                isLoading = false,
                selectedItem = null,
                canGoBack = !backStack.isEmpty()
            )
        }
    }

    fun navigateInto(file: NexFile) {
        if (file.isDirectory) {
            val target = File(file.path)
            if (target.exists() && target.isDirectory) {
                backStack.push(currentDir)
                loadDirectory(target)
            }
        } else {
            selectItem(file)
            if (file.isWindowsExe) {
                inspectExecutable(File(file.path))
            }
        }
    }

    fun navigateUp() {
        val parent = currentDir.parentFile
        if (parent != null && parent.absolutePath.startsWith(fileRepository.nexOsDriveDir.parentFile?.absolutePath ?: "")) {
            backStack.push(currentDir)
            loadDirectory(parent)
        }
    }

    fun navigateBack() {
        if (!backStack.isEmpty()) {
            val prev = backStack.pop()
            loadDirectory(prev)
        }
    }

    fun selectItem(item: NexFile?) {
        _uiState.value = _uiState.value.copy(selectedItem = item)
    }

    fun updateSearchQuery(query: String) {
        _uiState.value = _uiState.value.copy(
            searchQuery = query,
            filteredItems = filterFiles(_uiState.value.items, query)
        )
    }

    fun toggleViewMode() {
        _uiState.value = _uiState.value.copy(isGridView = !_uiState.value.isGridView)
    }

    fun createNewDocument(name: String, onDocumentCreated: (File) -> Unit) {
        viewModelScope.launch {
            val result = fileRepository.createNewDocument(currentDir, name)
            result.onSuccess {
                loadDirectory(currentDir)
                onDocumentCreated(File(it.path))
            }.onFailure {
                _uiState.value = _uiState.value.copy(errorMessage = "Failed to create document: ${it.message}")
            }
        }
    }

    fun createFolder(name: String) {
        viewModelScope.launch {
            val result = fileRepository.createFolder(currentDir, name)
            result.onSuccess {
                loadDirectory(currentDir)
            }.onFailure {
                _uiState.value = _uiState.value.copy(errorMessage = "Failed to create folder: ${it.message}")
            }
        }
    }

    fun deleteSelectedItem() {
        val item = _uiState.value.selectedItem ?: return
        viewModelScope.launch {
            val result = fileRepository.deleteFile(File(item.path))
            result.onSuccess {
                loadDirectory(currentDir)
            }.onFailure {
                _uiState.value = _uiState.value.copy(errorMessage = "Delete failed: ${it.message}")
            }
        }
    }

    fun importFromUri(uri: Uri) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true)
            val result = fileRepository.importFromUri(uri, fileRepository.installersDir)
            result.onSuccess { importedFile ->
                if (importedFile.extension.equals("exe", true) || importedFile.extension.equals("msi", true)) {
                    val pe = PeFileInspector.inspect(importedFile)
                    _uiState.value = _uiState.value.copy(
                        infoMessage = "Imported ${importedFile.name} (${pe.machineName}). Ready for runtime.",
                        inspectedPe = pe
                    )
                } else {
                    _uiState.value = _uiState.value.copy(infoMessage = "Imported ${importedFile.name} successfully.")
                }
                loadDirectory(currentDir)
            }.onFailure {
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    errorMessage = "Failed to import file: ${it.message}"
                )
            }
        }
    }

    private fun inspectExecutable(file: File) {
        viewModelScope.launch {
            val pe = PeFileInspector.inspect(file)
            _uiState.value = _uiState.value.copy(inspectedPe = pe)
        }
    }

    fun dismissMessage() {
        _uiState.value = _uiState.value.copy(errorMessage = null, infoMessage = null)
    }

    private fun filterFiles(files: List<NexFile>, query: String): List<NexFile> {
        if (query.isBlank()) return files
        return files.filter { it.name.contains(query, ignoreCase = true) }
    }
}
