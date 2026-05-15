package com.aegis.pdf.di  
  
import android.content.Context  
import com.aegis.pdf.core.pdf.*  
import com.aegis.pdf.data.cloud.* // Naya import  
import com.aegis.pdf.data.local.AppDatabase  
import com.aegis.pdf.data.local.DocumentDataSource  
import com.aegis.pdf.data.local.RecentFileDao  
import com.aegis.pdf.data.local.db.CloudCacheDao // Naya import  
import com.aegis.pdf.data.repository.PdfRepository  
import com.aegis.pdf.data.repository.FileRepository  
import com.aegis.pdf.domain.usecase.*  
import com.aegis.pdf.utils.TransferNotificationManager // Naya import  
import dagger.Module  
import dagger.Provides  
import dagger.hilt.InstallIn  
import dagger.hilt.android.qualifiers.ApplicationContext  
import dagger.hilt.components.SingletonComponent  
import okhttp3.OkHttpClient  
import javax.inject.Singleton  
  
@Module  
@InstallIn(SingletonComponent::class)  
object AppModule {  
  
    // --- PDF Core Components ---  
    @Provides @Singleton  
    fun providePdfManager(): PdfManager = PdfManager()  
  
    @Provides @Singleton  
    fun providePdfSessionManager(): PdfSessionManager = PdfSessionManager()  
  
    @Provides @Singleton  
    fun providePdfMerger(): PdfMerger = PdfMerger()  
  
    @Provides @Singleton  
    fun providePdfSplitter(): PdfSplitter = PdfSplitter()  
  
    @Provides @Singleton  
    fun providePdfCompressor(): PdfCompressor = PdfCompressor()  
  
    @Provides @Singleton  
    fun providePdfConverter(): PdfConverter = PdfConverter()  
  
    // --- Database & Local Storage ---  
    @Provides @Singleton  
    fun provideAppDatabase(@ApplicationContext context: Context): AppDatabase =  
        AppDatabase.getInstance(context)  
  
    @Provides @Singleton  
    fun provideRecentFileDao(db: AppDatabase): RecentFileDao = db.recentFileDao()  
  
    @Provides @Singleton  
    fun provideCloudCacheDao(db: AppDatabase): CloudCacheDao = db.cloudCacheDao() // Bridge for Cloud Cache  
  
    @Provides @Singleton  
    fun provideDocumentDataSource(@ApplicationContext context: Context): DocumentDataSource =  
        DocumentDataSource(context)  
  
    // --- Cloud Components (NEW) ---  
    @Provides @Singleton  
    fun provideOkHttpClient(): OkHttpClient = OkHttpClient.Builder().build()  
  
    @Provides @Singleton  
    fun provideTransferNotificationManager(@ApplicationContext context: Context): TransferNotificationManager =  
        TransferNotificationManager(context)  
  
    @Provides @Singleton  
    fun provideCloudOrchestrator(  
        repository: CloudFileRepository,  
        cacheDao: CloudCacheDao  
    ): CloudOrchestrator = CloudOrchestrator(repository, cacheDao)  
  
    // --- Repositories ---  
    @Provides @Singleton  
    fun providePdfRepository(  
        @ApplicationContext context: Context,  
        pdfManager: PdfManager,  
        recentFileDao: RecentFileDao  
    ): PdfRepository = PdfRepository(context, pdfManager, recentFileDao)  
  
    @Provides @Singleton  
    fun provideFileRepository(@ApplicationContext context: Context): FileRepository =  
        FileRepository(context)  
  
    // --- UseCases ---  
    @Provides @Singleton  
    fun provideMergePdfUseCase(  
        pdfMerger: PdfMerger,  
        dataSource: DocumentDataSource,  
        repo: PdfRepository  
    ): MergePdfUseCase = MergePdfUseCase(pdfMerger, dataSource, repo)  
  
    @Provides @Singleton  
    fun provideSplitPdfUseCase(  
        pdfSplitter: PdfSplitter,  
        dataSource: DocumentDataSource,  
        repo: PdfRepository  
    ): SplitPdfUseCase = SplitPdfUseCase(pdfSplitter, dataSource, repo)  
  
    @Provides @Singleton  
    fun provideCompressPdfUseCase(  
        pdfCompressor: PdfCompressor,  
        dataSource: DocumentDataSource,  
        repo: PdfRepository  
    ): CompressPdfUseCase = CompressPdfUseCase(pdfCompressor, dataSource, repo)  
  
    @Provides @Singleton  
    fun provideConvertPdfUseCase(  
        pdfConverter: PdfConverter,  
        dataSource: DocumentDataSource,  
        repo: PdfRepository  
    ): ConvertPdfUseCase = ConvertPdfUseCase(pdfConverter, dataSource, repo)  
}