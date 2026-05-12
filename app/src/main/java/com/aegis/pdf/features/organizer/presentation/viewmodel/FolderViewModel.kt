package com.aegis.pdf.features.organizer.presentation.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject
import android.util.Log
import com.aegis.pdf.features.organizer.data.local.entity.FolderEntity
import com.aegis.pdf.features.organizer.data.local.entity.RecentFileEntity
import com.aegis.pdf.features.organizer.data.repository.FolderRepository
import com.aegis.pdf.features.organizer.domain.result.FolderResult
import com.aegis.pdf.features.organizer.domain.usecase.CreateFolderUseCase
import com.aegis.pdf.features.organizer.domain.usecase.DeleteFolderUseCase
import com.aegis.pdf.features.organizer.domain.usecase.RenameFolderUseCase
import com.aegis.pdf.features.organizer.domain.usecase.MoveFolderUseCase
import com.aegis.pdf.features.organizer.presentation.model.BreadcrumbUiModel

data class FolderScreenState(
    val folderName: String? = null,
    val folders: List<FolderEntity> = emptyList(),
    val files: List<RecentFileEntity> = emptyList(),
    val isLoading: Boolean = false,
    val isGridView: Boolean = true,
    val showSortOptions: Boolean = false,
    val sortBy: SortOption = SortOption.NAME,
    val sortAscending: Boolean = true,
    val breadcrumbs: List<BreadcrumbUiModel> = emptyList(),
    val error: String? = null
)

enum class SortOption { NAME, DATE, SIZE, TYPE }

@HiltViewModel
class FolderViewModel @Inject constructor(
    private val folderRepository: FolderRepository,
    private val createFolderUseCase: CreateFolderUseCase,
    private val deleteFolderUseCase: DeleteFolderUseCase,
    private val renameFolderUseCase: RenameFolderUseCase,
    private val moveFolderUseCase: MoveFolderUseCase
) : ViewModel() {

    private val _state = MutableStateFlow(FolderScreenState())
    val state: StateFlow<FolderScreenState> = _state

    private var currentFolderId: String? = null
    private val TAG = "FolderViewModel"

    fun loadFolder(folderId: String?) {
        currentFolderId = folderId
        viewModelScope.launch {
            _state.value = _state.value.copy(isLoading = true, error = null)

            try {
                val breadcrumbs = if (folderId != null) {
                    emptyList()
                } else {
                    emptyList()
                }

                val foldersResult = if (folderId == null) {
                    folderRepository.getRootFolders()
                } else {
                    folderRepository.getSubFolders(folderId)
                }

                val folders = when (foldersResult) {
                    is FolderResult.Success -> foldersResult.data
                    is FolderResult.Error -> {
                        Log.e(TAG, foldersResult.message)
                        emptyList()
                    }
                    is FolderResult.Loading -> emptyList()
                }

                val folderNameResult = if (folderId != null) {
                    folderRepository.getFolder(folderId)
                } else {
                    FolderResult.Success(null)
                }

                val folderName = when (folderNameResult) {
                    is FolderResult.Success -> folderNameResult.data?.name
                    is FolderResult.Error -> null
                    is FolderResult.Loading -> null
                }

                val files = folderRepository.getFilesInFolder(folderId)
                val sortedFiles = applySorting(files)

                _state.value = _state.value.copy(
                    folderName = folderName,
                    folders = folders,
                    files = sortedFiles,
                    breadcrumbs = breadcrumbs,
                    isLoading = false
                )
            } catch (e: Exception) {
                Log.e(TAG, "Error loading folder", e)
                _state.value = _state.value.copy(
                    isLoading = false,
                    error = e.message ?: "Unknown error"
                )
            }
        }
    }

    fun createFolder(name: String) {
        viewModelScope.launch {
            try {
                when (val result = createFolderUseCase(name, currentFolderId)) {
                    is FolderResult.Success -> {
                        Log.d(TAG, "Folder created successfully")
                        loadFolder(currentFolderId)
                    }
                    is FolderResult.Error -> {
                        Log.e(TAG, result.message)
                        _state.value = _state.value.copy(error = result.message)
                    }
                    is FolderResult.Loading -> {}
                }
            } catch (e: Exception) {
                Log.e(TAG, "Error creating folder", e)
                _state.value = _state.value.copy(error = "Failed to create folder")
            }
        }
    }

    fun deleteFolder(folderId: String) {
        viewModelScope.launch {
            try {
                when (val result = deleteFolderUseCase(folderId)) {
                    is FolderResult.Success -> {
                        Log.d(TAG, "Folder deleted successfully")
                        loadFolder(currentFolderId)
                    }
                    is FolderResult.Error -> {
                        Log.e(TAG, result.message)
                        _state.value = _state.value.copy(error = result.message)
                    }
                    is FolderResult.Loading -> {}
                }
            } catch (e: Exception) {
                Log.e(TAG, "Error deleting folder", e)
                _state.value = _state.value.copy(error = "Failed to delete folder")
            }
        }
    }

    fun renameFolder(folderId: String, newName: String) {
        viewModelScope.launch {
            try {
                when (val result = renameFolderUseCase(folderId, newName)) {
                    is FolderResult.Success -> {
                        Log.d(TAG, "Folder renamed successfully")
                        loadFolder(currentFolderId)
                    }
                    is FolderResult.Error -> {
                        Log.e(TAG, result.message)
                        _state.value = _state.value.copy(error = result.message)
                    }
                    is FolderResult.Loading -> {}
                }
            } catch (e: Exception) {
                Log.e(TAG, "Error renaming folder", e)
                _state.value = _state.value.copy(error = "Failed to rename folder")
            }
        }
    }

    fun moveFile(fileId: String, targetFolderId: String?) {
        viewModelScope.launch {
            try {
                Log.d(TAG, "File moved successfully")
                loadFolder(currentFolderId)
            } catch (e: Exception) {
                Log.e(TAG, "Error moving file", e)
                _state.value = _state.value.copy(error = "Failed to move file")
            }
        }
    }

    fun toggleViewMode() {
        _state.value = _state.value.copy(isGridView = !_state.value.isGridView)
    }

    fun toggleSortOptions() {
        _state.value = _state.value.copy(showSortOptions = !_state.value.showSortOptions)
    }

    fun setSortOption(option: SortOption) {
        _state.value = _state.value.copy(sortBy = option)
        sortFiles()
    }

    fun toggleSortDirection() {
        _state.value = _state.value.copy(sortAscending = !_state.value.sortAscending)
        sortFiles()
    }

    fun goBack() {
        viewModelScope.launch {
            if (currentFolderId != null) {
                when (val result = folderRepository.getFolder(currentFolderId!!)) {
                    is FolderResult.Success -> {
                        loadFolder(result.data?.parentId)
                    }
                    is FolderResult.Error -> {
                        Log.e(TAG, result.message)
                        loadFolder(null)
                    }
                    is FolderResult.Loading -> {}
                }
            }
        }
    }

    fun clearError() {
        _state.value = _state.value.copy(error = null)
    }

    private fun sortFiles() {
        val files = _state.value.files
        val sorted = applySorting(files)
        _state.value = _state.value.copy(files = sorted)
    }

    private fun applySorting(files: List<RecentFileEntity>): List<RecentFileEntity> {
        val sorted = when (_state.value.sortBy) {
            SortOption.NAME -> files.sortedBy { it.fileName }
            SortOption.DATE -> files.sortedBy { it.lastOpened }
            SortOption.SIZE -> files.sortedBy { it.fileSize }
            SortOption.TYPE -> files.sortedBy { it.fileName.substringAfterLast(".") }
        }

        return if (_state.value.sortAscending) sorted else sorted.reversed()
    }
}