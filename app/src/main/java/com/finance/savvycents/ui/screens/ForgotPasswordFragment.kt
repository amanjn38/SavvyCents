package com.finance.savvycents.ui.screens

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import com.finance.savvycents.databinding.FragmentForgotPasswordBinding
import com.finance.savvycents.utilities.Resource
import com.finance.savvycents.ui.viewmodels.ForgotPasswordViewModel
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch

@AndroidEntryPoint
class ForgotPasswordFragment : Fragment() {
    private var _binding: FragmentForgotPasswordBinding? = null
    private val binding get() = _binding!!
    private val viewModel: ForgotPasswordViewModel by viewModels()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentForgotPasswordBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setupListeners()
        observeResetPassword()
    }

    private fun setupListeners() {
        binding.btForgotPassword.setOnClickListener {
            val email = binding.etEmail.text.toString().trim()
            if (email.isNotEmpty()) {
                viewModel.sendForgotPasswordEmail(email)
            } else {
                Toast.makeText(context, "Please enter your email", Toast.LENGTH_SHORT).show()
            }
        }

        binding.tvBack.setOnClickListener {
            findNavController().popBackStack()
        }
    }

    private fun observeResetPassword() {
        lifecycleScope.launch {
            viewModel.resetPasswordStatus.collect { result ->
                when (result) {
                    is Resource.Loading -> {
                        showLoadingIndicator()
                    }
                    is Resource.Success -> {
                        hideLoadingIndicator()
                        Toast.makeText(
                            context,
                            "Password reset email sent successfully",
                            Toast.LENGTH_LONG
                        ).show()
                        findNavController().popBackStack()
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

    private fun showLoadingIndicator() {
        binding.progressBar.visibility = View.VISIBLE
        binding.btForgotPassword.isEnabled = false
    }

    private fun hideLoadingIndicator() {
        binding.progressBar.visibility = View.GONE
        binding.btForgotPassword.isEnabled = true
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}