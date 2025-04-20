package com.finance.savvycents.ui.screens

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.IntentSenderRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import com.finance.savvycents.R
import com.finance.savvycents.databinding.FragmentLoginBinding
import com.finance.savvycents.ui.viewmodels.LoginViewModel
import com.finance.savvycents.utilities.Resource
import com.google.android.gms.auth.api.identity.BeginSignInRequest
import com.google.android.gms.auth.api.identity.Identity
import com.google.android.gms.auth.api.identity.SignInClient
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
    private lateinit var oneTapClient: SignInClient
    private lateinit var signInRequest: BeginSignInRequest
    private lateinit var auth: FirebaseAuth
    private lateinit var googleSignInLauncher: ActivityResultLauncher<IntentSenderRequest>

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentLoginBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        auth = Firebase.auth
        setupGoogleSignIn()

        // Password field is always visible now, no need to toggle visibility
        binding.btLogin.text = "Login"

        binding.btLogin.setOnClickListener {
            handleLoginClick()
        }

        binding.tvDontHaveAnAccount.setOnClickListener {
            findNavController().navigate(R.id.action_loginFragment_to_registerFragment)
        }

        binding.ivGoogle.setOnClickListener {
            beginGoogleSignIn()
        }

        binding.tvBack.setOnClickListener {
            findNavController().popBackStack()
        }

        binding.tvSkip.setOnClickListener {
            Intent(
                activity,
                HomeActivity::class.java
            ).also {
                startActivity(it)
                requireActivity().finish()
            }
        }

        binding.tvForgotPassword.setOnClickListener {
            findNavController().navigate(R.id.action_loginFragment_to_forgotPasswordFragment)
        }

        observeLoginFlow()
    }

    private fun handleLoginClick() {
        val email = binding.etEmail.text.toString().trim()
        val password = binding.etPassword.text.toString()
        val emailValidation = viewModel.validateEmail(email)
        if (emailValidation is com.finance.savvycents.utilities.Validator.Error) {
            Toast.makeText(context, emailValidation.errorMsg, Toast.LENGTH_SHORT).show()
            return
        }
        val passwordValidation = viewModel.validatePassword(password)
        if (passwordValidation is com.finance.savvycents.utilities.Validator.Error) {
            Toast.makeText(context, passwordValidation.errorMsg, Toast.LENGTH_SHORT).show()
            return
        }
        // Check email sign-in methods
        showLoading(true)
        viewModel.checkEmail(email)
        viewModel.emailSignInMethods.observe(viewLifecycleOwner) { methods ->
            showLoading(false)
            if (methods.isNullOrEmpty()) {
                Toast.makeText(requireContext(), "Email not registered. Please sign up.", Toast.LENGTH_SHORT).show()
            } else if (methods.contains("google.com")) {
                Toast.makeText(requireContext(), "This email is registered via Google Sign-In. Please use Google login.", Toast.LENGTH_LONG).show()
            } else if (methods.contains("password")) {
                viewModel.loginUser(email, password)
            }
        }
    }

    private fun setupGoogleSignIn() {
        oneTapClient = Identity.getSignInClient(requireContext())

        signInRequest = BeginSignInRequest.builder()
            .setGoogleIdTokenRequestOptions(
                BeginSignInRequest.GoogleIdTokenRequestOptions.builder()
                    .setSupported(true)
                    .setServerClientId(getString(R.string.web_client_id))
                    .setFilterByAuthorizedAccounts(false)
                    .build()
            )
            .setAutoSelectEnabled(true)
            .build()

        googleSignInLauncher = registerForActivityResult(
            ActivityResultContracts.StartIntentSenderForResult()
        ) { result ->
            hideLoadingIndicator() // Hide progress bar as soon as result is received
            if (result.resultCode == Activity.RESULT_OK) {
                try {
                    val credential = oneTapClient.getSignInCredentialFromIntent(result.data)
                    val idToken = credential.googleIdToken
                    if (idToken != null) {
                        val firebaseCredential = GoogleAuthProvider.getCredential(idToken, null)
                        auth.signInWithCredential(firebaseCredential)
                            .addOnCompleteListener(requireActivity()) { task ->
                                if (task.isSuccessful) {
                                    val user = auth.currentUser
                                    if (user != null) {
                                        viewModel.checkUserExists(user.uid)
                                            .observe(viewLifecycleOwner) { exists ->
                                                if (exists) {
                                                    Intent(
                                                        activity,
                                                        HomeActivity::class.java
                                                    ).also {
                                                        startActivity(it)
                                                        requireActivity().finish()
                                                    }
                                                } else {
                                                    findNavController().navigate(R.id.registerFragment)
                                                }
                                            }
                                    }
                                } else {
                                    Toast.makeText(
                                        context,
                                        "Google sign in failed: ${task.exception?.message}",
                                        Toast.LENGTH_LONG
                                    ).show()
                                }
                            }
                    }
                } catch (e: Exception) {
                    Toast.makeText(
                        context,
                        "Google sign in failed: ${e.message}",
                        Toast.LENGTH_LONG
                    ).show()
                }
            }
        }
    }

    private fun beginGoogleSignIn() {
        showLoading(true) // Show progress bar immediately when Google sign-in starts
        oneTapClient.beginSignIn(signInRequest)
            .addOnSuccessListener { result ->
                try {
                    val intentSenderRequest =
                        IntentSenderRequest.Builder(result.pendingIntent).build()
                    googleSignInLauncher.launch(intentSenderRequest)
                } catch (e: Exception) {
                    hideLoadingIndicator() // Hide if failed
                    Toast.makeText(
                        context,
                        "Google sign in failed: ${e.message}",
                        Toast.LENGTH_LONG
                    ).show()
                }
            }
            .addOnFailureListener { e ->
                hideLoadingIndicator() // Hide if failed
                Toast.makeText(
                    context,
                    "Google sign in failed: ${e.message}",
                    Toast.LENGTH_LONG
                ).show()
            }
    }

    private fun showLoadingIndicator() {
        binding.btLogin.isEnabled = false
        binding.ivGoogle.isEnabled = false
    }

    private fun hideLoadingIndicator() {
        binding.btLogin.isEnabled = true
        binding.ivGoogle.isEnabled = true
    }

    private fun showLoading(show: Boolean) {
        binding.progressBar.visibility = if (show) View.VISIBLE else View.GONE
        binding.btLogin.isEnabled = !show
        binding.etEmail.isEnabled = !show
        binding.etPassword.isEnabled = !show
    }

    private fun observeLoginFlow() {
        viewLifecycleOwner.lifecycleScope.launch {
            viewModel.loginFlow.collect { result ->
                when (result) {
                    is Resource.Loading -> {
                        showLoading(true)
                    }
                    is Resource.Success -> {
                        val user = FirebaseAuth.getInstance().currentUser
                        if (user != null && !user.isEmailVerified) {
                            FirebaseAuth.getInstance().signOut()
                            showLoading(false)
                            showEmailNotVerifiedDialog(user)
                        } else {
                            showLoading(false)
                            val intent = Intent(requireContext(), HomeActivity::class.java)
                            startActivity(intent)
                            requireActivity().finish()
                        }
                    }
                    is Resource.Error -> {
                        showLoading(false)
                        val message = result.message
                        val displayMsg = if (message != null && message.contains("The password is invalid or user doesn't have a password")) {
                            "The password is invalid or user doesn't exist"
                        } else {
                            message ?: "Login failed"
                        }
                        Toast.makeText(requireContext(), displayMsg, Toast.LENGTH_SHORT).show()
                    }
                    null -> {
                        showLoading(false)
                    }
                    is Resource.Idle<*> -> { /* No-op or reset UI if needed */ }
                }
            }
        }
    }

    private fun showEmailNotVerifiedDialog(user: com.google.firebase.auth.FirebaseUser) {
        androidx.appcompat.app.AlertDialog.Builder(requireContext())
            .setTitle("Email Not Verified")
            .setMessage("Please verify your email before logging in.")
            .setCancelable(false)
            .setPositiveButton("OK") { dialog, _ ->
                dialog.dismiss()
            }
            .setNegativeButton("Resend Email") { dialog, _ ->
                user.sendEmailVerification()
                Toast.makeText(requireContext(), "Verification email sent!", Toast.LENGTH_SHORT).show()
                dialog.dismiss()
            }
            .show()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}