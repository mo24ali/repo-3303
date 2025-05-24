package com.example.mycolloc.Adapter

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.mycolloc.R
import com.example.mycolloc.databinding.ItemBidUserBinding
import com.example.mycolloc.model.Bid
import com.google.firebase.database.FirebaseDatabase

class OfferBidsAdapter(
    private val context: Context,
    private val bids: List<Bid>,
    private val onStatusChanged: () -> Unit
) : RecyclerView.Adapter<OfferBidsAdapter.BidViewHolder>() {

    inner class BidViewHolder(val binding: ItemBidUserBinding) :
        RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): BidViewHolder {
        val binding = ItemBidUserBinding.inflate(
            LayoutInflater.from(parent.context), parent, false
        )
        return BidViewHolder(binding)
    }

    override fun onBindViewHolder(holder: BidViewHolder, position: Int) {
        val bid = bids[position]

        holder.binding.offerAmount.text = "Proposé : ${bid.amount} MAD"
        holder.binding.bidTime.text = getTimeAgo(bid.timestamp)

        Glide.with(context)
            .load(R.drawable.acount)
            .into(holder.binding.userAvatar)

        // 🔁 Récupère le nom complet de l'utilisateur
        val userRef = FirebaseDatabase.getInstance().getReference("users").child(bid.userId)
        userRef.get().addOnSuccessListener { snapshot ->
            val firstName = snapshot.child("firstName").getValue(String::class.java) ?: ""
            val lastName = snapshot.child("lastName").getValue(String::class.java) ?: ""
            val fullName = "$firstName $lastName"
            holder.binding.userName.text = fullName

            val phoneNumber = snapshot.child("phoneNumber").getValue(String::class.java)

            // 📞 Bouton d’appel
            holder.binding.callIcon.setOnClickListener {
                if (!phoneNumber.isNullOrEmpty()) {
                    val intent = Intent(Intent.ACTION_DIAL, Uri.parse("tel:$phoneNumber"))
                    context.startActivity(intent)
                } else {
                    Toast.makeText(context, "Numéro non disponible", Toast.LENGTH_SHORT).show()
                }
            }
        }

        // ✅ Bouton Accept avec confirmation
        holder.binding.acceptButton.setOnClickListener {
            AlertDialog.Builder(context)
                .setTitle("Confirmer l'acceptation")
                .setMessage("Voulez-vous accepter cette offre ?")
                .setPositiveButton("Accepter") { _, _ ->
                    updateBidStatus(bid.bidId, "accepted")
                }
                .setNegativeButton("Annuler", null)
                .show()
        }

        // ✅ Bouton Decline avec confirmation
        holder.binding.declineButton.setOnClickListener {
            AlertDialog.Builder(context)
                .setTitle("Confirmer le refus")
                .setMessage("Voulez-vous refuser cette offre ?")
                .setPositiveButton("Refuser") { _, _ ->
                    updateBidStatus(bid.bidId, "declined")
                }
                .setNegativeButton("Annuler", null)
                .show()
        }
    }


    override fun getItemCount(): Int = bids.size

    private fun updateBidStatus(bidId: String, status: String) {
        val ref = FirebaseDatabase.getInstance().getReference("bids").child(bidId)
        ref.child("status").setValue(status)
            .addOnSuccessListener {
                Toast.makeText(context, "Bid $status", Toast.LENGTH_SHORT).show()
                onStatusChanged()
            }
            .addOnFailureListener {
                Toast.makeText(context, "Erreur de mise à jour", Toast.LENGTH_SHORT).show()
            }
    }

    private fun getTimeAgo(timestamp: Long): String {
        val diff = System.currentTimeMillis() - timestamp
        val minutes = diff / 60000
        return if (minutes < 60) "$minutes min ago" else "${minutes / 60}h ago"
    }
}