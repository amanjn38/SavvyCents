package com.finance.savvycents.ui.screens

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.IntentSenderRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.fragment.app.viewModels
import androidx.lifecycle.Observer
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import com.finance.savvycents.R
import com.finance.savvycents.databinding.FragmentRegisterBinding
import com.finance.savvycents.models.User
import com.finance.savvycents.utilities.Resource
import com.finance.savvycents.utilities.Validator
import com.finance.savvycents.viewmodels.LoginViewModel
import com.google.android.gms.auth.api.identity.BeginSignInRequest
import com.google.android.gms.auth.api.identity.Identity
import com.google.android.gms.auth.api.identity.SignInClient
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.auth.GoogleAuthProvider
import com.google.firebase.auth.ktx.auth
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.ktx.Firebase
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.tasks.await

@AndroidEntryPoint
class RegisterFragment : Fragment() {
    private var _binding: FragmentRegisterBinding? = null
    private val binding get() = _binding!!
    private val viewModel: LoginViewModel by viewModels()
    private lateinit var oneTapClient: SignInClient
    private lateinit var signInRequest: BeginSignInRequest
    private lateinit var auth: FirebaseAuth
    private var isLoading = false
    private lateinit var name: String
    private lateinit var email: String
    private lateinit var phone: String
    private lateinit var password: String

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = FragmentRegisterBinding.inflate(
            inflater,
            container,
            false
        )
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val inputEmail = arguments?.getString("email", null)
        binding.etEmail.setText(inputEmail)
        binding.btSignup.setOnClickListener() {
            name = binding.etName.text.toString()
            email = binding.etEmail.text.toString()
            password = binding.etPassword.text.toString()
            val confirmPassword = binding.etConfirmPassword.text.toString()
            phone = binding.etPhone.text.toString()
            val context = requireContext()

            val validName = viewModel.validateName(name)

            if (validName is Validator.Error) {
                Toast.makeText(context, validName.errorMsg, Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            val validEmail = viewModel.validateEmail(email)

            if (validEmail is Validator.Error) {
                Toast.makeText(context, validEmail.errorMsg, Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            val validPas = viewModel.validatePassword(password)

            if (validPas is Validator.Error) {
                Toast.makeText(context, validPas.errorMsg, Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            val validConfirmPassword =
                viewModel.validateConfirmPassword(password, confirmPassword)

            if (validConfirmPassword is Validator.Error) {
                Toast.makeText(context, validConfirmPassword.errorMsg, Toast.LENGTH_SHORT)
                    .show()
                return@setOnClickListener
            }

            if(binding.etPhone.text.toString() != ""){
                val validPhone = viewModel.validatePhone(phone)

                if (validPhone is Validator.Error) {
                    Toast.makeText(context, validPhone.errorMsg, Toast.LENGTH_SHORT).show()
                    return@setOnClickListener
                }
            }


            viewModel.register(email, password)
        }

        viewModel.authResult.observe(viewLifecycleOwner) { (success, message) ->
            if (success) {
                saveUserData(name, email, phone)
                Intent(activity, HomeActivity::class.java).also {
                    startActivity(it)
                    requireActivity().finish()
                }
            } else {
                // Show error message
                Toast.makeText(requireContext(), message, Toast.LENGTH_SHORT).show()
            }
        }

        auth = Firebase.auth
        oneTapClient = Identity.getSignInClient(requireContext())

        signInRequest = BeginSignInRequest.builder()
            .setGoogleIdTokenRequestOptions(
                BeginSignInRequest.GoogleIdTokenRequestOptions.builder()
                    .setSupported(true)
                    // Your server's client ID, not your Android client ID.
                    .setServerClientId(getString(R.string.web_client_id))
                    // Only show accounts previously used to sign in.
                    .setFilterByAuthorizedAccounts(false)
                    .build()
            )
            // Automatically sign in when exactly one credential is retrieved.
            .setAutoSelectEnabled(true)
            .build()

        val activityResultLauncher: ActivityResultLauncher<IntentSenderRequest> =
            registerForActivityResult(
                ActivityResultContracts.StartIntentSenderForResult()
            ) { result ->
                if (result.resultCode == Activity.RESULT_OK) {
                    try {
                        val credential = oneTapClient.getSignInCredentialFromIntent(result.data)
                        val idToken = credential.googleIdToken
                        if (idToken != null) {
                            val auth = Firebase.auth
                            val authCredential = GoogleAuthProvider.getCredential(idToken, null)
                            auth.signInWithCredential(authCredential)
                                .addOnCompleteListener(requireActivity()) { authTask ->
                                    if (authTask.isSuccessful) {
                                        val user: FirebaseUser? = auth.currentUser
                                        val sharedPreferences =
                                            requireContext().getSharedPreferences(
                                                "RememberLogin",
                                                Context.MODE_PRIVATE
                                            )
                                        sharedPreferences.edit().putBoolean("remember_me", true)
                                            .apply()

                                        Toast.makeText(
                                            context,
                                            "User created: ${user?.email}",
                                            Toast.LENGTH_LONG
                                        ).show()
                                        hideLoadingIndicator()
                                        showSuccessFeedback()
                                        saveUserData(user?.displayName!!, user.email!!, "")
                                        val intent = Intent(activity, HomeActivity::class.java)
                                        startActivity(intent)
                                    } else {
                                        Toast.makeText(
                                            context,
                                            "Error creating user: ${authTask.exception?.message}",
                                            Toast.LENGTH_LONG
                                        ).show()
                                    }
                                }
                        } else {
                            Toast.makeText(context, "Error: ID token is null", Toast.LENGTH_LONG)
                                .show()
                        }
                    } catch (e: Exception) {
                        Toast.makeText(context, e.message, Toast.LENGTH_LONG).show()
                    }

                } else {
                    Toast.makeText(context, "Signup failed", Toast.LENGTH_LONG).show()
                }
            }

        binding.tvBack.setOnClickListener {
            findNavController().popBackStack()
        }

        binding.signupGoogle.setOnClickListener {
            showLoadingIndicator()
            oneTapClient.beginSignIn(signInRequest)
                .addOnSuccessListener(requireActivity()) { result ->
                    try {
                        val intentSenderRequest =
                            IntentSenderRequest.Builder(result.pendingIntent.intentSender)
                                .build()

                        activityResultLauncher.launch(intentSenderRequest)
                    } catch (e: Exception) {
                        Toast.makeText(context, e.localizedMessage, Toast.LENGTH_LONG).show()
                    }
                }
                .addOnFailureListener(requireActivity()) { e ->
                    Toast.makeText(context, e.message, Toast.LENGTH_LONG).show()
                }
        }
        binding.tvSkip.setOnClickListener {
            val intent = Intent(activity, HomeActivity::class.java)
            startActivity(intent)
        }

        binding.tvAlreadyHaveAnAccount.setOnClickListener {
            findNavController().navigate(R.id.action_registerFragment_to_loginFragment)
        }

    }

    override fun onDestroy() {
        super.onDestroy()
        _binding = null
    }

    private fun saveUserData(name: String, email: String, phone: String) {
        val userId = FirebaseAuth.getInstance().currentUser?.uid
        if (userId != null) {
            val user = User(true, userId, email, name, phone)
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
