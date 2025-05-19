package com.example.mycolloc.ui.home

import android.content.Intent
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import android.view.View
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.viewpager2.widget.ViewPager2
import com.example.mycolloc.R
import com.example.mycolloc.databinding.ActivityHomeBinding
import com.example.mycolloc.ui.auth.LoginActivity
import com.example.mycolloc.ui.post.PostOfferActivity
import com.example.mycolloc.viewmodels.AuthViewModel
import com.example.mycolloc.viewmodels.HomeViewModel
import com.example.mycolloc.viewmodels.HomeUiState
import com.google.android.material.snackbar.Snackbar
import com.google.android.material.tabs.TabLayoutMediator

class HomeActivity : AppCompatActivity() {
    private lateinit var binding: ActivityHomeBinding
    private val authViewModel: AuthViewModel by viewModels()
    private val homeViewModel: HomeViewModel by viewModels()
    private lateinit var viewPagerAdapter: HomeViewPagerAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityHomeBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setupViewPager()
        setupObservers()
        setupClickListeners()
    }

    private fun setupViewPager() {
        viewPagerAdapter = HomeViewPagerAdapter(this)
        binding.viewPager.adapter = viewPagerAdapter

        TabLayoutMediator(binding.tabLayout, binding.viewPager) { tab, position ->
            tab.text = when (position) {
                0 -> "List"
                1 -> "Map"
                else -> null
            }
        }.attach()

        binding.viewPager.registerOnPageChangeCallback(object : ViewPager2.OnPageChangeCallback() {
            override fun onPageSelected(position: Int) {
                super.onPageSelected(position)
                if (position == 1) { // Map tab
                    // Request location updates when map tab is selected
                    (viewPagerAdapter.getFragment(1) as? MapFragment)?.requestLocationUpdates()
                }
            }
        })
    }

    private fun setupObservers() {
        // Observe authentication state
        authViewModel.currentUser.observe(this) { user ->
            if (user == null) {
                // User is not logged in, navigate to login screen
                startActivity(Intent(this, LoginActivity::class.java))
                finish()
            }
        }

        // Observe home state
        homeViewModel.uiState.observe(this) { state ->
            when (state) {
                is HomeUiState.Loading -> {
                    showLoading(true)
                    binding.fabAddOffer.isEnabled = false
                }
                is HomeUiState.Success -> {
                    showLoading(false)
                    binding.fabAddOffer.isEnabled = true
                }
                is HomeUiState.Error -> {
                    showLoading(false)
                    binding.fabAddOffer.isEnabled = true
                    showError(state.message)
                }
            }
        }
    }

    private fun setupClickListeners() {
        binding.fabAddOffer.setOnClickListener {
            startActivity(Intent(this, PostOfferActivity::class.java))
        }
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        menuInflater.inflate(R.menu.menu_home, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            R.id.action_logout -> {
                logout()
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }

    private fun logout() {
        authViewModel.signOut()
    }

    private fun showLoading(show: Boolean) {
        binding.progressBar.visibility = if (show) View.VISIBLE else View.GONE
    }

    private fun showError(message: String) {
        Snackbar.make(binding.root, message, Snackbar.LENGTH_LONG).show()
    }
} 