package com.example.mycolloc.ui.post

import android.Manifest
import android.content.pm.PackageManager
import android.location.Geocoder
import android.location.Location
import android.os.Bundle
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
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

    private lateinit var fusedLocationClient: FusedLocationProviderClient
    private var userLocation: Location? = null

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

        val cities = listOf("Casablanca", "Rabat", "Marrakech")
        citySpinner.adapter = ArrayAdapter(this, android.R.layout.simple_spinner_dropdown_item, cities)

        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this)
        getLastKnownLocation()

        postOfferButton.setOnClickListener {
            saveOfferToFirebase()
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

//    private fun saveOfferToFirebase() {
//        val title = titleEditText.text.toString()
//        val description = descriptionEditText.text.toString()
//        val price = priceEditText.text.toString().toDoubleOrNull() ?: 0.0
//        val bedrooms = bedroomEditText.text.toString().toIntOrNull() ?: 0
//        val toilets = toiletEditText.text.toString().toIntOrNull() ?: 0
//        val surface = surfaceEditText.text.toString().toDoubleOrNull() ?: 0.0
//
//        if (title.isBlank() || description.isBlank() || price <= 0.0) {
//            Toast.makeText(this, "Please fill in all fields correctly.", Toast.LENGTH_SHORT).show()
//            return
//        }
//
//        val androidLocation = userLocation
//        if (androidLocation == null) {
//            Toast.makeText(this, "Position non disponible. Réessayez.", Toast.LENGTH_SHORT).show()
//            return
//        }
//
//        val geocoder = Geocoder(this, Locale.getDefault())
//        val addresses = geocoder.getFromLocation(androidLocation.latitude, androidLocation.longitude, 1)
//        val city = addresses?.firstOrNull()?.locality ?: "Unknown"
//        val address = addresses?.firstOrNull()?.getAddressLine(0) ?: "Unknown Address"
//
//        val customLocation = CustomLocation(
//
//            city = city,
//            address = address,
//            latitude = androidLocation.latitude,
//            longitude = androidLocation.longitude,
//        )
//
//        val userId = FirebaseAuth.getInstance().currentUser?.uid ?: "unknown_user"
//        val offerId = UUID.randomUUID().toString()
//
//        val offer = Offer(
//            id = offerId,
//            userId = userId,
//            title = title,
//            description = description,
//            price = price,
//            category = "Apartment",
//            location = customLocation,
//            latitude = customLocation.latitude ?: 0.0,
//            longitude = customLocation.longitude ?: 0.0,
//            bedrooms = bedrooms,
//            toilets = toilets,
//            surface = surface
//            images = listOf(),
//            isActive = true
//        )
//
//        FirebaseDatabase.getInstance().getReference("offers")
//            .child(offerId)
//            .setValue(offer)
//            .addOnSuccessListener {
//                Toast.makeText(this, "Offre enregistrée avec position et ville !", Toast.LENGTH_SHORT).show()
//                finish()
//            }
//            .addOnFailureListener {
//                Toast.makeText(this, "Erreur : ${it.message}", Toast.LENGTH_LONG).show()
//            }
//    }


    private fun saveOfferToFirebase() {
        val title = titleEditText.text.toString()
        val description = descriptionEditText.text.toString()
        val price = priceEditText.text.toString().toDoubleOrNull() ?: 0.0
        val bedrooms = bedroomEditText.text.toString().toIntOrNull()
        val bathrooms = toiletEditText.text.toString().toIntOrNull()
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

        val customLocation = com.example.mycolloc.data.local.Location(
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
            category = "Apartment",
            latitude = androidLocation.latitude,
            longitude = androidLocation.longitude,
            images = listOf(),
            imageUrl = "",
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
                Toast.makeText(this, "Offre enregistrée avec succès", Toast.LENGTH_SHORT).show()
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
