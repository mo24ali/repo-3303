package com.example.mycolloc.ui.home

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.mycolloc.databinding.FragmentOffersListBinding
import com.example.mycolloc.ui.details.DetailsArticleActivity
import com.example.mycolloc.viewmodels.HomeViewModel
import com.example.mycolloc.viewmodels.HomeUiState

class OffersListFragment : Fragment() {
    private var _binding: FragmentOffersListBinding? = null
    private val binding get() = _binding!!
    private val viewModel: HomeViewModel by activityViewModels()
    private lateinit var offersAdapter: OffersAdapter

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentOffersListBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setupRecyclerView()
        observeOffers()
    }

    private fun setupRecyclerView() {
        offersAdapter = OffersAdapter { offer ->
            // Navigate to offer details
            val intent = Intent(requireContext(), DetailsArticleActivity::class.java).apply {
                putExtra(DetailsArticleActivity.EXTRA_OFFER_ID, offer.id)
            }
            startActivity(intent)
        }

        binding.recyclerView.apply {
            layoutManager = LinearLayoutManager(requireContext())
            adapter = offersAdapter
        }
    }

    private fun observeOffers() {
        viewModel.offers.observe(viewLifecycleOwner) { offers ->
            offersAdapter.submitList(offers)
        }

        viewModel.uiState.observe(viewLifecycleOwner) { state ->
            when (state) {
                is HomeUiState.Loading -> {
                    binding.progressBar.visibility = View.VISIBLE
                    binding.recyclerView.visibility = View.GONE
                }
                is HomeUiState.Success -> {
                    binding.progressBar.visibility = View.GONE
                    binding.recyclerView.visibility = View.VISIBLE
                }
                is HomeUiState.Error -> {
                    binding.progressBar.visibility = View.GONE
                    binding.recyclerView.visibility = View.VISIBLE
                    // Error is handled by the activity
                }
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
} 