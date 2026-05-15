package com.aegis.pdf.data.local  
  
import android.content.Context  
import androidx.room.Database  
import androidx.room.Room  
import androidx.room.RoomDatabase  
import com.aegis.pdf.data.local.db.CloudCacheDao  
import com.aegis.pdf.data.local.db.CloudCacheEntity  
import com.aegis.pdf.data.local.db.OcrSearchDao  
import com.aegis.pdf.data.local.db.OcrSearchEntity  
  
@Database(  
    entities = [  
        RecentFileEntity::class,   
        CloudCacheEntity::class,   
        OcrSearchEntity::class // Full-Text Search Table  
    ],  
    version = 3, // Version 3 kyunki humne OCR Search add kiya hai  
    exportSchema = false  
)  
abstract class AppDatabase : RoomDatabase() {  
  
    // --- DAOs (Interfaces) ---  
    abstract fun recentFileDao(): RecentFileDao  
    abstract fun cloudCacheDao(): CloudCacheDao  
    abstract fun ocrSearchDao(): OcrSearchDao  
  
    companion object {  
        @Volatile  
        private var INSTANCE: AppDatabase? = null  
  
        private const val DB_NAME = "aegis_pdf_db"  
  
        fun getInstance(context: Context): AppDatabase {  
            return INSTANCE ?: synchronized(this) {  
                val instance = Room.databaseBuilder(  
                    context.applicationContext,  
                    AppDatabase::class.java,  
                    DB_NAME  
                )  
                /**  
                 * fallbackToDestructiveMigration:   
                 * Jab aap version 1 se 2 ya 3 par jaoge, toh ye purana data delete karke   
                 * naya table banayega. Production mein data bachane ke liye hum   
                 * .addMigrations() use karte hain.  
                 */  
                .fallbackToDestructiveMigration()   
                .build()  
                  
                INSTANCE = instance  
                instance  
            }  
        }  
    }  
}