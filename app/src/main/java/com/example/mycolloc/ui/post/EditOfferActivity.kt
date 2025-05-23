package com.example.mycolloc.ui.post



import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.mycolloc.databinding.ActivityEditOfferBinding
import com.example.mycolloc.model.Offer
import com.google.firebase.database.FirebaseDatabase

class EditOfferActivity : AppCompatActivity() {

    private lateinit var binding: ActivityEditOfferBinding
    private lateinit var offerId: String

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityEditOfferBinding.inflate(layoutInflater)
        setContentView(binding.root)

        offerId = intent.getStringExtra("offerId") ?: run {
            finish()
            return
        }

        loadOfferData()

        binding.btnSaveChanges.setOnClickListener {
            saveUpdatedOffer()
        }
    }

    private fun loadOfferData() {
        val ref = FirebaseDatabase.getInstance().getReference("offers").child(offerId)
        ref.get().addOnSuccessListener {
            val offer = it.getValue(Offer::class.java)
            if (offer != null) {
                binding.titleEditText.setText(offer.title)
                binding.descriptionEditText.setText(offer.description)
                binding.priceEditText.setText(offer.price.toString())
                binding.bedroomEditText.setText(offer.bedrooms?.toString() ?: "")
                binding.toiletEditText.setText(offer.bathrooms?.toString() ?: "")
                binding.surfaceEditText.setText(offer.surface ?: "")
            }
        }
    }

    private fun saveUpdatedOffer() {
        val updatedOffer = mapOf<String, Any?>(
            "title" to binding.titleEditText.text.toString(),
            "description" to binding.descriptionEditText.text.toString(),
            "price" to (binding.priceEditText.text.toString().toDoubleOrNull() ?: 0.0),
            "bedrooms" to binding.bedroomEditText.text.toString().toIntOrNull(),
            "bathrooms" to binding.toiletEditText.text.toString().toIntOrNull(),
            "surface" to binding.surfaceEditText.text.toString()
        )

        FirebaseDatabase.getInstance().getReference("offers")
            .child(offerId)
            .updateChildren(updatedOffer)
            .addOnSuccessListener {
                Toast.makeText(this, "Offre mise à jour", Toast.LENGTH_SHORT).show()
                finish()
            }
            .addOnFailureListener {
                Toast.makeText(this, "Erreur : ${it.message}", Toast.LENGTH_LONG).show()
            }
    }
}