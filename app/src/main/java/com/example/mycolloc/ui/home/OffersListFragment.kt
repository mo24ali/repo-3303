package com.example.mycolloc.ui.home

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.mycolloc.databinding.FragmentOffersListBinding
import com.example.mycolloc.ui.details.DetailsArticleActivity
import com.example.mycolloc.viewmodels.HomeUiState
import com.example.mycolloc.viewmodels.HomeViewModel

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
        observeViewModel()
    }

    private fun setupRecyclerView() {
        offersAdapter = OffersAdapter { offer ->
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

    private fun observeViewModel() {
        viewModel.uiState.observe(viewLifecycleOwner) { state ->
            when (state) {
                is HomeUiState.Loading -> {
                    binding.progressBar.isVisible = true
                    binding.recyclerView.isVisible = false
                    binding.emptyView.isVisible = false
                }
                is HomeUiState.Success -> {
                    binding.progressBar.isVisible = false
                    binding.recyclerView.isVisible = true
                    binding.emptyView.isVisible = state.offers.isEmpty()
                    offersAdapter.submitList(state.offers)
                }
                is HomeUiState.Error -> {
                    binding.progressBar.isVisible = false
                    binding.recyclerView.isVisible = false
                    binding.emptyView.isVisible = true
                    showError(state.message)
                }

                else -> {}
            }
        }
    }

    private fun showError(message: String) {
        Toast.makeText(requireContext(), message, Toast.LENGTH_LONG).show()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
