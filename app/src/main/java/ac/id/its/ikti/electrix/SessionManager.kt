    package ac.id.its.ikti.electrix

    import android.content.Context
    import android.content.SharedPreferences

    class SessionManager(context: Context) {

        private val PREF_NAME = "ElectrixSession"
        private val sharedPref: SharedPreferences = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE)
        private val editor: SharedPreferences.Editor = sharedPref.edit()

        fun saveUserSession(userId: String, username: String, email: String) {
            editor.putString("user_id", userId)
            editor.putString("username", username)
            editor.putString("email", email)
            editor.putBoolean("is_logged_in", true)
            editor.apply()
        }

        fun updateUser(userId: String, username: String, email: String) {
            editor.putString("user_id", userId)
            editor.putString("username", username)
            editor.putString("email", email)
            editor.apply()
        }

        fun isLoggedIn(): Boolean {
            return sharedPref.getBoolean("is_logged_in", false)
        }

        fun getUsername(): String? = sharedPref.getString("username", null)
        fun getUserId(): String? = sharedPref.getString("user_id", null)
        fun getEmail(): String? = sharedPref.getString("email", null)

        fun logout() {
            editor.clear()
            editor.apply()
        }
    }
