package com.aegis.pdf.data.cloud.orchestrator  
  
import com.aegis.pdf.data.cloud.model.CloudPdfFile  
import java.io.File  
  
object ConflictResolver {  
      
    enum class ConflictStrategy { KEEP_LOCAL, KEEP_CLOUD, ASK_USER }  
  
    fun resolve(localFile: File, remoteFile: CloudPdfFile): ConflictStrategy {  
        val localTimestamp = localFile.lastModified()  
        val remoteTimestamp = remoteFile.lastModified  
  
        return when {  
            // Agar local file nayi hai, toh cloud ko update karo  
            localTimestamp > remoteTimestamp + 2000 -> ConflictStrategy.KEEP_LOCAL  
              
            // Agar cloud file nayi hai, toh local ko overwrite karo  
            remoteTimestamp > localTimestamp + 2000 -> ConflictStrategy.KEEP_CLOUD  
              
            // Agar dono same hain, toh kuch mat karo  
            else -> ConflictStrategy.KEEP_LOCAL   
        }  
    }  
}