package com.finance.savvycents.ui.screens

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import com.finance.savvycents.R
import com.finance.savvycents.databinding.ActivityAddTransactionDialogBoxBinding
import com.finance.savvycents.databinding.ActivityHomeBinding

class AddTransactionDialogBoxActivity : AppCompatActivity() {
    private lateinit var binding: ActivityAddTransactionDialogBoxBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityAddTransactionDialogBoxBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val sender = intent.getStringExtra("sender") ?: ""
        val messageBody = intent.getStringExtra("messageBody") ?: ""

        // Set data to TextViews
        binding.textViewSender.text = "Sender: $sender"
        binding.textViewMessageBody.text = "Message: $messageBody"
        binding.textViewAdditionalInfo.text = "Additional Info: Your additional info here"

        // Set up close button click listener
        binding.buttonClose.setOnClickListener {
            finish() // Close the activity (dialog)
        }
    }
}