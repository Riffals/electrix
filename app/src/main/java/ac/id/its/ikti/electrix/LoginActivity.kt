package ac.id.its.ikti.electrix

import android.content.Intent
import android.graphics.Color
import android.os.Bundle
import android.text.SpannableString
import android.text.Spanned
import android.text.method.LinkMovementMethod
import android.text.style.ClickableSpan
import android.text.style.ForegroundColorSpan
import android.view.View
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import com.android.volley.Request.Method
import com.android.volley.toolbox.StringRequest
import com.android.volley.toolbox.Volley
import com.google.android.material.textfield.TextInputEditText
import org.json.JSONObject

class LoginActivity : AppCompatActivity() {

    private lateinit var emailInput: TextInputEditText
    private lateinit var passwordInput: TextInputEditText
    private lateinit var loginButton: Button
    private val LOGIN_URL = "https://picow-fix-v1.riffals.com/login.php"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Cek apakah user sudah login
        val sessionManager = SessionManager(this)
        if (sessionManager.isLoggedIn()) {
            startActivity(Intent(this, BerandaActivity::class.java))
            finish()
            return
        }

        setContentView(R.layout.activity_login)

        emailInput = findViewById(R.id.login_email_input)
        passwordInput = findViewById(R.id.login_password_input)
        loginButton = findViewById(R.id.login_button)

        // Setup teks link "Daftar"
        val signupTextView = findViewById<TextView>(R.id.login_signup_text)
        val text = "Belum punya akun? Daftar"
        val spannableString = SpannableString(text)
        val clickableSpan = object : ClickableSpan() {
            override fun onClick(widget: View) {
                startActivity(Intent(this@LoginActivity, SignUpActivity::class.java))
                finish()
            }
        }
        spannableString.setSpan(clickableSpan, text.indexOf("Daftar"), text.length, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE)
        spannableString.setSpan(ForegroundColorSpan(Color.parseColor("#3563F3")), text.indexOf("Daftar"), text.length, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE)
        signupTextView.text = spannableString
        signupTextView.movementMethod = LinkMovementMethod.getInstance()

        // Tombol Login
        loginButton.setOnClickListener {
            val email = emailInput.text.toString().trim()
            val password = passwordInput.text.toString().trim()

            // Validasi input
            if (email.isEmpty() || password.isEmpty()) {
                showToast("Email dan password wajib diisi")
                return@setOnClickListener
            }

            if (!android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
                showToast("Format email tidak valid")
                return@setOnClickListener
            }

            // Kirim request ke server
            val queue = Volley.newRequestQueue(this)
            val request = object : StringRequest(Method.POST, LOGIN_URL,
                { response ->
                    val json = JSONObject(response)
                    if (json.getString("status") == "success") {
                        showToast("Login berhasil!")

                        // Simpan session
                        sessionManager.saveUserSession(
                            json.getString("user_id"),
                            json.getString("username"),
                            json.getString("email")
                        )

                        val intent = Intent(this, BerandaActivity::class.java)
                        startActivity(intent)
                        finish()
                    } else {
                        val message = json.getString("message")
                        when (message) {
                            "Email tidak ditemukan" -> showToast("Email tidak terdaftar. Silakan daftar terlebih dahulu.")
                            "Password salah" -> showToast("Kata sandi salah. Silakan coba lagi.")
                            else -> showToast(message)
                        }
                    }
                },
                { error ->
                    showToast("Gagal terhubung ke server")
                }
            ) {
                override fun getParams(): Map<String, String> {
                    return mapOf(
                        "email" to email,
                        "password" to password
                    )
                }
            }
            queue.add(request)
        }

        // Klik "Lupa kata sandi"
        findViewById<TextView>(R.id.login_forgot_password_text).setOnClickListener {
            startActivity(Intent(this@LoginActivity, ForgotPasswordActivity::class.java))
        }
    }

    private fun showToast(msg: String) {
        Toast.makeText(this, msg, Toast.LENGTH_SHORT).show()
    }
}
