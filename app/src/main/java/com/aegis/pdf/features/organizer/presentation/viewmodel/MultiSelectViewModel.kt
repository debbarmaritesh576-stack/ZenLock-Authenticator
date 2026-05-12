package com.aegis.pdf.features.organizer.presentation.viewmodel

import androidx.lifecycle.ViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import javax.inject.Inject

data class SelectionState(
    val selectedFiles: Set<String> = emptySet(),
    val selectedFolders: Set<String> = emptySet(),
    val isSelectionMode: Boolean = false
)

@HiltViewModel
class MultiSelectViewModel @Inject constructor() : ViewModel() {

    private val _state = MutableStateFlow(SelectionState())
    val state: StateFlow<SelectionState> = _state

    fun enterSelectionMode() {
        _state.value = _state.value.copy(isSelectionMode = true)
    }

    fun exitSelectionMode() {
        _state.value = SelectionState()
    }

    fun isInSelectionMode(): Boolean = _state.value.isSelectionMode

    fun toggleFile(fileId: String) {
        val currentFiles = _state.value.selectedFiles.toMutableSet()
        if (fileId in currentFiles) {
            currentFiles.remove(fileId)
        } else {
            currentFiles.add(fileId)
        }
        _state.value = _state.value.copy(selectedFiles = currentFiles)
    }

    fun toggleFolder(folderId: String) {
        val currentFolders = _state.value.selectedFolders.toMutableSet()
        if (folderId in currentFolders) {
            currentFolders.remove(folderId)
        } else {
            currentFolders.add(folderId)
        }
        _state.value = _state.value.copy(selectedFolders = currentFolders)
    }

    fun selectFile(fileId: String) {
        val files = _state.value.selectedFiles.toMutableSet()
        files.add(fileId)
        _state.value = _state.value.copy(selectedFiles = files)
    }

    fun selectFolder(folderId: String) {
        val folders = _state.value.selectedFolders.toMutableSet()
        folders.add(folderId)
        _state.value = _state.value.copy(selectedFolders = folders)
    }

    fun deselectFile(fileId: String) {
        val files = _state.value.selectedFiles.toMutableSet()
        files.remove(fileId)
        _state.value = _state.value.copy(selectedFiles = files)
    }

    fun deselectFolder(folderId: String) {
        val folders = _state.value.selectedFolders.toMutableSet()
        folders.remove(folderId)
        _state.value = _state.value.copy(selectedFolders = folders)
    }

    fun isFileSelected(fileId: String): Boolean = fileId in _state.value.selectedFiles

    fun isFolderSelected(folderId: String): Boolean = folderId in _state.value.selectedFolders

    fun getSelectedFiles(): Set<String> = _state.value.selectedFiles.toSet()

    fun getSelectedFolders(): Set<String> = _state.value.selectedFolders.toSet()

    fun getSelectedCount(): Int = _state.value.selectedFiles.size + _state.value.selectedFolders.size

    fun selectAll(files: List<String>, folders: List<String>) {
        _state.value = _state.value.copy(
            selectedFiles = files.toSet(),
            selectedFolders = folders.toSet()
        )
    }

    fun clearAll() {
        _state.value = SelectionState()
    }

    fun getAvailableActions(): List<MultiSelectAction> {
        val fileCount = _state.value.selectedFiles.size
        val folderCount = _state.value.selectedFolders.size

        return when {
            fileCount == 1 && folderCount == 0 -> listOf(
                MultiSelectAction.OPEN,
                MultiSelectAction.SHARE,
                MultiSelectAction.RENAME,
                MultiSelectAction.MOVE,
                MultiSelectAction.DELETE,
                MultiSelectAction.COMPRESS
            )
            fileCount > 1 && folderCount == 0 -> listOf(
                MultiSelectAction.MERGE,
                MultiSelectAction.COMPRESS,
                MultiSelectAction.MOVE,
                MultiSelectAction.DELETE,
                MultiSelectAction.SHARE
            )
            folderCount == 1 && fileCount == 0 -> listOf(
                MultiSelectAction.RENAME,
                MultiSelectAction.MOVE,
                MultiSelectAction.DELETE
            )
            folderCount > 1 && fileCount == 0 -> listOf(
                MultiSelectAction.MOVE,
                MultiSelectAction.DELETE
            )
            fileCount > 0 && folderCount > 0 -> listOf(
                MultiSelectAction.MOVE,
                MultiSelectAction.DELETE
            )
            else -> listOf(MultiSelectAction.DELETE)
        }
    }

    override fun onCleared() {
        super.onCleared()
        _state.value = SelectionState()
    }
}

enum class MultiSelectAction {
    OPEN, SHARE, RENAME, MOVE, DELETE, COMPRESS, MERGE, PRINT, ADD_TAG
}