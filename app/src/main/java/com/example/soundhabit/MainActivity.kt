package com.example.soundhabit

import android.content.Intent
import android.os.Bundle
import android.provider.Settings
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.soundhabit.utils.AppInfoUtil


class MainActivity : AppCompatActivity() {

    companion object {
        const val TAG = "MainActivity"
    }

    private var appAdapter: InstalledAppAdapter? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        // retrieve installed applications
        AppInfoUtil.getInstalledApps(this)?.run {
            forEach { StorageUtil.savePackage(it.name) }
            appAdapter = InstalledAppAdapter(this)
        }

        initInstalledAppsList()
    }

    override fun onStart() {
        super.onStart()
        if (AppInfoUtil.needUsageStatsPermission(this)) {
            startActivity(Intent(Settings.ACTION_USAGE_ACCESS_SETTINGS))
        } else {
            startService()
        }
    }

    private fun initInstalledAppsList() {
        val appsRv = findViewById<RecyclerView>(R.id.rv_installed_apps).apply {
            setHasFixedSize(true)
            layoutManager = LinearLayoutManager(context, RecyclerView.VERTICAL, false)
            adapter = appAdapter
        }
    }

    private fun startService() {
        Intent(this@MainActivity, ScanForegroundService::class.java).also { intent ->
            ContextCompat.startForegroundService(this@MainActivity, intent)
        }
    }
}
