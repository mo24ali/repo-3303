package com.example.mycolloc.ui.profile

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.PopupMenu
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.mycolloc.Adapter.MyOffersAdapter
import com.example.mycolloc.R
import com.example.mycolloc.databinding.ActivityMyOfffersBinding
import com.example.mycolloc.model.Offer
import com.example.mycolloc.ui.post.EditOfferActivity
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.*

class MyOfffersActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMyOfffersBinding
    private lateinit var adapter: MyOffersAdapter
    private val currentUserId = FirebaseAuth.getInstance().currentUser?.uid ?: ""

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMyOfffersBinding.inflate(layoutInflater)
        setContentView(binding.root)

        adapter = MyOffersAdapter(
            onMenuClick = { offer, anchor -> showPopupMenu(offer, anchor) },
            onEditClick = { offer ->
                launchEditActivity(offer)
            },
            onDeleteClick = { offer ->
                confirmDeleteOffer(offer)
            }
        )

        binding.myOffersRecyclerView.layoutManager = LinearLayoutManager(this)
        binding.myOffersRecyclerView.adapter = adapter

        fetchUserOffers()
    }

    private fun fetchUserOffers() {
        val database = FirebaseDatabase.getInstance().getReference("offers")
        database.orderByChild("userId").equalTo(currentUserId)
            .addValueEventListener(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    val list = mutableListOf<Offer>()
                    for (child in snapshot.children) {
                        val offer = child.getValue(Offer::class.java)
                        if (offer != null) list.add(offer)
                    }

                    if (list.isEmpty()) {
                        binding.myOffersRecyclerView.visibility = View.GONE
                        binding.emptyStateLayout.visibility = View.VISIBLE
                    } else {
                        binding.myOffersRecyclerView.visibility = View.VISIBLE
                        binding.emptyStateLayout.visibility = View.GONE
                        adapter.submitList(list)
                    }
                }

                override fun onCancelled(error: DatabaseError) {
                    Toast.makeText(this@MyOfffersActivity, "Erreur : ${error.message}", Toast.LENGTH_SHORT).show()
                }
            })
    }

    private fun launchEditActivity(offer: Offer) {
        if (offer.id.isNullOrBlank()) {
            Toast.makeText(this, "Offre invalide : ID manquant", Toast.LENGTH_SHORT).show()
            return
        }
        val intent = Intent(this, EditOfferActivity::class.java)
        intent.putExtra("offerId", offer.id)
        startActivity(intent)
    }

    private fun confirmDeleteOffer(offer: Offer) {
        AlertDialog.Builder(this)
            .setTitle("Supprimer l'offre")
            .setMessage("Voulez-vous vraiment supprimer \"${offer.title}\" ?")
            .setPositiveButton("Supprimer") { _, _ ->
                FirebaseDatabase.getInstance().getReference("offers")
                    .child(offer.id)
                    .removeValue()
                    .addOnSuccessListener {
                        Toast.makeText(this, "Offre supprimée", Toast.LENGTH_SHORT).show()
                    }
                    .addOnFailureListener {
                        Toast.makeText(this, "Erreur suppression : ${it.message}", Toast.LENGTH_SHORT).show()
                    }
            }
            .setNegativeButton("Annuler", null)
            .show()
    }

    private fun showPopupMenu(offer: Offer, anchor: View) {
        val popup = PopupMenu(this, anchor)
        popup.menuInflater.inflate(R.menu.menu_offer_actions, popup.menu)

        popup.setOnMenuItemClickListener {
            when (it.itemId) {
                R.id.action_edit -> {
                    launchEditActivity(offer)
                    true
                }
                R.id.action_delete -> {
                    confirmDeleteOffer(offer)
                    true
                }
                R.id.action_details -> {
                    if (offer.id.isNullOrBlank()) {
                        Toast.makeText(this, "Offre invalide", Toast.LENGTH_SHORT).show()
                        return@setOnMenuItemClickListener false
                    }
                    val intent = Intent(this, activity_my_bids::class.java)
                    intent.putExtra("offerId", offer.id)
                    startActivity(intent)
                    true
                }
                else -> false
            }
        }
        popup.show()
    }
}
