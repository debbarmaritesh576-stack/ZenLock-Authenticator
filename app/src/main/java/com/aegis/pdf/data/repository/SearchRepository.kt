package com.aegis.pdf.data.repository  
  
import com.aegis.pdf.data.local.db.CloudCacheDao  
import com.aegis.pdf.data.local.RecentFileDao  
import kotlinx.coroutines.flow.Flow  
import kotlinx.coroutines.flow.combine  
import javax.inject.Inject  
  
class SearchRepository @Inject constructor(  
    private val recentFileDao: RecentFileDao,  
    private val cloudCacheDao: CloudCacheDao  
) {  
    // Local aur Cloud dono files mein ek saath search karne ke liye  
    fun searchAllFiles(query: String): Flow<List<SearchQueryResult>> {  
        val localFlow = recentFileDao.searchFiles("%$query%")  
        val cloudFlow = cloudCacheDao.searchFiles("%$query%")  
  
        return combine(localFlow, cloudFlow) { local, cloud ->  
            // Dono results ko merge karke ek list banana  
            val combined = mutableListOf<SearchQueryResult>()  
            combined.addAll(local.map { SearchQueryResult.Local(it) })  
            combined.addAll(cloud.map { SearchQueryResult.Cloud(it) })  
            combined  
        }  
    }  
}  
  
sealed class SearchQueryResult {  
    data class Local(val file: RecentFileEntity) : SearchQueryResult()  
    data class Cloud(val file: CloudCacheEntity) : SearchQueryResult()  
}