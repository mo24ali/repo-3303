package com.example.mycolloc.Adapter

import android.content.Intent
import android.net.Uri
import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.Toast
import androidx.core.content.ContextCompat.startActivity
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.mycolloc.R
import com.example.mycolloc.databinding.ItemUserBidtoofferBinding
import com.example.mycolloc.model.Bid
import com.google.firebase.database.FirebaseDatabase

class BidAdapter(private val bidList: List<Bid>) :
    RecyclerView.Adapter<BidAdapter.BidViewHolder>() {

    inner class BidViewHolder(val binding: ItemUserBidtoofferBinding) : RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): BidViewHolder {
        val binding = ItemUserBidtoofferBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return BidViewHolder(binding)
    }

    override fun onBindViewHolder(holder: BidViewHolder, position: Int) {
        val bid = bidList[position]
        val context = holder.binding.root.context
        holder.binding.callOwner.setOnClickListener {
            val userId = bid.userId
            val userRef = FirebaseDatabase.getInstance().getReference("users").child(userId)

            userRef.get().addOnSuccessListener { snapshot ->
                val phoneNumber = snapshot.child("phoneNumber").getValue(String::class.java)
                if (!phoneNumber.isNullOrEmpty()) {
                    val intent = Intent(Intent.ACTION_DIAL)
                    intent.data = Uri.parse("tel:$phoneNumber")
                    context.startActivity(intent)
                } else {
                    Toast.makeText(context, "Numéro de téléphone introuvable", Toast.LENGTH_SHORT).show()
                }
            }.addOnFailureListener {
                Toast.makeText(context, "Erreur lors de la récupération du numéro", Toast.LENGTH_SHORT).show()
            }
        }


        holder.binding.bidAmount.text = "Offered: ${bid.amount} MAD"
        //try please in this line below to modify bckground of status based on status retrived
        holder.binding.bidStatus.text = when (bid.status) {
            "accepted" -> "Accepted"
            "declined" -> "Declined"
            else -> "Pending"
        }

        when (bid.status) {
            "accepted" -> {
                holder.binding.bidStatus.setBackgroundResource(R.drawable.status_accepted_bg)
            }
            "declined" -> {
                holder.binding.bidStatus.setBackgroundResource(R.drawable.status_rejected_bg)
            }
            else -> {
                holder.binding.bidStatus.setBackgroundResource(R.drawable.status_pending_bg)
            }
        }
        holder.binding.ownerRole.text = "Propriétaire de la colocation"

        Glide.with(context)
            .load(R.drawable.acount)
            .into(holder.binding.ownerAvatar)

        fetchOwnerName(bid.userId) { fullName ->
            holder.binding.ownerName.text = fullName
        }

        fetchOfferTitle(bid.offerId) { title ->
            holder.binding.offerTitle.text = title
        }
    }

    private fun fetchOwnerName(userId: String, callback: (String) -> Unit) {
        val userRef = com.google.firebase.database.FirebaseDatabase.getInstance()
            .getReference("users").child(userId)

        userRef.get().addOnSuccessListener { snapshot ->
            val firstName = snapshot.child("firstName").getValue(String::class.java) ?: ""
            val lastName = snapshot.child("lastName").getValue(String::class.java) ?: ""
            callback("$firstName $lastName")
        }.addOnFailureListener {
            callback("Propriétaire")
        }
    }


    private fun fetchOfferTitle(offerId: String, callback: (String) -> Unit) {
        val offerRef = com.google.firebase.database.FirebaseDatabase.getInstance()
            .getReference("offers").child(offerId)

        offerRef.get().addOnSuccessListener { snapshot ->
            val title = snapshot.child("title").getValue(String::class.java) ?: "Annonce"
            callback(title)
        }.addOnFailureListener {
            callback("Annonce")
        }
    }


    override fun getItemCount(): Int = bidList.size
}
