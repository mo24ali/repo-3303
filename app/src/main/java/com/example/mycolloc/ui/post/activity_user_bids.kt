package com.example.mycolloc.ui.post

import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.mycolloc.Adapter.OfferBidsAdapter
import com.example.mycolloc.databinding.ActivityUserBidsBinding
import com.example.mycolloc.model.Bid
import com.google.firebase.database.*

class activity_user_bids : AppCompatActivity() {

    private lateinit var binding: ActivityUserBidsBinding
    private lateinit var database: DatabaseReference
    private var offerId: String? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityUserBidsBinding.inflate(layoutInflater)
        setContentView(binding.root)

        offerId = intent.getStringExtra("offerId")
        if (offerId.isNullOrEmpty()) {
            Toast.makeText(this, "Identifiant de l'offre introuvable", Toast.LENGTH_SHORT).show()
            finish()
            return
        }

        binding.bidsRecyclerView.layoutManager = LinearLayoutManager(this)

        fetchBids()
    }

    private fun fetchBids() {
        database = FirebaseDatabase.getInstance().getReference("bids")
        database.orderByChild("offerId").equalTo(offerId)
            .addListenerForSingleValueEvent(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    val bids = snapshot.children.mapNotNull { it.getValue(Bid::class.java) }

                    val adapter = OfferBidsAdapter(
                        context = this@activity_user_bids,
                        bids = bids,
                        onStatusChanged = {
                            // Refresh list after accept/decline
                            fetchBids()
                        }
                    )

                    binding.bidsRecyclerView.adapter = adapter
                }

                override fun onCancelled(error: DatabaseError) {
                    Toast.makeText(
                        this@activity_user_bids,
                        "Erreur lors du chargement : ${error.message}",
                        Toast.LENGTH_SHORT
                    ).show()
                }
            })
    }
}
