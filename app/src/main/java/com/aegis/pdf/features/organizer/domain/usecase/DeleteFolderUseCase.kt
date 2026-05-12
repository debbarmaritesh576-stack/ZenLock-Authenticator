package com.aegis.pdf.features.organizer.domain.usecase

import com.aegis.pdf.features.organizer.data.manager.FolderManager
import com.aegis.pdf.features.organizer.domain.result.FolderResult
import javax.inject.Inject

class DeleteFolderUseCase @Inject constructor(
    private val folderManager: FolderManager
) {
    suspend operator fun invoke(folderId: String): FolderResult<Unit> {
        if (folderId.isBlank()) {
            return FolderResult.Error("Folder ID cannot be empty")
        }
        return folderManager.deleteFolder(folderId)
    }
}