package com.todaystudio.soha.viewmodels

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.todaystudio.soha.data.AppVolumeRepository
import com.todaystudio.soha.data.entity.AppVolume
import kotlinx.coroutines.launch

class AppVolumeViewModel(private val volRepository: AppVolumeRepository) : ViewModel() {
    val apps: LiveData<List<AppVolume>> = volRepository.getAllVolumes()

    val hasUsageAccess = MutableLiveData<Boolean>().apply { postValue(false) }

    fun saveVolume(pkgName: String, enabled: Boolean) = viewModelScope.launch {
        volRepository.save(pkgName, enabled)
    }

    fun saveVolume(volume: AppVolume) = viewModelScope.launch{
        volRepository.save(volume)
    }

    fun saveVolume(volumes: List<AppVolume>) = viewModelScope.launch {
        volRepository.save(volumes)
    }
}