package com.example.flowdesk_android.feature.user_management.presentation.users.detail

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import com.example.flowdesk_android.R
import com.example.flowdesk_android.databinding.DialogUserChangePasswordBinding
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.google.android.material.bottomsheet.BottomSheetDialogFragment

class UserChangePasswordBottomSheet(
    private val onConfirm: (String) -> Unit
) : BottomSheetDialogFragment() {

    private var _binding: DialogUserChangePasswordBinding? = null
    private val binding get() = _binding!!

    override fun getTheme(): Int = R.style.CustomBottomSheetDialogTheme

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = DialogUserChangePasswordBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // btn_close click listener
        binding.btnClose.setOnClickListener {
            dismiss()
        }

        binding.btnConfirm.setOnClickListener {
            val newPassword = binding.etNewPassword.text.toString().trim()
            if (newPassword.length < 6) {
                Toast.makeText(
                    requireContext(),
                    getString(R.string.error_password_min_length),
                    Toast.LENGTH_SHORT
                ).show()
                return@setOnClickListener
            }
            onConfirm(newPassword)
            dismiss()
        }
    }

    override fun onStart() {
        super.onStart()
        (dialog as? BottomSheetDialog)?.behavior?.isShouldRemoveExpandedCorners = false
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
