package com.example.flowdesk_android.feature.auth.presentation.signup

import android.os.Bundle
import android.text.method.HideReturnsTransformationMethod
import android.text.method.PasswordTransformationMethod
import android.view.View
import android.widget.ImageView
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.navigation.fragment.findNavController
import com.example.flowdesk_android.R
import com.example.flowdesk_android.databinding.FragmentAuthSignupPasswordBinding
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch

@AndroidEntryPoint
class SignUpPasswordFragment : Fragment(R.layout.fragment_auth_signup_password) {

    private var _binding: FragmentAuthSignupPasswordBinding? = null
    private val binding get() = _binding!!

    // Activity 범위 ViewModel — SignUpFragment와 데이터 공유
    private val viewModel: SignUpViewModel by activityViewModels()

    private var isPasswordVisible = false
    private var isPasswordConfirmVisible = false

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        _binding = FragmentAuthSignupPasswordBinding.bind(view)

        setupListeners()
        observeViewModel()
    }

    private fun setupListeners() {
        binding.btnBackPassword.setOnClickListener {
            findNavController().popBackStack()
        }

        // 비밀번호 필드 포커스 리스너 연동
        binding.etPassword.setOnFocusChangeListener { _, hasFocus ->
            binding.layoutPasswordInputBox.isSelected = hasFocus
        }

        // 비밀번호 확인 필드 포커스 리스너 연동
        binding.etPasswordConfirm.setOnFocusChangeListener { _, hasFocus ->
            binding.layoutPasswordConfirmInputBox.isSelected = hasFocus
        }

        binding.ivPasswordToggle.setOnClickListener {
            isPasswordVisible = !isPasswordVisible
            toggleVisibility(binding.etPassword, binding.ivPasswordToggle, isPasswordVisible)
        }

        binding.ivPasswordConfirmToggle.setOnClickListener {
            isPasswordConfirmVisible = !isPasswordConfirmVisible
            toggleVisibility(binding.etPasswordConfirm, binding.ivPasswordConfirmToggle, isPasswordConfirmVisible)
        }

        binding.btnSignUp.setOnClickListener {
            val password = binding.etPassword.text.toString()
            val passwordConfirm = binding.etPasswordConfirm.text.toString()

            binding.tvPasswordError.visibility = View.GONE
            binding.tvPasswordConfirmError.visibility = View.GONE

            val passwordPattern = Regex("^(?=.*[A-Za-z])(?=.*\\d)(?=.*[!@#\$%^&*(),.?\":{}|<>]).{8,}$")
            if (!passwordPattern.matches(password)) {
                binding.tvPasswordError.visibility = View.VISIBLE
                return@setOnClickListener
            }

            if (password != passwordConfirm) {
                binding.tvPasswordConfirmError.visibility = View.VISIBLE
                return@setOnClickListener
            }

            viewModel.signUp(password)
        }
    }

    private fun toggleVisibility(editText: android.widget.EditText, icon: ImageView, isVisible: Boolean) {
        val sel = editText.selectionEnd
        if (isVisible) {
            editText.transformationMethod = HideReturnsTransformationMethod.getInstance()
            icon.setImageResource(R.drawable.ic_visibility_on)
        } else {
            editText.transformationMethod = PasswordTransformationMethod.getInstance()
            icon.setImageResource(R.drawable.ic_visibility_off)
        }
        editText.setSelection(sel)
    }

    private fun observeViewModel() {
        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.uiState.collect { state ->
                    when (state) {
                        is SignUpUiState.Loading -> binding.btnSignUp.isEnabled = false
                        is SignUpUiState.Success -> {
                            binding.btnSignUp.isEnabled = true
                            Toast.makeText(context, "회원가입에 성공했습니다.", Toast.LENGTH_LONG).show()
                            findNavController().navigate(R.id.action_signUpPasswordFragment_to_loginFragment)
                        }
                        is SignUpUiState.Error -> {
                            binding.btnSignUp.isEnabled = true
                            Toast.makeText(context, "회원가입 실패: ${state.message}", Toast.LENGTH_LONG).show()
                        }
                        is SignUpUiState.Idle -> binding.btnSignUp.isEnabled = true
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
