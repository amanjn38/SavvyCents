package com.finance.savvycents.ui.screens

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import com.finance.savvycents.R
import com.finance.savvycents.databinding.FragmentForgotPasswordBinding
import com.finance.savvycents.utilities.Resource
import com.finance.savvycents.viewmodels.LoginViewModel
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.GoogleAuthProvider
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class ForgotPasswordFragment : Fragment() {
    private var _binding: FragmentForgotPasswordBinding? = null
    private val binding get() = _binding!!
    private val viewModel: LoginViewModel by viewModels()
    private lateinit var firebaseAuth: FirebaseAuth
    private var isLoading = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = FragmentForgotPasswordBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        firebaseAuth = FirebaseAuth.getInstance()
        binding.btForgotPassword.setOnClickListener {
            val email = binding.etEmail.text.toString()
            if (validateInputs(email)) {
                checkAuthenticationAndSendResetEmail(email)
            }
        }

        viewModel.resetPasswordStatus.observe(viewLifecycleOwner) { resource ->
            when (resource) {
                is Resource.Loading -> {
                    showLoadingIndicator()
                }
                is Resource.Success -> {
                    Toast.makeText(
                        context,
                        "Password reset link send successfully",
                        Toast.LENGTH_LONG
                    ).show()
                    findNavController().navigate(R.id.action_forgotPasswordFragment_to_loginFragment)
                }
                is Resource.Error -> {
                    val errorMessage = resource.message ?: "An error occurred"
                    Toast.makeText(context, errorMessage + "Error", Toast.LENGTH_LONG).show()

                    hideLoadingIndicator()
                }
            }
        }
    }

    private fun checkAuthenticationAndSendResetEmail(email: String) {
        if (email.isNotBlank()) {
            firebaseAuth.fetchSignInMethodsForEmail(email)
                .addOnCompleteListener { task ->
                    if (task.isSuccessful) {
                        val signInMethods = task.result?.signInMethods
                        val isGoogleAuth =
                            signInMethods?.contains(GoogleAuthProvider.GOOGLE_SIGN_IN_METHOD)
                                ?: false
                        if (!isGoogleAuth) {
                            viewModel.sendForgotPasswordEmail(email)
                        } else {
                            Toast.makeText(
                                requireContext(),
                                "Please sign in using your google account ",
                                Toast.LENGTH_LONG
                            ).show()
                        }
                    }
                }
        } else {
            Toast.makeText(
                requireContext(),
                "Please enter your email. ",
                Toast.LENGTH_LONG
            ).show()

        }
        binding.tvBack.setOnClickListener {
            findNavController().popBackStack()
        }
    }

    private fun validateInputs(
        email: String,
    ): Boolean {
        if (email.isEmpty()) {
            binding.etEmail.error = "Email cannot be empty"
            binding.etEmail.requestFocus()
            return false
        }

        // Add more validation logic for email format if needed
        val emailPattern = "[a-zA-Z0-9._-]+@[a-z]+\\.+[a-z]+"
        val isValidEmail = email.matches(emailPattern.toRegex())
        if (!isValidEmail) {
            binding.etEmail.error = "Invalid email format"
            binding.etEmail.requestFocus()
            return false
        }
        return true
    }


    override fun onDestroy() {
        super.onDestroy()

        _binding = null
    }

    private fun showLoadingIndicator() {
        isLoading = true
        // Show a loading indicator (e.g., progress bar)
        binding.progressBar.visibility = View.VISIBLE
        binding.btForgotPassword.visibility = View.INVISIBLE
    }

    private fun hideLoadingIndicator() {
        isLoading = false
        // Hide the loading indicator (e.g., progress bar)
        binding.progressBar.visibility = View.GONE
        binding.btForgotPassword.visibility = View.VISIBLE
    }

}