package com.example.flowdesk_android.presentation.ui.users

import android.graphics.Color
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.core.content.ContextCompat
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import com.example.flowdesk_android.R
import com.example.flowdesk_android.data.remote.dto.RoleDto
import com.example.flowdesk_android.data.remote.dto.UpdateUserInfoRequest
import com.example.flowdesk_android.data.remote.dto.UserDetailDto
import com.example.flowdesk_android.databinding.FragmentUserDetailBinding
import com.example.flowdesk_android.presentation.viewmodel.UserDetailState
import com.example.flowdesk_android.presentation.viewmodel.UserDetailViewModel
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch
import androidx.navigation.fragment.findNavController
import java.text.SimpleDateFormat
import java.util.Locale
import java.util.TimeZone

@AndroidEntryPoint
class UserDetailFragment : Fragment() {

    private var _binding: FragmentUserDetailBinding? = null
    private val binding get() = _binding!!
    private val viewModel: UserDetailViewModel by viewModels()

    private var userId: Int = -1
    private var currentUserData: UserDetailDto? = null
    private val selectedRoleIds = mutableSetOf<Int>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {
            userId = it.getInt(ARG_USER_ID, -1)
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentUserDetailBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        if (userId != -1) {
            viewModel.getUserDetail(userId)
        } else {
            Toast.makeText(requireContext(), "잘못된 접근입니다.", Toast.LENGTH_SHORT).show()
            findNavController().popBackStack()
        }

        setupListeners()
        observeViewModel()
    }

    private fun setupListeners() {
        binding.btnBack.setOnClickListener {
            findNavController().popBackStack()
        }

        binding.btnToggleStatus.setOnClickListener {
            currentUserData?.let { user ->
                val newStatus = if (user.isActive == 1) 0 else 1
                viewModel.updateUserStatus(userId, newStatus)
            }
        }

        binding.btnSaveRoles.setOnClickListener {
            viewModel.updateUserRoles(userId, selectedRoleIds.toList())
        }

        binding.btnChangePassword.setOnClickListener {
            val newPwd = binding.etNewPassword.text.toString()
            if (newPwd.length < 6) {
                Toast.makeText(requireContext(), "비밀번호는 6자 이상 입력해주세요.", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }
            viewModel.adminChangePassword(userId, newPwd)
        }
        binding.btnInvalidateTokens.setOnClickListener {
            viewModel.invalidateTokens(userId)
        }
        
        binding.btnEditInfo.setOnClickListener {
            toggleEditMode(true)
        }
        
        binding.btnCancelEdit.setOnClickListener {
            toggleEditMode(false)
            currentUserData?.let { bindUserData(it) } // Revert input
        }
        
        binding.btnSaveEdit.setOnClickListener {
            val name = binding.etInfoName.text.toString().trim()
            if (name.isEmpty()) {
                Toast.makeText(requireContext(), "이름을 입력해주세요.", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }
            
            val request = UpdateUserInfoRequest(
                corpName = binding.etInfoCompany.text.toString().trim().takeIf { it.isNotEmpty() },
                userName = name,
                userEmail = binding.etInfoEmail.text.toString().trim(),
                userTel = binding.etInfoTel.text.toString().trim().takeIf { it.isNotEmpty() },
                userHp = binding.etInfoHp.text.toString().trim().takeIf { it.isNotEmpty() },
                roleIds = null
            )
            viewModel.updateUser(userId, request)
        }
    }

    private fun Int.dpToPx(): Int = (this * resources.displayMetrics.density).toInt()

    private fun toggleEditMode(isEditing: Boolean) {
        binding.btnEditInfo.isVisible = !isEditing
        binding.llEditActions.isVisible = isEditing
        
        val fields = listOf(binding.etInfoCompany, binding.etInfoName, binding.etInfoEmail, binding.etInfoTel, binding.etInfoHp)
        
        fields.forEach { field ->
            field.isEnabled = isEditing
            if (isEditing) {
                field.setBackgroundResource(R.drawable.bg_edit_text_gray)
                val padH = 12.dpToPx()
                val padV = 8.dpToPx()
                field.setPadding(padH, padV, padH, padV)
            } else {
                field.background = null
                field.setPadding(0, 0, 0, 0)
            }
        }
    }

    private fun observeViewModel() {
        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.state.collect { state ->
                    binding.progressBar.isVisible = state is UserDetailState.Loading

                    when (state) {
                        is UserDetailState.Success -> {
                            bindUserData(state.data)
                        }
                        is UserDetailState.StatusChangeSuccess -> {
                            val oldStatus = currentUserData?.isActive ?: 0
                            val isNowActive = oldStatus == 0
                            if (isNowActive) {
                                showTopToast("사용자가 활성화되었습니다.")
                            } else {
                                showTopToast("사용자가 비활성화되었습니다.")
                            }
                            viewModel.resetState()
                            viewModel.getUserDetail(userId) // Reload data
                        }
                        is UserDetailState.InfoChangeSuccess -> {
                            showTopToast("정보가 성공적으로 변경되었습니다.")
                            toggleEditMode(false)
                            viewModel.resetState()
                            viewModel.getUserDetail(userId)
                        }
                        is UserDetailState.RoleChangeSuccess -> {
                            showTopToast("역할이 성공적으로 변경되었습니다.")
                            viewModel.resetState()
                            viewModel.getUserDetail(userId)
                        }
                        is UserDetailState.PasswordChangeSuccess -> {
                            showTopToast("비밀번호가 변경되었습니다.")
                            binding.etNewPassword.text?.clear()
                            viewModel.resetState()
                        }
                        is UserDetailState.TokenInvalidateSuccess -> {
                            showTopToast("모든 토큰이 무효화 되었습니다.")
                            viewModel.resetState()
                        }
                        is UserDetailState.Error -> {
                            Toast.makeText(context, state.message, Toast.LENGTH_LONG).show()
                            viewModel.resetState()
                        }
                        else -> {}
                    }
                }
            }
        }
    }

    private fun showTopToast(message: String) {
        val inflater = LayoutInflater.from(requireContext())
        val layout = inflater.inflate(R.layout.custom_top_toast, null)
        layout.findViewById<TextView>(R.id.tv_toast_message).text = message

        val toast = Toast(requireContext())
        toast.setGravity(android.view.Gravity.TOP or android.view.Gravity.CENTER_HORIZONTAL, 0, 100)
        toast.duration = Toast.LENGTH_SHORT
        toast.view = layout
        toast.show()
    }

    private fun bindUserData(user: UserDetailDto) {
        currentUserData = user
        
        // Header
        binding.tvName.text = user.userName
        binding.tvId.text = "@${user.userId}"
        
        // Set badge using first assigned role if any
        val mainRole = user.availableRoles?.firstOrNull { it.isAssigned }?.displayName ?: "사용자"
        binding.tvRoleBadge.text = mainRole

        // Status
        if (user.isActive == 1) {
            binding.vStatusDot.setBackgroundResource(R.drawable.bg_oval_green)
            binding.tvStatusText.text = "활성"
            binding.btnToggleStatus.text = "비활성화"
            
            binding.ivAvatar.setBackgroundResource(R.drawable.bg_oval_light_blue)
            binding.ivAvatar.setColorFilter(ContextCompat.getColor(requireContext(), R.color.login_blue))
        } else {
            binding.vStatusDot.setBackgroundResource(R.drawable.bg_oval_red)
            binding.tvStatusText.text = "정지"
            binding.btnToggleStatus.text = "활성화"
            
            binding.ivAvatar.setBackgroundResource(R.drawable.bg_oval_gray_light)
            binding.ivAvatar.setColorFilter(ContextCompat.getColor(requireContext(), R.color.gray_text))
        }

        // Basic Info
        binding.etInfoCompany.setText(user.corpName ?: "")
        binding.etInfoName.setText(user.userName)
        binding.etInfoEmail.setText(user.userEmail ?: "")
        binding.etInfoTel.setText(user.userTel ?: "")
        binding.etInfoHp.setText(user.userHp ?: "")

        // Registration Date
        user.regDtm?.let { isoString ->
            try {
                // Parse assuming format "2026-01-28T11:11:44.000Z"
                val parser = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'", Locale.US)
                parser.timeZone = TimeZone.getTimeZone("UTC")
                val date = parser.parse(isoString)
                
                if (date != null) {
                    val formatter = SimpleDateFormat("yyyy. MM. dd. a hh:mm", Locale.KOREA)
                    formatter.timeZone = TimeZone.getDefault()
                    binding.tvRegDate.text = formatter.format(date)
                } else {
                    binding.tvRegDate.text = isoString
                }
            } catch (e: Exception) {
                binding.tvRegDate.text = isoString
            }
        } ?: run {
            binding.tvRegDate.text = "-"
        }

        // Roles
        selectedRoleIds.clear()
        user.assignedRoleIds?.let { selectedRoleIds.addAll(it) }

        binding.llRolesContainer.removeAllViews()

        // Temporarily use mockup roles as requested
        val mockRoles = listOf(
            RoleDto(1, "super_admin", "슈퍼 관리자", "시스템 전체 관리 권한을 가진 최고 관리자", 1, selectedRoleIds.contains(1)),
            RoleDto(2, "admin", "관리자", "테넌트 관리 권한", 1, selectedRoleIds.contains(2)),
            RoleDto(3, "manager", "매니저", "팀원 관리 및 승인 권한", 1, selectedRoleIds.contains(3)),
            RoleDto(4, "member", "팀원", "기본적인 업무 수행 권한", 1, selectedRoleIds.contains(4))
        )
        
        mockRoles.forEach { role ->
            addRoleView(role)
        }
    }

    private fun addRoleView(role: RoleDto) {
        val roleView = LayoutInflater.from(requireContext()).inflate(R.layout.item_role_checkbox, binding.llRolesContainer, false)
        
        val tvName = roleView.findViewById<TextView>(R.id.tv_role_name)
        val tvDesc = roleView.findViewById<TextView>(R.id.tv_role_desc)
        val ivCheckbox = roleView.findViewById<ImageView>(R.id.iv_checkbox)
        
        tvName.text = role.displayName
        tvDesc.text = role.description ?: ""
        
        fun updateCheckboxUI(isSelected: Boolean) {
            if (isSelected) {
                ivCheckbox.setImageResource(R.drawable.ic_role_checkbox_selected)
                roleView.setBackgroundResource(R.drawable.bg_card_rounded_border_selected)
            } else {
                ivCheckbox.setImageResource(R.drawable.ic_role_checkbox_unselected)
                roleView.setBackgroundResource(R.drawable.bg_card_rounded_border)
            }
        }
        
        updateCheckboxUI(selectedRoleIds.contains(role.roleId))
        
        roleView.setOnClickListener {
            val isCurrentSelected = selectedRoleIds.contains(role.roleId)
            if (isCurrentSelected) {
                selectedRoleIds.remove(role.roleId)
            } else {
                selectedRoleIds.add(role.roleId)
            }
            updateCheckboxUI(!isCurrentSelected)
        }
        
        binding.llRolesContainer.addView(roleView)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    companion object {
        private const val ARG_USER_ID = "user_id"

        fun newInstance(userId: Int) = UserDetailFragment().apply {
            arguments = Bundle().apply {
                putInt(ARG_USER_ID, userId)
            }
        }
    }
}
