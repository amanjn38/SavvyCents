package com.finance.savvycents.ui.screens

import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import com.finance.savvycents.R
import com.finance.savvycents.databinding.FragmentRegisterBinding
import com.finance.savvycents.utilities.Resource
import com.finance.savvycents.utilities.Validator
import com.finance.savvycents.ui.viewmodels.RegisterViewModel
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

@AndroidEntryPoint
class RegisterFragment : Fragment(R.layout.fragment_register) {
    private lateinit var binding: FragmentRegisterBinding
    private val viewModel: RegisterViewModel by viewModels()

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding = FragmentRegisterBinding.bind(view)
        setupListeners()
        observeRegistration()
    }

    private fun setupListeners() {
        binding.btnRegister.setOnClickListener {
            validateAndRegister()
        }

        binding.btnGoogleSignIn.setOnClickListener {
            // Handle Google Sign In
        }

        // Hide phone/OTP registration UI
        binding.btnPhoneSignIn.visibility = View.GONE
    }

    private fun validateAndRegister() {
        val name = binding.etName.text.toString().trim()
        val email = binding.etEmail.text.toString().trim()
        val password = binding.etPassword.text.toString()
        val confirmPassword = binding.etConfirmPassword.text.toString()
        val phone = binding.etPhone.text.toString().trim()

        // Validate name
        when (val nameValidation = viewModel.validateName(name)) {
            is Validator.Error -> {
                binding.tilName.error = nameValidation.errorMsg
                return
            }
            is Validator.Success -> binding.tilName.error = null
        }

        // Validate email
        when (val emailValidation = viewModel.validateEmail(email)) {
            is Validator.Error -> {
                binding.tilEmail.error = emailValidation.errorMsg
                return
            }
            is Validator.Success -> binding.tilEmail.error = null
        }

        // Validate password
        when (val passwordValidation = viewModel.validatePassword(password)) {
            is Validator.Error -> {
                binding.tilPassword.error = passwordValidation.errorMsg
                return
            }
            is Validator.Success -> binding.tilPassword.error = null
        }

        // Validate confirm password
        when (val confirmPasswordValidation = viewModel.validateConfirmPassword(password, confirmPassword)) {
            is Validator.Error -> {
                binding.tilConfirmPassword.error = confirmPasswordValidation.errorMsg
                return
            }
            is Validator.Success -> binding.tilConfirmPassword.error = null
        }

        // If all validations pass, register
        viewModel.register(email, password, name, phone)
    }

    private fun observeRegistration() {
        viewLifecycleOwner.lifecycleScope.launch {
            viewModel.registerFlow.collectLatest { result ->
                when (result) {
                    is Resource.Loading<*> -> {
                        binding.progressBar.visibility = View.VISIBLE
                        binding.btnRegister.isEnabled = false
                        binding.btnGoogleSignIn.isEnabled = false
                    }
                    is Resource.Success<*> -> {
                        binding.progressBar.visibility = View.GONE
                        binding.btnRegister.isEnabled = true
                        binding.btnGoogleSignIn.isEnabled = true
                        onRegistrationSuccess()
                    }
                    is Resource.Error<*> -> {
                        binding.progressBar.visibility = View.GONE
                        binding.btnRegister.isEnabled = true
                        binding.btnGoogleSignIn.isEnabled = true
                        Toast.makeText(requireContext(), result.message, Toast.LENGTH_SHORT).show()
                    }
                    else -> {
                        binding.progressBar.visibility = View.GONE
                        binding.btnRegister.isEnabled = true
                        binding.btnGoogleSignIn.isEnabled = true
                    }
                }
            }
        }
    }

    private fun onRegistrationSuccess() {
        val user = com.google.firebase.auth.FirebaseAuth.getInstance().currentUser
        user?.sendEmailVerification()
        Toast.makeText(requireContext(), "Registration successful! Please verify your email before logging in.", Toast.LENGTH_LONG).show()
        findNavController().navigate(R.id.action_registerFragment_to_loginFragment)
    }
}
