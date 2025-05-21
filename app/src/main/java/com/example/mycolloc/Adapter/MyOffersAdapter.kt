package com.example.mycolloc.Adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.PopupMenu
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.mycolloc.R
import com.example.mycolloc.databinding.ItemOfferBinding
import com.example.mycolloc.model.Offer
import com.google.firebase.auth.FirebaseAuth

class MyOffersAdapter(
    private val onMenuClick: (Offer, View) -> Unit,
    private val onEditClick: ((Offer) -> Unit)? = null,
    private val onDeleteClick: ((Offer) -> Unit)? = null
) : RecyclerView.Adapter<MyOffersAdapter.MyViewHolder>() {

    private val items = mutableListOf<Offer>()

    fun submitList(newItems: List<Offer>) {
        items.clear()
        items.addAll(newItems)
        notifyDataSetChanged()
    }

    inner class MyViewHolder(val binding: ItemOfferBinding) : RecyclerView.ViewHolder(binding.root) {
        fun bind(offer: Offer) {
            binding.offerPrice.text = "${offer.price.toInt()} MAD"

            Glide.with(binding.offerImage.context)
                .load(offer.imageUrl.ifEmpty { R.drawable.ic_rentable_house })
                .into(binding.offerImage)

            val isOwner = offer.userId == FirebaseAuth.getInstance().currentUser?.uid

            binding.btnMore.visibility = if (isOwner) View.VISIBLE else View.GONE
            binding.btnFavorite.visibility = if (isOwner) View.GONE else View.VISIBLE

            if (isOwner) {
                binding.btnMore.setOnClickListener {
                    showPopupMenu(it, offer)
                }
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MyViewHolder {
        val binding = ItemOfferBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return MyViewHolder(binding)
    }

    private fun showPopupMenu(anchor: View, offer: Offer) {
        val popup = PopupMenu(anchor.context, anchor)
        popup.menuInflater.inflate(R.menu.menu_offer_actions, popup.menu)
        popup.setOnMenuItemClickListener {
            when (it.itemId) {
                R.id.action_edit -> {
                    onEditClick?.invoke(offer)
                    true
                }
                R.id.action_delete -> {
                    onDeleteClick?.invoke(offer)
                    true
                }
                else -> false
            }
        }
        popup.show()
    }

    override fun getItemCount() = items.size

    override fun onBindViewHolder(holder: MyViewHolder, position: Int) {
        holder.bind(items[position])
    }
}
