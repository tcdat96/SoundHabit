package com.todaystudio.soha.viewmodels

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.todaystudio.soha.data.entity.AppVolume
import com.todaystudio.soha.data.AppVolumeRepository

class AppVolumeViewModel(private val volRepository: AppVolumeRepository) : ViewModel() {
    val apps: LiveData<List<AppVolume>> = volRepository.getAllVolumes()

    val hasUsageAccess = MutableLiveData<Boolean>().apply { postValue(false) }

    fun saveVolume(pkgName: String, enabled: Boolean) {
        volRepository.save(pkgName, enabled)
    }

    fun saveVolume(volume: AppVolume) {
        volRepository.save(volume)
    }

    fun saveVolume(volumes: List<AppVolume>) {
        volRepository.save(volumes)
    }
}