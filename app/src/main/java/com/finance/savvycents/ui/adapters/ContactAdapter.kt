package com.finance.savvycents.ui.adapters

import android.content.Context
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.Typeface
import android.graphics.drawable.BitmapDrawable
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.CheckBox
import android.widget.ImageView
import android.widget.TextView
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.RecyclerView
import com.finance.savvycents.R
import com.finance.savvycents.models.ContactEntity
import java.util.Locale


class ContactAdapter(
    private val contacts: List<ContactEntity>,
    private val context: Context,
    private val fragment: Fragment
) :
    RecyclerView.Adapter<ContactAdapter.ContactViewHolder>() {
    private var filteredContacts: List<ContactEntity> = contacts

    var selectedContacts = mutableSetOf<ContactEntity>()

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ContactViewHolder {
        val itemView = LayoutInflater.from(parent.context)
            .inflate(R.layout.contact_item_layout, parent, false)
        return ContactViewHolder(itemView)
    }

    override fun onBindViewHolder(holder: ContactViewHolder, position: Int) {
        val contact = filteredContacts[position]

        // Set initials to Circular ImageView
        holder.imageInitials.background = getCircularDrawable(contact.name)

        // Set contact name
        holder.textName.text = contact.name

        holder.checkBox.isChecked = selectedContacts.contains(contact)
        holder.checkBox.setOnCheckedChangeListener { _, isChecked ->
            if (isChecked) selectedContacts.add(contact)
            else selectedContacts.remove(contact)
        }
    }

    private fun getCircularDrawable(name: String): BitmapDrawable {
        val bitmap = Bitmap.createBitmap(50, 50, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(bitmap)

        val paint = Paint()
        paint.color = Color.parseColor("#3498db") // Background color
        canvas.drawCircle(25f, 25f, 25f, paint)

        paint.color = Color.WHITE // Text color
        paint.textSize = 20f
        paint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
        paint.textAlign = Paint.Align.CENTER

        val xPos = canvas.width / 2
        val yPos = (canvas.height / 2 - (paint.descent() + paint.ascent()) / 2)
        canvas.drawText(name.getInitials().uppercase(Locale.ROOT), xPos.toFloat(), yPos, paint)

        return BitmapDrawable(bitmap)
    }

    override fun getItemCount(): Int {
        return filteredContacts.size
    }

    private fun String.getInitials(): String {
        val words = this.split(" ")
        return if (words.size >= 2) {
            "${words[0][0]}${words[1][0]}"
        } else {
            "${words[0][0]}"
        }
    }

    inner class ContactViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val imageInitials: ImageView = itemView.findViewById(R.id.imageInitials)
        val textName: TextView = itemView.findViewById(R.id.textName)
        val checkBox: CheckBox = itemView.findViewById(R.id.checkBox)
        val parent: ConstraintLayout = itemView.findViewById(R.id.parent)
    }

    fun filter(query: String?) {
        filteredContacts = if (query.isNullOrBlank()) {
            contacts
        } else {
            val searchText = query.lowercase(Locale.getDefault())
            contacts.filter {
                it.name.lowercase(Locale.getDefault()).contains(searchText) ||
                it.email.lowercase(Locale.getDefault()).contains(searchText) ||
                it.phoneNumber.lowercase(Locale.getDefault()).contains(searchText)
            }
        }
        notifyDataSetChanged()
    }

    fun getSelectedContacts(): List<ContactEntity> = selectedContacts.toList()
}
