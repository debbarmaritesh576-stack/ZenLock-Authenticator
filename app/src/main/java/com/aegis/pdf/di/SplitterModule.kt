package com.aegis.pdf.di  
  
import com.aegis.pdf.core.pdf.PdfSplitter  
import dagger.Module  
import dagger.Provides  
import dagger.hilt.InstallIn  
import dagger.hilt.components.SingletonComponent  
import javax.inject.Singleton  
  
@Module  
@InstallIn(SingletonComponent::class)  
object SplitterModule {  
  
    @Provides  
    @Singleton  
    fun providePdfSplitter(): PdfSplitter {  
        return PdfSplitter()  
    }  
}