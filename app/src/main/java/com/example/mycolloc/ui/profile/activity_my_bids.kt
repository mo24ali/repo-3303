package com.example.mycolloc.ui.profile

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.mycolloc.R
import com.example.mycolloc.Adapter.BidAdapter

import com.example.mycolloc.databinding.ActivityMyBidsBinding
import com.example.mycolloc.model.Bid
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.FirebaseDatabase

class activity_my_bids : AppCompatActivity() {
    private lateinit var binding: ActivityMyBidsBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMyBidsBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val userId = FirebaseAuth.getInstance().currentUser?.uid ?: return

        val bidsRef = FirebaseDatabase.getInstance().getReference("bids")
        bidsRef.orderByChild("userId").equalTo(userId)
            .get().addOnSuccessListener { snapshot ->
                val bids = snapshot.children.mapNotNull { it.getValue(Bid::class.java) }
                val adapter = BidAdapter(bids)
                binding.myBidsRecyclerView.adapter = adapter
                binding.myBidsRecyclerView.layoutManager = LinearLayoutManager(this)
            }
    }
}