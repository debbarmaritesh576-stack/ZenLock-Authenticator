package com.aegis.pdf.ui.viewer  
  
import android.content.Context  
import android.graphics.Bitmap  
import android.util.AttributeSet  
import android.view.SurfaceHolder  
import android.view.SurfaceView  
import com.aegis.pdf.core.pdf.NativeBridge  
import kotlinx.coroutines.*  
  
class PdfRendererView @Context constructor(  
    context: Context,  
    attrs: AttributeSet? = null  
) : SurfaceView(context, attrs), SurfaceHolder.Callback {  
  
    private val nativeBridge = NativeBridge.getInstance()  
    private var documentPtr: Long = 0  
    private var currentPage = 0  
      
    private val renderScope = CoroutineScope(Dispatchers.Default + Job())  
  
    init {  
        holder.addCallback(this)  
    }  
  
    // 1. PDF Load Karne ka Function  
    fun openPdf(path: String) {  
        renderScope.launch {  
            documentPtr = nativeBridge.nativeOpenDocument(path, null)  
            renderCurrentPage()  
        }  
    }  
  
    // 2. High-Performance Rendering Logic  
    private fun renderCurrentPage() {  
        if (documentPtr == 0L) return  
  
        renderScope.launch {  
            val canvas = holder.lockCanvas()  
            if (canvas != null) {  
                // Ek khali bitmap banao jismein C++ data bharega  
                val bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)  
                  
                // JNI Call: C++ ko bolo pixels bharne ke liye  
                nativeBridge.nativeRenderPage(documentPtr, currentPage, bitmap, 160)  
                  
                // Canvas par bitmap draw karo  
                canvas.drawBitmap(bitmap, 0f, 0f, null)  
                holder.unlockCanvasAndPost(canvas)  
                  
                // Memory bachane ke liye bitmap recycle karo  
                bitmap.recycle()  
            }  
        }  
    }  
  
    override fun surfaceCreated(holder: SurfaceHolder) {  
        // Ready to draw  
    }  
  
    override fun surfaceChanged(holder: SurfaceHolder, format: Int, width: Int, height: Int) {  
        renderCurrentPage()  
    }  
  
    override fun surfaceDestroyed(holder: SurfaceHolder) {  
        // C++ memory free karo taaki app crash na ho  
        renderScope.launch {  
            if (documentPtr != 0L) {  
                nativeBridge.nativeCloseDocument(documentPtr)  
                documentPtr = 0L  
            }  
            renderScope.cancel()  
        }  
    }  
}