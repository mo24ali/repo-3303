package com.example.mycolloc.ui.home

import android.content.Intent
import android.location.Geocoder
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import android.view.*
import android.widget.Toast
import androidx.core.widget.addTextChangedListener
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.mycolloc.databinding.FragmentHomeBinding
import com.example.mycolloc.model.Offer
import com.example.mycolloc.ui.details.DetailsArticleActivity
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.*
import java.util.Locale

class HomeFragment : Fragment() {

    private var _binding: FragmentHomeBinding? = null
    private val binding get() = _binding!!

    private lateinit var offersAdapter: OffersAdapter
    private lateinit var databaseRef: DatabaseReference
    val user = FirebaseAuth.getInstance().currentUser
    val userName = user?.displayName ?: "there" // fallback



    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentHomeBinding.inflate(inflater, container, false)

        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setupRecyclerView()
        databaseRef = FirebaseDatabase.getInstance().getReference("offers")
        val uid = FirebaseAuth.getInstance().currentUser?.uid
        if (uid != null) {
            val userRef = FirebaseDatabase.getInstance().getReference("users").child(uid)

            userRef.addListenerForSingleValueEvent(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    val firstName = snapshot.child("firstName").getValue(String::class.java)
                    val latitude = snapshot.child("latitude").getValue(Double::class.java)
                    val longitude = snapshot.child("longitude").getValue(Double::class.java)
                    if (!firstName.isNullOrEmpty()) {
                        binding.welcomingText.text = "Bonjour $firstName,"
                        //binding.userLocation.text = ""
                    } else {
                        binding.welcomingText.text = "Bonjour,"
                        //binding.userLocation.text = "Los Angeles, CA"
                    }
                    if (latitude != null && longitude != null) {
                        val geocoder = Geocoder(requireContext(), Locale.getDefault())
                        try {
                            val addresses = geocoder.getFromLocation(latitude, longitude, 1)
                            if (!addresses.isNullOrEmpty()) {
                                val cityName = addresses[0].locality ?: addresses[0].subAdminArea ?: "Unknown City"
                                binding.userLocation.text = cityName
                            } else {
                                binding.userLocation.text = "Unknown Location"
                            }
                        } catch (e: Exception) {
                            e.printStackTrace()
                            binding.userLocation.text = "Location Error"
                        }
                    } else {
                        binding.userLocation.text = "Unknown Location"
                    }
                }

                override fun onCancelled(error: DatabaseError) {
                    binding.welcomingText.text = "Good Morning,"
                    binding.userLocation.text = "Los Angeles, CA"
                }
            })
        }
        fetchAllOffersFromFirebase()
        fetchOffers()
    }

    private fun setupRecyclerView() {
        offersAdapter = OffersAdapter { offer: Offer ->
            val intent = Intent(requireContext(), DetailsArticleActivity::class.java).apply {
                putExtra(DetailsArticleActivity.EXTRA_OFFER_ID, offer.id)
            }
            startActivity(intent)
        }

        val layoutManager = LinearLayoutManager(requireContext(), LinearLayoutManager.VERTICAL, false)
        layoutManager.initialPrefetchItemCount = 6

        binding.popularRecyclerView.apply {
            this.layoutManager = layoutManager
            adapter = offersAdapter
            setHasFixedSize(true)
            recycledViewPool.setMaxRecycledViews(0, 10)
        }

        binding.searchEditText.addTextChangedListener { editable ->
            val query = editable.toString().trim()
            if (query.isNotEmpty()) {
                searchOffersByCity(query)
            } else {
                fetchAllOffersFromFirebase()
            }
        }
    }

    //    private fun fetchOffers() {
//        databaseRef.addValueEventListener(object : ValueEventListener {
//            override fun onDataChange(snapshot: DataSnapshot) {
//                val offerList = mutableListOf<Offer>()
//                for (offerSnap in snapshot.children) {
//                    val offer = offerSnap.getValue(Offer::class.java)
//                    if (offer != null) {
//                        offerList.add(offer)
//                    }
//                }
//                offersAdapter.submitList(offerList)
//            }
//
//            override fun onCancelled(error: DatabaseError) {
//                Toast.makeText(requireContext(), "Erreur Firebase : ${error.message}", Toast.LENGTH_LONG).show()
//            }
//        })
//    }
    private fun fetchOffers() {
        databaseRef.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val offerList = mutableListOf<Offer>()
                for (offerSnap in snapshot.children) {
                    try {
                        val id = offerSnap.child("id").getValue(String::class.java) ?: ""
                        val userId = offerSnap.child("userId").getValue(String::class.java) ?: ""
                        val title = offerSnap.child("title").getValue(String::class.java) ?: ""
                        val description = offerSnap.child("description").getValue(String::class.java) ?: ""
                        val price = offerSnap.child("price").getValue(Double::class.java) ?: 0.0
                        val category = offerSnap.child("category").getValue(String::class.java) ?: ""
                        val imageUrl = offerSnap.child("imageUrl").getValue(String::class.java) ?: ""
                        val isActive = offerSnap.child("isActive").getValue(Boolean::class.java) ?: true
                        val createdAt = offerSnap.child("createdAt").getValue(Long::class.java) ?: 0L
                        val updatedAt = offerSnap.child("updatedAt").getValue(Long::class.java) ?: 0L

                        // Location manuelle
                        val locationSnap = offerSnap.child("location")
                        val city = locationSnap.child("city").getValue(String::class.java)
                        val address = locationSnap.child("address").getValue(String::class.java)
                        val latitude = locationSnap.child("latitude").getValue(Double::class.java) ?: 0.0
                        val longitude = locationSnap.child("longitude").getValue(Double::class.java) ?: 0.0
                        val location = com.example.mycolloc.data.local.Location(city, address, latitude, longitude)

                        // Images (liste)
                        val images = offerSnap.child("images").children.mapNotNull { it.getValue(String::class.java) }

                        val offer = Offer(
                            id = id,
                            userId = userId,
                            title = title,
                            description = description,
                            price = price,
                            category = category,
                            imageUrl = imageUrl,
                            images = images,
                            latitude = latitude,
                            longitude = longitude,
                            isActive = isActive,
                            location = location,
                            createdAt = createdAt,
                            updatedAt = updatedAt
                        )
                        offerList.add(offer)
                    } catch (e: Exception) {
                        Log.e("FirebaseParseError", "Erreur offre ${offerSnap.key}", e)
                    }
                }
                offersAdapter.submitList(offerList)
            }

            override fun onCancelled(error: DatabaseError) {
                Toast.makeText(requireContext(), "Erreur Firebase : ${error.message}", Toast.LENGTH_LONG).show()
            }
        })
    }

    private fun setupSearchBox() {
        binding.searchEditText.addTextChangedListener(object : TextWatcher {
            override fun afterTextChanged(s: Editable?) {
                val query = s.toString().trim()
                if (query.isNotEmpty()) {
                    searchOffersByCity(query)
                } else {
                    // Recharge toutes les offres si le champ est vide
                    fetchAllOffersFromFirebase()
                }
            }

            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
        })
    }

    private fun fetchAllOffersFromFirebase() {
        val databaseRef = FirebaseDatabase.getInstance().getReference("offers")

        databaseRef.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val offersList = mutableListOf<Offer>()

                snapshot.children.forEach { child ->
                    try {
                        val offer = child.getValue(Offer::class.java)
                        if (offer != null) {
                            offersList.add(offer)
                        }
                    } catch (e: Exception) {
                        // 🔥 Log the corrupted offer for future cleanup
                        Log.e("FirebaseParseError", "Invalid offer format at ${child.key}", e)
                    }
                }

                offersAdapter.submitList(offersList)

            }

            override fun onCancelled(error: DatabaseError) {
                Toast.makeText(requireContext(), "Erreur : ${error.message}", Toast.LENGTH_SHORT).show()
            }
        })
    }

    private fun searchOffersByCity(cityQuery: String) {
        val databaseRef = FirebaseDatabase.getInstance().getReference("offers")

        databaseRef.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val filteredOffers = mutableListOf<Offer>()

                for (offerSnap in snapshot.children) {
                    val offer = offerSnap.getValue(Offer::class.java)
                    if (offer != null && offer.location?.city?.contains(cityQuery, ignoreCase = true) == true) {
                        filteredOffers.add(offer)
                    }
                }

                offersAdapter.submitList(filteredOffers)
            }

            override fun onCancelled(error: DatabaseError) {
                Toast.makeText(requireContext(), "Error: ${error.message}", Toast.LENGTH_SHORT).show()
            }
        })
    }


    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}