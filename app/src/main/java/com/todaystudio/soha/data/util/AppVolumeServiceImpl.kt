package com.todaystudio.soha.data.util

import android.annotation.TargetApi
import android.app.AppOpsManager
import android.content.Context
import android.content.pm.ApplicationInfo
import android.content.pm.PackageManager
import android.os.Build
import android.os.Process
import com.todaystudio.soha.data.entity.AppVolume

class AppVolumeServiceImpl(private val context: Context) : AppVolumeService {
    override fun getInstalledApps(): List<AppVolume>? {
        val pm = context.packageManager
        return pm?.getInstalledApplications(PackageManager.GET_META_DATA)?.run {
            filter { !isSystemPackage(pm, it) }.map { app ->
                val appName = pm.getApplicationLabel(app) as String
                AppVolume(app.packageName).apply {
                    name = appName
                    icon = app.loadIcon(pm)
                }
            }.sortedBy { it.name }
        }
    }

    private fun isSystemPackage(pm: PackageManager, pkgInfo: ApplicationInfo): Boolean {
        return pm.getLaunchIntentForPackage(pkgInfo.packageName) == null
    }

    fun needUsageStatsPermission(context: Context): Boolean {
        return Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP
                && !hasUsageStatsPermission(context)
    }

    @TargetApi(Build.VERSION_CODES.KITKAT)
    private fun hasUsageStatsPermission(context: Context): Boolean {
        val appOps = context.getSystemService(Context.APP_OPS_SERVICE) as AppOpsManager
        val mode = appOps.checkOpNoThrow(
            "android:get_usage_stats",
            Process.myUid(), context.packageName
        )
        return mode == AppOpsManager.MODE_ALLOWED
    }
}