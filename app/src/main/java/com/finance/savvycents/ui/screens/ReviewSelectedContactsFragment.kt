package com.finance.savvycents.ui.screens

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.finance.savvycents.databinding.FragmentReviewSelectedContactsBinding
import com.finance.savvycents.models.ContactEntity
import com.finance.savvycents.repository.FriendsRepository
import com.finance.savvycents.ui.adapters.ReviewContactsAdapter
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch
import javax.inject.Inject

@AndroidEntryPoint
class ReviewSelectedContactsFragment : Fragment() {
    private var _binding: FragmentReviewSelectedContactsBinding? = null
    private val binding get() = _binding!!
    private lateinit var selectedContacts: List<ContactEntity>

    @Inject
    lateinit var friendsRepository: FriendsRepository

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = FragmentReviewSelectedContactsBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        selectedContacts = arguments?.getParcelableArrayList("selectedContacts") ?: emptyList()
        val adapter = ReviewContactsAdapter(selectedContacts)
        binding.recyclerView.layoutManager = LinearLayoutManager(context)
        binding.recyclerView.adapter = adapter

        binding.finishButton.setOnClickListener {
            addFriendsAndInvite()
        }
    }

    private fun addFriendsAndInvite() {
        binding.finishButton.isEnabled = false
        binding.finishButton.text = "Adding..."
        lifecycleScope.launch {
            selectedContacts.forEach { contact ->
                friendsRepository.addFriend(
                    com.finance.savvycents.models.FriendEntity(
                        name = contact.name,
                        phoneNumber = contact.phoneNumber,
                        email = contact.email,
                        isRegisteredUser = false,
                        inviteSent = false
                    )
                )
            }
            showInviteDialog()
        }
    }

    private fun showInviteDialog() {
        AlertDialog.Builder(requireContext())
            .setTitle("Send SMS Invite?")
            .setMessage("Would you like to send a text message to your new friends inviting them to join SavvyCents?")
            .setPositiveButton("Send") { _, _ ->
                // Use ACTION_VIEW with sms: URI for a normal SMS
                val smsIntent = Intent(Intent.ACTION_VIEW)
                smsIntent.data = Uri.parse("sms:")
                val message = "Hey! Tap the link to add me as a friend on SavvyCents: <app_link>"
                smsIntent.putExtra("sms_body", message)
                try {
                    startActivity(smsIntent)
                } catch (e: Exception) {
                    Toast.makeText(requireContext(), "No SMS app found", Toast.LENGTH_SHORT).show()
                }
                Toast.makeText(requireContext(), "Friends added!", Toast.LENGTH_SHORT).show()
                findNavController().popBackStack()
            }
            .setNegativeButton("No") { _, _ ->
                Toast.makeText(requireContext(), "Friends added!", Toast.LENGTH_SHORT).show()
                findNavController().popBackStack()
            }
            .setCancelable(false)
            .show()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
