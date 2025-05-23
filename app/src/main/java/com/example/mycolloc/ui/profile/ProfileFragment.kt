package com.example.mycolloc.ui.profile

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.lifecycleScope
import com.bumptech.glide.Glide
import com.example.mycolloc.data.local.AppDatabase
import com.example.mycolloc.databinding.FragmentProfileBinding
import com.example.mycolloc.viewmodels.AuthViewModel
import com.google.firebase.auth.FirebaseAuth
import kotlinx.coroutines.launch

class ProfileFragment : Fragment() {

    private var _binding: FragmentProfileBinding? = null
    private val binding get() = _binding!!
    private val authViewModel: AuthViewModel by activityViewModels()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentProfileBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setupUI()
        observeUser()
        loadProfilePicture()

    }

    private fun loadProfilePicture() {
        val uid = FirebaseAuth.getInstance().currentUser?.uid ?: return

        lifecycleScope.launch {
            val db = AppDatabase.getDatabase(requireContext())
            val picture = db.profilePictureDao().getProfilePicture(uid)

            picture?.let {
                val uri = Uri.parse(it.localPath)
                //binding.profileImage.setImageURI(uri)
                Glide.with(requireContext())
                    .load(uri)
                    .into(binding.profileImage)

            }
        }
    }

    private fun setupUI() {
        binding.logoutButton.setOnClickListener {
            authViewModel.signOut()
            Toast.makeText(requireContext(), "Déconnecté", Toast.LENGTH_SHORT).show()
        }


        binding.myOffersButton.setOnClickListener {
            startActivity(Intent(requireContext(), com.example.mycolloc.ui.profile.MyOfffersActivity::class.java))
        }

        binding.showb.setOnClickListener {
            startActivity(Intent(requireContext(), com.example.mycolloc.ui.profile.activity_my_bids::class.java))
        }


    }


    private fun observeUser() {
        authViewModel.currentUser.observe(viewLifecycleOwner) { user ->
            user?.let {
                binding.userNameText.text = "${it.firstName} ${it.lastName}"
                binding.userEmailText.text = it.email
                binding.userPhoneText.text = it.phoneNumber
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
