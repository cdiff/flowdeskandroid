package com.example.flowdesk_android.presentation.ui.auth

import android.os.Bundle
import android.text.method.HideReturnsTransformationMethod
import android.text.method.PasswordTransformationMethod
import android.view.View
import android.widget.Toast
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.navigation.fragment.findNavController
import com.example.flowdesk_android.R
import com.example.flowdesk_android.databinding.FragmentSignupBinding
import com.example.flowdesk_android.presentation.viewmodel.SignUpState
import com.example.flowdesk_android.presentation.viewmodel.SignUpViewModel
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch

@AndroidEntryPoint
class SignUpFragment : Fragment(R.layout.fragment_signup) {

    private var _binding: FragmentSignupBinding? = null
    private val binding get() = _binding!!
    private val viewModel: SignUpViewModel by viewModels()

    private var isPasswordVisible = false
    private var isPasswordConfirmVisible = false

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        _binding = FragmentSignupBinding.bind(view)

        setupEmailDomainDropdown()
        setupListeners()
        observeViewModel()
    }

    private fun setupEmailDomainDropdown() {
        val domains = resources.getStringArray(R.array.email_domains)
        val adapter = android.widget.ArrayAdapter(requireContext(), android.R.layout.simple_dropdown_item_1line, domains)
        binding.etEmailDomain.setAdapter(adapter)

        binding.etEmailDomain.setOnClickListener {
            binding.etEmailDomain.showDropDown()
            binding.etEmailDomain.requestFocus()
        }

        binding.etEmailDomain.setOnFocusChangeListener { _, hasFocus ->
            if (hasFocus) {
                binding.etEmailDomain.showDropDown()
            }
        }
        
        binding.ivEmailDomainArrow.setOnClickListener {
            binding.etEmailDomain.showDropDown()
            binding.etEmailDomain.requestFocus()
        }
        
        binding.etEmailDomain.setOnItemClickListener { parent, _, position, _ ->
            val selectedItem = parent.getItemAtPosition(position).toString()
            if (selectedItem == "직접입력") {
                binding.etEmailDomain.setText("")
                binding.etEmailDomain.requestFocus()
            }
        }
    }

    private fun setupListeners() {
        binding.ivPasswordToggle.setOnClickListener {
            isPasswordVisible = !isPasswordVisible
            togglePasswordVisibility(binding.etPassword, binding.ivPasswordToggle, isPasswordVisible)
        }

        binding.ivPasswordConfirmToggle.setOnClickListener {
            isPasswordConfirmVisible = !isPasswordConfirmVisible
            togglePasswordVisibility(binding.etPasswordConfirm, binding.ivPasswordConfirmToggle, isPasswordConfirmVisible)
        }

        binding.btnSignUpAction.setOnClickListener {
            val companyName = binding.etCompany.text.toString().trim()
            val adminName = binding.etAdminName.text.toString().trim()
            val emailId = binding.etEmailId.text.toString().trim()
            val emailDomain = binding.etEmailDomain.text.toString().trim()
            val phone = binding.etPhone.text.toString().trim()
            val password = binding.etPassword.text.toString()
            val passwordConfirm = binding.etPasswordConfirm.text.toString()

            // Basic Validation
            if (companyName.isEmpty() || adminName.isEmpty() || emailId.isEmpty() || phone.isEmpty() || password.isEmpty()) {
                Toast.makeText(context, "모든 필드를 입력해주세요.", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            if (password != passwordConfirm) {
                binding.tvPasswordConfirmError.visibility = View.VISIBLE
                binding.tvPasswordConfirmError.text = getString(R.string.error_password_mismatch)
                return@setOnClickListener
            } else {
                binding.tvPasswordConfirmError.visibility = View.GONE
            }

            val passwordPattern = Regex("^(?=.*[A-Za-z])(?=.*\\d)(?=.*[!@#\$%^&*(),.?\":{}|<>]).{8,}\$")
            if (!passwordPattern.matches(password)) {
                 binding.tvPasswordError.visibility = View.VISIBLE
                 return@setOnClickListener
            } else {
                 binding.tvPasswordError.visibility = View.GONE
            }

            val fullEmail = if (emailDomain.isNotEmpty()) "$emailId@$emailDomain" else emailId
            
            viewModel.signUp(companyName, adminName, fullEmail, phone, password)
        }

        binding.tvSnsEasyLogin.setOnClickListener {
            findNavController().popBackStack()
        }
    }

    private fun togglePasswordVisibility(editText: android.widget.EditText, toggleIcon: android.widget.ImageView, isVisible: Boolean) {
        val selection = editText.selectionEnd
        if (isVisible) {
            editText.transformationMethod = HideReturnsTransformationMethod.getInstance()
            toggleIcon.setImageResource(R.drawable.ic_visibility_on)
        } else {
            editText.transformationMethod = PasswordTransformationMethod.getInstance()
            toggleIcon.setImageResource(R.drawable.ic_visibility_off)
        }
        editText.setSelection(selection)
    }

    private fun observeViewModel() {
        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.signUpState.collect { state ->
                    when (state) {
                        is SignUpState.Loading -> {
                            binding.btnSignUpAction.isEnabled = false
                            // potentially show progress bar if added to layout
                        }
                        is SignUpState.Success -> {
                            binding.btnSignUpAction.isEnabled = true
                            Toast.makeText(context, "회원가입 성공: ${state.message}", Toast.LENGTH_LONG).show()
                            findNavController().popBackStack() // Go back to Login
                        }
                        is SignUpState.Error -> {
                            binding.btnSignUpAction.isEnabled = true
                            Toast.makeText(context, "회원가입 실패: ${state.message}", Toast.LENGTH_LONG).show()
                        }
                        is SignUpState.Idle -> {
                            binding.btnSignUpAction.isEnabled = true
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
