package com.finance.savvycents.ui.screens

import android.Manifest
import android.content.pm.PackageManager
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.appcompat.widget.SearchView
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.recyclerview.widget.LinearLayoutManager
import com.finance.savvycents.databinding.FragmentAddFriendsBinding
import com.finance.savvycents.ui.adapters.ContactAdapter
import com.finance.savvycents.ui.viewmodels.ContactViewModel
import com.finance.savvycents.utilities.Resource
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class AddFriendsFragment : Fragment() {
    private var _binding: FragmentAddFriendsBinding? = null
    private val binding get() = _binding!!
    private val viewModel: ContactViewModel by viewModels()
    private val READ_CONTACTS_PERMISSION_REQUEST = 1
    private lateinit var adapter: ContactAdapter
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = FragmentAddFriendsBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        // Check if the permission is already granted
        if (ContextCompat.checkSelfPermission(
                requireContext(),
                Manifest.permission.READ_CONTACTS
            ) == PackageManager.PERMISSION_GRANTED
        ) {
            // Permission already granted, proceed with your logic
            loadContacts()

        } else {
            // Permission not granted, request it
            requestContactsPermission()

        }
        viewModel.contacts.observe(viewLifecycleOwner) { resource ->
            when (resource) {
                is Resource.Success -> {
                    // Update UI with the retrieved contacts
                    // You can use RecyclerView or any other UI component
                    val contacts = resource.data
                    val layoutManager = LinearLayoutManager(context)
                    binding.recyclerView.layoutManager = layoutManager

                    adapter = contacts?.let { ContactAdapter(it) }!!
                    binding.recyclerView.adapter = adapter

                    binding.searchView.setOnQueryTextListener(object :
                        SearchView.OnQueryTextListener {
                        override fun onQueryTextSubmit(query: String?): Boolean {
                            return false
                        }

                        override fun onQueryTextChange(newText: String?): Boolean {
                            // Cancel the previous search job if it's still running
                            adapter.filter(newText)
                            return true
                        }
                    })

                }

                is Resource.Error -> {
                    // Handle error state
                    val errorMessage = resource.message
                    Toast.makeText(context, "Error: $errorMessage", Toast.LENGTH_SHORT).show()
                }

                is Resource.Loading -> {
                    // Handle loading state
                }

                is Resource.Idle -> {
                    // Handle idle state
                }
            }
        }

    }

    private fun requestContactsPermission() {
        // Request the permission
        ActivityCompat.requestPermissions(
            requireActivity(),
            arrayOf(Manifest.permission.READ_CONTACTS),
            READ_CONTACTS_PERMISSION_REQUEST
        )
    }

    // Handle the result of the permission request
    @Deprecated("Deprecated in Java")
    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        when (requestCode) {
            READ_CONTACTS_PERMISSION_REQUEST -> {
                System.out.println("testingContact3")

                if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    // Permission granted, proceed with your logic
                    loadContacts()
                } else {
                    // Permission denied, show a message or handle it accordingly
                    // You may want to explain why you need the permission
                    // and prompt the user again or handle the case when the user denies permission
                    // and doesn't want to be asked again.
                }
            }

            else -> {
                // Handle other permission requests if needed
                super.onRequestPermissionsResult(requestCode, permissions, grantResults)
            }
        }
    }

    private fun loadContacts() {
        System.out.println("testingContact4")

        viewModel.loadContacts()
    }

//    private fun setupSearch() {
//        // Set up the listener
//        binding.searchView.setOnQueryTextListener(object : SearchView.OnQueryTextListener {
//            override fun onQueryTextSubmit(query: String?): Boolean {
//                // Handle query submission if needed
//                return false
//            }
//
//            override fun onQueryTextChange(newText: String?): Boolean {
//                // Filter the adapter when the text changes
//                adapter.filter.filter(newText)
//                return true
//            }
//        })
//    }
}