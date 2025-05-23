package com.example.mycolloc.Adapter

import android.content.Intent
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.PopupMenu
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.mycolloc.R
import com.example.mycolloc.databinding.ItemApartmentCardBinding
import com.example.mycolloc.model.Offer
import com.example.mycolloc.ui.post.EditOfferActivity
import com.example.mycolloc.ui.post.activity_user_bids
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

    inner class MyViewHolder(val binding: ItemApartmentCardBinding) : RecyclerView.ViewHolder(binding.root) {
        fun bind(offer: Offer) {
            binding.price.text = "${offer.price.toInt()} MAD"
            binding.apartmentType.text = offer.category
            binding.address.text = offer.description

            Glide.with(binding.imageApartment.context)
                .load(offer.imageUrl.ifEmpty { R.drawable.sample_apartment })
                .placeholder(R.drawable.sample_apartment)
                .into(binding.imageApartment)

            val isOwner = offer.userId == FirebaseAuth.getInstance().currentUser?.uid

            binding.more.visibility = if (isOwner) View.VISIBLE else View.GONE

            if (isOwner) {
                binding.more.setOnClickListener {
                    showPopupMenu(it, offer)
                }
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MyViewHolder {
        val binding = ItemApartmentCardBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return MyViewHolder(binding)
    }

    override fun getItemCount() = items.size

    override fun onBindViewHolder(holder: MyViewHolder, position: Int) {
        holder.bind(items[position])
    }

    private fun showPopupMenu(anchor: View, offer: Offer) {
        val popup = PopupMenu(anchor.context, anchor)
        popup.menuInflater.inflate(R.menu.menu_offer_actions, popup.menu)

        popup.setOnMenuItemClickListener {
            when (it.itemId) {
                R.id.action_edit -> {
                    val intent = Intent(anchor.context, EditOfferActivity::class.java)
                    intent.putExtra("offerId", offer.id) // ✅ CORRECTED KEY
                    anchor.context.startActivity(intent)
                    true
                }
                R.id.action_delete -> {
                    androidx.appcompat.app.AlertDialog.Builder(anchor.context)
                        .setTitle("Supprimer l'offre")
                        .setMessage("Êtes-vous sûr de vouloir supprimer cette offre ?")
                        .setPositiveButton("Supprimer") { _, _ ->
                            onDeleteClick?.invoke(offer)
                        }
                        .setNegativeButton("Annuler", null)
                        .show()
                    true
                }
                R.id.action_details -> {
                    val intent = Intent(anchor.context, activity_user_bids::class.java)
                    intent.putExtra("offerId", offer.id) // ✅ Ensure consistent key
                    anchor.context.startActivity(intent)
                    true
                }
                else -> false
            }
        }

        popup.show()
    }
}
