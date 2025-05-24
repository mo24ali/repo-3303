package com.example.mycolloc.ui.home

import android.Manifest
import android.content.Intent
import android.content.IntentSender
import android.content.pm.PackageManager
import android.location.Location
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import com.example.mycolloc.R
import com.example.mycolloc.ui.details.DetailsArticleActivity
import com.google.android.gms.common.api.ResolvableApiException
import com.google.android.gms.location.*
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.*
import com.google.android.gms.tasks.CancellationTokenSource
import com.google.firebase.database.*

class MapFragment : Fragment(), OnMapReadyCallback {

    private lateinit var mMap: GoogleMap
    private lateinit var fusedLocationClient: FusedLocationProviderClient
    private lateinit var locationRequest: LocationRequest
    private lateinit var settingsClient: SettingsClient

    private val LOCATION_PERMISSION_CODE = 1001
    private val LOCATION_REQUEST_CODE = 2001

    private var userLocation: Location? = null

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View = inflater.inflate(R.layout.fragment_map, container, false)

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        val mapFragment = childFragmentManager.findFragmentById(R.id.map) as SupportMapFragment
        mapFragment.getMapAsync(this)

        fusedLocationClient = LocationServices.getFusedLocationProviderClient(requireActivity())
        settingsClient = LocationServices.getSettingsClient(requireActivity())

        locationRequest = LocationRequest.create().apply {
            priority = Priority.PRIORITY_HIGH_ACCURACY
            interval = 10000
            fastestInterval = 5000
        }

        checkLocationPermission()
    }

    override fun onMapReady(googleMap: GoogleMap) {
        mMap = googleMap
    }

    private fun checkLocationPermission() {
        if (ContextCompat.checkSelfPermission(
                requireContext(),
                Manifest.permission.ACCESS_FINE_LOCATION
            ) == PackageManager.PERMISSION_GRANTED
        ) {
            checkLocationSettings()
        } else {
            requestPermissions(arrayOf(Manifest.permission.ACCESS_FINE_LOCATION), LOCATION_PERMISSION_CODE)
        }
    }

    private fun checkLocationSettings() {
        val builder = LocationSettingsRequest.Builder().addLocationRequest(locationRequest)
        settingsClient.checkLocationSettings(builder.build())
            .addOnSuccessListener { getUserLocation() }
            .addOnFailureListener { exception ->
                if (exception is ResolvableApiException) {
                    try {
                        exception.startResolutionForResult(requireActivity(), LOCATION_REQUEST_CODE)
                    } catch (sendEx: IntentSender.SendIntentException) {
                        Toast.makeText(requireContext(), "Impossible d'activer la localisation", Toast.LENGTH_LONG).show()
                    }
                } else {
                    Toast.makeText(requireContext(), "Localisation désactivée", Toast.LENGTH_LONG).show()
                }
            }
    }

    private fun getUserLocation() {
        fusedLocationClient.lastLocation.addOnSuccessListener { location ->
            if (location != null) {
                userLocation = location
                showUserMarker(location)
                loadOffersFromFirebase()
            } else {
                fusedLocationClient.getCurrentLocation(
                    Priority.PRIORITY_HIGH_ACCURACY,
                    CancellationTokenSource().token
                ).addOnSuccessListener { newLocation ->
                    if (newLocation != null) {
                        userLocation = newLocation
                        showUserMarker(newLocation)
                        loadOffersFromFirebase()
                    } else {
                        Toast.makeText(requireContext(), "Impossible de récupérer la localisation", Toast.LENGTH_LONG).show()
                    }
                }
            }
        }
    }

    private fun showUserMarker(location: Location) {
        val userLatLng = LatLng(location.latitude, location.longitude)
        mMap.clear()
        mMap.addMarker(
            MarkerOptions()
                .position(userLatLng)
                .title("Vous êtes ici")
                .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_AZURE))
        )
        mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(userLatLng, 14f))
    }

    private fun loadOffersFromFirebase() {
        val dbRef = FirebaseDatabase.getInstance().getReference("offers")

        dbRef.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                for (child in snapshot.children) {
                    val lat = child.child("latitude").getValue(Double::class.java)
                    val lon = child.child("longitude").getValue(Double::class.java)
                    val title = child.child("title").getValue(String::class.java)
                    val price = child.child("price").getValue(Double::class.java)

                    if (lat != null && lon != null && title != null && price != null) {
                        val offerLatLng = LatLng(lat, lon)
                        val distanceText = calculateDistanceToUser(lat, lon)

                        val marker = mMap.addMarker(
                            MarkerOptions()
                                .position(offerLatLng)
                                .title(title)
                                .snippet("${price.toInt()} MAD - $distanceText")
                                .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_ORANGE))
                        )

                        // 🎯 Associer l'ID de l'offre au marqueur
                        marker?.tag = child.key  // child.key est l'ID Firebase (offerId)
                    }
                }

                // 🎯 Gérer le clic sur les marqueurs pour afficher le pop-up ou aller aux détails
                mMap.setOnMarkerClickListener { marker ->
                    val offerId = marker.tag as? String
                    if (offerId != null) {
                        showPopupOrDetails(marker.title ?: "", offerId)
                    } else {
                        Toast.makeText(requireContext(), "Erreur : ID de l'offre manquant", Toast.LENGTH_SHORT).show()
                    }
                    true
                }
            }

            override fun onCancelled(error: DatabaseError) {
                Toast.makeText(requireContext(), "Erreur chargement des offres", Toast.LENGTH_SHORT).show()
            }
        })
    }
    private fun showPopupOrDetails(title: String, offerId: String) {
        AlertDialog.Builder(requireContext())
            .setTitle(title)
            .setMessage("Souhaitez-vous voir les détails de cette offre ?")
            .setPositiveButton("Voir détails") { _, _ ->
                val intent = Intent(requireContext(), DetailsArticleActivity::class.java)
                intent.putExtra("extra_offer_id", offerId)
                startActivity(intent)
            }
            .setNegativeButton("Annuler", null)
            .show()
    }



    private fun calculateDistanceToUser(lat: Double, lon: Double): String {
        userLocation?.let { userLoc ->
            val offerLocation = Location("").apply {
                latitude = lat
                longitude = lon
            }
            val distanceInMeters = userLoc.distanceTo(offerLocation)
            return if (distanceInMeters >= 1000) {
                String.format("%.2f km", distanceInMeters / 1000)
            } else {
                "${distanceInMeters.toInt()} m"
            }
        }
        return "Distance inconnue"
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<String>, grantResults: IntArray) {
        if (requestCode == LOCATION_PERMISSION_CODE) {
            if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                checkLocationSettings()
            } else {
                Toast.makeText(requireContext(), "Permission localisation refusée", Toast.LENGTH_LONG).show()
            }
        }
    }
}