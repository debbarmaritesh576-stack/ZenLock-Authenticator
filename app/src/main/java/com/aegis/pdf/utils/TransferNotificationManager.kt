package com.aegis.pdf.utils  
  
import android.app.NotificationChannel  
import android.app.NotificationManager  
import android.content.Context  
import android.os.Build  
import androidx.core.app.NotificationCompat  
import com.aegis.pdf.R  
  
class TransferNotificationManager(private val context: Context) {  
  
    private val notificationManager =  
        context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager  
  
    private val CHANNEL_ID = "cloud_transfer_channel"  
  
    init {  
        createNotificationChannel()  
    }  
  
    private fun createNotificationChannel() {  
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {  
            val channel = NotificationChannel(  
                CHANNEL_ID,  
                "Cloud File Transfers",  
                NotificationManager.IMPORTANCE_LOW  
            ).apply {  
                description = "Shows progress of cloud uploads and downloads"  
            }  
            notificationManager.createNotificationChannel(channel)  
        }  
    }  
  
    fun showProgress(fileName: String, progress: Int, isDownload: String) {  
        val title = if (isDownload == "download") "Downloading $fileName" else "Uploading $fileName"  
          
        val notification = NotificationCompat.Builder(context, CHANNEL_ID)  
            .setContentTitle(title)  
            .setSmallIcon(R.drawable.ic_cloud_sync) // Make sure this icon exists  
            .setPriority(NotificationCompat.PRIORITY_LOW)  
            .setOngoing(true) // User swipe karke hata nahi payega jab tak kaam ho raha hai  
            .setProgress(100, progress, false)  
            .build()  
  
        notificationManager.notify(fileName.hashCode(), notification)  
    }  
  
    fun completeNotification(fileName: String) {  
        val notification = NotificationCompat.Builder(context, CHANNEL_ID)  
            .setContentTitle("Transfer Complete")  
            .setContentText(fileName)  
            .setSmallIcon(R.drawable.ic_check_circle)  
            .setAutoCancel(true) // Click karte hi gayab  
            .build()  
  
        notificationManager.notify(fileName.hashCode(), notification)  
    }  
}