package com.finance.savvycents.ui.screens

import android.app.Activity
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
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import com.finance.savvycents.R
import com.finance.savvycents.databinding.FragmentLoginBinding
import com.finance.savvycents.models.User
import com.finance.savvycents.utilities.Resource
import com.finance.savvycents.utilities.Validator
import com.finance.savvycents.utilities.isUserLoggedIn
import com.finance.savvycents.utilities.saveUserData
import com.finance.savvycents.viewmodels.LoginViewModel
import com.google.android.gms.auth.api.identity.BeginSignInRequest
import com.google.android.gms.auth.api.identity.Identity
import com.google.android.gms.auth.api.identity.SignInClient
import com.google.android.gms.auth.api.identity.SignInCredential
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.GoogleAuthProvider
import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch

@AndroidEntryPoint
class LoginFragment : Fragment() {
    private var _binding: FragmentLoginBinding? = null
    private val binding get() = _binding!!
    private val viewModel: LoginViewModel by viewModels()
    private var isLoading = false
    private lateinit var oneTapClient: SignInClient
    private lateinit var signInRequest: BeginSignInRequest
    private lateinit var auth: FirebaseAuth

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentLoginBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setObservers()
        auth = FirebaseAuth.getInstance()

        binding.tvBack.setOnClickListener {
            findNavController().popBackStack()
        }

        binding.tvDontHaveAnAccount.setOnClickListener {
            findNavController().navigate(R.id.action_loginFragment_to_registerFragment)
        }

        binding.btLogin.setOnClickListener {

            val inputText = binding.etEmail.text.toString()
            if (isEmail(inputText)) {
                val validEmail = viewModel.validateEmail(inputText)

                if (validEmail is Validator.Error) {
                    Toast.makeText(context, validEmail.errorMsg, Toast.LENGTH_SHORT).show()
                    return@setOnClickListener
                }
                val action = LoginFragmentDirections.actionLoginFragmentToOtpFragment(
                    inputText,
                    "email",
                    "loginFragment"
                )
                findNavController().navigate(action)

            } else if (isPhoneNumber(inputText)) {
                val validPhone = viewModel.validatePhone(inputText)

                if (validPhone is Validator.Error) {
                    Toast.makeText(context, validPhone.errorMsg, Toast.LENGTH_SHORT).show()
                    return@setOnClickListener
                }
                val action = LoginFragmentDirections.actionLoginFragmentToOtpFragment(
                    inputText,
                    "phone",
                    "loginFragment"
                )
                findNavController().navigate(action)
            }
        }

        oneTapClient = Identity.getSignInClient(requireContext())

        signInRequest = BeginSignInRequest.builder()
            .setGoogleIdTokenRequestOptions(
                BeginSignInRequest.GoogleIdTokenRequestOptions.builder()
                    .setSupported(true)
                    .setServerClientId(getString(R.string.web_client_id))
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
                        val username = credential.id
                        if (idToken != null) {
                            auth.fetchSignInMethodsForEmail(username)
                                .addOnCompleteListener { fetchTask ->
                                    if (fetchTask.isSuccessful) {
                                        val signInMethods = fetchTask.result?.signInMethods
                                        if (!signInMethods.isNullOrEmpty()) {
                                            signInWithGoogleCredentials(credential)
                                        } else {
                                            createUserWithGoogleCredential(credential)
                                        }
                                    } else {
                                        // Handle fetch error
                                        Toast.makeText(
                                            context,
                                            "Error fetching sign-in methods",
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

        binding.tvForgotPass.setOnClickListener {
            findNavController().navigate(R.id.action_loginFragment_to_forgotPasswordFragment)
        }
    }

    private fun createUserWithGoogleCredential(credential: SignInCredential) {
        val idToken = credential.googleIdToken
        if (idToken != null) {
            val authCredential = GoogleAuthProvider.getCredential(idToken, null)
            auth.signInWithCredential(authCredential)
                .addOnCompleteListener(requireActivity()) { authTask ->
                    if (authTask.isSuccessful) {
                        hideLoadingIndicator()
                        auth.currentUser?.let { user ->

                            viewModel.saveLoginCredential(
                                User(
                                    isLoggedIn = true,
                                    userId = user.uid,
                                    email = user.email,
                                    name = user.displayName!!,
                                    phone = ""

                                )
                            )
                            saveUserData(requireContext(), user.displayName!!, user.email!!, "")
                        }

                        showSuccessFeedback()
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
            Toast.makeText(context, "Error: ID token is null", Toast.LENGTH_LONG).show()
        }
    }

    private fun signInWithGoogleCredentials(credential: SignInCredential) {
        val auth = Firebase.auth
        val idToken = credential.googleIdToken
        val authCredential = GoogleAuthProvider.getCredential(idToken, null)
        auth.signInWithCredential(authCredential)
            .addOnCompleteListener(requireActivity()) { authTask ->
                if (authTask.isSuccessful) {
                    auth.currentUser?.let { user ->

                        viewModel.saveLoginCredential(
                            User(
                                isLoggedIn = true,
                                email = user.email,
                                userId = user.uid,
                                name = user.displayName!!,
                                phone = "123"
                            )
                        )

                    }
                    showSuccessFeedback()
                    val intent = Intent(activity, HomeActivity::class.java)
                    startActivity(intent)
                } else {
                    Toast.makeText(
                        context,
                        "Error logging in: ${authTask.exception?.message}",
                        Toast.LENGTH_LONG
                    ).show()
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
        binding.btLogin.visibility = View.INVISIBLE
    }

    private fun hideLoadingIndicator() {
        isLoading = false
        // Hide the loading indicator (e.g., progress bar)
        binding.progressBar.visibility = View.GONE
        binding.btLogin.visibility = View.VISIBLE
    }

    private fun showSuccessFeedback() {
        // Show a success message to the user
        Toast.makeText(context, "Login successful!", Toast.LENGTH_SHORT).show()
    }

    private fun setObservers() {
        showLoadingIndicator()
        if (isUserLoggedIn()) {
            lifecycleScope.launch {

                viewModel.loginFlow.collect { it ->
                    val result = it ?: return@collect

                    hideLoadingIndicator()

                    when (result) {
                        is Resource.Error -> {
                            hideLoadingIndicator()
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
                                hideLoadingIndicator()
                                showSuccessFeedback()
                                val intent = Intent(activity, HomeActivity::class.java)
                                startActivity(intent)
                            }

                        }
                    }
                }
            }
        } else {

        }
    }

    private fun isEmail(input: String): Boolean {
        // Check if the input looks like an email address
        return android.util.Patterns.EMAIL_ADDRESS.matcher(input).matches()
    }

    private fun isPhoneNumber(input: String): Boolean {
        // Check if the input looks like a phone number
        return android.util.Patterns.PHONE.matcher(input).matches()
    }
}