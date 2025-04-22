package com.finance.savvycents.ui.adapters

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.finance.savvycents.R
import com.finance.savvycents.models.FriendEntity

class FriendListAdapter(private var friends: List<FriendEntity>) : RecyclerView.Adapter<FriendListAdapter.FriendViewHolder>() {
    class FriendViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val name: TextView = itemView.findViewById(R.id.friendName)
        val email: TextView = itemView.findViewById(R.id.friendEmail)
        val phone: TextView = itemView.findViewById(R.id.friendPhone)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): FriendViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_friend, parent, false)
        return FriendViewHolder(view)
    }

    override fun onBindViewHolder(holder: FriendViewHolder, position: Int) {
        val friend = friends[position]
        holder.name.text = friend.name
        holder.email.text = friend.email
        holder.phone.text = friend.phoneNumber
    }

    override fun getItemCount(): Int = friends.size

    fun updateFriends(newFriends: List<FriendEntity>) {
        friends = newFriends
        notifyDataSetChanged()
    }
}
