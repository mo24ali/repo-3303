package com.example.mycolloc.ui.post

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.widget.ProgressBar
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import com.example.mycolloc.databinding.ActivityBidsBinding
import com.example.mycolloc.model.Offer
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.FirebaseDatabase

class BidsActivity : AppCompatActivity() {

    private lateinit var binding: ActivityBidsBinding
    private var phoneNumber: String? = null
    private var name: String? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityBidsBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val offer = intent.getStringExtra("OfferID")
        val useroffer = intent.getStringExtra("OfferUSER")


        if (offer != null) {
            if (useroffer != null) {
                fetchOwnerData(useroffer)
            }
        } else {
            Toast.makeText(this, "Identifiant du propriétaire manquant", Toast.LENGTH_SHORT).show()
        }

        binding.callButton.setOnClickListener {
            phoneNumber?.let {
                val intent = Intent(Intent.ACTION_DIAL)
                intent.data = Uri.parse("tel:$it")
                startActivity(intent)
            } ?: Toast.makeText(this, "Numéro indisponible", Toast.LENGTH_SHORT).show()
        }
        binding.sendBidButton.setOnClickListener {
            val amountText = binding.offerAmountEditText.text.toString()

            if (amountText.isBlank()) {
                binding.amountInputLayout.error = "Veuillez entrer un montant"
                return@setOnClickListener
            }

            binding.amountInputLayout.error = null
            val bidAmount = amountText.toDoubleOrNull()

            if (bidAmount == null || bidAmount <= 0) {
                binding.amountInputLayout.error = "Montant invalide"
                return@setOnClickListener
            }

            // Afficher AlertDialog de chargement
            val loadingDialog = AlertDialog.Builder(this)
                .setTitle("Envoi en cours")
                .setMessage("Veuillez patienter...")
                .setCancelable(false)
                .setView(ProgressBar(this))
                .create()

            loadingDialog.show()

            val currentUser = FirebaseAuth.getInstance().currentUser
            val userId = currentUser?.uid ?: return@setOnClickListener
            val bidId = FirebaseDatabase.getInstance().getReference("bids").push().key!!
            val timestamp = System.currentTimeMillis()

            val userRef = FirebaseDatabase.getInstance().getReference("users").child(userId)
            userRef.get().addOnSuccessListener { snapshot ->

                val bidData = mapOf(
                    "bidId" to bidId,
                    "userId" to userId,
                    "offerId" to offer,
                    "amount" to bidAmount,
                    "timestamp" to timestamp,
                    "status" to "pending"
                )


                FirebaseDatabase.getInstance().getReference("bids").child(bidId)
                    .setValue(bidData)
                    .addOnSuccessListener {
                        loadingDialog.dismiss()
                        Toast.makeText(this, "Votre offre a été envoyée", Toast.LENGTH_SHORT).show()
                        finish()
                    }
                    .addOnFailureListener {
                        loadingDialog.dismiss()
                        Toast.makeText(this, "Erreur lors de l'envoi", Toast.LENGTH_SHORT).show()
                    }
            }


        }
    }

    private fun fetchOwnerData(ownerId: String) {
        val userRef = FirebaseDatabase.getInstance().getReference("users").child(ownerId)

        userRef.get().addOnSuccessListener { snapshot ->
            name = snapshot.child("firstName").getValue(String::class.java) +" "+ snapshot.child("lastName").getValue(String::class.java)
            val tel = snapshot.child("phoneNumber").getValue(String::class.java)

            binding.ownerName.text = name ?: "Nom inconnu"
            phoneNumber = tel
        }.addOnFailureListener {
            Toast.makeText(this, "Erreur de chargement des infos", Toast.LENGTH_SHORT).show()
        }
    }
}
