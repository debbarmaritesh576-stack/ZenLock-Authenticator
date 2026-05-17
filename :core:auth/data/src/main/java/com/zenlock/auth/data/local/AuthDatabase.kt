package com.zenlock.auth.data.local

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import com.zenlock.auth.data.local.dao.AuthAccountDao
import com.zenlock.auth.data.local.entity.AuthAccountEntity

@Database(
    entities = [AuthAccountEntity::class],
    version = 1,
    exportSchema = false
)
abstract class AuthDatabase : RoomDatabase() {

    abstract fun authAccountDao(): AuthAccountDao

    companion object {

        @Volatile
        private var INSTANCE: AuthDatabase? = null

        fun getDatabase(context: Context): AuthDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AuthDatabase::class.java,
                    "zenlock_auth_db"
                )
                    .fallbackToDestructiveMigration()
                    .build()

                INSTANCE = instance
                instance
            }
        }
    }
}