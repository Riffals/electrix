package ac.id.its.ikti.electrix

import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat // <- pastikan ini di-import

class SplashActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState) // Panggil DULU ini
        setContentView(R.layout.activity_splash)

        // Baru setelah itu jalankan Foreground Service
        val intent = Intent(this, AnomalyPollingService::class.java)
        ContextCompat.startForegroundService(this, intent)

        Handler(Looper.getMainLooper()).postDelayed({
            val sessionManager = SessionManager(this)

            if (sessionManager.isLoggedIn()) {
                startActivity(Intent(this, BerandaActivity::class.java))
            } else {
                startActivity(Intent(this, LoginActivity::class.java))
            }

            finish()
        }, 2000)
    }
}
