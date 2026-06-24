package com.example.flowdesk_android.feature.mypage.presentation.change_password

import android.os.Bundle
import android.view.View
import androidx.appcompat.app.AlertDialog
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
import com.example.flowdesk_android.databinding.FragmentMypageChangePasswordBinding
import com.example.flowdesk_android.databinding.DialogCommonConfirmBinding
import com.example.flowdesk_android.core.extension.showTopToast
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch

@AndroidEntryPoint
class ChangePasswordFragment : Fragment(R.layout.fragment_mypage_change_password) {

    private var _binding: FragmentMypageChangePasswordBinding? = null
    private val binding get() = _binding!!
    private val viewModel: ChangePasswordViewModel by viewModels()

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        _binding = FragmentMypageChangePasswordBinding.bind(view)

        ViewCompat.setOnApplyWindowInsetsListener(binding.topBar) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.updatePadding(top = systemBars.top)
            insets
        }

        setupListeners()
        observeViewModel()
    }

    private fun setupListeners() {
        binding.btnBack.setOnClickListener {
            findNavController().navigateUp()
        }

        binding.btnChange.setOnClickListener {
            val current = binding.etCurrentPassword.text.toString()
            val newPass = binding.etNewPassword.text.toString()
            val confirm = binding.etConfirmPassword.text.toString()

            if (current.isBlank() || newPass.isBlank() || confirm.isBlank()) {
                showTopToast(getString(R.string.mypage_msg_enter_all_fields))
                return@setOnClickListener
            }

            if (newPass != confirm) {
                showTopToast(getString(R.string.mypage_msg_passwords_do_not_match))
                return@setOnClickListener
            }

            showConfirmationDialog(current, newPass, confirm)
        }
    }

    private fun showConfirmationDialog(current: String, new: String, confirm: String) {
        val dialogView = android.view.LayoutInflater.from(requireContext()).inflate(R.layout.dialog_common_confirm, null)
        val dialogBinding = DialogCommonConfirmBinding.bind(dialogView)
        
        val builder = AlertDialog.Builder(requireContext())
            .setView(dialogView)
        
        val dialog = builder.create()
        
        dialog.window?.setBackgroundDrawable(android.graphics.drawable.ColorDrawable(android.graphics.Color.TRANSPARENT))
        
        dialogBinding.tvTitle.text = getString(R.string.mypage_dialog_change_password_title)
        dialogBinding.tvMessage.text = getString(R.string.mypage_dialog_change_password_message)
        
        dialogBinding.btnConfirm.isEnabled = false
        dialogBinding.btnConfirm.alpha = 0.5f

        dialogBinding.cbConfirm.setOnCheckedChangeListener { _, isChecked ->
            dialogBinding.btnConfirm.isEnabled = isChecked
            dialogBinding.btnConfirm.alpha = if (isChecked) 1.0f else 0.5f
        }
        
        dialogBinding.btnCancel.setOnClickListener {
             dialog.dismiss()
        }
        
        dialogBinding.btnConfirm.setOnClickListener {
            viewModel.changePassword(current, new, confirm)
            dialog.dismiss()
        }
        
        dialog.show()
    }

    private fun observeViewModel() {
        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.uiState.collect { state ->
                    when (state) {
                        is ChangePasswordUiState.Loading -> { /* show loading */ }
                        is ChangePasswordUiState.Success -> {
                            showTopToast(getString(R.string.mypage_msg_password_changed))
                            findNavController().navigateUp()
                            viewModel.resetState()
                        }
                        is ChangePasswordUiState.Error -> {
                            showTopToast(state.message.takeIf { it.isNotEmpty() } ?: getString(R.string.mypage_error_change_password_failed))
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
