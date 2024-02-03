package com.finance.savvycents.utilities

import android.content.SharedPreferences
import com.finance.savvycents.models.User
import com.google.gson.Gson
import javax.inject.Inject

class PreferenceHelper @Inject constructor(
    private val sharedPreferences: SharedPreferences
) {

    fun setLoggedIn(isLoggedIn: Boolean) {
        sharedPreferences.edit().putBoolean("isLoggedIn", isLoggedIn).apply()
    }

    fun isUserLoggedIn() = sharedPreferences.getBoolean("isAuthSkipped", false)

    fun isLoggedIn(): Boolean {

        val user =
            Gson().fromJson(sharedPreferences.getString("user", null), User::class.java)
        return user?.isLoggedIn ?: false

    }

    fun saveLoginCredential(user: User) {
        val userStr = Gson().toJson(user)
        sharedPreferences.edit().putString("user", userStr).apply()
    }

    fun clearLoginSession() {
        sharedPreferences.edit().remove("user").apply()
    }

}