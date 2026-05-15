package com.aegis.pdf.data.cloud  
  
import com.aegis.pdf.data.cloud.model.CloudPdfFile  
import com.aegis.pdf.data.cloud.model.NetworkResult  
import com.aegis.pdf.data.local.db.CloudCacheDao  
import com.aegis.pdf.data.local.db.CloudCacheEntity  
import kotlinx.coroutines.flow.*  
import javax.inject.Inject  
import javax.inject.Singleton  
  
@Singleton  
class CloudOrchestrator @Inject constructor(  
    private val repository: CloudFileRepository,  
    private val cacheDao: CloudCacheDao  
) {  
  
    /**  
     * Logic: Pehle Room DB se data emit karo, phir Network se fetch karke   
     * DB update karo aur naya data emit karo.  
     */  
    fun getFiles(providerName: String): Flow<NetworkResult<List<CloudPdfFile>>> = flow {  
        emit(NetworkResult.Loading)  
  
        // 1. Load from Local Cache first  
        val cachedData = cacheDao.getFilesByProvider(providerName).firstOrNull()?.map { it.toDomain() }  
        if (!cachedData.isNullOrEmpty()) {  
            emit(NetworkResult.Success(cachedData))  
        }  
  
        // 2. Fetch from Network  
        try {  
            val providerEnum = CloudFileRepository.Provider.valueOf(providerName)  
            val remoteFiles = repository.getProvider(providerEnum).listPdfFiles()  
  
            // 3. Update Cache in DB  
            cacheDao.refreshCache(providerName, remoteFiles.map { it.toEntity(providerName) })  
  
            // 4. Emit fresh data  
            emit(NetworkResult.Success(remoteFiles))  
        } catch (e: Exception) {  
            // Agar pehle cache mil gaya tha, toh error mat dikhao, bas log karo  
            if (cachedData.isNullOrEmpty()) {  
                emit(NetworkResult.Error("Sync failed: ${e.localizedMessage}"))  
            }  
        }  
    }  
  
    // Mappers to convert between DB Entity and UI Model  
    private fun CloudCacheEntity.toDomain() = CloudPdfFile(id, name, size, provider)  
    private fun CloudPdfFile.toEntity(p: String) = CloudCacheEntity(id, name, size, p)  
}