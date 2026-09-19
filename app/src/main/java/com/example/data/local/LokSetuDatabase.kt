package com.example.data.local

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import com.example.data.model.ContactHistoryEntity
import com.example.data.model.ProviderEntity

@Database(
    entities = [ProviderEntity::class, ContactHistoryEntity::class],
    version = 2,
    exportSchema = false
)
abstract class LokSetuDatabase : RoomDatabase() {

    abstract fun providerDao(): ProviderDao

    companion object {
        @Volatile
        private var INSTANCE: LokSetuDatabase? = null

        fun getInstance(context: Context): LokSetuDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    LokSetuDatabase::class.java,
                    "loksetu_db"
                )
                    .fallbackToDestructiveMigration()
                    .build()
                INSTANCE = instance
                instance
            }
        }
    }
}
