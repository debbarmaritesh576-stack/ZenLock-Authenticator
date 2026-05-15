package com.aegis.pdf.utils  
  
import android.content.Context  
import android.media.MediaScannerConnection  
import java.io.File  
  
object CloudFileScanner {  
    fun scanFile(context: Context, file: File) {  
        MediaScannerConnection.scanFile(  
            context,  
            arrayOf(file.absolutePath),  
            arrayOf("application/pdf")  
        ) { path, uri ->  
            // File scan complete, ab ye system wide accessible hai  
        }  
    }  
}