package com.finance.savvycents.ui.screens

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import com.finance.savvycents.R
import com.finance.savvycents.databinding.FragmentHomeBinding
import com.finance.savvycents.models.Transaction
import com.finance.savvycents.ui.adapters.TransactionAdapter
import com.finance.savvycents.utilities.Resource
import com.finance.savvycents.ui.viewmodels.TransactionViewModel
import com.google.firebase.auth.FirebaseAuth
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class HomeFragment : Fragment() {
    private var _binding: FragmentHomeBinding? = null
    private val binding get() = _binding!!
    private val viewModel: TransactionViewModel by viewModels()
    private lateinit var transactionAdapter: TransactionAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = FragmentHomeBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val user = FirebaseAuth.getInstance().currentUser
        if (user != null && !user.isEmailVerified) {
            showEmailVerificationDialog(user)
        } else {
            initTransactionAdapter()
            user?.let { user ->
                val uid = user.uid

//                for(i in 1..19){
//                    viewModel.addTransaction(uid, transaction)
//                }

                viewModel.getTransactions(uid)
                // Observe the transactions LiveData
                viewModel.transactions.observe(viewLifecycleOwner) { result ->
                    when (result) {
                        is Resource.Success -> {
                            // Update the RecyclerView with the new list of transactions
                            System.out.println("testing" + result.data)
                            result.data?.let { transactionAdapter.submitList(it) }
                        }
                        is Resource.Error -> {
                            // Handle error, show error message, etc.
                        }
                        is Resource.Loading -> {
                            // Show loading indicator or perform any loading-related actions
                        }
                        is Resource.Idle -> {
                            // No-op for Idle state
                        }
                    }
                }

                // Observe the addTransactionStatus LiveData
                viewModel.addTransactionStatus.observe(viewLifecycleOwner) { result ->
                    when (result) {
                        is Resource.Success -> {
                            // Transaction added successfully, handle success case if needed
                        }
                        is Resource.Error -> {
                            // Handle error, show error message, etc.
                        }
                        is Resource.Loading -> {
                            // Show loading indicator or perform any loading-related actions
                        }
                        is Resource.Idle -> {
                            // No-op for Idle state
                        }
                    }
                }
            } ?: run {
                // User not logged in, navigate back to login
                findNavController().navigate(R.id.action_homeFragment_to_loginFragment)
            }
        }
    }

    override fun onStart() {
        super.onStart()
    }

    override fun onStop() {
        super.onStop()
    }

    private fun initTransactionAdapter() {
        if (this::transactionAdapter.isInitialized) {
            if (binding.recyclerView.adapter == null)
                binding.recyclerView.adapter = transactionAdapter
            return
        }
        transactionAdapter = TransactionAdapter()
        binding.recyclerView.adapter = transactionAdapter
    }

    private fun showEmailVerificationDialog(user: com.google.firebase.auth.FirebaseUser) {
        val dialog = androidx.appcompat.app.AlertDialog.Builder(requireContext())
            .setTitle("Email Verification Required")
            .setMessage("Please verify your email address to continue using SavvyCents. Check your inbox and click the verification link. If you didn't receive an email, you can resend it.")
            .setPositiveButton("Resend Email") { _, _ ->
                user.sendEmailVerification()
                android.widget.Toast.makeText(requireContext(), "Verification email sent.", android.widget.Toast.LENGTH_SHORT).show()
            }
            .setNegativeButton("I've Verified") { _, _ ->
                user.reload().addOnCompleteListener { task ->
                    if (task.isSuccessful && user.isEmailVerified) {
                        android.widget.Toast.makeText(requireContext(), "Thank you for verifying!", android.widget.Toast.LENGTH_SHORT).show()
                    } else {
                        android.widget.Toast.makeText(requireContext(), "Still not verified. Please check your email.", android.widget.Toast.LENGTH_SHORT).show()
                        showEmailVerificationDialog(user)
                    }
                }
            }
            .setCancelable(false)
            .create()
        dialog.show()
    }
}