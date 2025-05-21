package com.example.mycolloc.ui.details

import android.os.Bundle
import android.widget.TextView
import android.widget.Toast
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import com.bumptech.glide.Glide
import com.example.mycolloc.R
import com.example.mycolloc.databinding.ActivityDetailsArticleBinding
import com.example.mycolloc.model.Offer
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
            .load(offer.imageUrl.ifEmpty { R.drawable.ic_rentable_house })
            .into(apartmentImage)

        prixText.text = "${offer.price.toInt()} MAD"
        bookNowBtn.text = "Réserver pour ${offer.price.toInt()} MAD"
        adressLocation.text = offer.location?.address ?: "Adresse inconnue"
        descriptionText.text = offer.description

        root.findViewById<TextView>(R.id.bedroomsText)?.text = "${offer.bedrooms ?: 0} Bedrooms"
        root.findViewById<TextView>(R.id.bathroomsText)?.text = "${offer.bathrooms ?: 0} Bathrooms"
        root.findViewById<TextView>(R.id.surfaceText)?.text = "${offer.surface ?: "?"} m²"
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
