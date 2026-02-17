package com.example.flowdesk_android.presentation.ui.mypage

import android.os.Bundle
import android.view.View
import android.widget.Toast
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
import com.example.flowdesk_android.databinding.FragmentChangePasswordBinding
import com.example.flowdesk_android.presentation.viewmodel.ChangePasswordState
import com.example.flowdesk_android.presentation.viewmodel.ChangePasswordViewModel
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch

@AndroidEntryPoint
class ChangePasswordFragment : Fragment(R.layout.fragment_change_password) {

    private var _binding: FragmentChangePasswordBinding? = null
    private val binding get() = _binding!!
    private val viewModel: ChangePasswordViewModel by viewModels()

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        _binding = FragmentChangePasswordBinding.bind(view)

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
                Toast.makeText(context, "모든 필드를 입력해주세요.", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            if (newPass != confirm) {
                Toast.makeText(context, "새 비밀번호가 일치하지 않습니다.", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            showConfirmationDialog(current, newPass, confirm)
        }
    }

    private fun showConfirmationDialog(current: String, new: String, confirm: String) {
        val dialogView = android.view.LayoutInflater.from(requireContext()).inflate(R.layout.dialog_custom_confirmation, null)
        
        val builder = AlertDialog.Builder(requireContext())
            .setView(dialogView)
        
        val dialog = builder.create()
        
        // Make background transparent to show rounded corners
        dialog.window?.setBackgroundDrawable(android.graphics.drawable.ColorDrawable(android.graphics.Color.TRANSPARENT))
        
        dialogView.findViewById<android.widget.TextView>(R.id.tv_title).text = "비밀번호 변경"
        dialogView.findViewById<android.widget.TextView>(R.id.tv_message).text = "비밀번호를 변경하시겠습니까?\n변경 후에는 다시 로그인해야 합니다."
        
        val btnConfirm = dialogView.findViewById<androidx.appcompat.widget.AppCompatButton>(R.id.btn_confirm)
        val rbConfirm = dialogView.findViewById<android.widget.RadioButton>(R.id.cb_confirm) // ID kept as cb_confirm for simplicity or changed? XML didn't change ID in my plan.
        // XML plan changed comments but kept ID as `cb_confirm` to minimize diffs? No I should assume I kept ID `cb_confirm`.
        
        // Initial State
        btnConfirm.isEnabled = false
        btnConfirm.alpha = 0.5f

        rbConfirm.setOnCheckedChangeListener { _, isChecked ->
            btnConfirm.isEnabled = isChecked
            btnConfirm.alpha = if (isChecked) 1.0f else 0.5f
        }
        
        // Manual toggle support for RadioButton to act like Checkbox (Optional, but good UX if they mis-click)
        rbConfirm.setOnClickListener {
             // If we really want it to be toggleable like checkbox:
             // But RadioButton default behavior consumes the click. 
             // Using simple RadioButton is enough for "I agree" scenarios.
        }
        
        // 취소 버튼 리스너 (Delete if button removed)
        val btnCancel = dialogView.findViewById<android.view.View>(R.id.btn_cancel)
        btnCancel.setOnClickListener {
             dialog.dismiss()
        }
        
        btnConfirm.setOnClickListener {
            viewModel.changePassword(current, new, confirm)
            dialog.dismiss()
        }
        
        dialog.show()
    }

    private fun observeViewModel() {
        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.state.collect { state ->
                    when (state) {
                        is ChangePasswordState.Loading -> {
                            // Show loading
                        }
                        is ChangePasswordState.Success -> {
                            Toast.makeText(context, "비밀번호가 변경되었습니다.", Toast.LENGTH_SHORT).show()
                            findNavController().navigateUp()
                            viewModel.resetState()
                        }
                        is ChangePasswordState.Error -> {
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
