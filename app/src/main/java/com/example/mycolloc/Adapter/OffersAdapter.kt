package com.example.mycolloc.Adapter

import android.view.*
import android.widget.Toast
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
            binding.offerCategory.text = offer.category
            binding.offerLocation.text = offer.title

            Glide.with(context)
                .load(offer.imageUrl.ifEmpty { R.drawable.sample_apartment })
                .into(binding.offerImage)

            // === ❤️ GESTION FAVORIS (hors mode "mes offres") ===
            if (isMyOffersContext) {
                binding.btnFavorite.visibility = View.GONE
                binding.btnMore.visibility = View.GONE // aucune action menu ici
            } else {
                binding.btnFavorite.visibility = View.VISIBLE
                binding.btnMore.visibility = View.GONE

                if (currentUser != null) {
                    val favRef = database.getReference("favorites")
                        .child(currentUser.uid)
                        .child(offer.id)

                    favRef.addListenerForSingleValueEvent(object : ValueEventListener {
                        override fun onDataChange(snapshot: DataSnapshot) {
                            var isFavorite = snapshot.exists()
                            val icon = if (isFavorite) {
                                R.drawable.ic_heart_filled
                            } else {
                                R.drawable.ic_heart_outlined
                            }
                            binding.btnFavorite.setImageResource(icon)

                            binding.btnFavorite.setOnClickListener {
                                isFavorite = !isFavorite
                                if (isFavorite) {
                                    favRef.setValue(offer).addOnSuccessListener {
                                        binding.btnFavorite.setImageResource(R.drawable.ic_heart_filled)
                                        Toast.makeText(context, "Ajouté aux favoris", Toast.LENGTH_SHORT).show()
                                    }
                                } else {
                                    favRef.removeValue().addOnSuccessListener {
                                        binding.btnFavorite.setImageResource(R.drawable.ic_heart_outlined)
                                        Toast.makeText(context, "Retiré des favoris", Toast.LENGTH_SHORT).show()
                                    }
                                }
                            }
                        }

                        override fun onCancelled(error: DatabaseError) {}
                    })
                } else {
                    binding.btnFavorite.setOnClickListener {
                        Toast.makeText(context, "Veuillez vous connecter pour ajouter aux favoris", Toast.LENGTH_SHORT).show()
                    }
                }
            }

//            binding.btnMore.setOnClickListener {
//                val context = binding.root.context
//                val intent = Intent(context, EditOfferActivity::class.java).apply {
//                    putExtra("offerId", offer.id)
//                }
//                context.startActivity(intent)
//            }


            // 🎯 Clic général sur l’offre
            binding.root.setOnClickListener { onClick(offer) }
        }




    }

    class OfferDiffCallback : DiffUtil.ItemCallback<Offer>() {
        override fun areItemsTheSame(oldItem: Offer, newItem: Offer) = oldItem.id == newItem.id
        override fun areContentsTheSame(oldItem: Offer, newItem: Offer) = oldItem == newItem
    }
}