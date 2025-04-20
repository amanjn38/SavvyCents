package com.finance.savvycents.ui.screens

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import androidx.core.view.get
import androidx.navigation.NavController
import androidx.navigation.fragment.NavHostFragment
import com.finance.savvycents.R
import com.finance.savvycents.databinding.ActivityHomeBinding
import com.google.firebase.auth.FirebaseAuth
import dagger.hilt.android.AndroidEntryPoint
import androidx.core.view.size
import android.view.View
import android.content.Intent

@AndroidEntryPoint
class HomeActivity : AppCompatActivity() {

    private lateinit var binding: ActivityHomeBinding
    private lateinit var navController: NavController

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityHomeBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Set up Navigation
        val navHostFragment = supportFragmentManager
            .findFragmentById(R.id.nav_host_fragment) as NavHostFragment
        navController = navHostFragment.navController

        // If user is not logged in, go to MainActivity and finish HomeActivity
        if (FirebaseAuth.getInstance().currentUser == null) {
            startActivity(Intent(this, MainActivity::class.java))
            finish()
            return
        }

        // Set up bottom navigation
        binding.bottomNavigationView.background = null
        if (binding.bottomNavigationView.menu.size > 2) {
            binding.bottomNavigationView.menu[2].isEnabled = false
        }
        binding.bottomNavigationView.setOnItemSelectedListener { item ->
            when (item.itemId) {
                R.id.home -> navController.navigate(R.id.homeFragment)
                R.id.analytics -> navController.navigate(R.id.analyticsFragment)
                R.id.cards -> navController.navigate(R.id.cardAndAccountFragment)
                R.id.profile -> navController.navigate(R.id.profileFragment)
            }
            true
        }

        // Set up FAB
        binding.fab.setOnClickListener {
            navController.navigate(R.id.addTransactionFragment)
        }
    }
}