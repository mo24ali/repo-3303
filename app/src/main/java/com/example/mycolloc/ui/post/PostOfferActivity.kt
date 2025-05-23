package com.example.mycolloc.ui.post

import android.Manifest
import android.app.Activity
import android.content.Intent
import android.content.pm.PackageManager
import android.location.Geocoder
import android.location.Location
import android.net.Uri
import android.os.Bundle
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import com.bumptech.glide.Glide
import com.example.mycolloc.R
import com.example.mycolloc.model.Offer
import com.example.mycolloc.data.local.Location as CustomLocation
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.FirebaseDatabase
import java.util.*

class PostOfferActivity : AppCompatActivity() {

    private lateinit var titleEditText: EditText
    private lateinit var descriptionEditText: EditText
    private lateinit var priceEditText: EditText
    private lateinit var bedroomEditText: EditText
    private lateinit var toiletEditText: EditText
    private lateinit var surfaceEditText: EditText
    private lateinit var citySpinner: Spinner
    private lateinit var imagePreview: ImageView
    private lateinit var postOfferButton: Button
    private lateinit var typeEstateSpinner: Spinner

    private lateinit var fusedLocationClient: FusedLocationProviderClient
    private var userLocation: Location? = null

    private val PICK_IMAGE_REQUEST = 1
    private var imageUri: Uri? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_post_offer)

        titleEditText = findViewById(R.id.titleEditText)
        descriptionEditText = findViewById(R.id.descriptionEditText)
        priceEditText = findViewById(R.id.priceEditText)
        bedroomEditText = findViewById(R.id.bedroomEditText)
        toiletEditText = findViewById(R.id.toiletEditText)
        surfaceEditText = findViewById(R.id.surfaceEditText)
        citySpinner = findViewById(R.id.citySpinner)
        imagePreview = findViewById(R.id.imagePreview)
        postOfferButton = findViewById(R.id.postOfferButton)
        typeEstateSpinner = findViewById(R.id.typeestate)

        val estateTypes = listOf("Appartement", "Villa", "Maison", "Studio")
        typeEstateSpinner.adapter = ArrayAdapter(this, android.R.layout.simple_spinner_dropdown_item, estateTypes)

        val cities = listOf("Agadir", "Al Hoce\u00efma", "Azilal", "Beni Mellal", "Ben Guerir", "Berkane", "Berrechid", "Boujdour", "Boulemane", "Casablanca", "Chefchaouen", "Chichaoua", "Dakhla", "Driouch", "El Hajeb", "El Jadida", "El Kelaa des Sraghna", "Errachidia", "Essaouira", "F\u00e8s", "Figuig", "Guelmim", "Guercif", "Ifrane", "Jerada", "K\u00e9nitra", "Khemisset", "Khenifra", "Khouribga", "La\u00e2youne", "Larache", "Marrakech", "Mekn\u00e8s", "Midelt", "Mohammedia", "Nador", "Ouarzazate", "Oued Zem", "Oujda", "Rabat", "Safi", "Sal\u00e9", "Sefrou", "Settat", "Sidi Bennour", "Sidi Ifni", "Sidi Kacem", "Sidi Slimane", "Skhirat", "Tanger", "Taounate", "Taourirt", "Tarfaya", "Taza", "T\u00e9mara", "T\u00e9touan", "Tinghir", "Tiznit", "Youssoufia", "Zagora")
        citySpinner.adapter = ArrayAdapter(this, android.R.layout.simple_spinner_dropdown_item, cities)

        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this)
        getLastKnownLocation()

        imagePreview.setOnClickListener {
            val intent = Intent(Intent.ACTION_PICK)
            intent.type = "image/*"
            startActivityForResult(intent, PICK_IMAGE_REQUEST)
        }

        postOfferButton.setOnClickListener {
            saveOfferToFirebase()
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == PICK_IMAGE_REQUEST && resultCode == Activity.RESULT_OK && data != null) {
            imageUri = data.data
            Glide.with(this).load(imageUri).into(imagePreview)
        }
    }

    private fun getLastKnownLocation() {
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, arrayOf(Manifest.permission.ACCESS_FINE_LOCATION), 101)
            return
        }

        fusedLocationClient.lastLocation.addOnSuccessListener { location ->
            if (location != null) {
                userLocation = location
            } else {
                Toast.makeText(this, "Localisation introuvable", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun saveOfferToFirebase() {
        val title = titleEditText.text.toString()
        val description = descriptionEditText.text.toString()
        val price = priceEditText.text.toString().toDoubleOrNull() ?: 0.0
        val bedrooms = bedroomEditText.text.toString().toIntOrNull() ?: 0
        val bathrooms = toiletEditText.text.toString().toIntOrNull() ?: 0
        val surface = surfaceEditText.text.toString()

        if (title.isBlank() || description.isBlank() || price <= 0.0) {
            Toast.makeText(this, "Veuillez remplir tous les champs correctement", Toast.LENGTH_SHORT).show()
            return
        }

        val androidLocation = userLocation
        if (androidLocation == null) {
            Toast.makeText(this, "Localisation introuvable", Toast.LENGTH_SHORT).show()
            return
        }

        val geocoder = Geocoder(this, Locale.getDefault())
        val addressList = geocoder.getFromLocation(androidLocation.latitude, androidLocation.longitude, 1)
        val city = addressList?.firstOrNull()?.locality ?: "Ville inconnue"
        val address = addressList?.firstOrNull()?.getAddressLine(0) ?: "Adresse inconnue"

        val customLocation = CustomLocation(
            city = city,
            address = address,
            latitude = androidLocation.latitude,
            longitude = androidLocation.longitude
        )

        val userId = FirebaseAuth.getInstance().currentUser?.uid ?: "unknown_user"
        val offerId = UUID.randomUUID().toString()

        val offer = Offer(
            id = offerId,
            userId = userId,
            title = title,
            description = description,
            price = price,
            category = typeEstateSpinner.selectedItem.toString(),
            latitude = androidLocation.latitude,
            longitude = androidLocation.longitude,
            images = listOf(),
            imageUrl = imageUri?.toString() ?: "",
            isActive = true,
            location = customLocation,
            bedrooms = bedrooms,
            bathrooms = bathrooms,
            surface = surface
        )

        FirebaseDatabase.getInstance().getReference("offers")
            .child(offerId)
            .setValue(offer)
            .addOnSuccessListener {
                Toast.makeText(this, "Offre enregistr\u00e9e avec succ\u00e8s", Toast.LENGTH_SHORT).show()
                finish()
            }
            .addOnFailureListener {
                Toast.makeText(this, "Erreur : ${it.message}", Toast.LENGTH_LONG).show()
            }
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == 101 && grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
            getLastKnownLocation()
        } else {
            Toast.makeText(this, "Permission localisation requise", Toast.LENGTH_SHORT).show()
        }
    }
}
