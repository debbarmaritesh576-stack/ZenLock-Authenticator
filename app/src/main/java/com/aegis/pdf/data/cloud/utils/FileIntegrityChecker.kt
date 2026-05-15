package com.aegis.pdf.data.cloud.utils  
  
import java.io.File  
import java.io.FileInputStream  
import java.security.MessageDigest  
  
object FileIntegrityChecker {  
      
    // MD5 Hash nikalne ka logic taaki cloud hash se match kar sakein  
    fun calculateMD5(file: File): String {  
        val digest = MessageDigest.getInstance("MD5")  
        val inputStream = FileInputStream(file)  
        val buffer = ByteArray(8192)  
        var read: Int  
        try {  
            while (inputStream.read(buffer).also { read = it } > 0) {  
                digest.update(buffer, 0, read)  
            }  
            val md5sum = digest.digest()  
            return md5sum.joinToString("") { "%02x".format(it) }  
        } finally {  
            inputStream.close()  
        }  
    }  
}