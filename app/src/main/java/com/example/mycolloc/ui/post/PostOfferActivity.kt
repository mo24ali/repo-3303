package com.example.mycolloc.ui.post

import android.os.Bundle
import android.view.MenuItem
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import com.example.mycolloc.databinding.ActivityPostOfferBinding
import com.example.mycolloc.viewmodels.PostOfferViewModel

class PostOfferActivity : AppCompatActivity() {
    private lateinit var binding: ActivityPostOfferBinding
    private val viewModel: PostOfferViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityPostOfferBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setSupportActionBar(binding.toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.title = "Post an Offer"

        setupViews()
        observeViewModel()
    }

    private fun setupViews() {
        // TODO: Implement view setup
    }

    private fun observeViewModel() {
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