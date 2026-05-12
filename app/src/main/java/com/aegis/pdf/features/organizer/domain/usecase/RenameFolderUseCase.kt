package com.aegis.pdf.features.organizer.domain.usecase

import com.aegis.pdf.features.organizer.data.manager.FolderManager
import com.aegis.pdf.features.organizer.domain.result.FolderResult
import javax.inject.Inject

class RenameFolderUseCase @Inject constructor(
    private val folderManager: FolderManager
) {
    suspend operator fun invoke(
        folderId: String,
        newName: String
    ): FolderResult<Unit> {
        if (newName.isBlank()) {
            return FolderResult.Error("Folder name cannot be empty")
        }
        return folderManager.renameFolder(folderId, newName)
    }
}