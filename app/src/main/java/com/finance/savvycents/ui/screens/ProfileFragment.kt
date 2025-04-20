package com.finance.savvycents.ui.screens

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import com.finance.savvycents.databinding.FragmentProfileBinding
import com.finance.savvycents.utilities.Resource
import com.finance.savvycents.ui.viewmodels.ProfileViewModel
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch

@AndroidEntryPoint
class ProfileFragment : Fragment() {
    private lateinit var binding: FragmentProfileBinding
    private val viewModel: ProfileViewModel by viewModels()

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        binding = FragmentProfileBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setupUserData()
        setupListeners()
        observeLogout()
    }

    private fun setupUserData() {
        viewModel.userData.observe(viewLifecycleOwner) { resource: com.finance.savvycents.utilities.Resource<com.finance.savvycents.models.User> ->
            when (resource) {
                is Resource.Loading -> showLoadingIndicator()
                is Resource.Success -> {
                    hideLoadingIndicator()
                    val user = resource.data
                    if (user != null) {
                        binding.tvName.text = user.name
                        binding.tvEmail.text = user.email
                        binding.tvPhone.text = user.phone
                    }
                }
                is Resource.Error -> {
                    hideLoadingIndicator()
                    Toast.makeText(context, resource.message, Toast.LENGTH_LONG).show()
                }
                is Resource.Idle -> {
                    // handle idle
                }
            }
        }
    }

    private fun setupListeners() {
        binding.btLogout.setOnClickListener {
            viewModel.logout()
        }

        binding.tvBack.setOnClickListener {
            findNavController().navigateUp()
        }
    }

    private fun observeLogout() {
        lifecycleScope.launch {
            viewModel.logoutFlow.collect { result ->
                when (result) {
                    is Resource.Loading -> {
                        showLoadingIndicator()
                    }
                    is Resource.Success -> {
                        hideLoadingIndicator()
                        navigateToLogin()
                    }
                    is Resource.Error -> {
                        hideLoadingIndicator()
                        Toast.makeText(context, result.message, Toast.LENGTH_LONG).show()
                    }
                    is Resource.Idle -> {
                        // handle idle
                    }
                }
            }
        }
    }

    private fun navigateToLogin() {
        Intent(activity, MainActivity::class.java).also {
            startActivity(it)
            requireActivity().finish()
        }
    }

    private fun showLoadingIndicator() {
        binding.progressBar.visibility = View.VISIBLE
        binding.btLogout.isEnabled = false
    }

    private fun hideLoadingIndicator() {
        binding.progressBar.visibility = View.GONE
        binding.btLogout.isEnabled = true
    }
}