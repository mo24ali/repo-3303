package com.example.mycolloc.ui.home

import android.Manifest
import android.app.Activity
import android.content.IntentSender
import android.content.pm.PackageManager
import android.location.Location
import android.os.Bundle
import android.provider.Settings
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import com.example.mycolloc.R
import com.example.mycolloc.viewmodels.HomeViewModel
import com.google.android.gms.location.*
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.BitmapDescriptorFactory
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.MarkerOptions

class MapFragment : Fragment(), OnMapReadyCallback {

    private lateinit var mMap: GoogleMap
    private lateinit var fusedLocationClient: FusedLocationProviderClient
    private lateinit var locationRequest: LocationRequest
    private lateinit var settingsClient: SettingsClient
    private val viewModel: HomeViewModel by activityViewModels()
    private val LOCATION_PERMISSION_CODE = 1001
    private val LOCATION_REQUEST_CODE = 2001

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
                showUserAndOffers(location)
            } else {
                // Fallback : demander une localisation active
                fusedLocationClient.getCurrentLocation(
                    Priority.PRIORITY_HIGH_ACCURACY,
                    CancellationTokenSource().token
                ).addOnSuccessListener { newLocation ->
                    if (newLocation != null) {
                        showUserAndOffers(newLocation)
                    } else {
                        Toast.makeText(requireContext(), "Impossible de récupérer la localisation", Toast.LENGTH_LONG).show()
                    }
                }
            }
        }
    }

    private fun showUserAndOffers(location: Location) {
        val userLatLng = LatLng(location.latitude, location.longitude)
        mMap.clear()
        mMap.addMarker(
            MarkerOptions()
                .position(userLatLng)
                .title("Vous êtes ici")
                .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_BLUE))
        )
        mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(userLatLng, 14f))

        viewModel.loadNearbyOffers(location.latitude, location.longitude)
        observeNearbyOffers()
    }

    private fun observeNearbyOffers() {
        viewModel.nearbyOffers.observe(viewLifecycleOwner) { offers ->
            offers.forEach { offer ->
                val lat = offer.latitude
                val lon = offer.longitude
                if (lat != null && lon != null) {
                    mMap.addMarker(
                        MarkerOptions()
                            .position(LatLng(lat, lon))
                            .title(offer.title)
                            .snippet("${offer.price} MAD")
                    )
                }
            }
        }
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
