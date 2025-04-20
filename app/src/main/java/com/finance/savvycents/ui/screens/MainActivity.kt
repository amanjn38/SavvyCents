package com.finance.savvycents.ui.screens

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Bundle
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.navigation.fragment.NavHostFragment
import com.finance.savvycents.R
import com.finance.savvycents.notifications.Token
import com.finance.savvycents.utilities.SmsUtils
import com.finance.savvycents.utilities.SmsUtils.isDefaultSmsApp
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.messaging.FirebaseMessaging
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MainActivity : AppCompatActivity() {

    private val requestSetDefaultSmsApp =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
            if (result.resultCode == RESULT_OK) {
                // Handle the result if needed
                if (isDefaultSmsApp(this)) {
                    Toast.makeText(this, "Your app is now the default SMS app", Toast.LENGTH_SHORT)
                        .show()
                } else {
                    Toast.makeText(this, "Failed to set as default SMS app", Toast.LENGTH_SHORT)
                        .show()
                }
            }
        }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        val navHostFragment =
            supportFragmentManager.findFragmentById(R.id.nav_host_fragment) as NavHostFragment
        val navController = navHostFragment.navController

        // If user is already logged in, go to HomeActivity and finish MainActivity
        if (FirebaseAuth.getInstance().currentUser != null) {
            startActivity(Intent(this, HomeActivity::class.java))
            finish()
            return
        }

        // No need to navigate to loginFragment explicitly; nav_auth.xml handles startDestination

        if (FirebaseAuth.getInstance().currentUser != null) {
            FirebaseMessaging.getInstance().token.addOnCompleteListener { task ->
                if (task.isSuccessful && task.result != null) {
                    val token = task.result
                    updateToken(token)
                }
            }
        }

        if (checkSelfPermission(Manifest.permission.RECEIVE_SMS) != PackageManager.PERMISSION_GRANTED) {
            SmsUtils.requestSmsPermission(this) { granted ->
                if (granted) {
                    // Register the SMS receiver
                } else {
                    // Handle permission denied
                    Toast.makeText(this, "SMS permission denied", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

    private fun updateToken(token: String) {
        val user = FirebaseAuth.getInstance().currentUser
        user?.let {
            val database = FirebaseDatabase.getInstance().getReference("Tokens")
            val tokenObj = Token(token)
            database.child(user.uid).setValue(tokenObj)
        }
    }
}