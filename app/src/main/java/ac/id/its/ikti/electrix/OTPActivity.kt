package ac.id.its.ikti.electrix

import android.content.Intent
import android.os.Bundle
import android.os.CountDownTimer
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.core.text.HtmlCompat

class OTPActivity : AppCompatActivity() {

    private lateinit var resendTimer: CountDownTimer
    private var isTimerRunning = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_otp)

        // Bold email di deskripsi
        val otpDescText = findViewById<TextView>(R.id.otp_desc_text)
        otpDescText.text = HtmlCompat.fromHtml(
            getString(R.string.otp_desc_text),
            HtmlCompat.FROM_HTML_MODE_LEGACY
        )

        // Tombol BACK
        val backButton = findViewById<ImageView>(R.id.otp_toolbar_back_icon)
        backButton.setOnClickListener {
            finish()  // kembali ke screen sebelumnya
        }

        // Tombol Lanjut (Verify)
        val otpVerifyButton = findViewById<Button>(R.id.otp_verify_button)
        otpVerifyButton.setOnClickListener {
            // Pindah ke LoginActivity
            val intent = Intent(this@OTPActivity, LoginActivity::class.java)
            startActivity(intent)
            finish()
        }

        // Setup Timer
        val resendTextView = findViewById<TextView>(R.id.otp_resend_timer)
        startResendCountdown(resendTextView)

        // Tambahkan klik listener untuk resend jika timer habis
        resendTextView.setOnClickListener {
            if (!isTimerRunning) {
                // TODO: di sini panggil API untuk resend OTP
                startResendCountdown(resendTextView)
            }
        }
    }

    private fun startResendCountdown(resendTextView: TextView) {
        isTimerRunning = true
        resendTextView.isClickable = false

        resendTimer = object : CountDownTimer(60000, 1000) {
            override fun onTick(millisUntilFinished: Long) {
                val seconds = millisUntilFinished / 1000
                resendTextView.text = "Kirim ulang <font color='#3563F3'>0:${String.format("%02d", seconds)}</font>"
                resendTextView.text = HtmlCompat.fromHtml(resendTextView.text.toString(), HtmlCompat.FROM_HTML_MODE_LEGACY)
            }

            override fun onFinish() {
                resendTextView.text = "Kirim ulang"
                resendTextView.isClickable = true
                isTimerRunning = false
            }
        }.start()
    }

    override fun onDestroy() {
        super.onDestroy()
        if (::resendTimer.isInitialized) {
            resendTimer.cancel()
        }
    }
}
