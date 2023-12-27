package com.finance.savvycents.ui.screens

import android.content.Intent
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
    private lateinit var email: String
    private lateinit var name: String
    private lateinit var password: String
    private lateinit var phone: String
    private val viewModel: LoginViewModel by viewModels()
    private var isLoading = false

    @Inject
    lateinit var firebaseAuth: FirebaseAuth
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
        arguments?.let {
            name = it.getString("name", null)
            email = it.getString("email", null)
            password = it.getString("password", null)
            phone = it.getString("phone", null)
        }
        lifecycleScope.launch {
            sendVerificationCode(email, name, password, phone)
        }
//        binding.btSignup.setOnClickListener {
//            val otp = binding.pinview.text.toString()
//            viewModel.verifyOtpWithCode(otp)
//        }
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
                                    name = name,
                                    phone = phone
                                )
                            )

                            showSuccessFeedback()
                            saveUserData(requireContext(), name, email, phone)
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

    private suspend fun sendVerificationCode(
        email: String,
        name: String,
        password: String,
        number: String
    ) {
        var verified = false
        var codeEntered: String

        var mCallBack: PhoneAuthProvider.OnVerificationStateChangedCallbacks? = null

        mCallBack = object : PhoneAuthProvider.OnVerificationStateChangedCallbacks() {
            override fun onCodeSent(
                verificationId: String,
                forceResendingToken: PhoneAuthProvider.ForceResendingToken
            ) {
                binding.btSignup.setOnClickListener {
                    if (verified) return@setOnClickListener
                    showLoadingIndicator()
                    codeEntered = binding.pinview.text.toString()
                    System.out.println("testingOTP" + codeEntered)
                    if (codeEntered.isEmpty() || codeEntered.length < 6) {
                        System.out.println("testingOTP" + "working")

                        Toast.makeText(
                            requireActivity(),
                            "Enter a 6 digit valid OTP",
                            Toast.LENGTH_SHORT
                        ).show()
                        return@setOnClickListener
                    }
                    verified = true
                    val credential = PhoneAuthProvider.getCredential(verificationId, codeEntered)
                    lifecycleScope.launch {
                        try {
                            val result = firebaseAuth.signInWithCredential(credential).await()
                            val currentUser = firebaseAuth.currentUser

                            if (currentUser != null) {
                                Toast.makeText(
                                    requireActivity(),
                                    "Login Successful",
                                    Toast.LENGTH_LONG
                                ).show()
                                viewModel.signupUser(name, email, password, phone, currentUser)
                                hideLoadingIndicator()
                                saveUserData(requireContext(), name, email, phone)
                                val intent = Intent(activity, HomeActivity::class.java)
                                startActivity(intent)
                            } else {
                                Toast.makeText(
                                    requireActivity(),
                                    "Verification Failed",
                                    Toast.LENGTH_LONG
                                ).show()

//                                Resource.Error("Verification failed")
                            }
                        } catch (e: Exception) {
                            Toast.makeText(requireActivity(), e.message, Toast.LENGTH_LONG).show()
                        }
                    }
                }
            }

            override fun onVerificationCompleted(phoneAuthCredential: PhoneAuthCredential) {
                if (!verified) {
                    verified = true
                    lifecycleScope.launch {
                        try {
                            val result =
                                firebaseAuth.signInWithCredential(phoneAuthCredential).await()
                            val currentUser = firebaseAuth.currentUser
                            if (currentUser != null) {
                                hideLoadingIndicator()
                                Toast.makeText(
                                    requireActivity(),
                                    "Login Successful",
                                    Toast.LENGTH_LONG
                                ).show()
                                viewModel.signupUser(name, email, password, phone, currentUser)
                                hideLoadingIndicator()
                                System.out.println("testingStorage")
                                saveUserData(requireContext(), name, email, phone)
                                val intent = Intent(activity, HomeActivity::class.java)
                                startActivity(intent)
                            } else {
                                Toast.makeText(
                                    requireActivity(),
                                    "Verification Failed",
                                    Toast.LENGTH_LONG
                                ).show()

                            }
                        } catch (e: Exception) {
                            Toast.makeText(requireActivity(), e.message, Toast.LENGTH_LONG).show()
                        }
                    }
                }
            }

            override fun onVerificationFailed(e: FirebaseException) {
                verified = false
                Toast.makeText(requireActivity(), e.message, Toast.LENGTH_LONG).show()
            }
        }

//        PhoneAuthProvider.getInstance().verifyPhoneNumber(
//            number,
//            60,
//            TimeUnit.SECONDS,
//            requireActivity(),
//            mCallBack!!
//        )
        val options = PhoneAuthOptions.newBuilder(firebaseAuth)
            .setPhoneNumber(number)       // Phone number to verify
            .setTimeout(60L, TimeUnit.SECONDS) // Timeout and unit
            .setActivity(requireActivity())                 // Activity (for callback binding)
            .setCallbacks(mCallBack)          // OnVerificationStateChangedCallbacks
            .build()

        PhoneAuthProvider.verifyPhoneNumber(options)

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

    private fun showSuccessFeedback() {
        // Show a success message to the user
        Toast.makeText(context, "Signup successful!", Toast.LENGTH_SHORT).show()
    }
}