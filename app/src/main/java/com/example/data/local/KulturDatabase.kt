package com.example.data.local

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase

@Database(
    entities = [CartEntity::class, WishlistEntity::class, UserSessionEntity::class],
    version = 1,
    exportSchema = false
)
abstract class KulturDatabase : RoomDatabase() {
    abstract fun kulturDao(): KulturDao

    companion object {
        @Volatile
        private var INSTANCE: KulturDatabase? = null

        fun getDatabase(context: Context): KulturDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    KulturDatabase::class.java,
                    "kultur_database"
                )
                .fallbackToDestructiveMigration()
                .build()
                INSTANCE = instance
                instance
            }
        }
    }
}
