package com.example.flowdesk_android.feature.mypage.presentation.edit_profile

import android.os.Bundle
import android.view.View
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.updatePadding
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.navigation.fragment.findNavController
import com.example.flowdesk_android.R
import com.example.flowdesk_android.databinding.FragmentMypageEditProfileBinding
import com.example.flowdesk_android.core.extension.showTopToast
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch

@AndroidEntryPoint
class EditProfileFragment : Fragment(R.layout.fragment_mypage_edit_profile) {

    private var _binding: FragmentMypageEditProfileBinding? = null
    private val binding get() = _binding!!
    private val viewModel: EditProfileViewModel by viewModels()

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        _binding = FragmentMypageEditProfileBinding.bind(view)

        setupListeners()
        observeViewModel()
        
        // Initial Load
        viewModel.triggerRefresh()
    }

    private fun setupListeners() {
        binding.btnSave.setOnClickListener {
            val name = binding.etUserName.text.toString()
            val email = binding.etEmail.text.toString()
            val corpName = binding.etCorpName.text.toString()
            val tel = binding.etTel.text.toString()
            val hp = binding.etHp.text.toString()

            if (name.isBlank() || email.isBlank()) {
                showTopToast(getString(R.string.mypage_msg_name_email_required))
                return@setOnClickListener
            }

            viewModel.updateProfile(name, email, corpName, tel, hp)
        }

        binding.btnChangePassword.setOnClickListener {
             findNavController().navigate(R.id.changePasswordFragment)
        }
    }

    private fun observeViewModel() {
        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                launch {
                    viewModel.uiState.collect { state ->
                        when (state) {
                            is EditProfileUiState.Loading -> {
                                // Show loading indicator if avail
                            }
                            is EditProfileUiState.Success -> {
                                val user = state.user.user
                                binding.etCorpName.setText(user.corpName)
                                binding.etUserName.setText(user.name)
                                binding.etEmail.setText(user.email)
                                binding.etTel.setText(user.tel)
                                binding.etHp.setText(user.hp)
                            }
                            is EditProfileUiState.Error -> {
                                showTopToast(state.message.takeIf { it.isNotEmpty() } ?: getString(R.string.mypage_error_load_failed))
                            }
                        }
                    }
                }
                
                launch {
                    viewModel.event.collect { event ->
                        when (event) {
                            is EditProfileEvent.Updated -> {
                                showTopToast(getString(R.string.mypage_msg_profile_updated))
                                findNavController().navigateUp()
                            }
                            is EditProfileEvent.Error -> {
                                showTopToast(event.message.takeIf { it.isNotEmpty() } ?: getString(R.string.mypage_error_update_failed))
                            }
                        }
                    }
                }
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
