package ac.id.its.ikti.electrix

import android.content.Intent
import android.os.Bundle
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import com.android.volley.toolbox.StringRequest
import com.android.volley.toolbox.Volley
import org.json.JSONObject

class SavePasswordActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_save_password)

        val backIcon = findViewById<ImageView>(R.id.save_toolbar_back_icon)
        val inputPassword = findViewById<EditText>(R.id.save_password_input)
        val inputConfirm = findViewById<EditText>(R.id.save_email_input)
        val saveButton = findViewById<Button>(R.id.save_verification_button)

        val email = intent.getStringExtra("email") ?: ""

        backIcon.setOnClickListener {
            startActivity(Intent(this, ForgotPasswordActivity::class.java))
            finish()
        }

        saveButton.setOnClickListener {
            val password = inputPassword.text.toString().trim()
            val confirm = inputConfirm.text.toString().trim()

            if (password.length < 8) {
                Toast.makeText(this, "Kata sandi minimal 8 karakter", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            if (password != confirm) {
                Toast.makeText(this, "Konfirmasi tidak cocok", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            val request = object : StringRequest(
                Method.POST,
                "https://picow-fix-v1.riffals.com/update_password.php",
                { response ->
                    val json = JSONObject(response)
                    if (json.getString("status") == "success") {
                        Toast.makeText(this, "Password berhasil diubah", Toast.LENGTH_SHORT).show()
                        startActivity(Intent(this, LoginActivity::class.java))
                        finish()
                    } else {
                        Toast.makeText(this, "Gagal mengubah password", Toast.LENGTH_SHORT).show()
                    }
                },
                {
                    Toast.makeText(this, "Gagal terhubung ke server", Toast.LENGTH_SHORT).show()
                }
            ) {
                override fun getParams(): Map<String, String> {
                    return mapOf("email" to email, "password" to password)
                }
            }

            Volley.newRequestQueue(this).add(request)
        }
    }
}
