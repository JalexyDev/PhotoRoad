package com.jalexy.photoroad.room

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.sqlite.db.SupportSQLiteDatabase
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch

@Database(entities = arrayOf(Photo::class), version = 1)
abstract class PhotoRoomDatabase : RoomDatabase() {

    abstract fun photoDao(): PhotoDao

    companion object {
        @Volatile
        private var INSTANCE: PhotoRoomDatabase? = null

        fun getDatabase(context: Context, scope: CoroutineScope): PhotoRoomDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    PhotoRoomDatabase::class.java,
                    "photo_database"
                )
                    .build()

                INSTANCE = instance
                return instance
            }
        }
    }
}