package com.example.mycolloc.ui.profile

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import com.example.mycolloc.databinding.FragmentProfileBinding
import com.example.mycolloc.viewmodels.AuthViewModel

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
    }

    private fun setupUI() {
        binding.editProfileButton.setOnClickListener {
            // TODO: Navigate to edit profile
        }

        binding.myOffersButton.setOnClickListener {
            // TODO: Navigate to my offers
        }

        binding.settingsButton.setOnClickListener {
            // TODO: Navigate to settings
        }

        binding.logoutButton.setOnClickListener {
            authViewModel.signOut()
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
