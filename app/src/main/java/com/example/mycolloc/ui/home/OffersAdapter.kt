package com.example.mycolloc.ui.home

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.example.mycolloc.databinding.ItemOfferBinding
import com.example.mycolloc.model.Offer
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
                val position = adapterPosition
                if (position != RecyclerView.NO_POSITION) {
                    onOfferClick(getItem(position))
                }
            }
        }

        fun bind(offer: Offer) {
            binding.apply {
                offerTitle.text = offer.title
                offerPrice.text = formatPrice(offer.price)
                offerLocation.text = offer.location.city.takeIf { it.isNotBlank() } ?: "Location not specified"
                offerDescription.text = offer.description

                // Set click listener for the "Make an offer" button
                buttonMakeOffer.setOnClickListener {
                    onOfferClick(offer)
                }
            }
        }

        private fun formatPrice(price: Double): String {
            val format = NumberFormat.getCurrencyInstance(Locale("fr", "FR"))
            return format.format(price)
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