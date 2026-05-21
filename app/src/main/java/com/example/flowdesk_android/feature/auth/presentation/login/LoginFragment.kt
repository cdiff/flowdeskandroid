package com.example.flowdesk_android.feature.auth.presentation.login

import android.os.Bundle
import android.text.method.HideReturnsTransformationMethod
import android.text.method.PasswordTransformationMethod
import android.view.View
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.navigation.fragment.findNavController
import com.example.flowdesk_android.R
import com.example.flowdesk_android.databinding.FragmentAuthLoginBinding
import com.example.flowdesk_android.feature.auth.domain.model.AuthUser
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch

@AndroidEntryPoint
class LoginFragment : Fragment(R.layout.fragment_auth_login) {

    private var _binding: FragmentAuthLoginBinding? = null
    private val binding get() = _binding!!
    private val viewModel: LoginViewModel by viewModels()
    private var isPasswordVisible = false

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        _binding = FragmentAuthLoginBinding.bind(view)

        setupListeners()
        observeViewModel()
    }

    private fun setupListeners() {
        // Login Button
        binding.btnLogin.setOnClickListener {
            val tenantName = binding.etTenant.text.toString()
            val userId = binding.etUserId.text.toString()
            val password = binding.etPassword.text.toString()
            viewModel.login(tenantName, userId, password)
        }

        // SignUp Button
        binding.btnSignUp.setOnClickListener {
            findNavController().navigate(R.id.action_loginFragment_to_signUpFragment)
        }
        
        // Password Toggle
        binding.ivPasswordToggle.setOnClickListener {
            togglePasswordVisibility()
        }
    }

    private fun togglePasswordVisibility() {
        val selection = binding.etPassword.selectionEnd
        if (isPasswordVisible) {
            // Hide Password
            binding.etPassword.transformationMethod = PasswordTransformationMethod.getInstance()
            binding.ivPasswordToggle.setImageResource(R.drawable.ic_visibility_off)
        } else {
            // Show Password
            binding.etPassword.transformationMethod = HideReturnsTransformationMethod.getInstance()
            binding.ivPasswordToggle.setImageResource(R.drawable.ic_visibility_on)
        }
        isPasswordVisible = !isPasswordVisible
        binding.etPassword.setSelection(selection)
    }

    private fun observeViewModel() {
        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.uiState.collect { state ->
                    handleLoginState(state)
                }
            }
        }
    }

    private fun handleLoginState(state: LoginUiState) {
        when (state) {
            is LoginUiState.Loading -> {
                binding.btnLogin.isEnabled = false
                binding.etTenant.isEnabled = false
                binding.etUserId.isEnabled = false
                binding.etPassword.isEnabled = false
            }
            is LoginUiState.Success -> {
                binding.btnLogin.isEnabled = true
                binding.etTenant.isEnabled = true
                binding.etUserId.isEnabled = true
                binding.etPassword.isEnabled = true

                val welcomeMsg = "환영합니다, ${state.user.name} (${state.user.corpName})님!"
                Toast.makeText(requireContext(), welcomeMsg, Toast.LENGTH_LONG).show()

                val intent = android.content.Intent(requireActivity(), com.example.flowdesk_android.feature.main.MainActivity::class.java)
                startActivity(intent)
                requireActivity().finish()
            }
            is LoginUiState.Error -> {
                binding.btnLogin.isEnabled = true
                binding.etTenant.isEnabled = true
                binding.etUserId.isEnabled = true
                binding.etPassword.isEnabled = true
                Toast.makeText(requireContext(), "로그인 실패: ${state.message}", Toast.LENGTH_LONG).show()
            }
            is LoginUiState.Idle -> {
                binding.btnLogin.isEnabled = true
                binding.etTenant.isEnabled = true
                binding.etUserId.isEnabled = true
                binding.etPassword.isEnabled = true
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
