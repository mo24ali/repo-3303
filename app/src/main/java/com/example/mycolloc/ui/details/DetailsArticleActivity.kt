package com.example.mycolloc.ui.details

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.widget.TextView
import android.widget.Toast
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import com.bumptech.glide.Glide
import com.example.mycolloc.R
import com.example.mycolloc.databinding.ActivityDetailsArticleBinding
import com.example.mycolloc.model.Offer
import com.example.mycolloc.ui.post.BidsActivity
import com.example.mycolloc.viewmodels.DetailsViewModel
import com.google.firebase.database.*

class DetailsArticleActivity : AppCompatActivity() {

    private lateinit var binding: ActivityDetailsArticleBinding
    private val viewModel: DetailsViewModel by viewModels()

    companion object {
        const val EXTRA_OFFER_ID = "extra_offer_id"
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityDetailsArticleBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val offerId = intent.getStringExtra(EXTRA_OFFER_ID)
        if (offerId == null) {
            Toast.makeText(this, "Aucune annonce sélectionnée.", Toast.LENGTH_SHORT).show()
            finish()
            return
        }

        fetchOfferDetails(offerId)
    }

    private fun fetchOfferDetails(offerId: String) {
        val ref = FirebaseDatabase.getInstance().getReference("offers").child(offerId)
        ref.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val offer = snapshot.getValue(Offer::class.java)
                if (offer != null) {
                    bindOfferToUI(offer)
                    fetchOwnerName(offer.userId) // 🔍 afficher nom du propriétaire
                } else {
                    Toast.makeText(this@DetailsArticleActivity, "Offre introuvable.", Toast.LENGTH_SHORT).show()
                }
            }

            override fun onCancelled(error: DatabaseError) {
                Toast.makeText(this@DetailsArticleActivity, "Erreur: ${error.message}", Toast.LENGTH_SHORT).show()
            }
        })
    }

    private fun bindOfferToUI(offer: Offer) = with(binding) {
        Glide.with(this@DetailsArticleActivity)
            .load(offer.imageUrl.ifEmpty { R.drawable.sample_apartment })
            .placeholder(R.drawable.sample_apartment)
            .into(apartmentImage)

        prixText.text = "${offer.price.toInt()} DH"
        bookNowBtn.text = "Faire une proposition"
        adressLocation.text = offer.location?.address ?: "Adresse inconnue"
        descriptionText.text = offer.description
        categorie.text = offer.category
        textOferTitle.text = offer.title

        root.findViewById<TextView>(R.id.bedroomsText)?.text = "${offer.bedrooms ?: 0} Bedrooms"
        root.findViewById<TextView>(R.id.bathroomsText)?.text = "${offer.bathrooms ?: 0} Bathrooms"
        root.findViewById<TextView>(R.id.surfaceText)?.text = "${offer.surface ?: "?"} m²"

        binding.buttonCall.setOnClickListener {
            val userId = offer.userId
            val userRef = FirebaseDatabase.getInstance().getReference("users").child(userId)

            userRef.get().addOnSuccessListener { snapshot ->
                val phoneNumber = snapshot.child("phoneNumber").getValue(String::class.java)
                if (!phoneNumber.isNullOrEmpty()) {
                    val intent = Intent(Intent.ACTION_DIAL)
                    intent.data = Uri.parse("tel:$phoneNumber")
                    startActivity(intent)
                } else {
                    Toast.makeText(this@DetailsArticleActivity, "Numéro de téléphone introuvable", Toast.LENGTH_SHORT).show()
                }
            }.addOnFailureListener {
                Toast.makeText(this@DetailsArticleActivity, "Erreur lors de la récupération du numéro", Toast.LENGTH_SHORT).show()
            }
        }
        binding.bookNowBtn.setOnClickListener {
            val intent = Intent(this@DetailsArticleActivity, BidsActivity::class.java)
            intent.putExtra("OfferID", offer.id)
            intent.putExtra("OfferUSER", offer.userId)
            startActivity(intent)
        }


    }

    private fun fetchOwnerName(userId: String) {
        val userRef = FirebaseDatabase.getInstance().getReference("users").child(userId)
        userRef.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val name = snapshot.child("firstName").getValue(String::class.java)
                binding.userNameDetailText.text = name ?: "Utilisateur inconnu"
            }

            override fun onCancelled(error: DatabaseError) {
                binding.userNameDetailText.text = "Erreur"
            }
        })
    }
}
