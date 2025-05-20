package com.example.mycolloc.ui.home

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.NavHostFragment
import androidx.navigation.ui.setupWithNavController
import com.example.mycolloc.databinding.ActivityHomeBinding
import com.example.mycolloc.repository.FirebaseRepository
import com.example.mycolloc.ui.post.PostOfferActivity
import com.example.mycolloc.viewmodels.AuthState
import com.example.mycolloc.viewmodels.AuthViewModel
import com.example.mycolloc.viewmodels.HomeUiState
import com.example.mycolloc.viewmodels.HomeViewModel
import com.example.mycolloc.viewmodels.HomeViewModelFactory

class HomeActivity : AppCompatActivity() {
    private lateinit var binding: ActivityHomeBinding
    private lateinit var homeViewModel: HomeViewModel
    private lateinit var authViewModel: AuthViewModel

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityHomeBinding.inflate(layoutInflater)
        setContentView(binding.root)
        initViewModels()
        setupNavigation()
        setupObservers()
        setupClickListeners()
    }

    private fun initViewModels() {
        val repository = FirebaseRepository()
        val factory = HomeViewModelFactory(repository)
        homeViewModel = ViewModelProvider(this, factory)[HomeViewModel::class.java]
        authViewModel = ViewModelProvider(this)[AuthViewModel::class.java]
    }

    private fun setupNavigation() {
        val navHostFragment = supportFragmentManager
            .findFragmentById(com.example.mycolloc.R.id.nav_host_fragment) as NavHostFragment
        val navController = navHostFragment.navController

        binding.bottomNav.setupWithNavController(navController)

        navController.addOnDestinationChangedListener { _, destination, _ ->
            binding.fabAddOffer.visibility = when (destination.id) {
                com.example.mycolloc.R.id.navigation_home -> View.VISIBLE
                else -> View.GONE
            }
        }
    }

    private fun setupObservers() {
        homeViewModel.uiState.observe(this) { state ->
            when (state) {
                is HomeUiState.Loading -> {
                    binding.progressBar.visibility = View.VISIBLE
                }
                is HomeUiState.Success -> {
                    binding.progressBar.visibility = View.GONE
                }
                is HomeUiState.Error -> {
                    binding.progressBar.visibility = View.GONE
                    showError(state.message)
                }
            }
        }

        authViewModel.authState.observe(this) { state ->
            if (state is AuthState.SignedOut) {
                finish()
            }
        }
    }

    private fun setupClickListeners() {
        binding.fabAddOffer.setOnClickListener {
            startActivity(Intent(this, PostOfferActivity::class.java))
        }
    }

    private fun showError(message: String) {
        Toast.makeText(this, message, Toast.LENGTH_LONG).show()
    }
}
