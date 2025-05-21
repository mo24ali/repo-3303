package com.example.mycolloc.ui.profile

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.bumptech.glide.Glide
import com.example.mycolloc.data.local.AppDatabase
import com.example.mycolloc.databinding.ActivityEditProfileBinding
import com.example.mycolloc.profilePic.ProfilePicture
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.*
import kotlinx.coroutines.launch

class EditProfileActivity : AppCompatActivity() {

    private lateinit var binding: ActivityEditProfileBinding
    private lateinit var database: DatabaseReference
    private val currentUser = FirebaseAuth.getInstance().currentUser
    private var selectedImagePath: String? = null
    private val IMAGE_PICK_CODE = 1000
    private val uid: String = currentUser?.uid ?: ""

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityEditProfileBinding.inflate(layoutInflater)
        setContentView(binding.root)

        database = FirebaseDatabase.getInstance().reference

        loadUserData()
        loadProfilePicture()
        setupListeners()
    }

    private fun loadUserData() {
        currentUser?.let { user ->
            database.child("users").child(user.uid)
                .addListenerForSingleValueEvent(object : ValueEventListener {
                    override fun onDataChange(snapshot: DataSnapshot) {
                        val email = snapshot.child("email").getValue(String::class.java)
                        val phone = snapshot.child("phoneNumber").getValue(String::class.java)

                        binding.emailEditText.setText(email ?: "")
                        binding.phoneEditText.setText(phone ?: "")
                    }

                    override fun onCancelled(error: DatabaseError) {
                        Toast.makeText(this@EditProfileActivity, "Failed to load data", Toast.LENGTH_SHORT).show()
                    }
                })
        }
    }
    private fun loadProfilePicture() {
        lifecycleScope.launch {
            val db = AppDatabase.getDatabase(applicationContext)
            val picture = db.profilePictureDao().getProfilePicture(uid)

            picture?.let {
                val uri = Uri.parse(it.localPath)
                //binding.profileImageView.setImageURI(uri)
                Glide.with(this@EditProfileActivity)
                    .load(uri)
                    .into(binding.profileImageView)
            }
        }
    }
    private fun setupListeners() {
        // 🔹 Choisir une image depuis la galerie
        binding.editImageIcon.setOnClickListener {
            val intent = Intent(Intent.ACTION_PICK).apply {
                type = "image/*"
            }
            startActivityForResult(intent, IMAGE_PICK_CODE)
        }

        // 🔹 Sauvegarder les modifications
        binding.saveProfileButton.setOnClickListener {
            val email = binding.emailEditText.text.toString().trim()
            val phone = binding.phoneEditText.text.toString().trim()

            if (email.isEmpty() || phone.isEmpty()) {
                Toast.makeText(this, "Please fill all fields", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            // 🔸 Mettre à jour email et téléphone dans Firebase
            currentUser?.let { user ->
                val updates = mapOf(
                    "email" to email,
                    "phoneNumber" to phone
                )

                database.child("users").child(user.uid).updateChildren(updates)
                    .addOnSuccessListener {
                        Toast.makeText(this, "Profile updated", Toast.LENGTH_SHORT).show()
                    }
                    .addOnFailureListener {
                        Toast.makeText(this, "Update failed", Toast.LENGTH_SHORT).show()
                    }
            }

            // 🔸 Enregistrer l’image localement
            selectedImagePath?.let { path ->
                lifecycleScope.launch {
                    val profilePicture = ProfilePicture(
                        userId = uid,
                        localPath = path
                    )

                    val db = AppDatabase.getDatabase(applicationContext)
                    db.profilePictureDao().insert(profilePicture)

                    Toast.makeText(this@EditProfileActivity, "Profile picture saved locally", Toast.LENGTH_SHORT).show()
                    finish()
                }
            } ?: run {
                // si aucune image n'a été choisie, ignorer l'enregistrement local mais terminer l'activité
                finish()
            }
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == IMAGE_PICK_CODE && resultCode == RESULT_OK && data != null) {
            val imageUri: Uri? = data.data
            selectedImagePath = imageUri.toString()
            binding.profileImageView.setImageURI(imageUri)
        }
    }
}
