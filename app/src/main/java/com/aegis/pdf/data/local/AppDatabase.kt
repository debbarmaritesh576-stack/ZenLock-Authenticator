package com.aegis.pdf.data.local  
  
import android.content.Context  
import androidx.room.Database  
import androidx.room.Room  
import androidx.room.RoomDatabase  
import com.aegis.pdf.data.local.db.CloudCacheDao  
import com.aegis.pdf.data.local.db.CloudCacheEntity  
  
@Database(  
    entities = [  
        RecentFileEntity::class,   
        CloudCacheEntity::class // <--- Cloud Cache Entity yahan add ho gayi  
    ],   
    version = 2, // Version upgrade kiya kyunki schema change hua hai  
    exportSchema = false  
)  
abstract class AppDatabase : RoomDatabase() {  
  
    // Saare DAOs yahan declare honge  
    abstract fun recentFileDao(): RecentFileDao  
    abstract fun cloudCacheDao(): CloudCacheDao // <--- Naya DAO bridge  
  
    companion object {  
        @Volatile  
        private var INSTANCE: AppDatabase? = null  
  
        fun getInstance(context: Context): AppDatabase {  
            return INSTANCE ?: synchronized(this) {  
                val instance = Room.databaseBuilder(  
                    context.applicationContext,  
                    AppDatabase::class.java,  
                    "aegis_pdf_db"  
                )  
                // Production Tip: Migrations handle karna best hota hai,   
                // par abhi ke liye hum destructive migration use kar rahe hain  
                .fallbackToDestructiveMigration()   
                .build()  
                  
                INSTANCE = instance  
                instance  
            }  
        }  
    }  
}