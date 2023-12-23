package com.finance.savvycents.ui.screens

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.RecyclerView
import com.finance.savvycents.R
import com.finance.savvycents.databinding.FragmentHomeBinding
import com.finance.savvycents.databinding.FragmentLoginBinding
import com.finance.savvycents.models.Transaction
import com.finance.savvycents.repository.TransactionRepositoryImpl
import com.finance.savvycents.ui.adapters.TransactionAdapter
import com.finance.savvycents.utilities.Resource
import com.finance.savvycents.viewmodels.TransactionViewModel
import com.firebase.ui.firestore.FirestoreRecyclerOptions
import com.google.firebase.firestore.FirebaseFirestore
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch

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
// Set up RecyclerView and Adapter
        val recyclerView: RecyclerView = view.findViewById(R.id.recyclerView)
        transactionAdapter = TransactionAdapter(/* pass your data here */)
        recyclerView.adapter = transactionAdapter

        // Observe the transactions LiveData
        viewModel.transactions.observe(viewLifecycleOwner) { result ->
            when (result) {
                is Resource.Success -> {
                    // Update the RecyclerView with the new list of transactions
                    result.data?.let { transactionAdapter.submitList(it) }
                    // Hide loading indicator or perform any other UI updates
                }
                is Resource.Error -> {
                    // Handle error, show error message, etc.
                }
                is Resource.Loading -> {
                    // Show loading indicator or perform any loading-related actions
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
            }
        }
    }

    override fun onStart() {
        super.onStart()
    }

    override fun onStop() {
        super.onStop()
    }
}