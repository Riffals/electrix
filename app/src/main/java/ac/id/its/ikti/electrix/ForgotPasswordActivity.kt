package ac.id.its.ikti.electrix

import android.content.Intent
import android.os.Bundle
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import com.android.volley.toolbox.StringRequest
import com.android.volley.toolbox.Volley
import org.json.JSONObject

class ForgotPasswordActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_forgot_password)

        val backIcon = findViewById<ImageView>(R.id.forgot_toolbar_back_icon)
        val emailInput = findViewById<EditText>(R.id.forgot_email_input)
        val verifyButton = findViewById<Button>(R.id.forgot_verification_button)

        backIcon.setOnClickListener {
            startActivity(Intent(this, LoginActivity::class.java))
            finish()
        }

        verifyButton.setOnClickListener {
            val email = emailInput.text.toString().trim()
            if (email.isEmpty()) {
                Toast.makeText(this, "Email tidak boleh kosong", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            val request = object : StringRequest(
                Method.POST,
                "https://picow-fix-v1.riffals.com/forgot_password_check.php",
                { response ->
                    val json = JSONObject(response)
                    if (json.getString("status") == "success") {
                        val intent = Intent(this, SavePasswordActivity::class.java)
                        intent.putExtra("email", email)
                        startActivity(intent)
                        finish()
                    } else {
                        Toast.makeText(this, "Email tidak ditemukan", Toast.LENGTH_SHORT).show()
                    }
                },
                {
                    Toast.makeText(this, "Gagal terhubung ke server", Toast.LENGTH_SHORT).show()
                }
            ) {
                override fun getParams(): Map<String, String> {
                    return mapOf("email" to email)
                }
            }

            Volley.newRequestQueue(this).add(request)
        }
    }
}
