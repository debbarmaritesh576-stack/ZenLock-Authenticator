package com.aegis.pdf.data.cloud.utils  
  
import okhttp3.Interceptor  
import okhttp3.Response  
import java.io.File  
  
class DownloadInterceptor(private val downloadDir: File) : Interceptor {  
    override fun intercept(chain: Interceptor.Chain): Response {  
        val request = chain.request()  
        val fileName = request.url.pathSegments.last()  
        val partialFile = File(downloadDir, "$fileName.part")  
  
        val newRequest = if (partialFile.exists() && partialFile.length() > 0) {  
            // Server ko bolo: "Mujhe sirf bacha hua data chahiye"  
            request.newBuilder()  
                .header("Range", "bytes=${partialFile.length()}-")  
                .build()  
        } else {  
            request  
        }  
  
        return chain.proceed(newRequest)  
    }  
}