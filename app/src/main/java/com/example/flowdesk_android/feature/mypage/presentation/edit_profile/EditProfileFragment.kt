package com.example.flowdesk_android.feature.mypage.presentation.edit_profile

import android.os.Bundle
import android.view.View
import android.widget.Toast
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

        // Handle Insets
        ViewCompat.setOnApplyWindowInsetsListener(binding.topBar) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.updatePadding(top = systemBars.top)
            insets
        }

        setupListeners()
        observeViewModel()
        
        // Initial Load
        viewModel.loadProfile()
    }

    private fun setupListeners() {
        binding.btnBack.setOnClickListener {
            findNavController().navigateUp()
        }

        binding.btnSave.setOnClickListener {
            val name = binding.etUserName.text.toString()
            val email = binding.etEmail.text.toString()
            val corpName = binding.etCorpName.text.toString()
            val tel = binding.etTel.text.toString()
            val hp = binding.etHp.text.toString()

            if (name.isBlank() || email.isBlank()) {
                Toast.makeText(context, "이름과 이메일은 필수입니다.", Toast.LENGTH_SHORT).show()
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
                                Toast.makeText(context, state.message, Toast.LENGTH_SHORT).show()
                            }
                        }
                    }
                }
                
                launch {
                    viewModel.event.collect { event ->
                        when (event) {
                            is EditProfileEvent.Updated -> {
                                Toast.makeText(context, "정보가 수정되었습니다.", Toast.LENGTH_SHORT).show()
                                findNavController().navigateUp()
                            }
                            is EditProfileEvent.Error -> {
                                Toast.makeText(context, event.message, Toast.LENGTH_SHORT).show()
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
