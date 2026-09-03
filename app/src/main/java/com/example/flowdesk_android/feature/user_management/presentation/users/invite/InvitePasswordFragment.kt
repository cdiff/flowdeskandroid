package com.example.flowdesk_android.feature.user_management.presentation.users.invite

import android.os.Bundle
import android.text.method.HideReturnsTransformationMethod
import android.text.method.PasswordTransformationMethod
import android.view.View
import android.widget.ImageView
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.navigation.fragment.findNavController
import com.example.flowdesk_android.R
import com.example.flowdesk_android.databinding.FragmentInvitePasswordBinding
import dagger.hilt.android.AndroidEntryPoint
import androidx.navigation.fragment.FragmentNavigatorExtras
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.updatePadding

@AndroidEntryPoint
class InvitePasswordFragment : Fragment(R.layout.fragment_invite_password) {

    private var _binding: FragmentInvitePasswordBinding? = null
    private val binding get() = _binding!!

    private val viewModel: InviteTeamViewModel by activityViewModels()

    private var isPasswordVisible = false
    private var isPasswordConfirmVisible = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        sharedElementEnterTransition = androidx.transition.TransitionInflater.from(requireContext())
            .inflateTransition(android.R.transition.move)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        _binding = FragmentInvitePasswordBinding.bind(view)

        // 요약 카드 바인딩
        binding.tvSummaryName.text = viewModel.userName
        binding.tvSummaryEmail.text = viewModel.userEmail

        setupListeners()
    }

    private fun setupListeners() {
        binding.etPassword.setOnFocusChangeListener { _, hasFocus ->
            binding.layoutPasswordInputBox.isSelected = hasFocus
        }

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

        binding.btnInviteNext.setOnClickListener {
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

            // 비밀번호를 임시 보관 후 역할 선택 화면으로 이동 (진행 표시기 공유 요소 애니메이션 포함)
            val extras = FragmentNavigatorExtras(binding.layoutProgressIndicators to "progress_dots")
            findNavController().navigate(
                R.id.action_invitePasswordFragment_to_inviteRoleFragment,
                android.os.Bundle().apply { putString("password", password) },
                null,
                extras
            )
        }
    }

    private fun toggleVisibility(editText: android.widget.EditText, icon: ImageView, isVisible: Boolean) {
        val sel = editText.selectionEnd
        if (isVisible) {
            editText.transformationMethod = HideReturnsTransformationMethod.getInstance()
            icon.setImageResource(R.drawable.ic_lucide_eye_off)
        } else {
            editText.transformationMethod = PasswordTransformationMethod.getInstance()
            icon.setImageResource(R.drawable.ic_lucide_eye)
        }
        editText.setSelection(sel)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
