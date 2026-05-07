package com.aegis.pdf.di

import android.content.Context
import com.aegis.pdf.core.pdf.PdfManager
import com.aegis.pdf.core.pdf.PdfMerger
import com.aegis.pdf.core.pdf.PdfSplitter
import com.aegis.pdf.core.pdf.PdfCompressor
import com.aegis.pdf.core.pdf.PdfConverter
import com.aegis.pdf.data.local.AppDatabase
import com.aegis.pdf.data.local.RecentFileDao
import com.aegis.pdf.data.repository.PdfRepository
import com.aegis.pdf.data.repository.FileRepository
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object AppModule {

    @Provides
    @Singleton
    fun providePdfManager(): PdfManager = PdfManager()

    @Provides
    @Singleton
    fun providePdfMerger(): PdfMerger = PdfMerger()

    @Provides
    @Singleton
    fun providePdfSplitter(): PdfSplitter = PdfSplitter()

    @Provides
    @Singleton
    fun providePdfCompressor(): PdfCompressor = PdfCompressor()

    @Provides
    @Singleton
    fun providePdfConverter(): PdfConverter = PdfConverter()

    @Provides
    @Singleton
    fun provideAppDatabase(@ApplicationContext context: Context): AppDatabase {
        return AppDatabase.getInstance(context)
    }

    @Provides
    @Singleton
    fun provideRecentFileDao(database: AppDatabase): RecentFileDao {
        return database.recentFileDao()
    }

    @Provides
    @Singleton
    fun providePdfRepository(
        @ApplicationContext context: Context,
        pdfManager: PdfManager,
        recentFileDao: RecentFileDao
    ): PdfRepository = PdfRepository(context, pdfManager, recentFileDao)

    @Provides
    @Singleton
    fun provideFileRepository(@ApplicationContext context: Context): FileRepository {
        return FileRepository(context)
    }
}