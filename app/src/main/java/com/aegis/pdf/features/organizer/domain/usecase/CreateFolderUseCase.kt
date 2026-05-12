package com.aegis.pdf.features.organizer.domain.usecase

import com.aegis.pdf.features.organizer.data.manager.FolderManager
import com.aegis.pdf.features.organizer.domain.result.FolderResult
import com.aegis.pdf.features.organizer.data.local.entity.FolderEntity
import javax.inject.Inject

class CreateFolderUseCase @Inject constructor(
    private val folderManager: FolderManager
) {
    suspend operator fun invoke(
        name: String,
        parentId: String? = null
    ): FolderResult<FolderEntity> {
        if (name.isBlank()) {
            return FolderResult.Error("Folder name cannot be empty")
        }
        return folderManager.createFolder(name, parentId)
    }
}