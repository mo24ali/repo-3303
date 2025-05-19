package com.example.mycolloc.ui.details

import android.os.Bundle
import android.view.MenuItem
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import com.example.mycolloc.databinding.ActivityDetailsArticleBinding
import com.example.mycolloc.viewmodels.DetailsViewModel

class DetailsArticleActivity : AppCompatActivity() {
    private lateinit var binding: ActivityDetailsArticleBinding
    private val viewModel: DetailsViewModel by viewModels()

    companion object {
        const val EXTRA_OFFER_ID = "extra_offer_id"
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityDetailsArticleBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setSupportActionBar(binding.toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)

        val offerId = intent.getStringExtra(EXTRA_OFFER_ID)
        if (offerId == null) {
            finish()
            return
        }

        setupViews()
        observeViewModel(offerId)
    }

    private fun setupViews() {
        // TODO: Implement view setup
    }

    private fun observeViewModel(offerId: String) {
        // TODO: Implement view model observation
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            android.R.id.home -> {
                onBackPressed()
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }
} 