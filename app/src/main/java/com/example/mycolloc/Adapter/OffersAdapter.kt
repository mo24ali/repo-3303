package com.example.mycolloc.Adapter

import android.view.*
import android.widget.PopupMenu
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.mycolloc.R
import com.example.mycolloc.databinding.ItemOfferBinding
import com.example.mycolloc.model.Offer
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.*

class OffersAdapter(
    private val onClick: (Offer) -> Unit,
    private val isMyOffersContext: Boolean = false,

) : ListAdapter<Offer, OffersAdapter.OfferViewHolder>(OfferDiffCallback()) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): OfferViewHolder {
        val inflater = LayoutInflater.from(parent.context)
        val binding = ItemOfferBinding.inflate(inflater, parent, false)
        return OfferViewHolder(binding)
    }

    override fun onBindViewHolder(holder: OfferViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    inner class OfferViewHolder(private val binding: ItemOfferBinding) :
        RecyclerView.ViewHolder(binding.root) {

        fun bind(offer: Offer) {
            val context = binding.root.context
            val currentUser = FirebaseAuth.getInstance().currentUser
            val database = FirebaseDatabase.getInstance()

            binding.offerPrice.text = "${offer.price.toInt()} MAD"

            Glide.with(context)
                .load(offer.imageUrl.ifEmpty { R.drawable.ic_rentable_house })
                .into(binding.offerImage)

            if (isMyOffersContext) {
                // 🟦 Afficher bouton menu (3 points) pour modifier/supprimer
                binding.btnFavorite.visibility = View.GONE
                binding.btnMore.visibility = View.VISIBLE
                binding.btnMore.setOnClickListener {
                    //showPopupMenu(it, offer)
                }
            } else {
                // ❤️ Favoris : visible si ce n'est PAS le mode "mes offres"
                binding.btnFavorite.visibility = View.VISIBLE
                binding.btnMore.visibility = View.GONE

                if (currentUser != null) {
                    val favoritesRef = database.getReference("favorites")
                        .child(currentUser.uid)
                        .child(offer.id)

                    favoritesRef.addListenerForSingleValueEvent(object : ValueEventListener {
                        override fun onDataChange(snapshot: DataSnapshot) {
                            val icon = if (snapshot.exists()) {
                                R.drawable.ic_heart_filled
                            } else {
                                R.drawable.ic_heart_outlined
                            }
                            binding.btnFavorite.setImageResource(icon)
                        }

                        override fun onCancelled(error: DatabaseError) {}
                    })

                    binding.btnFavorite.setOnClickListener {
                        favoritesRef.setValue(offer).addOnSuccessListener {
                            binding.btnFavorite.setImageResource(R.drawable.ic_heart_filled)
                        }
                    }
                }
            }


            binding.root.setOnClickListener { onClick(offer) }
        }


    }

    class OfferDiffCallback : DiffUtil.ItemCallback<Offer>() {
        override fun areItemsTheSame(oldItem: Offer, newItem: Offer) = oldItem.id == newItem.id
        override fun areContentsTheSame(oldItem: Offer, newItem: Offer) = oldItem == newItem
    }
}
