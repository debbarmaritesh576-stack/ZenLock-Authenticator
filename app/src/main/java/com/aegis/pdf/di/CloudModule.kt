package com.aegis.pdf.di  
  
import android.content.Context  
import com.aegis.pdf.data.cloud.*  
import com.aegis.pdf.data.local.db.AppDatabase  
import com.aegis.pdf.data.local.db.CloudCacheDao  
import dagger.Module  
import dagger.Provides  
import dagger.hilt.InstallIn  
import dagger.hilt.android.qualifiers.ApplicationContext  
import dagger.hilt.components.SingletonComponent  
import okhttp3.OkHttpClient  
import java.util.concurrent.TimeUnit  
import javax.inject.Singleton  
  
@Module  
@InstallIn(SingletonComponent::class)  
object CloudModule {  
  
    @Provides  
    @Singleton  
    fun provideOkHttpClient(): OkHttpClient {  
        return OkHttpClient.Builder()  
            .connectTimeout(30, TimeUnit.SECONDS)  
            .readTimeout(30, TimeUnit.SECONDS)  
            .build()  
    }  
  
    @Provides  
    fun provideCloudCacheDao(db: AppDatabase): CloudCacheDao {  
        return db.cloudCacheDao()  
    }  
  
    @Provides  
    @Singleton  
    fun provideCloudOrchestrator(  
        repository: CloudFileRepository,  
        cacheDao: CloudCacheDao  
    ): CloudOrchestrator {  
        return CloudOrchestrator(repository, cacheDao)  
    }  
}