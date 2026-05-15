package com.aegis.pdf.data.repository  
  
import com.aegis.pdf.data.local.RecentFileDao  
import com.aegis.pdf.data.local.db.CloudCacheDao  
import com.aegis.pdf.data.local.db.OcrSearchDao  
import com.aegis.pdf.data.local.db.OcrSearchEntity  
import kotlinx.coroutines.Dispatchers  
import kotlinx.coroutines.flow.*  
import javax.inject.Inject  
import javax.inject.Singleton  
  
@Singleton  
class SearchRepository @Inject constructor(  
    private val recentFileDao: RecentFileDao,  
    private val cloudCacheDao: CloudCacheDao,  
    private val ocrSearchDao: OcrSearchDao  
) {  
  
    /**  
     * Search strategy:   
     * 1. Metadata Search (Filenames)  
     * 2. Deep Content Search (Inside PDF text using FTS5)  
     */  
    fun searchUniversal(query: String): Flow<List<SearchResult>> {  
        if (query.isBlank()) return flowOf(emptyList())  
  
        val formattedQuery = "*$query*" // FTS5 prefix/suffix search logic  
  
        return combine(  
            recentFileDao.searchFiles("%$query%"),  
            cloudCacheDao.searchFiles("%$query%"),  
            // FTS5 content search ko Flow mein convert karna  
            flow { emit(ocrSearchDao.searchInsidePdfs(formattedQuery)) }  
        ) { local, cloud, content ->  
            val results = mutableListOf<SearchResult>()  
  
            // Local Files mapping  
            local.forEach { results.add(SearchResult.LocalFile(it.fileName, it.filePath)) }  
  
            // Cloud Files mapping  
            cloud.forEach { results.add(SearchResult.CloudFile(it.name, it.provider, it.fileId)) }  
  
            // Deep Content mapping  
            content.forEach { results.add(SearchResult.DeepContent(it.fileName, it.fileId)) }  
  
            results.distinctBy { it.identifier } // Duplicate hatao  
        }.flowOn(Dispatchers.IO)  
    }  
}  
  
sealed class SearchResult {  
    abstract val title: String  
    abstract val identifier: String  
  
    data class LocalFile(override val title: String, override val identifier: String) : SearchResult()  
    data class CloudFile(override val title: String, val provider: String, override val identifier: String) : SearchResult()  
    data class DeepContent(override val title: String, override val identifier: String) : SearchResult()  
}