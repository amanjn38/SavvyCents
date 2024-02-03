package com.finance.savvycents.utilities

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.provider.Telephony
import android.telephony.SmsMessage
import android.widget.Toast
import androidx.core.content.ContextCompat.startActivity
import com.finance.savvycents.ui.screens.AddTransactionDialogBoxActivity

class SmsReceiver : BroadcastReceiver() {

    override fun onReceive(context: Context, intent: Intent) {
        if (Telephony.Sms.Intents.SMS_RECEIVED_ACTION == intent.action) {
            Toast.makeText(context, "Message format is correct  5", Toast.LENGTH_SHORT).show()

            val bundle = intent.extras
            if (bundle != null) {
                Toast.makeText(context, "Message format is correct", Toast.LENGTH_SHORT).show()

                val pdus = bundle.get("pdus") as Array<*>
                val messages = arrayOfNulls<SmsMessage>(pdus.size)
                for (i in pdus.indices) {
                    messages[i] = SmsMessage.createFromPdu(pdus[i] as ByteArray)
                }

                if (messages.isNotEmpty()) {
                    val sender = messages[0]?.originatingAddress
                    val messageBody = StringBuilder()
                    Toast.makeText(context, "Message format is correct 1", Toast.LENGTH_SHORT).show()

                    for (message in messages) {
                        Toast.makeText(context, "Message format is correct 10", Toast.LENGTH_SHORT).show()
                        messageBody.append(message?.messageBody)
                        Toast.makeText(context, "Message format is correct 11", Toast.LENGTH_SHORT).show()
                    }

                    // Define a more generalized regex pattern for transaction messages
                    val transactionRegex = """(?i)A/c\s*(\S+)\s*(credited|debited)\s*.*?INR\s*([\d.]+).*?(\d{2}-\S+-\d{2}\s*\d{2}:\d{2}).*?(UPI|Card|Bank A/c)""".toRegex()
                    Toast.makeText(context, "Message format is correct 8" + messageBody , Toast.LENGTH_SHORT).show()

                    // Match the message against the regex
                    val matchResult = transactionRegex.find(messageBody)
                    Toast.makeText(context, "Message format is correct 2" + matchResult , Toast.LENGTH_SHORT).show()

                    if (matchResult != null) {
                        Toast.makeText(context, "Message format is correct 6" + matchResult , Toast.LENGTH_SHORT).show()

                        // Extract relevant information from the matched result
                        val accountNumber = matchResult.groupValues[1]
                        val transactionType = matchResult.groupValues[2]
                        val amount = matchResult.groupValues[3]
                        val transactionDateTime = matchResult.groupValues[4]
                        val modeOfPayment = matchResult.groupValues[5]

                        // Open dialog or activity to handle the extracted information
                        val dialogIntent = Intent(context, AddTransactionDialogBoxActivity::class.java)
                        dialogIntent.putExtra("sender", sender)
                        dialogIntent.putExtra("accountNumber", accountNumber)
                        dialogIntent.putExtra("transactionType", transactionType)
                        dialogIntent.putExtra("amount", amount)
                        dialogIntent.putExtra("transactionDateTime", transactionDateTime)
                        dialogIntent.putExtra("modeOfPayment", modeOfPayment)
                        dialogIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                        context.startActivity(dialogIntent)
                    }else{
                        Toast.makeText(context, "Message format is not correct", Toast.LENGTH_SHORT).show()
                    }
                }
            }
        }
    }
}
