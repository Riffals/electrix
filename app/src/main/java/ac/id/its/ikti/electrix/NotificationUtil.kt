package ac.id.its.ikti.electrix

import android.app.*
import android.content.Context
import android.os.Build
import androidx.core.app.NotificationCompat
import java.util.*

object NotificationUtil {
    fun showLocalNotification(context: Context, title: String, message: String) {
        val channelId = "anomaly_alert_channel"
        val manager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(channelId, "Anomaly Alerts", NotificationManager.IMPORTANCE_HIGH).apply {
                enableLights(true)
                enableVibration(true)
                description = "Notifikasi peringatan anomali listrik"
                lockscreenVisibility = Notification.VISIBILITY_PUBLIC
                setShowBadge(true)
            }
            manager.createNotificationChannel(channel)
        }

        val builder = NotificationCompat.Builder(context, channelId)
            .setSmallIcon(R.drawable.ic_notification) // pastikan ada dan valid
            .setContentTitle(title)
            .setContentText(message)
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setCategory(NotificationCompat.CATEGORY_ALARM)
            .setAutoCancel(true)

        manager.notify(Random().nextInt(), builder.build())
    }
}
