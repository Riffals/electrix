package ac.id.its.ikti.electrix

import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.view.View
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import com.android.volley.Request
import com.android.volley.toolbox.*
import com.google.android.material.bottomnavigation.BottomNavigationView
import org.json.JSONObject

class BerandaActivity : AppCompatActivity() {

    private var energyKwh = 0.0
    private lateinit var spinnerHargaListrik: Spinner
    private lateinit var hargaTarifList: List<Double>
    private var isInitialSelection = true

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val sessionManager = SessionManager(this)
        if (!sessionManager.isLoggedIn()) {
            startActivity(Intent(this, LoginActivity::class.java))
            finish()
            return
        }

        setContentView(R.layout.activity_beranda)

        val tvWelcome: TextView = findViewById(R.id.beranda_toolbar_title)
        val userName = sessionManager.getUsername()
        tvWelcome.text = "Welcome, ${userName ?: "User"}!"

        spinnerHargaListrik = findViewById(R.id.beranda_spinner_harga_listrik)

        val layoutHargaResult: LinearLayout = findViewById(R.id.beranda_layout_harga_result)
        val tvHargaValue: TextView = findViewById(R.id.beranda_tv_harga_value)
        val tvHintHarga: TextView = findViewById(R.id.beranda_tv_hint_harga)

        val tvVoltage: TextView = findViewById(R.id.tv_voltage)
        val tvCurrent: TextView = findViewById(R.id.tv_current)
        val tvPower: TextView = findViewById(R.id.tv_power)
        val tvFrequency: TextView = findViewById(R.id.tv_frequency)
        val tvEnergy: TextView = findViewById(R.id.tv_energy)
        val tvPowerFactor: TextView = findViewById(R.id.tv_power_factor)
        val tvBatt: TextView = findViewById(R.id.tv_batt)

        hargaTarifList = listOf(0.0)

        fun updateHarga() {
            val hargaPos = spinnerHargaListrik.selectedItemPosition
            val hargaPerKwh = hargaTarifList.getOrNull(hargaPos) ?: 0.0

            if (hargaPerKwh > 0) {
                val totalHarga = energyKwh * hargaPerKwh
                tvHargaValue.text = "Rp " + String.format("%,.0f", totalHarga)
                layoutHargaResult.visibility = View.VISIBLE
                tvHintHarga.visibility = View.GONE
            } else {
                layoutHargaResult.visibility = View.GONE
                tvHintHarga.visibility = View.VISIBLE
            }
        }

        spinnerHargaListrik.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>, view: View?, position: Int, id: Long) {
                updateHarga()
                if (!isInitialSelection) {
                    updateKonfigurasiKeServer(position)
                }
                isInitialSelection = false
            }

            override fun onNothingSelected(parent: AdapterView<*>) {}
        }

        loadHargaListrikFromAPI()
        getKonfigurasiAktif()

        val bellIcon: ImageView = findViewById(R.id.beranda_notification_icon)
        bellIcon.setOnClickListener {
            startActivity(Intent(this, NotificationActivity::class.java))
        }

        val bottomNavBar: BottomNavigationView = findViewById(R.id.bottom_navigation)
        setupBottomNavBar(bottomNavBar, R.id.nav_beranda)

        val url = "https://picow-fix-v1.riffals.com/getdata.php"
        val requestQueue = Volley.newRequestQueue(this)

        val handler = Handler()
        val runnable = object : Runnable {
            override fun run() {
                val jsonArrayRequest = JsonArrayRequest(
                    Request.Method.GET, url, null,
                    { response ->
                        try {
                            if (response.length() > 0) {
                                val latestData = response.getJSONObject(0)
                                val voltage = latestData.getDouble("voltage")
                                val current = latestData.getDouble("current")
                                val power = latestData.getDouble("power")
                                val frequency = latestData.getDouble("frequency")
                                val energyWh = latestData.getDouble("energy")
                                val powerFactor = latestData.getDouble("power_factor")
                                val batt = latestData.getDouble("batt")

                                energyKwh = energyWh / 1000

                                tvVoltage.text = String.format("%.1f V", voltage)
                                tvCurrent.text = "$current A"
                                tvPower.text = String.format("%.1f W", power)
                                tvFrequency.text = String.format("%.1f Hz", frequency)
                                tvEnergy.text = String.format("%.1f kWh", energyKwh)
                                tvPowerFactor.text = "$powerFactor"
                                tvBatt.text = "$batt V"

                                updateHarga()
                            }
                        } catch (e: Exception) {
                            Toast.makeText(this@BerandaActivity, "Parsing error: ${e.message}", Toast.LENGTH_SHORT).show()
                        }
                    },
                    { error ->
                        Toast.makeText(this@BerandaActivity, "Gagal ambil data: ${error.message}", Toast.LENGTH_SHORT).show()
                    }
                )
                requestQueue.add(jsonArrayRequest)
                handler.postDelayed(this, 5000)
            }
        }
        handler.post(runnable)
    }

    private fun getKonfigurasiAktif() {
        val url = "https://picow-fix-v1.riffals.com/get_device_class.php"
        val requestQueue = Volley.newRequestQueue(this)

        val request = JsonObjectRequest(Request.Method.GET, url, null,
            { response ->
                try {
                    val selectedClassId = response.getInt("electricity_class_id")
                    spinnerHargaListrik.setSelection(selectedClassId)
                } catch (e: Exception) {
                    Toast.makeText(this, "Gagal parsing konfigurasi aktif", Toast.LENGTH_SHORT).show()
                }
            },
            { error ->
                Toast.makeText(this, "Gagal ambil konfigurasi aktif", Toast.LENGTH_SHORT).show()
            }
        )

        requestQueue.add(request)
    }

    private fun updateKonfigurasiKeServer(classId: Int) {
        val url = "https://picow-fix-v1.riffals.com/update_device_class.php"
        val requestQueue = Volley.newRequestQueue(this)

        val jsonBody = JSONObject()
        jsonBody.put("electricity_class_id", classId)

        val request = JsonObjectRequest(Request.Method.POST, url, jsonBody,
            { response ->
                val status = response.optString("status")
                if (status == "success") {
                    Toast.makeText(this, "Konfigurasi diperbarui", Toast.LENGTH_SHORT).show()
                } else {
                    Toast.makeText(this, "Gagal memperbarui konfigurasi", Toast.LENGTH_SHORT).show()
                }
            },
            { error ->
                Toast.makeText(this, "Error simpan konfigurasi: ${error.message}", Toast.LENGTH_SHORT).show()
            }
        )

        requestQueue.add(request)
    }

    private fun loadHargaListrikFromAPI() {
        val url = "https://picow-fix-v1.riffals.com/get_electricity_classes.php"
        val requestQueue = Volley.newRequestQueue(this)

        val spinnerItems = mutableListOf("Pilih Golongan Listrik")
        val tarifItems = mutableListOf(0.0)

        val jsonArrayRequest = JsonArrayRequest(
            Request.Method.GET, url, null,
            { response ->
                try {
                    for (i in 0 until response.length()) {
                        val obj = response.getJSONObject(i)
                        val className = obj.getString("class_name")
                        val baseTariff = obj.getDouble("base_tariff")

                        spinnerItems.add("$className - Rp${String.format("%,.2f", baseTariff)} / kWh")
                        tarifItems.add(baseTariff)
                    }

                    val adapter = ArrayAdapter(this, android.R.layout.simple_spinner_item, spinnerItems)
                    adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
                    spinnerHargaListrik.adapter = adapter
                    hargaTarifList = tarifItems

                } catch (e: Exception) {
                    Toast.makeText(this, "Parsing error: ${e.message}", Toast.LENGTH_SHORT).show()
                }
            },
            { error ->
                Toast.makeText(this, "Gagal ambil data harga listrik: ${error.message}", Toast.LENGTH_SHORT).show()
            }
        )

        requestQueue.add(jsonArrayRequest)
    }
}
