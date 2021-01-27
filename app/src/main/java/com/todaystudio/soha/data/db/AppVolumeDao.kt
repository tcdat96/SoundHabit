package com.todaystudio.soha.data.db

import androidx.lifecycle.LiveData
import androidx.room.*
import com.todaystudio.soha.data.entity.AppVolume

@Dao
interface AppVolumeDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insert(appVolume: AppVolume)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insert(appVolumes: List<AppVolume>)

    @Query("UPDATE APP_VOLUME SET enabled=:enabled WHERE pkg_name=:pkgName")
    fun update(pkgName: String, enabled: Boolean)

    @Query("SELECT * FROM APP_VOLUME")
    fun loadAll(): LiveData<List<AppVolume>>

    @Query("SELECT * FROM APP_VOLUME WHERE pkg_name=:pkgName")
    fun load(pkgName: String): AppVolume?
}