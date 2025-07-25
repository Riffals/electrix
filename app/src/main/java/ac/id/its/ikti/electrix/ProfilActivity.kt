package ac.id.its.ikti.electrix

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.View
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import com.android.volley.toolbox.StringRequest
import com.android.volley.toolbox.Volley
import com.google.android.material.bottomnavigation.BottomNavigationView
import org.json.JSONObject

class ProfilActivity : AppCompatActivity() {

    private lateinit var sessionManager: SessionManager
    private lateinit var namaInput: EditText
    private lateinit var emailInput: EditText
    private lateinit var btnEdit: ImageView
    private lateinit var btnSimpan: ImageView
    private lateinit var btnLogout: Button

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        sessionManager = SessionManager(this)
        if (!sessionManager.isLoggedIn()) {
            startActivity(Intent(this, LoginActivity::class.java))
            finish()
            return
        }

        setContentView(R.layout.activity_profil)

        val bottomNavBar: BottomNavigationView = findViewById(R.id.bottom_navigation)
        setupBottomNavBar(bottomNavBar, R.id.nav_profil)

        // Ambil data dari session
        val userId = sessionManager.getUserId()
        val username = sessionManager.getUsername()
        val email = sessionManager.getEmail()

        // Input
        namaInput = findViewById(R.id.profil_nama_input)
        emailInput = findViewById(R.id.profil_email_input)
        namaInput.setText(username)
        emailInput.setText(email)

        // Nonaktifkan input awalnya
        namaInput.isEnabled = false
        emailInput.isEnabled = false

        // Tombol
        btnEdit = findViewById(R.id.btn_edit)
        btnSimpan = findViewById(R.id.btn_simpan)
        btnLogout = findViewById(R.id.btn_logout)

        // Simpan tombol simpan di-hide dulu
        btnSimpan.visibility = View.GONE

        // Edit → aktifkan input
        btnEdit.setOnClickListener {
            btnEdit.visibility = View.GONE
            btnSimpan.visibility = View.VISIBLE
            namaInput.isEnabled = true
            emailInput.isEnabled = true
        }

        // Simpan → kirim ke server
        btnSimpan.setOnClickListener {
            val newName = namaInput.text.toString().trim()
            val newEmail = emailInput.text.toString().trim()
            val userIdValue = sessionManager.getUserId() ?: ""

            if (newName.isEmpty() || newEmail.isEmpty()) {
                Toast.makeText(this, "Nama dan email tidak boleh kosong", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            val request = object : StringRequest(
                Method.POST,
                "https://picow-fix-v1.riffals.com/update_profile.php",
                { response ->
                    try {
                        val json = JSONObject(response)
                        val status = json.getString("status")
                        val message = json.getString("message")

                        if (status == "success") {
                            // ✅ Simpan lengkap
                            sessionManager.updateUser(userIdValue, newName, newEmail)
                            Toast.makeText(this, message, Toast.LENGTH_SHORT).show()

                            // Kunci kembali input
                            namaInput.isEnabled = false
                            emailInput.isEnabled = false
                            btnSimpan.visibility = View.GONE
                            btnEdit.visibility = View.VISIBLE
                        } else {
                            Toast.makeText(this, message, Toast.LENGTH_SHORT).show()
                        }
                    } catch (e: Exception) {
                        e.printStackTrace()
                        Toast.makeText(this, "Gagal parsing response dari server", Toast.LENGTH_SHORT).show()
                    }
                },
                { error ->
                    error.printStackTrace()
                    Toast.makeText(this, "Gagal terhubung ke server", Toast.LENGTH_SHORT).show()
                }
            ) {
                override fun getParams(): Map<String, String> {
                    return mapOf(
                        "user_id" to userIdValue,
                        "username" to newName,
                        "email" to newEmail
                    )
                }
            }

            Volley.newRequestQueue(this).add(request)
        }


        // Logout
        btnLogout.setOnClickListener {
            sessionManager.logout()
            Toast.makeText(this, "Berhasil logout", Toast.LENGTH_SHORT).show()
            startActivity(Intent(this@ProfilActivity, LoginActivity::class.java))
            finish()
        }

        // Help ke WhatsApp
        findViewById<ImageView>(R.id.icon_help).setOnClickListener {
            startActivity(Intent(Intent.ACTION_VIEW, Uri.parse("https://api.whatsapp.com/send/?phone=%2B6287857097780")))
        }
        findViewById<TextView>(R.id.tv_help).setOnClickListener {
            startActivity(Intent(Intent.ACTION_VIEW, Uri.parse("https://api.whatsapp.com/send/?phone=%2B6287857097780")))
        }

        // Lupa Kata Sandi
        val forgotIntent = Intent(this, ForgotPasswordActivity::class.java)
        findViewById<ImageView>(R.id.icon_lupa_sandi).setOnClickListener { startActivity(forgotIntent) }
        findViewById<TextView>(R.id.tv_lupa_kata_sandi).setOnClickListener { startActivity(forgotIntent) }
    }
}
