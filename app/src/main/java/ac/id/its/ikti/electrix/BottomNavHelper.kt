package ac.id.its.ikti.electrix

import android.app.Activity
import android.content.Intent
import com.google.android.material.bottomnavigation.BottomNavigationView

fun Activity.setupBottomNavBar(bottomNavBar: BottomNavigationView, activeItemId: Int) {
    bottomNavBar.selectedItemId = activeItemId

    bottomNavBar.setOnItemSelectedListener { item ->
        when (item.itemId) {
            R.id.nav_beranda -> {
                if (activeItemId != R.id.nav_beranda) {
                    startActivity(Intent(this, BerandaActivity::class.java))
                    overridePendingTransition(0, 0)
                    finish()
                }
                true
            }
            R.id.nav_listrik -> {
                if (activeItemId != R.id.nav_listrik) {
                    startActivity(Intent(this, ListrikActivity::class.java))
                    overridePendingTransition(0, 0)
                    finish()
                }
                true
            }
            R.id.nav_riwayat -> {
                if (activeItemId != R.id.nav_riwayat) {
                    startActivity(Intent(this, RiwayatActivity::class.java))
                    overridePendingTransition(0, 0)
                    finish()
                }
                true
            }
            R.id.nav_profil -> {
                if (activeItemId != R.id.nav_profil) {
                    startActivity(Intent(this, ProfilActivity::class.java))
                    overridePendingTransition(0, 0)
                    finish()
                }
                true
            }
            else -> false
        }
    }
}
