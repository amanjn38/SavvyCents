package com.finance.savvycents.ui.adapters

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.finance.savvycents.R
import com.finance.savvycents.models.ContactEntity

class ReviewContactsAdapter(private val contacts: List<ContactEntity>) : RecyclerView.Adapter<ReviewContactsAdapter.ViewHolder>() {
    class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val name: TextView = itemView.findViewById(R.id.textName)
        val email: TextView = itemView.findViewById(R.id.textEmail)
        val phone: TextView = itemView.findViewById(R.id.textPhone)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_review_contact, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val contact = contacts[position]
        holder.name.text = contact.name
        holder.email.text = contact.email
        holder.phone.text = contact.phoneNumber
    }

    override fun getItemCount(): Int = contacts.size
}
