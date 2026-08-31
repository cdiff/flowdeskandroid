package com.example.flowdesk_android.feature.user_management.presentation.roles.list

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.WindowManager
import android.widget.Toast
import com.example.flowdesk_android.R
import com.example.flowdesk_android.databinding.DialogRoleCreateBinding
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class CreateRoleBottomSheetFragment : BottomSheetDialogFragment() {

    var onSuccess: (() -> Unit)? = null
    var onConfirm: ((displayName: String, roleCode: String, description: String) -> Unit)? = null

    private var roleId: Int = -1
    private var initialDisplayName: String? = null
    private var initialRoleName: String? = null
    private var initialDescription: String? = null

    private var _binding: DialogRoleCreateBinding? = null
    private val binding get() = _binding!!

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {
            roleId = it.getInt("roleId", -1)
            initialDisplayName = it.getString("displayName")
            initialRoleName = it.getString("roleName")
            initialDescription = it.getString("description")
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = DialogRoleCreateBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun getTheme(): Int {
        return R.style.CustomBottomSheetDialogTheme
    }

    override fun onStart() {
        super.onStart()
        // 키보드 활성화 시 바텀시트가 허공으로 붕 뜨는 현상(adjustPan) 방지
        dialog?.window?.setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_RESIZE)
        val bottomSheetDialog = dialog as? BottomSheetDialog ?: return
        val bottomSheet = bottomSheetDialog.findViewById<View>(com.google.android.material.R.id.design_bottom_sheet) ?: return
        BottomSheetBehavior.from(bottomSheet).apply {
            state = BottomSheetBehavior.STATE_EXPANDED
            skipCollapsed = true
            isShouldRemoveExpandedCorners = false
        }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setupUI()
        setupListeners()
    }

    private fun setupUI() {
        if (roleId != -1) {
            // 수정 모드인 경우
            binding.tvHeaderTitle.text = "역할 수정"
            binding.tvHeaderSubtitle.text = "역할의 기본 정보를 수정합니다"
            binding.btnCreate.text = "수정하기"

            // 기존 값 세팅
            binding.etDisplayName.setText(initialDisplayName)
            binding.etRoleName.setText(initialRoleName)
            binding.etDescription.setText(initialDescription)

            // 시스템 코드는 고유 식별명이므로 수정하지 못하도록 비활성화 처리
            binding.etRoleName.isEnabled = false
        }
    }

    private fun setupListeners() {
        binding.btnCreate.setOnClickListener {
            val displayName = binding.etDisplayName.text.toString()
            val roleName = binding.etRoleName.text.toString()
            val description = binding.etDescription.text.toString()

            if (displayName.isBlank() || roleName.isBlank() || description.isBlank()) {
                Toast.makeText(context, getString(R.string.error_fill_all_fields), Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            // 생성/수정 모두 onConfirm 콜백으로 상위 Fragment에 위임
            onConfirm?.invoke(displayName, roleName, description)
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    companion object {
        const val TAG = "CreateRoleBottomSheet"

        fun newInstance(
            roleId: Int = -1,
            displayName: String? = null,
            roleName: String? = null,
            description: String? = null
        ): CreateRoleBottomSheetFragment {
            return CreateRoleBottomSheetFragment().apply {
                arguments = Bundle().apply {
                    putInt("roleId", roleId)
                    putString("displayName", displayName)
                    putString("roleName", roleName)
                    putString("description", description)
                }
            }
        }
    }
}
