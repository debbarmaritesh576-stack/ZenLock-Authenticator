package com.aegis.pdf.di  
  
import android.content.Context  
import com.aegis.pdf.core.pdf.*  
import com.aegis.pdf.core.ocr.*  
import com.aegis.pdf.data.local.AppDatabase  
import com.aegis.pdf.data.local.RecentFileDao  
import com.aegis.pdf.data.local.db.CloudCacheDao  
import com.aegis.pdf.data.local.db.OcrSearchDao  
import com.aegis.pdf.data.repository.*  
import com.aegis.pdf.domain.usecase.*  
import dagger.Module  
import dagger.Provides  
import dagger.hilt.InstallIn  
import dagger.hilt.android.qualifiers.ApplicationContext  
import dagger.hilt.components.SingletonComponent  
import javax.inject.Singleton  
  
@Module  
@InstallIn(SingletonComponent::class)  
object AppModule {  
  
    // --- 1. Database & DAOs ---  
    @Provides @Singleton  
    fun provideAppDatabase(@ApplicationContext context: Context): AppDatabase =  
        AppDatabase.getInstance(context)  
  
    @Provides @Singleton  
    fun provideRecentFileDao(db: AppDatabase): RecentFileDao = db.recentFileDao()  
  
    @Provides @Singleton  
    fun provideCloudCacheDao(db: AppDatabase): CloudCacheDao = db.cloudCacheDao()  
  
    @Provides @Singleton  
    fun provideOcrSearchDao(db: AppDatabase): OcrSearchDao = db.ocrSearchDao()  
  
  
    // --- 2. PDF Core Engines ---  
    @Provides @Singleton  
    fun providePdfManager(): PdfManager = PdfManager()  
  
    @Provides @Singleton  
    fun providePdfSessionManager(): PdfSessionManager = PdfSessionManager()  
  
  
    // --- 3. OCR & Search Logic ---  
    @Provides @Singleton  
    fun provideOcrEngine(): OcrEngine = OcrEngine()  
  
    @Provides @Singleton  
    fun provideOcrProcessor(engine: OcrEngine): OcrProcessor = OcrProcessor(engine)  
  
    @Provides @Singleton  
    fun provideSearchRepository(  
        recentDao: RecentFileDao,  
        cloudDao: CloudCacheDao,  
        ocrDao: OcrSearchDao  
    ): SearchRepository = SearchRepository(recentDao, cloudDao, ocrDao)  
  
  
    // --- 4. Repositories ---  
    @Provides @Singleton  
    fun providePdfRepository(  
        @ApplicationContext context: Context,  
        pdfManager: PdfManager,  
        recentFileDao: RecentFileDao  
    ): PdfRepository = PdfRepository(context, pdfManager, recentFileDao)  
  
    @Provides @Singleton  
    fun provideFileRepository(@ApplicationContext context: Context): FileRepository =  
        FileRepository(context)  
  
  
    // --- 5. Use Cases (Business Logic) ---  
    @Provides @Singleton  
    fun provideMergePdfUseCase(repo: PdfRepository): MergePdfUseCase = MergePdfUseCase(repo)  
  
    @Provides @Singleton  
    fun provideOcrUseCase(processor: OcrProcessor, ocrDao: OcrSearchDao): OcrUseCase =   
        OcrUseCase(processor, ocrDao)  
}