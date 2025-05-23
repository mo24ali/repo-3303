package com.example.mycolloc.ui.post

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.mycolloc.databinding.FragmentFavoritesOffersBinding
import com.example.mycolloc.model.Offer
import com.example.mycolloc.Adapter.OffersAdapter
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.*

class FavoritesOffersFragment : Fragment() {

    private lateinit var binding: FragmentFavoritesOffersBinding
    private lateinit var adapter: OffersAdapter
    private val database = FirebaseDatabase.getInstance()
    private val currentUser = FirebaseAuth.getInstance().currentUser

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentFavoritesOffersBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        adapter = OffersAdapter(
            onClick = { offer ->
                // TODO: Gérer le clic sur une offre favorite (détails)
            },
            isMyOffersContext = false
        )

        binding.favoritesRecyclerView.layoutManager = LinearLayoutManager(requireContext())
        binding.favoritesRecyclerView.adapter = adapter

        loadFavorites()
    }

    private fun loadFavorites() {
        if (currentUser == null) {
            Toast.makeText(requireContext(), "Non connecté", Toast.LENGTH_SHORT).show()
            return
        }

        val ref = database.getReference("favorites").child(currentUser.uid)
        ref.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val favorites = snapshot.children.mapNotNull { it.getValue(Offer::class.java) }

                if (favorites.isEmpty()) {
                    binding.favoritesRecyclerView.visibility = View.GONE
                    binding.emptyStateLayout.visibility = View.VISIBLE
                } else {
                    binding.favoritesRecyclerView.visibility = View.VISIBLE
                    binding.emptyStateLayout.visibility = View.GONE
                    adapter.submitList(favorites)
                }
            }

            override fun onCancelled(error: DatabaseError) {
                Toast.makeText(requireContext(), "Erreur : ${error.message}", Toast.LENGTH_SHORT).show()
            }
        })
    }
}
