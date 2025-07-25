package ac.id.its.ikti.electrix

import android.app.*
import android.content.Intent
import android.os.*
import androidx.core.app.NotificationCompat
import org.json.JSONObject
import java.net.HttpURLConnection
import java.net.URL
import java.util.*

class AnomalyPollingService : Service() {

    private val handler = Handler(Looper.getMainLooper())
    private var lastAnomalyTimestamp: String? = null

    private val pollingRunnable = object : Runnable {
        override fun run() {
            checkAnomalies()
            handler.postDelayed(this, 30_000) // Interval pengecekan (boleh ubah jadi 10_000 kalau ingin lebih cepat)
        }
    }

    override fun onCreate() {
        super.onCreate()
        startForegroundServiceNotification()
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        handler.post(pollingRunnable)
        return START_STICKY
    }

    override fun onDestroy() {
        handler.removeCallbacks(pollingRunnable)
        super.onDestroy()
    }

    override fun onBind(intent: Intent?): IBinder? = null

    private fun checkAnomalies() {
        Thread {
            try {
                val url = URL("https://picow-fix-v1.riffals.com/api/get_anomalies.php")
                val connection = url.openConnection() as HttpURLConnection
                connection.requestMethod = "GET"

                if (connection.responseCode == 200) {
                    val response = connection.inputStream.bufferedReader().readText()
                    val json = JSONObject(response)

                    val timestamp = json.getString("timestamp_used")
                    val anomalies = json.getJSONArray("anomalies_detected")

                    if (anomalies.length() > 0 && timestamp != lastAnomalyTimestamp) {
                        lastAnomalyTimestamp = timestamp

                        val deskripsi = (0 until anomalies.length()).joinToString(", ") {
                            when (anomalies.getInt(it)) {
                                1 -> "Tegangan Rendah"
                                2 -> "Tegangan Tinggi"
                                3 -> "Frekuensi Rendah"
                                4 -> "Frekuensi Tinggi"
                                5 -> "Faktor Daya Buruk"
                                6 -> "Pemadaman Listrik"
                                else -> "Anomali Tidak Dikenal"
                            }
                        }

                        val intent = Intent(this, BerandaActivity::class.java)
                        val pendingIntent = PendingIntent.getActivity(
                            this,
                            0,
                            intent,
                            PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
                        )

                        val channelId = "anomaly_alert_channel"
                        val manager = getSystemService(NOTIFICATION_SERVICE) as NotificationManager

                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                            val channel = NotificationChannel(
                                channelId,
                                "Anomaly Alerts",
                                NotificationManager.IMPORTANCE_HIGH
                            )
                            manager.createNotificationChannel(channel)
                        }

                        val builder = NotificationCompat.Builder(this, channelId)
                            .setSmallIcon(R.drawable.ic_notification)
                            .setContentTitle("Peringatan Listrik")
                            .setContentText("Terdeteksi: $deskripsi pada $timestamp")
                            .setPriority(NotificationCompat.PRIORITY_HIGH)
                            .setContentIntent(pendingIntent)
                            .setAutoCancel(true)

                        manager.notify(Random().nextInt(), builder.build())
                    }
                }

                connection.disconnect()

            } catch (e: Exception) {
                e.printStackTrace()
            }
        }.start()
    }

    private fun startForegroundServiceNotification() {
        val channelId = "polling_service_channel"
        val channelName = "Anomaly Foreground Service"

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val chan = NotificationChannel(
                channelId,
                channelName,
                NotificationManager.IMPORTANCE_LOW
            )
            val manager = getSystemService(NotificationManager::class.java)
            manager.createNotificationChannel(chan)
        }

        val notification = NotificationCompat.Builder(this, channelId)
            .setContentTitle("Electrix Monitoring Aktif")
            .setContentText("Sedang memantau anomali listrik setiap 30 detik")
            .setSmallIcon(R.drawable.ic_notification)
            .build()

        startForeground(1, notification)
    }
}
