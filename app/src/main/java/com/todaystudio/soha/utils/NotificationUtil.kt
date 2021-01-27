import android.app.*
import android.content.Context
import android.content.Intent
import android.os.Build
import android.provider.Settings
import androidx.core.app.NotificationCompat
import com.todaystudio.soha.R

object NotificationUtil {
    fun getNotification(context: Context, channelId: String): Notification {
        createNotificationChannel(context, channelId)

        // prompt user to hide the notification
        val intent = Intent().apply {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                action = Settings.ACTION_APP_NOTIFICATION_SETTINGS
                putExtra(Settings.EXTRA_APP_PACKAGE, context.packageName)
            } else {
                action = "android.settings.APP_NOTIFICATION_SETTINGS"
                putExtra("app_package", context.packageName)
                putExtra("app_uid", context.applicationInfo.uid)
            }
        }
        val pendingIntent = PendingIntent.getActivity(context, 0, intent, 0)

        return NotificationCompat.Builder(context, channelId)
            .setContentTitle(context.getText(R.string.notification_title))
            .setContentText(context.getText(R.string.notification_message))
            .setSmallIcon(R.drawable.ic_launcher_foreground)
            .setContentIntent(pendingIntent)
            .build()
    }

    private fun createNotificationChannel(context: Context, channelId: String) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            // Create the NotificationChannel
            val mChannel = NotificationChannel(
                channelId,
                context.getString(R.string.channel_name),
                NotificationManager.IMPORTANCE_DEFAULT
            )
//            mChannel.description = getString(R.string.channel_description)
            // Register the channel with the system; you can't change the importance
            // or other notification behaviors after this
            val notificationManager =
                context.getSystemService(Service.NOTIFICATION_SERVICE) as NotificationManager
            notificationManager.createNotificationChannel(mChannel)
        }
    }
}