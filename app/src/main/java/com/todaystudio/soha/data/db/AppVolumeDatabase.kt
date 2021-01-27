package com.todaystudio.soha.data.db

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import com.todaystudio.soha.data.entity.AppVolume

@Database(entities = [AppVolume::class], version = 1)
abstract class AppVolumeDatabase: RoomDatabase() {
    abstract val appVolumeDao: AppVolumeDao

    companion object {
        @Volatile
        private var INSTANCE: AppVolumeDatabase? = null

        fun getInstance(context: Context): AppVolumeDatabase =
            INSTANCE ?: synchronized(this) {
                INSTANCE ?: Room.databaseBuilder(
                    context.applicationContext,
                    AppVolumeDatabase::class.java,
                    "appVolumes.db"
                ).build()
                    .also { INSTANCE = it }
            }
    }
}