package com.example.mycolloc.ui.home

import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.Toast
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.mycolloc.R
import com.example.mycolloc.databinding.ItemOfferBinding
import com.example.mycolloc.model.Offer
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import java.text.NumberFormat
import java.util.Locale

class OffersAdapter(
    private val onOfferClick: (Offer) -> Unit
) : ListAdapter<Offer, OffersAdapter.OfferViewHolder>(OfferDiffCallback()) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): OfferViewHolder {
        val binding = ItemOfferBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )
        return OfferViewHolder(binding)
    }

    override fun onBindViewHolder(holder: OfferViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    inner class OfferViewHolder(
        private val binding: ItemOfferBinding
    ) : RecyclerView.ViewHolder(binding.root) {

        init {
            binding.root.setOnClickListener {
                //val position = bindingAdapterPosition
                val position = adapterPosition
                if (position != RecyclerView.NO_POSITION) {
                    onOfferClick(getItem(position))
                }
            }
        }

        fun bind(offer: Offer) {
            val context = binding.root.context
            val currentUser = FirebaseAuth.getInstance().currentUser
            val database = FirebaseDatabase.getInstance()

            binding.apply {
                offerPrice.text = "${offer.price.toInt()} DH / mois"
                offerLocation.text = offer.location?.city!!.ifBlank { "Location not specified" }
                offerCategory.text = offer.category.ifBlank { "Uncategorized" }

                Glide.with(context)
                    .load(offer.imageUrl.ifBlank { R.drawable.sample_apartment })
                    .placeholder(R.drawable.sample_apartment)
                    .into(offerImage)

                // === FAVORIS ===
                if (currentUser != null) {
                    val favRef = database.getReference("favorites")
                        .child(currentUser.uid)
                        .child(offer.id)

                    favRef.addListenerForSingleValueEvent(object : ValueEventListener {
                        override fun onDataChange(snapshot: DataSnapshot) {
                            var isFavorite = snapshot.exists()
                            btnFavorite.setImageResource(
                                if (isFavorite) R.drawable.ic_heart_filled else R.drawable.ic_heart_outlined
                            )

                            btnFavorite.setOnClickListener {
                                isFavorite = !isFavorite
                                if (isFavorite) {
                                    favRef.setValue(offer).addOnSuccessListener {
                                        btnFavorite.setImageResource(R.drawable.ic_heart_filled)
                                        Toast.makeText(context, "Ajouté aux favoris", Toast.LENGTH_SHORT).show()
                                    }
                                } else {
                                    favRef.removeValue().addOnSuccessListener {
                                        btnFavorite.setImageResource(R.drawable.ic_heart_outlined)
                                        Toast.makeText(context, "Retiré des favoris", Toast.LENGTH_SHORT).show()
                                    }
                                }
                            }
                        }

                        override fun onCancelled(error: DatabaseError) {}
                    })
                } else {
                    btnFavorite.setOnClickListener {
                        Toast.makeText(context, "Veuillez vous connecter", Toast.LENGTH_SHORT).show()
                    }
                }
            }
        }

        private fun formatPrice(price: Double): String {
            return NumberFormat.getCurrencyInstance(Locale.getDefault()).format(price)
        }
    }

    private class OfferDiffCallback : DiffUtil.ItemCallback<Offer>() {
        override fun areItemsTheSame(oldItem: Offer, newItem: Offer): Boolean {
            return oldItem.id == newItem.id
        }

        override fun areContentsTheSame(oldItem: Offer, newItem: Offer): Boolean {
            return oldItem == newItem
        }
    }
} 