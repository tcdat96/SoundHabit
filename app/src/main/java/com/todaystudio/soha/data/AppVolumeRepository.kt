package com.todaystudio.soha.data

import androidx.lifecycle.LiveData
import androidx.lifecycle.liveData
import com.todaystudio.soha.data.db.AppVolumeDao
import com.todaystudio.soha.data.entity.AppVolume
import com.todaystudio.soha.data.util.AppVolumeService

class AppVolumeRepository private constructor(private val appVolumeDao: AppVolumeDao) {

    private var appVolumeService: AppVolumeService? = null

    companion object {
        @Volatile
        private var INSTANCE: AppVolumeRepository? = null

        fun getInstance(appVolumeDao: AppVolumeDao): AppVolumeRepository =
            INSTANCE ?: synchronized(this) {
                INSTANCE ?: AppVolumeRepository(appVolumeDao)
            }
    }

    fun init(appVolumeService: AppVolumeService) {
        this.appVolumeService = appVolumeService
    }

    fun getAllVolumes(): LiveData<List<AppVolume>> = liveData {
        emitSource(appVolumeDao.loadAll())
        refreshAppVolumeList()
    }

    private suspend fun refreshAppVolumeList() {
        appVolumeService?.getInstalledApps()?.let { installs ->
            appVolumeDao.loadAll().value?.map { it.packageName }?.toSet()?.let { existed ->
                val newApps = installs.filter { install -> install.packageName !in existed}
                appVolumeDao.insert(newApps)
            }
        }
    }

    suspend fun getVolume(pkgName: String): AppVolume? {
        return appVolumeDao.load(pkgName)
    }

    suspend fun save(pkgName: String, enabled: Boolean) {
        getVolume(pkgName)?.run {
            this.enabled = enabled
            save(this)
        }
    }

    suspend fun save(volume: AppVolume) {
        appVolumeDao.insert(volume)
    }

    suspend fun save(volume: List<AppVolume>) {
        appVolumeDao.insert(volume)
    }
}
