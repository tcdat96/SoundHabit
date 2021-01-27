package com.todaystudio.soha.services

import AudioUtil
import AudioUtil.SoundMode
import NotificationUtil
import android.app.Service
import android.content.Intent
import android.os.*
import android.util.Log
import com.rvalerio.fgchecker.AppChecker
import com.todaystudio.soha.data.AppVolumeRepository
import com.todaystudio.soha.data.db.AppVolumeDatabase
import kotlinx.serialization.UnstableDefault

class ScanForegroundService() : Service() {
    companion object {
        const val TAG = "ScanForegroundService"
        const val SCAN_INTERVAL = 5000
        const val CHANNEL_ID = "SCAN_FRGR_CHANNEL_ID"
    }

    private var serviceLooper: Looper? = null
    private var serviceHandler: ServiceHandler? = null

    private lateinit var repository: AppVolumeRepository

    override fun onCreate() {
        super.onCreate()

        HandlerThread("ServiceStartArguments", Process.THREAD_PRIORITY_BACKGROUND).apply {
            start()
            serviceLooper = looper
            serviceHandler = ServiceHandler(looper)
        }

        val database = AppVolumeDatabase.getInstance(applicationContext)
        repository = AppVolumeRepository.getInstance(database.appVolumeDao)

        showNotification()
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        serviceHandler?.obtainMessage()?.let { msg ->
            msg.arg1 = startId
            serviceHandler?.sendMessage(msg)
        }
        return START_STICKY
    }

    private fun showNotification() {
        val notification = NotificationUtil.getNotification(this, CHANNEL_ID)
        startForeground(1, notification)
    }

    override fun onBind(intent: Intent?): IBinder? {
        return null
    }

    override fun onDestroy() {
        super.onDestroy()
        Log.d(TAG, "Service is destroyed")
    }

    private inner class ServiceHandler(looper: Looper) : Handler(looper) {
        private lateinit var appChecker: AppChecker
        private var prevPackage = ""

        @UnstableDefault
        override fun handleMessage(msg: Message) {
            appChecker = AppChecker().apply {
                whenAny { handlePackageChange(it) }
                timeout(SCAN_INTERVAL)
                start(this@ScanForegroundService)
            }
        }

        @UnstableDefault
        fun handlePackageChange(packageName: String?) {
            val context = this@ScanForegroundService

            if (packageName == null) {
                appChecker.stop()
                stopSelf()
                return
            }

            if (prevPackage != packageName) {
                val currVolume = AudioUtil.getCurrentVolume(context)
                val soundMode = AudioUtil.getSoundMode(context)

                // save current volume for previous app
                repository.getVolume(prevPackage)?.run {
                    when (soundMode) {
                        SoundMode.SPEAKER -> speakerVolume = currVolume
                        SoundMode.WIRED -> wiredVolume = currVolume
                        SoundMode.BLUETOOTH -> bleVolume = currVolume
                    }
                    repository.save(this)
                }

                // load previously saved volume of current app
                repository.getVolume(packageName)?.run {
                    val volume = when (soundMode) {
                        SoundMode.SPEAKER -> speakerVolume
                        SoundMode.WIRED -> wiredVolume
                        SoundMode.BLUETOOTH -> bleVolume
                    }
                    if (volume != currVolume) {
                        AudioUtil.setCurrentVolume(context, volume)
                        Log.d(TAG, "$packageName: set to $volume")
                    }
                }

                prevPackage = packageName
            }
        }
    }
}