package com.example.flowdesk_android.presentation.ui.mypage

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
import com.example.flowdesk_android.databinding.FragmentEditProfileBinding
import com.example.flowdesk_android.presentation.viewmodel.EditProfileViewModel
import com.example.flowdesk_android.presentation.viewmodel.EditProfileState
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch

@AndroidEntryPoint
class EditProfileFragment : Fragment(R.layout.fragment_edit_profile) {

    private var _binding: FragmentEditProfileBinding? = null
    private val binding get() = _binding!!
    private val viewModel: EditProfileViewModel by viewModels()

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        _binding = FragmentEditProfileBinding.bind(view)

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
            val tel = binding.etTel.text.toString()
            val hp = binding.etHp.text.toString()

            if (name.isBlank() || email.isBlank()) {
                Toast.makeText(context, "이름과 이메일은 필수입니다.", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            viewModel.updateProfile(name, email, tel, hp)
        }

        binding.btnChangePassword.setOnClickListener {
             Toast.makeText(context, "비밀번호 변경 기능은 준비 중입니다.", Toast.LENGTH_SHORT).show()
        }
    }

    private fun observeViewModel() {
        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.state.collect { state ->
                    when (state) {
                        is EditProfileState.Loading -> {
                            // Show loading indicator if avail
                        }
                        is EditProfileState.LoadSuccess -> {
                            val user = state.user
                            binding.etCorpName.setText(user.corpName)
                            binding.etUserName.setText(user.userName)
                            binding.etEmail.setText(user.userEmail)
                            binding.etTel.setText(user.userTel)
                            binding.etHp.setText(user.userHp)
                        }
                        is EditProfileState.UpdateSuccess -> {
                            Toast.makeText(context, "정보가 수정되었습니다.", Toast.LENGTH_SHORT).show()
                            findNavController().navigateUp()
                        }
                        is EditProfileState.Error -> {
                            Toast.makeText(context, state.message, Toast.LENGTH_SHORT).show()
                        }
                        else -> {}
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
