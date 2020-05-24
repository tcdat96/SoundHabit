package com.example.soundhabit

import AudioUtil
import NotificationUtil
import StorageUtil
import android.app.Service
import android.content.Intent
import android.os.*
import android.util.Log
import com.rvalerio.fgchecker.AppChecker

class ScanForegroundService : Service() {
    companion object {
        const val TAG = "ScanForegroundService"
        const val SCAN_INTERVAL = 3000
        const val CHANNEL_ID = "SCAN_FRGR_CHANNEL_ID"
    }

    private var serviceLooper: Looper? = null
    private var serviceHandler: ServiceHandler? = null

    private inner class ServiceHandler(looper: Looper) : Handler(looper) {
        private var prevPackage = ""

        override fun handleMessage(msg: Message) {
            val context = this@ScanForegroundService
            AppChecker()
                .whenAny { packageName ->
                    if (prevPackage != packageName) {
                        // save current volume for previous app
                        val currVolume = AudioUtil.getCurrentVolume(context)
                        StorageUtil.saveCurrentVolume(prevPackage, currVolume)
                        Log.d(TAG, "$prevPackage: saved as $currVolume");
                        // load previously saved volume of current app
                        StorageUtil.getPreviousVolume(packageName)?.let {
                            if (it != currVolume) {
                                AudioUtil.setCurrentVolume(context, it)
                                Log.d(TAG, "$packageName: set to $it");
                            }
                        }

                        prevPackage = packageName
                    }
                }
                .timeout(SCAN_INTERVAL)
                .start(context)
        }
    }

    override fun onCreate() {
        super.onCreate()

        StorageUtil.init(this)

        HandlerThread("ServiceStartArguments", Process.THREAD_PRIORITY_BACKGROUND).apply {
            start()
            serviceLooper = looper
            serviceHandler = ServiceHandler(looper)
        }
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        showNotification()
        serviceHandler?.obtainMessage()?.also { msg ->
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
        Log.d(TAG, "Service is destroyed");
    }
}