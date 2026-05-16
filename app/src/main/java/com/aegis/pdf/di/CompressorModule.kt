package com.aegis.pdf.di  
  
import android.content.Context  
import com.aegis.pdf.core.pdf.PdfCompressorEngine  
import dagger.Module  
import dagger.Provides  
import dagger.hilt.InstallIn  
import dagger.hilt.android.qualifiers.ApplicationContext  
import dagger.hilt.components.SingletonComponent  
import javax.inject.Singleton  
  
@Module  
@InstallIn(SingletonComponent::class)  
object CompressorModule {  
  
    @Provides  
    @Singleton  
    fun providePdfCompressorEngine(  
        @ApplicationContext context: Context  
    ): PdfCompressorEngine {  
        return PdfCompressorEngine(context)  
    }  
}