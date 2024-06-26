package com.finance.savvycents.ui.screens

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import androidx.fragment.app.Fragment
import com.finance.savvycents.R
import com.finance.savvycents.databinding.ActivityHomeBinding
import com.finance.savvycents.models.category.Category
import com.finance.savvycents.models.category.SubCategory
import com.finance.savvycents.utilities.FirestoreUtil
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch

@AndroidEntryPoint
class HomeActivity : AppCompatActivity() {

    private lateinit var binding: ActivityHomeBinding
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityHomeBinding.inflate(layoutInflater)
        setContentView(binding.root)


        binding.bottomNavigationView.background = null
        binding.bottomNavigationView.menu.getItem(2).isEnabled = false
        replaceFragment(HomeFragment())
        binding.bottomNavigationView.setOnItemSelectedListener {
            when (it.itemId) {
                R.id.home -> replaceFragment(AddTransactionFragment())
                R.id.analytics -> replaceFragment(AnalyticsFragment())
                R.id.cards -> replaceFragment(CardAndAccountFragment())
                R.id.profile -> replaceFragment(ProfileFragment())

                else -> {

                }
            }

            true
        }
    }

    private fun replaceFragment(fragment: Fragment) {
        val fragmentManager = supportFragmentManager
        val fragmentTransaction = fragmentManager.beginTransaction()
        fragmentTransaction.replace(R.id.frame_layout, fragment)
        fragmentTransaction.commit()

    }
}