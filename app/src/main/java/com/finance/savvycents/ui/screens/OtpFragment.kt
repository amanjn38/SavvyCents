package com.finance.savvycents.ui.screens

import android.content.Intent
import android.content.SharedPreferences
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import com.finance.savvycents.databinding.FragmentOtpBinding
import com.finance.savvycents.models.User
import com.finance.savvycents.utilities.Resource
import com.finance.savvycents.utilities.Validator
import com.finance.savvycents.utilities.saveUserData
import com.finance.savvycents.viewmodels.LoginViewModel
import com.google.firebase.FirebaseException
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.PhoneAuthCredential
import com.google.firebase.auth.PhoneAuthOptions
import com.google.firebase.auth.PhoneAuthProvider
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import java.util.concurrent.TimeUnit
import javax.inject.Inject

@AndroidEntryPoint
class OtpFragment : Fragment() {

    private var _binding: FragmentOtpBinding? = null
    private val binding get() = _binding!!
    private var email: String? = null
    private var name: String? = null
    private var password: String? = null
    private var phone: String? = null
    private var loginType: String? = null
    private var textInput: String? = null
    private val viewModel: LoginViewModel by viewModels()
    private var isLoading = false

    @Inject
    lateinit var firebaseAuth: FirebaseAuth

    @Inject
    lateinit var sharedPreferences: SharedPreferences
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        _binding = FragmentOtpBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setObservers()
        setObserversForLogin()
        val from = arguments?.getString("from", null)
        if (from.equals("loginFragment")) {
            binding.btSignup.text = "Login"
            loginType = arguments?.getString("loginType", null).toString()
            if (loginType.equals("email")) {
                binding.pinview.visibility = View.GONE
                binding.etPassword.visibility = View.VISIBLE
                textInput = arguments?.getString("emailOrNumber")
                binding.btSignup.visibility = View.GONE
                binding.btLoginEmail.visibility = View.VISIBLE
                binding.btLoginPhone.visibility = View.GONE
            }
        } else if (from.equals("registerFragment")) {
            binding.btSignup.text = "Signup"
            binding.btSignup.visibility = View.VISIBLE

            arguments?.let {
                name = it.getString("name", null)
                email = it.getString("email", null)
                password = it.getString("password", null)
                phone = it.getString("phone", null)
            }


//            lifecycleScope.launch {
//                sendVerificationCode(email, name, password, phone)
//            }

        }
        binding.btLoginEmail.setOnClickListener {
            val password = binding.etPassword.text.toString()

            val validPas = viewModel.validatePassword(password)

            if (validPas is Validator.Error) {
                Toast.makeText(context, validPas.errorMsg, Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            viewModel.loginUser(textInput!!, password)
        }
    }

    private fun setObservers() {
        lifecycleScope.launch {

            viewModel.signUpFlow.collect { it ->
                val result = it ?: return@collect

                hideLoadingIndicator()

                when (result) {
                    is Resource.Error -> {
                        val errorMessage = result.message ?: "An error occurred"
                        Toast.makeText(context, errorMessage, Toast.LENGTH_LONG).show()

                    }

                    is Resource.Loading -> {
                        showLoadingIndicator()
                    }

                    is Resource.Success -> {

                        result.data?.let { user ->
                            viewModel.saveLoginCredential(
                                User(
                                    isLoggedIn = true,
                                    email = email,
                                    userId = user.uid,
                                    name = name!!,
                                    phone = phone
                                )
                            )

                            showSuccessFeedback("Signup Successful")
                            saveUserData(requireContext(), name!!, email!!, phone!!)
//                            findNavController().navigate(R.id.action_registerFragment_to_homeFragment)
                            val intent = Intent(activity, HomeActivity::class.java)
                            startActivity(intent)
                        }
                    }
                }
            }
        }
    }

    override fun onDestroy() {
        super.onDestroy()

        _binding = null
    }

    private fun showLoadingIndicator() {
        isLoading = true
        // Show a loading indicator (e.g., progress bar)
        binding.progressBar.visibility = View.VISIBLE
        binding.btSignup.visibility = View.INVISIBLE
    }

    private fun hideLoadingIndicator() {
        isLoading = false
        // Hide the loading indicator (e.g., progress bar)
        binding.progressBar.visibility = View.GONE
        binding.btSignup.visibility = View.VISIBLE
    }

    private fun showSuccessFeedback(message: String) {
        // Show a success message to the user
        Toast.makeText(context, message, Toast.LENGTH_SHORT).show()
    }

    private fun setObserversForLogin() {
        lifecycleScope.launch {

            viewModel.loginFlow.collect { it ->
                val result = it ?: return@collect

                hideLoadingIndicator()

                when (result) {
                    is Resource.Error -> {

                        val errorMessage = result.message ?: "An error occurred"
                        Toast.makeText(context, errorMessage, Toast.LENGTH_LONG).show()

                    }

                    is Resource.Loading -> {
                        showLoadingIndicator()
                    }

                    is Resource.Success -> {

                        result.data?.let { user ->
                            viewModel.saveLoginCredential(
                                User(
                                    isLoggedIn = true,
                                    email = user.email,
                                    userId = user.userId,
                                    name = user.name,
                                    phone = user.phone
                                )
                            )

                            showSuccessFeedback("Login Successful")
                            val intent = Intent(activity, HomeActivity::class.java)
                            startActivity(intent)
                        }

                    }
                }
            }
        }
    }
}