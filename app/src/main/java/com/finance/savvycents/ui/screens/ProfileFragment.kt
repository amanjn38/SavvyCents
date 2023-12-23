package com.finance.savvycents.ui.screens

import android.app.AlertDialog
import android.content.Context
import android.content.DialogInterface
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import com.finance.savvycents.R
import com.finance.savvycents.databinding.FragmentProfileBinding
import com.finance.savvycents.viewmodels.LoginViewModel
import com.google.firebase.auth.FirebaseAuth
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class ProfileFragment : Fragment() {
    private var _binding: FragmentProfileBinding? = null
    private val binding get() = _binding!!
    private lateinit var auth: FirebaseAuth
    private val viewModel: LoginViewModel by viewModels()
    private var source: String? = null


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = FragmentProfileBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        auth = FirebaseAuth.getInstance()
        source = arguments?.getString("source")

        val currentUser = auth.currentUser
        currentUser?.let {
            val name = currentUser.displayName
            val email = currentUser.email
            binding.tvName.text = name
            binding.tvEmail.text = email
        }

        binding.btLogout.setOnClickListener {
            showLogoutConfirmationDialog()
        }

        binding.tvBack.setOnClickListener {
//            if (source.equals("FragmentSearch")) {
//                findNavController().navigate(R.id.action_fragmentProfile_to_fragmentSearch)
//            } else if (source.equals("FragmentResult")) {
//                findNavController().navigate(R.id.action_fragmentProfile_to_fragmentResult)
//            }
            findNavController().navigateUp()
        }
    }

    override fun onDestroy() {
        super.onDestroy()

        _binding = null
    }


    private fun showLogoutConfirmationDialog() {
        val sharedPreferences =
            requireContext().getSharedPreferences("RememberLogin", Context.MODE_PRIVATE)

        // Create an AlertDialog.Builder instance
        val builder = AlertDialog.Builder(requireContext())

        // Set the dialog title and message
        builder.setTitle("Logout")
            .setMessage("Are you sure you want to logout?")

        // Set the positive button and its click listener
        builder.setPositiveButton("Yes") { _: DialogInterface, _: Int ->
            // Clear the shared preference
            if (sharedPreferences.contains("remember_me")) {
                sharedPreferences.edit().remove("remember_me").apply()
            }
            viewModel.logout()
            findNavController().navigate(R.id.action_profileFragment_to_loginFragment)
            Toast.makeText(requireContext(), "Logged out successfully", Toast.LENGTH_SHORT).show()
        }

        // Set the negative button and its click listener
        builder.setNegativeButton("No") { dialogInterface: DialogInterface, _: Int ->
            // Dismiss the dialog if the user chooses not to logout
            dialogInterface.dismiss()
        }

        // Create and show the AlertDialog
        val alertDialog: AlertDialog = builder.create()
        alertDialog.show()

    }

}