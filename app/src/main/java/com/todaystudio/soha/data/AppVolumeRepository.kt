package com.todaystudio.soha.data

import androidx.lifecycle.LiveData
import com.todaystudio.soha.data.db.AppVolumeDao
import com.todaystudio.soha.data.util.AppVolumeService
import com.todaystudio.soha.data.entity.AppVolume
import kotlinx.serialization.Serializable
import java.util.concurrent.Executor

class AppVolumeRepository private constructor(private val appVolumeDao: AppVolumeDao) {

    private var executor: Executor? = null
    private var appVolumeService: AppVolumeService? = null

    companion object {
        @Volatile
        private var INSTANCE: AppVolumeRepository? = null

        fun getInstance(appVolumeDao: AppVolumeDao): AppVolumeRepository =
            INSTANCE ?: synchronized(this) {
                INSTANCE ?: AppVolumeRepository(appVolumeDao)
            }
    }

    fun init(executor: Executor, appVolumeService: AppVolumeService) {
        this.executor = executor
        this.appVolumeService = appVolumeService
    }

    fun getAllVolumes(): LiveData<List<AppVolume>> {
        refreshAppVolumeList()
        return appVolumeDao.loadAll()
    }

    private fun refreshAppVolumeList() {
        executor?.execute {
            appVolumeService?.getInstalledApps()?.run {
                appVolumeDao.insert(this)
            }
        }
    }

    fun getVolume(pkgName: String): AppVolume? {
        return appVolumeDao.load(pkgName)
    }

    fun save(pkgName: String, enabled: Boolean) {
        getVolume(pkgName)?.run {
            this.enabled = enabled
            save(this)
        }
    }

    fun save(volume: AppVolume) {
        appVolumeDao.insert(volume)
    }

    fun save(volume: List<AppVolume>) {
        appVolumeDao.insert(volume)
    }
}
