package com.finance.savvycents.ui.screens

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import com.finance.savvycents.R
import com.finance.savvycents.databinding.FragmentOtpBinding
import com.finance.savvycents.databinding.FragmentRegisterBinding
import com.finance.savvycents.models.User
import com.finance.savvycents.utilities.Resource
import com.finance.savvycents.viewmodels.LoginViewModel
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.FirebaseDatabase
import kotlinx.coroutines.launch

class OtpFragment : Fragment() {

    private var _binding: FragmentOtpBinding? = null
    private val binding get() = _binding!!
    private lateinit var email: String
    private lateinit var name: String
    private lateinit var password: String
    private lateinit var phone: String
    private val viewModel: LoginViewModel by viewModels()
    private var isLoading = false

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

        binding.btSignup.setOnClickListener {
            val otp = binding.pinview.text.toString()
            viewModel.verifyOtpWithCode(otp)
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
                                    email = user.email,
                                    userId = user.uid,
                                    name = user.displayName!!,
                                    phone = phone
                                )
                            )

                            showSuccessFeedback()
                            saveUserData(user.displayName!!, user.email!!)
                            findNavController().navigate(R.id.action_registerFragment_to_homeFragment)
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

    private fun saveUserData(name: String, email: String) {
        val userId = FirebaseAuth.getInstance().currentUser?.uid
        if (userId != null) {
            val user = User(true, email, userId, name, "123")
            val databaseReference = FirebaseDatabase.getInstance().getReference("users")
            databaseReference.child(userId).setValue(user)
                .addOnSuccessListener {
//                    findNavController().navigate(R.id.action_registerFragment_to_homeFragment)
                }
                .addOnFailureListener { exception ->
                    Toast.makeText(context, exception.message, Toast.LENGTH_LONG).show()
                }
        } else {
            Toast.makeText(context, "No user found", Toast.LENGTH_LONG).show()
        }
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