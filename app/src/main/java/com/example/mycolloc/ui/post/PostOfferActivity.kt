package com.example.mycolloc.ui.post

import android.net.Uri
import android.os.Bundle
import android.view.MenuItem
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.mycolloc.Adapter.SelectedImagesAdapter
import com.example.mycolloc.databinding.ActivityPostOfferBinding
import com.example.mycolloc.model.Location
import com.example.mycolloc.viewmodels.PostOfferUiState
import com.example.mycolloc.viewmodels.PostOfferViewModel

class PostOfferActivity : AppCompatActivity() {
    private lateinit var binding: ActivityPostOfferBinding
    private val viewModel: PostOfferViewModel by viewModels()

    private val selectedImages = mutableListOf<Uri>()
    private lateinit var imageAdapter: SelectedImagesAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityPostOfferBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setupViews()
        observeViewModel()
    }

    private fun setupViews() {
        imageAdapter = SelectedImagesAdapter(selectedImages)
        binding.imagesRecyclerView.apply {
            layoutManager = LinearLayoutManager(this@PostOfferActivity, LinearLayoutManager.HORIZONTAL, false)
            adapter = imageAdapter
        }

        binding.addImagesButton.setOnClickListener {
            imagePickerLauncher.launch("image/*")
        }

        binding.postButton.setOnClickListener {
            val title = binding.titleEditText.text.toString()
            val description = binding.descriptionEditText.text.toString()
            val price = binding.priceEditText.text.toString().toDoubleOrNull() ?: 0.0
            val images = selectedImages.map { it.toString() }

            val location = Location(
                latitude = 33.5731,
                longitude = -7.5898,
                address = "Casablanca",
                city = "Casablanca"
            )

            if (title.isBlank() || description.isBlank() || price <= 0.0) {
                Toast.makeText(this, "Veuillez remplir tous les champs", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            viewModel.createOffer(title, description, price, "Default", location, images)
        }
    }

    private fun observeViewModel() {
        viewModel.uiState.observe(this) { state ->
            when (state) {
                is PostOfferUiState.Loading -> {
                    binding.postButton.isEnabled = false
                    Toast.makeText(this, "Publication en cours...", Toast.LENGTH_SHORT).show()
                }
                is PostOfferUiState.Success -> {
                    binding.postButton.isEnabled = true
                    Toast.makeText(this, "Offre publiée avec succès", Toast.LENGTH_SHORT).show()
                    finish()
                }
                is PostOfferUiState.Error -> {
                    binding.postButton.isEnabled = true
                    Toast.makeText(this, "Erreur : ${state.message}", Toast.LENGTH_LONG).show()
                }
            }
        }
    }

    private val imagePickerLauncher = registerForActivityResult(
        ActivityResultContracts.GetMultipleContents()
    ) { uris ->
        if (!uris.isNullOrEmpty()) {
            selectedImages.clear()
            selectedImages.addAll(uris)
            imageAdapter.notifyDataSetChanged()
        }
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            android.R.id.home -> {
                onBackPressedDispatcher.onBackPressed()
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }
}
