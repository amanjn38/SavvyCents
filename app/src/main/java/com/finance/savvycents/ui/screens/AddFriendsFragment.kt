package com.finance.savvycents.ui.screens

import android.Manifest
import android.content.Intent
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
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.finance.savvycents.R
import com.finance.savvycents.databinding.FragmentAddFriendsBinding
import com.finance.savvycents.models.ContactEntity
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
                    binding.progressBar.visibility = View.GONE
                    val contacts = resource.data
                    val layoutManager = LinearLayoutManager(context)
                    binding.recyclerView.layoutManager = layoutManager
                    adapter = contacts?.let { ContactAdapter(it, requireContext(), this) }!!
                    binding.recyclerView.adapter = adapter
                    binding.searchView.setOnQueryTextListener(object :
                        SearchView.OnQueryTextListener {
                        override fun onQueryTextSubmit(query: String?): Boolean {
                            adapter.filter(query)
                            return true
                        }

                        override fun onQueryTextChange(newText: String?): Boolean {
                            adapter.filter(newText)
                            return true
                        }
                    })
                    // Show Next button
                    binding.nextButton.visibility = View.VISIBLE
                    binding.nextButton.setOnClickListener {
                        val selected = adapter.getSelectedContacts()
                        if (selected.isEmpty()) {
                            Toast.makeText(requireContext(), "Select at least one contact", Toast.LENGTH_SHORT).show()
                        } else {
                            val bundle = Bundle().apply {
                                putParcelableArrayList("selectedContacts", ArrayList(selected))
                            }
                            findNavController().navigate(R.id.action_addFriendsFragment_to_reviewSelectedContactsFragment, bundle)
                        }
                    }
                }

                is Resource.Error -> {
                    binding.progressBar.visibility = View.GONE
                    // Handle error state
                    val errorMessage = resource.message
                    Toast.makeText(context, "Error: $errorMessage", Toast.LENGTH_SHORT).show()
                }

                is Resource.Loading -> {
                    binding.progressBar.visibility = View.VISIBLE
                }

                is Resource.Idle -> {
                    binding.progressBar.visibility = View.GONE
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
        viewModel.loadContacts()
    }

    private fun sendInvite(contact: ContactEntity) {
        val intent = Intent(Intent.ACTION_SEND)
        intent.type = "text/plain"
        val message = "Hey! Join me on SavvyCents to manage expenses together. Download now: <app_link>"
        intent.putExtra(Intent.EXTRA_TEXT, message)
        startActivity(Intent.createChooser(intent, "Invite via"))
    }
}