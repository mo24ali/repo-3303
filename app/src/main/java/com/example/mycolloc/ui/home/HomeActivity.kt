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
    private val homeViewModel: HomeViewModel by viewModels()
    private val authViewModel: AuthViewModel by viewModels()
    private lateinit var viewPagerAdapter: HomeViewPagerAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityHomeBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setSupportActionBar(binding.toolbar)
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
        homeViewModel.uiState.observe(this) { state ->
            when (state) {
                is HomeUiState.Loading -> showLoading(true)
                is HomeUiState.Success -> showLoading(false)
                is HomeUiState.Error -> {
                    showLoading(false)
                    showError(state.message)
                }
            }
        }

        authViewModel.currentUser.observe(this) { user ->
            // Update UI based on user role
            invalidateOptionsMenu()
        }
    }

    private fun setupClickListeners() {
        binding.fabAddOffer.setOnClickListener {
            startActivity(Intent(this, PostOfferActivity::class.java))
        }
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        menuInflater.inflate(R.menu.menu_home, menu)

        // Show admin menu items only for admin users
        menu.findItem(R.id.action_admin_dashboard)?.isVisible = homeViewModel.isUserAdmin()

        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            R.id.action_logout -> {
                authViewModel.signOut()
                navigateToLogin()
                true
            }
            R.id.action_admin_dashboard -> {
                // TODO: Navigate to admin dashboard
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }

    private fun showLoading(show: Boolean) {
        binding.progressBar.visibility = if (show) View.VISIBLE else View.GONE
    }

    private fun showError(message: String) {
        Snackbar.make(binding.root, message, Snackbar.LENGTH_LONG).show()
    }

    private fun navigateToLogin() {
        startActivity(Intent(this, LoginActivity::class.java))
        finish()
    }
} 