package com.aegis.pdf.features.organizer.di

import com.aegis.pdf.features.organizer.data.manager.FolderManager
import com.aegis.pdf.features.organizer.data.manager.TagManager
import com.aegis.pdf.features.organizer.data.manager.StorageAnalytics
import com.aegis.pdf.features.organizer.data.repository.FolderRepository
import com.aegis.pdf.features.organizer.domain.usecase.CreateFolderUseCase
import com.aegis.pdf.features.organizer.domain.usecase.DeleteFolderUseCase
import com.aegis.pdf.features.organizer.domain.usecase.RenameFolderUseCase
import com.aegis.pdf.features.organizer.domain.usecase.MoveFolderUseCase
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object OrganizerModule {

    @Provides
    @Singleton
    fun provideCreateFolderUseCase(
        folderManager: FolderManager
    ): CreateFolderUseCase {
        return CreateFolderUseCase(folderManager)
    }

    @Provides
    @Singleton
    fun provideDeleteFolderUseCase(
        folderManager: FolderManager
    ): DeleteFolderUseCase {
        return DeleteFolderUseCase(folderManager)
    }

    @Provides
    @Singleton
    fun provideRenameFolderUseCase(
        folderManager: FolderManager
    ): RenameFolderUseCase {
        return RenameFolderUseCase(folderManager)
    }

    @Provides
    @Singleton
    fun provideMoveFolderUseCase(
        folderManager: FolderManager
    ): MoveFolderUseCase {
        return MoveFolderUseCase(folderManager)
    }
}