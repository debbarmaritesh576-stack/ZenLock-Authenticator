package com.aegis.pdf.core.pdf  
  
import android.graphics.Bitmap  
import android.view.Surface  
  
/**  
 * Ye file C++ (Native) side se baat karne ka ekmaatra zariya hai.  
 */  
class NativeBridge {  
  
    init {  
        System.loadLibrary("aegis_pdf_engine") // Tumhari C++ library ka naam  
    }  
  
    // PDF load karna (Memory address return karega)  
    external fun nativeOpenDocument(path: String, password: String?): Long  
  
    // Page render karna seedha Bitmap par (Super Fast)  
    external fun nativeRenderPage(  
        docPtr: Long,   
        pageNumber: Int,   
        bitmap: Bitmap,   
        dpi: Int  
    )  
  
    // PDF ke andar text search karna (C++ level par)  
    external fun nativeSearchText(docPtr: Long, query: String): Array<TextRect>  
  
    // Document band karke memory free karna (CRITICAL for Production)  
    external fun nativeCloseDocument(docPtr: Long)  
  
    companion object {  
        @Volatile  
        private var instance: NativeBridge? = null  
  
        fun getInstance(): NativeBridge {  
            return instance ?: synchronized(this) {  
                instance ?: NativeBridge().also { instance = it }  
            }  
        }  
    }  
}  
  
// Search result ki coordinates handle karne ke liye  
data class TextRect(val page: Int, val left: Float, val top: Float, val right: Float, val bottom: Float)