package com.example.mycolloc.ui.admin

import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.mycolloc.databinding.ActivityAdminDahboardBinding
import com.example.mycolloc.ui.auth.LoginActivity
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.*

class AdminDashboardActivity : AppCompatActivity() {

    private lateinit var binding: ActivityAdminDahboardBinding
    private val database = FirebaseDatabase.getInstance()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityAdminDahboardBinding.inflate(layoutInflater)
        setContentView(binding.root)

        fetchTotalUsers()
        fetchTotalOffers()
        fetchTotalBids()
        fetchActiveUsers()

        binding.logoutCard.setOnClickListener {
            FirebaseAuth.getInstance().signOut()
            Toast.makeText(this, "Déconnexion réussie", Toast.LENGTH_SHORT).show()
            startActivity(Intent(this, LoginActivity::class.java))
            finish()
        }
    }

    private fun fetchTotalUsers() {
        val ref = database.getReference("users")
        ref.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val total = snapshot.childrenCount
                binding.totalUsers.text = total.toString()
            }

            override fun onCancelled(error: DatabaseError) {}
        })
    }

    private fun fetchTotalOffers() {
        val ref = database.getReference("offers")
        ref.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val total = snapshot.childrenCount
                binding.totalOffers.text = total.toString()
            }

            override fun onCancelled(error: DatabaseError) {}
        })
    }

    private fun fetchTotalBids() {
        val ref = database.getReference("bids")
        ref.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val total = snapshot.childrenCount
                binding.totalBids.text = total.toString()
            }

            override fun onCancelled(error: DatabaseError) {}
        })
    }

    private fun fetchActiveUsers() {
        val ref = database.getReference("users")
        ref.orderByChild("enLigne").equalTo(true)
            .addListenerForSingleValueEvent(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    val total = snapshot.childrenCount
                    binding.activeUsers.text = total.toString()
                }

                override fun onCancelled(error: DatabaseError) {}
            })
    }
}
