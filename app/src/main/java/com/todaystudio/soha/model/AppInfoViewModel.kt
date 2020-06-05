package com.todaystudio.soha.model

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.todaystudio.soha.data.AppInfo
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

class AppInfoViewModel : ViewModel() {
    private val apps: MutableLiveData<List<AppInfo>> by lazy {
        MutableLiveData<List<AppInfo>>().also {
            loadApps()
        }
    }

    fun getApps() : LiveData<List<AppInfo>> {
        return apps
    }

    fun saveApps(newApps: MutableList<AppInfo>) = viewModelScope.launch {
        // save new package or get existing sound profile
        val iterator = newApps.listIterator()
        while (iterator.hasNext()) {
            val app = iterator.next()
            StorageUtil.savePackage(app.packageName)?.run {
                app.enabled = enabled
                app.volumes.addAll(volumes)
                iterator.set(app)
            }
        }
        apps.value = newApps
    }

    private fun loadApps() {

    }
}