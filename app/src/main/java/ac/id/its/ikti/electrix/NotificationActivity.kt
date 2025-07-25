package ac.id.its.ikti.electrix

import android.content.Intent
import android.graphics.Color
import android.graphics.Typeface
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import com.android.volley.Request
import com.android.volley.toolbox.JsonObjectRequest
import com.android.volley.toolbox.Volley
import com.google.android.material.tabs.TabLayout
import org.json.JSONArray
import org.json.JSONObject
import java.text.SimpleDateFormat
import java.util.*

class NotificationActivity : AppCompatActivity() {

    private var isAsc = true
    private lateinit var notifContainer: LinearLayout
    private val dateFormatter = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
    private val timeFormatter = SimpleDateFormat("HH:mm:ss", Locale.getDefault())
    private val fullTimestampFormatter = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault())

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Cek session
        val sessionManager = SessionManager(this)
        if (!sessionManager.isLoggedIn()) {
            startActivity(Intent(this, LoginActivity::class.java))
            finish()
            return
        }

        setContentView(R.layout.activity_notification)

        notifContainer = findViewById(R.id.linearLayout_notif_content)

        val backIcon: ImageView = findViewById(R.id.notif_toolbar_back_icon)
        backIcon.setOnClickListener { finish() }

        val tabLayout = findViewById<TabLayout>(R.id.notif_tabLayout)
        tabLayout.addTab(tabLayout.newTab().setText("Harian"))

        val sortIcon = findViewById<ImageView>(R.id.icon_sort)
        sortIcon.setOnClickListener {
            isAsc = !isAsc
            sortIcon.setImageResource(if (isAsc) R.drawable.arrow_down else R.drawable.arrow_up)
            fetchAnomalyData()
        }

        fetchAnomalyData()
    }

    private fun fetchAnomalyData() {
        val url = "https://picow-fix-v1.riffals.com/api/get_anomaly_daily.php"
        val requestQueue = Volley.newRequestQueue(this)

        val request = JsonObjectRequest(
            Request.Method.GET, url, null,
            { response ->
                notifContainer.removeAllViews()
                val groupedData = response.getJSONObject("data")
                val sortedKeys = groupedData.keys().asSequence().toList()
                    .sortedWith(if (isAsc) compareBy { it } else compareByDescending { it })

                for (dateKey in sortedKeys) {
                    val dateLabel = convertDate(dateKey)
                    val dateText = TextView(this).apply {
                        text = dateLabel
                        setTextColor(Color.parseColor("#2563EB"))
                        textSize = 14f
                        setPadding(0, 16, 0, 8)
                        setTypeface(null, Typeface.BOLD)
                    }
                    notifContainer.addView(dateText)

                    val items = groupedData.getJSONArray(dateKey)
                    val sortedItems = sortJSONArrayByTimestamp(items, isAsc)

                    for (item in sortedItems) {
                        val anomalyName = item.getString("anomaly_name")

                        val displayValue = when (anomalyName) {
                            "Tegangan Berlebih", "Tegangan Rendah" -> {
                                "${item.optDouble("voltage", 0.0).toInt()} V"
                            }
                            "Frekuensi Berlebih", "Frekuensi Rendah" -> {
                                "${String.format("%.2f", item.optDouble("frequency", 0.0))} Hz"
                            }
                            "Efisiensi Daya Buruk" -> {
                                String.format("%.2f", item.optDouble("power_factor", 0.0))
                            }
                            else -> "-"
                        }

                        val timestamp = item.getString("timestamp")

                        val timeOnly = timeFormatter.format(
                            fullTimestampFormatter.parse(timestamp) ?: Date()
                        )

                        val cardView = LayoutInflater.from(this)
                            .inflate(R.layout.item_notification_daily, notifContainer, false)

                        cardView.findViewById<TextView>(R.id.anomaly_title).text = anomalyName
                        cardView.findViewById<TextView>(R.id.anomaly_value).text = displayValue
                        cardView.findViewById<TextView>(R.id.anomaly_time).text = timeOnly

                        notifContainer.addView(cardView)
                    }
                }
            },
            { error ->
                error.printStackTrace()
            }
        )
        requestQueue.add(request)
    }

    private fun convertDate(dateStr: String): String {
        return try {
            val date = dateFormatter.parse(dateStr)
            SimpleDateFormat("d MMMM yyyy", Locale("id", "ID")).format(date!!)
        } catch (e: Exception) {
            dateStr
        }
    }

    private fun sortJSONArrayByTimestamp(array: JSONArray, ascending: Boolean = true): List<JSONObject> {
        val list = mutableListOf<JSONObject>()
        for (i in 0 until array.length()) {
            list.add(array.getJSONObject(i))
        }
        return list.sortedWith { a, b ->
            val timeA = fullTimestampFormatter.parse(a.getString("timestamp"))?.time ?: 0L
            val timeB = fullTimestampFormatter.parse(b.getString("timestamp"))?.time ?: 0L
            if (ascending) timeA.compareTo(timeB) else timeB.compareTo(timeA)
        }
    }

}
