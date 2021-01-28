package com.todaystudio.soha.data.util

import com.todaystudio.soha.data.entity.AppVolume

interface AppVolumeService {
    fun getInstalledApps() : List<AppVolume>?
}