package com.todaystudio.soha.model

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.todaystudio.soha.data.AppVolume
import kotlinx.coroutines.launch

class AppVolumeViewModel : ViewModel() {
    private val apps: MutableLiveData<List<AppVolume>> by lazy {
        MutableLiveData<List<AppVolume>>().also {
            loadApps()
        }
    }

    fun getApps() : LiveData<List<AppVolume>> {
        return apps
    }

    fun saveApps(newApps: MutableList<AppVolume>) = viewModelScope.launch {
        // save new package or get existing sound profile
        val iterator = newApps.listIterator()
        while (iterator.hasNext()) {
            val app = iterator.next()
            StorageUtil.savePackage(app.packageName)?.run {
                app.enabled = enabled
                app.values.addAll(volumes)
                iterator.set(app)
            }
        }
        apps.value = newApps
    }

    private fun loadApps() {

    }
}