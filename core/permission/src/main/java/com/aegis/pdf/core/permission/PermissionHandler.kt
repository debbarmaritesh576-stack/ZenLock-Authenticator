package com.aegis.pdf.core.permission  
  
import android.Manifest  
import android.content.Context  
import android.content.pm.PackageManager  
import android.os.Build  
import androidx.core.content.ContextCompat  
import javax.inject.Inject  
import javax.inject.Singleton  
  
@Singleton  
class PermissionHandler @Inject constructor(  
    private val context: Context  
) {  
    /**  
     * Determine karega ki current Android OS version ke mutabik kaunsi permissions zaroori hain.  
     */  
    fun getRequiredStoragePermissions(): List<String> {  
        return when {  
            // Android 13 (API 33) aur usse upar ke liye modern media permissions  
            Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU -> {  
                listOf(  
                    Manifest.permission.READ_MEDIA_IMAGES,  
                    Manifest.permission.READ_MEDIA_VIDEO  
                )  
            }  
            // Android 10, 11, 12 ke liye standard Read (Scoped storage automatic handles Write)  
            Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q -> {  
                listOf(Manifest.permission.READ_EXTERNAL_STORAGE)  
            }  
            // Legacy Android 9 (API 28) aur usse niche ke liye explicit Read & Write  
            else -> {  
                listOf(  
                    Manifest.permission.READ_EXTERNAL_STORAGE,  
                    Manifest.permission.WRITE_EXTERNAL_STORAGE  
                )  
            }  
        }  
    }  
  
    /**  
     * Check karta hai ki kya saari required permissions pehle se granted hain ya nahi.  
     */  
    fun isStoragePermissionGranted(): Boolean {  
        return getRequiredStoragePermissions().all { permission ->  
            ContextCompat.checkSelfPermission(context, permission) == PackageManager.PERMISSION_GRANTED  
        }  
    }  
}