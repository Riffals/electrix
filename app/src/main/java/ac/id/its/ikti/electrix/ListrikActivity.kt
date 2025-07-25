package ac.id.its.ikti.electrix

import android.content.Intent
import android.graphics.Color
import android.os.Bundle
import android.view.View
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import com.android.volley.Request
import com.android.volley.toolbox.*
import com.github.mikephil.charting.charts.LineChart
import com.github.mikephil.charting.components.XAxis
import com.github.mikephil.charting.data.Entry
import com.github.mikephil.charting.data.LineData
import com.github.mikephil.charting.data.LineDataSet
import com.github.mikephil.charting.formatter.IndexAxisValueFormatter
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.google.android.material.tabs.TabLayout
import org.json.JSONObject
import java.text.NumberFormat
import java.text.SimpleDateFormat
import java.util.*

class ListrikActivity : AppCompatActivity() {

    private lateinit var chart: LineChart
    private lateinit var btnBulanan: LinearLayout
    private lateinit var iconBulanan: ImageView
    private lateinit var tabLayoutMode: TabLayout
    private lateinit var gridParameter: GridLayout
//    private lateinit var manualInputLayout: LinearLayout
//    private lateinit var inputTegangan: EditText
//    private lateinit var btnSimpanManual: Button
    private lateinit var energiTextView: TextView

    private lateinit var inputUnderVoltage: EditText
    private lateinit var inputOverVoltage: EditText
    private lateinit var inputUnderFreq: EditText
    private lateinit var inputOverFreq: EditText
    private lateinit var inputUnderPf: EditText
    private lateinit var btnEditThreshold: Button
    private lateinit var layoutBillingSettings: LinearLayout


    private var isEditing = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val sessionManager = SessionManager(this)
        if (!sessionManager.isLoggedIn()) {
            startActivity(Intent(this, LoginActivity::class.java))
            finish()
            return
        }

        setContentView(R.layout.activity_listrik)

        val bottomNavBar: BottomNavigationView = findViewById(R.id.bottom_navigation)
        setupBottomNavBar(bottomNavBar, R.id.nav_listrik)

        chart = findViewById(R.id.listrik_line_chart)
        btnBulanan = findViewById(R.id.listrik_btn_bulanan)
        iconBulanan = btnBulanan.getChildAt(0) as ImageView
        tabLayoutMode = findViewById(R.id.tab_layout_mode)
        gridParameter = findViewById(R.id.gridParameter)
//        manualInputLayout = findViewById(R.id.manual_input_layout)
//        inputTegangan = findViewById(R.id.input_tegangan)
//        btnSimpanManual = findViewById(R.id.btn_simpan_manual)
        energiTextView = findViewById(R.id.listrik_tv_konsumsi_value)

        inputUnderVoltage = findViewById(R.id.input_under_voltage)
        inputOverVoltage = findViewById(R.id.input_over_voltage)
        inputUnderFreq = findViewById(R.id.input_under_freq)
        inputOverFreq = findViewById(R.id.input_over_freq)
        inputUnderPf = findViewById(R.id.input_under_pf)
        btnEditThreshold = findViewById(R.id.btn_edit_threshold)

//        val layoutBillingSettings = findViewById<LinearLayout>(R.id.layout_billing_settings)
        layoutBillingSettings = findViewById(R.id.layout_billing_settings)
        val pickerStartDay = findViewById<NumberPicker>(R.id.picker_start_day)
        val pickerEndDay = findViewById<NumberPicker>(R.id.picker_end_day)

        pickerStartDay.minValue = 1
        pickerStartDay.maxValue = 31
        pickerEndDay.minValue = 1
        pickerEndDay.maxValue = 31

        // Default value (opsional)
        pickerStartDay.value = 24
        pickerEndDay.value = 23

        val btnSaveBillingSettings = findViewById<Button>(R.id.btn_save_billing_settings)

        var isBillingEditing = false

        btnSaveBillingSettings.setOnClickListener {
            isBillingEditing = !isBillingEditing

            if (isBillingEditing) {
                btnSaveBillingSettings.text = "SIMPAN"
                btnSaveBillingSettings.setBackgroundColor(Color.parseColor("#22C55E"))
            } else {
                btnSaveBillingSettings.text = "Edit"
                btnSaveBillingSettings.setBackgroundColor(Color.parseColor("#2563EB"))
                saveBillingRange(pickerStartDay.value, pickerEndDay.value)
            }

            pickerStartDay.isEnabled = isBillingEditing
            pickerEndDay.isEnabled = isBillingEditing
        }

        btnEditThreshold.setOnClickListener {
            isEditing = !isEditing
            btnEditThreshold.text = if (isEditing) "SIMPAN" else "Edit"
            btnEditThreshold.setBackgroundColor(Color.parseColor(if (isEditing) "#22C55E" else "#2563EB"))

            enableEditText(isEditing)
            setEditTextStyle(isEditing)

            pickerStartDay.isEnabled = isEditing
            pickerEndDay.isEnabled = isEditing

            if (!isEditing) {
                saveThresholdData()
                saveBillingRange(pickerStartDay.value, pickerEndDay.value)
            }
        }


        setupChart()
        setupTabs()
        setToggle(true)
        getThresholdData()

        btnBulanan.setOnClickListener { setToggle(true) }

//        btnSimpanManual.setOnClickListener {
//            val input = inputTegangan.text.toString()
//            if (input.isNotEmpty()) {
//                Toast.makeText(this, "Tegangan disimpan: $input V", Toast.LENGTH_SHORT).show()
//            } else {
//                Toast.makeText(this, "Mohon isi tegangan terlebih dahulu", Toast.LENGTH_SHORT).show()
//            }
//        }

        btnEditThreshold.setOnClickListener {
            if (!isEditing) {
                isEditing = true
                btnEditThreshold.text = "SIMPAN"
                btnEditThreshold.setBackgroundColor(Color.parseColor("#22C55E"))
                enableEditText(true)
            } else {
                isEditing = false
                btnEditThreshold.text = "Edit"
                btnEditThreshold.setBackgroundColor(Color.parseColor("#2563EB"))
                enableEditText(false)
                saveThresholdData()
            }
            enableEditText(isEditing)
            setEditTextStyle(isEditing)
        }
    }

    private fun getThresholdData() {
        val url = "https://picow-fix-v1.riffals.com/get_normal_values.php"
        val request = JsonObjectRequest(Request.Method.GET, url, null,
            { response ->
                inputUnderVoltage.setText(response.getString("under_voltage"))
                inputOverVoltage.setText(response.getString("over_voltage"))
                inputUnderFreq.setText(response.getString("under_frequency"))
                inputOverFreq.setText(response.getString("over_frequency"))
                inputUnderPf.setText(response.getString("under_pf"))
            },
            { error ->
                Toast.makeText(this, "Gagal ambil data batas: ${error.message}", Toast.LENGTH_SHORT).show()
            })
        Volley.newRequestQueue(this).add(request)
    }

    private fun saveThresholdData() {
        val url = "https://picow-fix-v1.riffals.com/insert_new_normal_values.php"
        val postParams = HashMap<String, String>()
        postParams["under_voltage"] = inputUnderVoltage.text.toString()
        postParams["over_voltage"] = inputOverVoltage.text.toString()
        postParams["under_frequency"] = inputUnderFreq.text.toString()
        postParams["over_frequency"] = inputOverFreq.text.toString()
        postParams["under_pf"] = inputUnderPf.text.toString()

        val request = object : StringRequest(Method.POST, url,
            { response ->
                Toast.makeText(this, "Berhasil disimpan", Toast.LENGTH_SHORT).show()
            },
            { error ->
                Toast.makeText(this, "Gagal simpan: ${error.message}", Toast.LENGTH_SHORT).show()
            }
        ) {
            override fun getParams(): MutableMap<String, String> = postParams
        }

        Volley.newRequestQueue(this).add(request)
    }

    private fun enableEditText(state: Boolean) {
        inputUnderVoltage.isEnabled = state
        inputOverVoltage.isEnabled = state
        inputUnderFreq.isEnabled = state
        inputOverFreq.isEnabled = state
        inputUnderPf.isEnabled = state
    }


    private fun setEditTextStyle(isEditing: Boolean) {
        val editTextMap = listOf(
            inputUnderVoltage to "#007C99",
            inputOverVoltage to "#34D399",
            inputUnderFreq to "#F472B6",
            inputOverFreq to "#F87171",
            inputUnderPf to "#6366F1"
        )

        for ((editText, originalColor) in editTextMap) {
            if (isEditing) {
                editText.setBackgroundColor(Color.parseColor("#FFFFFF"))
                editText.setTextColor(Color.BLACK)
            } else {
                editText.setBackgroundColor(Color.TRANSPARENT)
                editText.setTextColor(Color.parseColor(originalColor))
            }
        }
    }

//    private fun setupTabs() {
//        tabLayoutMode.addTab(tabLayoutMode.newTab().setText("Atur Batas Penggunaan"))
//        tabLayoutMode.addTab(tabLayoutMode.newTab().setText("Atur Penagihan"))
//
//        tabLayoutMode.addOnTabSelectedListener(object : TabLayout.OnTabSelectedListener {
//            override fun onTabSelected(tab: TabLayout.Tab?) {
//                when (tab?.position) {
//                    0 -> {
//                        gridParameter.visibility = View.VISIBLE
//                        layoutBillingSettings.visibility = View.GONE
//                    }
//                    1 -> {
//                        gridParameter.visibility = View.GONE
//                        layoutBillingSettings.visibility = View.VISIBLE
//                    }
//                }
//            }
//
//            override fun onTabUnselected(tab: TabLayout.Tab?) {}
//            override fun onTabReselected(tab: TabLayout.Tab?) {}
//        })
//    }

    private fun setupTabs() {
        val pickerStartDay = findViewById<NumberPicker>(R.id.picker_start_day)
        val pickerEndDay = findViewById<NumberPicker>(R.id.picker_end_day)

        tabLayoutMode.addTab(tabLayoutMode.newTab().setText("Atur Batas Penggunaan"))
        tabLayoutMode.addTab(tabLayoutMode.newTab().setText("Atur Penagihan"))

        tabLayoutMode.addOnTabSelectedListener(object : TabLayout.OnTabSelectedListener {
            override fun onTabSelected(tab: TabLayout.Tab?) {
                when (tab?.position) {
                    0 -> {
                        gridParameter.visibility = View.VISIBLE
                        layoutBillingSettings.visibility = View.GONE
                        btnEditThreshold.visibility = View.VISIBLE  // ✅ Tampilkan tombol EDIT
                    }
                    1 -> {
                        gridParameter.visibility = View.GONE
                        layoutBillingSettings.visibility = View.VISIBLE
                        btnEditThreshold.visibility = View.GONE  // ✅ SEMBUNYIKAN tombol EDIT

                        pickerStartDay.isEnabled = false
                        pickerEndDay.isEnabled = false
                    }
                }
            }

            override fun onTabUnselected(tab: TabLayout.Tab?) {}
            override fun onTabReselected(tab: TabLayout.Tab?) {}
        })
    }


    private fun setupChart() {
        chart.apply {
            xAxis.position = XAxis.XAxisPosition.BOTTOM
            xAxis.granularity = 1f
            xAxis.setDrawGridLines(false)
            axisRight.isEnabled = false
            description.isEnabled = false
            legend.isEnabled = false
            animateY(1000)
        }
    }

    private fun setToggle(isBulanan: Boolean) {
        if (isBulanan) {
            btnBulanan.setBackgroundResource(R.drawable.bg_toggle_active_listrik)
            iconBulanan.setImageResource(R.drawable.bolt_yellow)
            loadMonthlyChartData()
        }
    }

    private fun loadMonthlyChartData() {
        val url = "https://picow-fix-v1.riffals.com/get_chart_bulanan.php"
        val request = JsonArrayRequest(url, { response ->
            val labels = mutableListOf<String>()
            val values = mutableListOf<Float>()
            val inputFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
            val outputFormat = SimpleDateFormat("MMM", Locale("id", "ID"))

            for (i in 0 until response.length()) {
                val item: JSONObject = response.getJSONObject(i)
                val date = inputFormat.parse(item.getString("billing_period"))
                val monthLabel = outputFormat.format(date ?: Date())
                labels.add(monthLabel)

                val totalBill = item.getDouble("total_bill").toFloat()
                values.add(totalBill)

                if (i == response.length() - 1) {
                    val latestEnergy = item.getDouble("total_energy") / 1000.0
                    val formatter = NumberFormat.getNumberInstance(Locale("id", "ID"))
                    formatter.minimumFractionDigits = 1
                    formatter.maximumFractionDigits = 2
                    energiTextView.text = "${formatter.format(latestEnergy)} kWh"
                }
            }

            updateChart(labels, values)
        }, { error ->
            Toast.makeText(this, "Gagal ambil data: ${error.message}", Toast.LENGTH_SHORT).show()
        })

        Volley.newRequestQueue(this).add(request)
    }

    private fun updateChart(labels: List<String>, values: List<Float>) {
        val entries = values.mapIndexed { index, value -> Entry(index.toFloat(), value) }
        val dataSet = LineDataSet(entries, "").apply {
            color = Color.parseColor("#2563EB")
            valueTextColor = Color.BLACK
            valueTextSize = 12f
            lineWidth = 2f
            setCircleColor(color)
            circleRadius = 4f
            mode = LineDataSet.Mode.CUBIC_BEZIER
            valueFormatter = object : com.github.mikephil.charting.formatter.ValueFormatter() {
                override fun getPointLabel(entry: Entry?): String {
                    entry ?: return ""
                    val formatter = NumberFormat.getCurrencyInstance(Locale("in", "ID"))
                    return formatter.format(entry.y).replace(",00", "")
                }
            }
        }

        chart.xAxis.valueFormatter = IndexAxisValueFormatter(labels)
        chart.data = LineData(dataSet)
        chart.invalidate()
    }

    private fun saveBillingRange(startDay: Int, endDay: Int) {
        val url = "https://picow-fix-v1.riffals.com/insert_billing_range.php"
        val postParams = HashMap<String, String>()
        postParams["start_day"] = startDay.toString()
        postParams["end_day"] = endDay.toString()

        val request = object : StringRequest(Request.Method.POST, url,
            { Toast.makeText(this, "Billing range disimpan", Toast.LENGTH_SHORT).show() },
//            { error -> Toast.makeText(this, "Gagal simpan range: ${error.message}", Toast.LENGTH_SHORT).show() }
            { Toast.makeText(this, "Berhasil menyimpan tanggal penagihan", Toast.LENGTH_SHORT).show() }
        ) {
            override fun getParams(): MutableMap<String, String> = postParams
        }

        Volley.newRequestQueue(this).add(request)
    }

}
