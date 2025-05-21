package com.example.mycolloc.Adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.mycolloc.R
import com.example.mycolloc.databinding.ItemOfferBinding
import com.example.mycolloc.model.Offer

class OffersAdapter(private val onClick: (Offer) -> Unit) :
    ListAdapter<Offer, OffersAdapter.OfferViewHolder>(OfferDiffCallback()) {

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
            binding.offerTitle.text = offer.title
            binding.offerPrice.text = "${offer.price.toInt()} MAD"

            Glide.with(binding.offerImage.context)
                .load(offer.imageUrl.ifEmpty { R.drawable.ic_rentable_house })
                .into(binding.offerImage)

            binding.root.setOnClickListener { onClick(offer) }
        }
    }

    class OfferDiffCallback : DiffUtil.ItemCallback<Offer>() {
        override fun areItemsTheSame(oldItem: Offer, newItem: Offer) = oldItem.id == newItem.id
        override fun areContentsTheSame(oldItem: Offer, newItem: Offer) = oldItem == newItem
    }
}
