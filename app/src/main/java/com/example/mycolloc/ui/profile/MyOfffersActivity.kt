package com.example.mycolloc.ui.profile

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.PopupMenu
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.mycolloc.databinding.ActivityMyOfffersBinding
import com.example.mycolloc.model.Offer
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.*
import androidx.recyclerview.widget.RecyclerView
import com.example.mycolloc.R
import com.bumptech.glide.Glide
import com.example.mycolloc.Adapter.MyOffersAdapter
import com.example.mycolloc.ui.post.EditOfferActivity

class MyOfffersActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMyOfffersBinding
    private lateinit var adapter: MyOffersAdapter
    private lateinit var database: DatabaseReference
    private val currentUserId = FirebaseAuth.getInstance().currentUser?.uid ?: ""

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMyOfffersBinding.inflate(layoutInflater)
        setContentView(binding.root)

        adapter = MyOffersAdapter(
            onMenuClick = { offer, anchorView -> showPopupMenu(offer, anchorView) },
            onEditClick = { offer ->
                val intent = Intent(this, EditOfferActivity::class.java)
                intent.putExtra("offerId", offer.id)
                startActivity(intent)
            },
            onDeleteClick = { offer ->
                FirebaseDatabase.getInstance().getReference("offers").child(offer.id).removeValue()
                Toast.makeText(this, "Offre supprimée", Toast.LENGTH_SHORT).show()
            }
        )



        binding.myOffersRecyclerView.layoutManager = LinearLayoutManager(this)
        binding.myOffersRecyclerView.adapter = adapter

        fetchUserOffers()
    }

    private fun fetchUserOffers() {
        database = FirebaseDatabase.getInstance().getReference("offers")
        database.orderByChild("userId").equalTo(currentUserId)
            .addValueEventListener(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    val list = mutableListOf<Offer>()
                    for (child in snapshot.children) {
                        val offer = child.getValue(Offer::class.java)
                        if (offer != null) list.add(offer)
                    }

                    // 🔄 Mise à jour UI en fonction du contenu
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

    private fun showPopupMenu(offer: Offer, anchor: View) {
        val popup = PopupMenu(this, anchor)
        popup.menuInflater.inflate(R.menu.menu_offer_actions, popup.menu)
        popup.setOnMenuItemClickListener {
            when (it.itemId) {
                R.id.action_edit -> {
                    Toast.makeText(this, "Modifier ${offer.title}", Toast.LENGTH_SHORT).show()
                    // TODO: Lancer EditOfferActivity avec offer.id
                    true
                }
                R.id.action_delete -> {
                    FirebaseDatabase.getInstance().getReference("offers").child(offer.id).removeValue()
                    Toast.makeText(this, "Offre supprimée", Toast.LENGTH_SHORT).show()
                    true
                }
                R.id.action_details -> {
                    Toast.makeText(this, "Détails de ${offer.title}", Toast.LENGTH_SHORT).show()
                    // TODO: Lancer DetailsActivity
                    true
                }
                else -> false
            }
        }
        popup.show()
    }
}
