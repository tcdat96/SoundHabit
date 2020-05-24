package com.example.soundhabit.utils

import android.annotation.TargetApi
import android.app.AppOpsManager
import android.content.Context
import android.content.pm.ApplicationInfo
import android.content.pm.PackageManager
import android.os.Build
import android.os.Process
import com.example.soundhabit.data.AppInfo

object AppInfoUtil {
    fun getInstalledApps(context: Context): List<AppInfo>? {
        val pm = context.packageManager
        return pm
            ?.getInstalledApplications(PackageManager.GET_META_DATA)
            ?.filter { !isSystemPackage(pm, it) }
            ?.map { AppInfo(it.packageName, it.loadIcon(pm)) }
    }

    private fun isSystemPackage(pm: PackageManager, pkgInfo: ApplicationInfo): Boolean {
        return pm.getLaunchIntentForPackage(pkgInfo.packageName) == null
//        return pkgInfo.flags and (ApplicationInfo.FLAG_SYSTEM or ApplicationInfo.FLAG_UPDATED_SYSTEM_APP) != 0
    }

    fun needUsageStatsPermission(context: Context): Boolean {
        return Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP
                && !hasUsageStatsPermission(context)
    }

    @TargetApi(Build.VERSION_CODES.KITKAT)
    fun hasUsageStatsPermission(context: Context): Boolean {
        val appOps = context.getSystemService(Context.APP_OPS_SERVICE) as AppOpsManager
        val mode = appOps.checkOpNoThrow(
            "android:get_usage_stats",
            Process.myUid(), context.packageName
        )
        return mode == AppOpsManager.MODE_ALLOWED
    }
}