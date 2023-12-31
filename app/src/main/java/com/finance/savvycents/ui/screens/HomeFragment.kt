package com.finance.savvycents.ui.screens

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.viewModels
import com.finance.savvycents.databinding.FragmentHomeBinding
import com.finance.savvycents.models.Transaction
import com.finance.savvycents.ui.adapters.TransactionAdapter
import com.finance.savvycents.utilities.Resource
import com.finance.savvycents.viewmodels.TransactionViewModel
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

        initTransactionAdapter()
        val uid = FirebaseAuth.getInstance().currentUser!!.uid
        val transaction = Transaction("testing", "testing", "testing", 10.0, "testing", "testing")

//        for(i in 1..19){
//            viewModel.addTransaction(uid, transaction)
//        }


        viewModel.getTransactions(uid)
        // Observe the transactions LiveData
        viewModel.transactions.observe(viewLifecycleOwner) { result ->
            when (result) {
                is Resource.Success -> {
                    // Update the RecyclerView with the new list of transactions
                    System.out.println("testingResult" + result.data)
                    result.data?.let { transactionAdapter.submitList(it) }
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

    private fun initTransactionAdapter() {

        if (this::transactionAdapter.isInitialized) {

            if (binding.recyclerView.adapter == null)
                binding.recyclerView.adapter = transactionAdapter

            return
        }


        transactionAdapter = TransactionAdapter()
        binding.recyclerView.adapter = transactionAdapter

    }

}