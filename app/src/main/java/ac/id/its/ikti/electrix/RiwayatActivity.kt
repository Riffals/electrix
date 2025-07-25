package ac.id.its.ikti.electrix

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import com.android.volley.Request
import com.android.volley.toolbox.JsonArrayRequest
import com.android.volley.toolbox.Volley
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.google.android.material.tabs.TabLayout
import org.json.JSONObject
import java.text.DecimalFormat
import java.text.DecimalFormatSymbols
import java.text.SimpleDateFormat
import java.util.*

class RiwayatActivity : AppCompatActivity() {

    private var isAsc = true
    private lateinit var layoutBulanan: LinearLayout

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // ✅ Cek session login
        val sessionManager = SessionManager(this)
        if (!sessionManager.isLoggedIn()) {
            startActivity(Intent(this, LoginActivity::class.java))
            finish()
            return
        }

        setContentView(R.layout.activity_riwayat)

        val bottomNavBar: BottomNavigationView = findViewById(R.id.bottom_navigation)
        setupBottomNavBar(bottomNavBar, R.id.nav_riwayat)

        val tabLayout = findViewById<TabLayout>(R.id.riwayat_tabLayout)
        tabLayout.addTab(tabLayout.newTab().setText("Bulanan"))
//        tabLayout.addTab(tabLayout.newTab().setText("Tahunan"))

        layoutBulanan = findViewById(R.id.layout_bulanan)
        val layoutTahunan = findViewById<LinearLayout>(R.id.layout_tahunan)

        layoutBulanan.visibility = View.VISIBLE
        layoutTahunan.visibility = View.GONE

        tabLayout.addOnTabSelectedListener(object : TabLayout.OnTabSelectedListener {
            override fun onTabSelected(tab: TabLayout.Tab?) {
                layoutBulanan.visibility = if (tab?.position == 0) View.VISIBLE else View.GONE
                layoutTahunan.visibility = if (tab?.position == 1) View.VISIBLE else View.GONE
            }

            override fun onTabUnselected(tab: TabLayout.Tab?) {}
            override fun onTabReselected(tab: TabLayout.Tab?) {}
        })

        val sortIcon = findViewById<ImageView>(R.id.riwayat_icon_sort)
        sortIcon.setOnClickListener {
            isAsc = !isAsc
            sortIcon.setImageResource(if (isAsc) R.drawable.arrow_down else R.drawable.arrow_up)
            loadRiwayatData()
        }

        loadRiwayatData()
    }

    private fun loadRiwayatData() {
        val url = "https://picow-fix-v1.riffals.com/get_invoices.php"

        val request = JsonArrayRequest(Request.Method.GET, url, null, { response ->
            layoutBulanan.removeAllViews()

            val invoices = mutableListOf<JSONObject>()
            for (i in 0 until response.length()) {
                invoices.add(response.getJSONObject(i))
            }

            if (!isAsc) invoices.reverse()

            // Formatter angka lokal Indonesia
            val symbols = DecimalFormatSymbols(Locale("in", "ID")).apply {
                decimalSeparator = ','
                groupingSeparator = '.'
            }
            val formatter = DecimalFormat("#,##0.0#", symbols)

            for (item in invoices) {
                val card = layoutInflater.inflate(R.layout.item_riwayat_card, null)

                val tanggalText = card.findViewById<TextView>(R.id.textTanggal)
                val tipeDayaText = card.findViewById<TextView>(R.id.textTipeDaya)
                val konsumsiText = card.findViewById<TextView>(R.id.textKonsumsi)
                val tagihanText = card.findViewById<TextView>(R.id.textTagihan)

                // Format tanggal
                val inputFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
                val outputFormat = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())
                val tglFormatted = outputFormat.format(
                    inputFormat.parse(item.getString("billing_period")) ?: Date()
                )

                // Ambil dan format angka
                val energy = item.getDouble("total_energy") / 1000 // ➗ 1000 dari Wh ke kWh
                val bill = item.getDouble("total_bill")

                tanggalText.text = tglFormatted
                tipeDayaText.text = item.getString("class_name")
                konsumsiText.text = "${formatter.format(energy)} kWh"
                tagihanText.text = "Rp ${formatter.format(bill)}"

                layoutBulanan.addView(card)
            }

        }, { error ->
            Toast.makeText(this, "Gagal ambil data: ${error.message}", Toast.LENGTH_LONG).show()
        })

        Volley.newRequestQueue(this).add(request)
    }
}
