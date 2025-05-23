package com.example.mycolloc.ui.post

import android.app.Activity
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import com.bumptech.glide.Glide
import com.example.mycolloc.R
import com.example.mycolloc.model.Offer
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.ValueEventListener

class EditOfferActivity : AppCompatActivity() {

    private lateinit var titleEditText: EditText
    private lateinit var descriptionEditText: EditText
    private lateinit var priceEditText: EditText
    private lateinit var bedroomEditText: EditText
    private lateinit var toiletEditText: EditText
    private lateinit var surfaceEditText: EditText
    private lateinit var citySpinner: Spinner
    private lateinit var typeEstateSpinner: Spinner
    private lateinit var imagePreview: ImageView
    private lateinit var updateOfferButton: Button

    private var imageUri: Uri? = null
    private val PICK_IMAGE_REQUEST = 1
    private lateinit var offerId: String

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_edit_offer)

        titleEditText = findViewById(R.id.titleEditText)
        descriptionEditText = findViewById(R.id.descriptionEditText)
        priceEditText = findViewById(R.id.priceEditText)
        bedroomEditText = findViewById(R.id.bedroomEditText)
        toiletEditText = findViewById(R.id.toiletEditText)
        surfaceEditText = findViewById(R.id.surfaceEditText)
        citySpinner = findViewById(R.id.citySpinner)
        typeEstateSpinner = findViewById(R.id.typeestate)
        imagePreview = findViewById(R.id.imagePreview)
        updateOfferButton = findViewById(R.id.postOfferButton)
        updateOfferButton.text = "Update Offer"

        // Populate spinners
        val estateTypes = listOf("Apartment", "Villa", "House", "Studio")
        typeEstateSpinner.adapter = ArrayAdapter(this, android.R.layout.simple_spinner_dropdown_item, estateTypes)

        val cities = listOf("Agadir", "Casablanca", "Fès", "Marrakech", "Rabat", "Tanger", "Safi")
        citySpinner.adapter = ArrayAdapter(this, android.R.layout.simple_spinner_dropdown_item, cities)

        offerId = intent.getStringExtra("offerId") ?: run {
            Toast.makeText(this, "Invalid offer ID", Toast.LENGTH_SHORT).show()
            finish()
            return
        }

        //loadOfferData()
        val offerId = intent.getStringExtra("offerId")
        if (offerId.isNullOrBlank()) {
            Toast.makeText(this, "ID de l'offre invalide", Toast.LENGTH_SHORT).show()
            finish()
            return
        }

        Log.d("EditOfferActivity", "📥 offerId reçu: $offerId")

        loadOfferData(offerId) // Méthode à définir pour charger l'offre
        imagePreview.setOnClickListener {
            val intent = Intent(Intent.ACTION_PICK)
            intent.type = "image/*"
            startActivityForResult(intent, PICK_IMAGE_REQUEST)
        }

        updateOfferButton.setOnClickListener {
            updateOffer()
        }
    }

    private fun loadOfferData(offerId: String) {
        val ref = FirebaseDatabase.getInstance().getReference("offers").child(offerId)

        ref.get().addOnSuccessListener { snapshot ->
            val offer = snapshot.getValue(Offer::class.java)

            if (offer != null) {
                // Remplir les champs
                findViewById<EditText>(R.id.titleEditText).setText(offer.title)
                findViewById<EditText>(R.id.descriptionEditText).setText(offer.description)
                findViewById<EditText>(R.id.priceEditText).setText(offer.price.toString())
                findViewById<EditText>(R.id.bedroomEditText).setText(offer.bedrooms.toString())
                findViewById<EditText>(R.id.toiletEditText).setText(offer.bathrooms.toString())
                findViewById<EditText>(R.id.surfaceEditText).setText(offer.surface)

                // Sélection de la ville dans le Spinner
                val citySpinner = findViewById<Spinner>(R.id.citySpinner)
                val cityIndex = (citySpinner.adapter as ArrayAdapter<String>).getPosition(offer.location.city)
                if (cityIndex >= 0) citySpinner.setSelection(cityIndex)

                // Sélection du type d'habitation
                val typeEstateSpinner = findViewById<Spinner>(R.id.typeestate)
                val typeIndex = (typeEstateSpinner.adapter as ArrayAdapter<String>).getPosition(offer.category)
                if (typeIndex >= 0) typeEstateSpinner.setSelection(typeIndex)

                // Charger l’image
                val imageView = findViewById<ImageView>(R.id.imagePreview)
                Glide.with(this)
                    .load(offer.imageUrl.ifBlank { R.drawable.ic_rentable_house })
                    .into(imageView)
            } else {
                Toast.makeText(this, "Offre non trouvée", Toast.LENGTH_SHORT).show()
                finish()
            }
        }.addOnFailureListener {
            Toast.makeText(this, "Erreur de chargement : ${it.message}", Toast.LENGTH_SHORT).show()
            finish()
        }
    }


    private fun updateOffer() {
        val title = titleEditText.text.toString()
        val description = descriptionEditText.text.toString()
        val price = priceEditText.text.toString().toDoubleOrNull() ?: 0.0
        val bedrooms = bedroomEditText.text.toString().toIntOrNull() ?: 0
        val bathrooms = toiletEditText.text.toString().toIntOrNull() ?: 0
        val surface = surfaceEditText.text.toString()

        if (title.isBlank() || description.isBlank() || price <= 0.0) {
            Toast.makeText(this, "Remplissez tous les champs", Toast.LENGTH_SHORT).show()
            return
        }

        val updates = mapOf(
            "title" to title,
            "description" to description,
            "price" to price,
            "bedrooms" to bedrooms,
            "bathrooms" to bathrooms,
            "surface" to surface,
            "category" to typeEstateSpinner.selectedItem.toString(),
            "imageUrl" to (imageUri?.toString() ?: ""),
            "location/city" to citySpinner.selectedItem.toString()
        )

        FirebaseDatabase.getInstance().getReference("offers")
            .child(offerId)
            .updateChildren(updates)
            .addOnSuccessListener {
                Toast.makeText(this, "Offre mise à jour avec succès", Toast.LENGTH_SHORT).show()
                finish()
            }
            .addOnFailureListener {
                Toast.makeText(this, "Erreur : ${it.message}", Toast.LENGTH_LONG).show()
            }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == PICK_IMAGE_REQUEST && resultCode == Activity.RESULT_OK && data != null) {
            imageUri = data.data
            Glide.with(this).load(imageUri).into(imagePreview)
        }
    }
}
