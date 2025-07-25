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
import com.android.volley.toolbox.StringRequest
import com.android.volley.toolbox.Volley
import com.google.android.material.textfield.TextInputEditText
import org.json.JSONObject

class SignUpActivity : AppCompatActivity() {

    private lateinit var nameInput: TextInputEditText
    private lateinit var emailInput: TextInputEditText
    private lateinit var passwordInput: TextInputEditText
    private lateinit var confirmPasswordInput: TextInputEditText
    private lateinit var signupButton: Button

    private val SIGNUP_URL = "https://picow-fix-v1.riffals.com/signup.php"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_sign_up)

        nameInput = findViewById(R.id.signup_name_input)
        emailInput = findViewById(R.id.signup_email_input)
        passwordInput = findViewById(R.id.signup_password_input)
        confirmPasswordInput = findViewById(R.id.signup_confirm_password_input)
        signupButton = findViewById(R.id.signup_button)

        // Teks link "Masuk"
        val loginTextView = findViewById<TextView>(R.id.signup_login_text)
        val text = "Sudah punya akun? Masuk"
        val spannableString = SpannableString(text)
        val clickableSpan = object : ClickableSpan() {
            override fun onClick(widget: View) {
                startActivity(Intent(this@SignUpActivity, LoginActivity::class.java))
                finish()
            }
        }
        spannableString.setSpan(clickableSpan, text.indexOf("Masuk"), text.length, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE)
        spannableString.setSpan(ForegroundColorSpan(Color.parseColor("#3563F3")), text.indexOf("Masuk"), text.length, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE)
        loginTextView.text = spannableString
        loginTextView.movementMethod = LinkMovementMethod.getInstance()

        // Tombol Sign Up
        signupButton.setOnClickListener {
            val name = nameInput.text.toString().trim()
            val email = emailInput.text.toString().trim()
            val password = passwordInput.text.toString().trim()
            val confirmPassword = confirmPasswordInput.text.toString().trim()

            // Validasi input
            if (name.isEmpty() || email.isEmpty() || password.isEmpty() || confirmPassword.isEmpty()) {
                showToast("Semua field wajib diisi!")
                return@setOnClickListener
            }

            if (!android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
                showToast("Format email tidak valid")
                return@setOnClickListener
            }

            if (password.length < 8) {
                showToast("Kata sandi minimal 8 karakter")
                return@setOnClickListener
            }

            if (password != confirmPassword) {
                showToast("Konfirmasi kata sandi tidak cocok")
                return@setOnClickListener
            }

            // Kirim data ke server
            val queue = Volley.newRequestQueue(this)
            val request = object : StringRequest(Method.POST, SIGNUP_URL,
                { response ->
                    val json = JSONObject(response)
                    if (json.getString("status") == "success") {
                        showToast("Pendaftaran berhasil! Silakan login.")
                        startActivity(Intent(this, LoginActivity::class.java))
                        finish()
                    } else {
                        showToast(json.getString("message"))
                    }
                },
                { error ->
                    showToast("Gagal terhubung ke server")
                }) {
                override fun getParams(): Map<String, String> {
                    return mapOf(
                        "username" to name,
                        "email" to email,
                        "password" to password
                    )
                }
            }
            queue.add(request)
        }
    }

    private fun showToast(msg: String) {
        Toast.makeText(this, msg, Toast.LENGTH_LONG).show()
    }
}
