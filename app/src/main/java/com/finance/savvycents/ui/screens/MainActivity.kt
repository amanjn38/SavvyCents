package com.finance.savvycents.ui.screens

import android.Manifest
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.provider.Telephony
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.navigation.fragment.NavHostFragment
import com.finance.savvycents.R
import com.finance.savvycents.utilities.SmsUtils
import com.finance.savvycents.utilities.SmsUtils.isDefaultSmsApp
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

        navController.navigate(R.id.loginFragment)
//        if (!isDefaultSmsApp(this)) {
//            // Prompt the user to set your app as the default SMS app
////            SmsUtils.openDefaultSmsSettings(this)
//        }
//
//        // Request SMS permission if not granted
//        SmsUtils.requestSmsPermission(this) { granted ->
//            if (granted) {
//                // Register the SMS receiver
//            } else {
//                // Handle permission denied
//                Toast.makeText(this, "SMS permission denied", Toast.LENGTH_SHORT).show()
//            }
//        }

        if(checkSelfPermission(Manifest.permission.RECEIVE_SMS) != PackageManager.PERMISSION_GRANTED) {

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
}