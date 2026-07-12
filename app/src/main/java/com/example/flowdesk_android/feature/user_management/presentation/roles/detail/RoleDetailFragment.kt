package com.example.flowdesk_android.feature.user_management.presentation.roles.detail

import android.os.Bundle
import android.util.TypedValue
import android.view.LayoutInflater
import android.view.View
import android.widget.TextView
import android.widget.Toast
import androidx.core.content.ContextCompat
import androidx.core.view.isVisible
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import com.example.flowdesk_android.R
import com.example.flowdesk_android.databinding.FragmentUserManagementRoleDetailBinding
import com.example.flowdesk_android.databinding.ItemRoleDetailPageBinding
import com.example.flowdesk_android.databinding.ItemRoleAssignedUserBinding
import com.example.flowdesk_android.core.base.BaseFragment
import com.example.flowdesk_android.feature.user_management.domain.model.RoleDetail
import com.example.flowdesk_android.feature.user_management.domain.model.PermissionAction
import com.example.flowdesk_android.feature.user_management.domain.model.Role
import com.google.android.material.chip.Chip
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch
import androidx.navigation.fragment.findNavController

@AndroidEntryPoint
class RoleDetailFragment : BaseFragment(R.layout.fragment_user_management_role_detail) {

    private var _binding: FragmentUserManagementRoleDetailBinding? = null
    private val binding get() = _binding!!

    private val viewModel: RoleDetailViewModel by viewModels()

    private var currentRoleId: Int = -1

    override fun getToolbarView(view: View): View? = binding.toolbar

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        _binding = FragmentUserManagementRoleDetailBinding.bind(view)
        super.onViewCreated(view, savedInstanceState)
    }

    override fun initView() {
        currentRoleId = arguments?.getInt("roleId") ?: return
        setupToolbar()
        setupEditButton()
        setupViewPagerAnimation()
        viewModel.loadRoleDetail(currentRoleId)
    }

    private fun setupToolbar() {
        binding.btnBack.setOnClickListener {
            requireActivity().onBackPressedDispatcher.onBackPressed()
        }
    }

    private fun setupEditButton() {
        binding.clDisplayNameRow.setOnClickListener {
            val state = viewModel.uiState.value
            if (state is RoleDetailUiState.Success) {
                val role = state.role
                val bottomSheet = com.example.flowdesk_android.feature.user_management.presentation.roles.list.CreateRoleBottomSheetFragment.newInstance(
                    roleId = currentRoleId,
                    displayName = role.displayName,
                    roleName = role.roleName,
                    description = role.description
                ).apply {
                    onConfirm = { displayName, roleName, description ->
                        viewModel.updateInfo(currentRoleId, roleName, displayName, description)
                    }
                }
                bottomSheet.show(childFragmentManager, com.example.flowdesk_android.feature.user_management.presentation.roles.list.CreateRoleBottomSheetFragment.TAG)
            }
        }
    }

    // ViewPager2 애니메이션 및 마진 패딩 세팅
    private fun setupViewPagerAnimation() {
        binding.vpRoleTemplates.apply {
            // 좌우 페이지가 약간 보이도록 클리핑 제거
            clipToPadding = false
            clipChildren = false
            offscreenPageLimit = 3

            // PageTransformer 추가: 현재 중앙 카드는 크게, 양옆 카드는 작고 반투명하게 페이드 아웃 및 간격 좁히기
            setPageTransformer { page, position ->
                val absPosition = Math.abs(position)
                
                // 크기 스케일 (중앙은 1.0f, 멀어질수록 최대 0.85f까지 감소)
                val scale = 0.85f + (1f - absPosition).coerceAtLeast(0f) * 0.15f
                page.scaleX = scale
                page.scaleY = scale

                // 투명도 (중앙은 1.0f, 멀어질수록 최대 0.6f까지 감소)
                val alpha = 0.6f + (1f - absPosition).coerceAtLeast(0f) * 0.4f
                page.alpha = alpha

                // 카드 사이 간격 좁히기 (수평 translation 보정)
                // 카드가 0.85배로 줄어들며 생기는 여백을 적절히 당겨서(5% 보정) 메꿔줌 (겹치지 않게 방지)
                val translationX = -position * page.width * 0.05f
                page.translationX = translationX
            }
        }
    }

    override fun observeViewModel() {
        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                
                // UI State Observe
                launch {
                    viewModel.uiState.collect { state ->
                        binding.progressBar.isVisible = state is RoleDetailUiState.Loading
                        binding.scrollView.visibility =
                            if (state is RoleDetailUiState.Success) View.VISIBLE else View.INVISIBLE

                        when (state) {
                            is RoleDetailUiState.Success -> {
                                bindRoleData(state.role)
                                bindCopyTemplates(state.templates)
                            }
                            is RoleDetailUiState.Error -> {
                                Toast.makeText(requireContext(), state.message, Toast.LENGTH_LONG).show()
                            }
                            else -> {}
                        }
                    }
                }

                // Event Observe
                launch {
                    viewModel.event.collect { event ->
                        when (event) {
                            is RoleDetailEvent.PermissionsCopied -> {
                                Toast.makeText(requireContext(), "역할 설정 복사가 완료되었습니다.", Toast.LENGTH_SHORT).show()
                            }
                            is RoleDetailEvent.InfoUpdated -> {
                                Toast.makeText(requireContext(), "역할 정보가 수정되었습니다.", Toast.LENGTH_SHORT).show()
                                findNavController().previousBackStackEntry?.savedStateHandle?.set("refresh", true)
                                findNavController().popBackStack()
                            }
                            is RoleDetailEvent.Error -> {
                                Toast.makeText(requireContext(), event.message, Toast.LENGTH_LONG).show()
                            }
                            else -> {}
                        }
                    }
                }

            }
        }
    }

    private fun bindRoleData(role: RoleDetail) {
        // 기본 정보
        binding.tvRoleDisplayName.text = role.displayName
        binding.tvRoleDesc.text = role.description ?: "설명이 없습니다."

        // 상태 배지 (border 배지 스타일: "활성" / "비활성")
        val isActive = role.isActive
        binding.tvRoleStatus.text = if (isActive) "활성" else "비활성"
        binding.tvRoleStatus.setTextColor(
            ContextCompat.getColor(
                requireContext(),
                if (isActive) R.color.color_success_active else R.color.text_hint
            )
        )
        binding.tvRoleStatus.setBackgroundResource(
            if (isActive) R.drawable.bg_badge_rounded_green_border
            else R.drawable.bg_badge_gray_border
        )

        // 시스템 역할 코드 배지
        binding.tvRoleCodeBadge.text = role.roleName
        binding.tvRoleCodeBadge.setTextColor(
            ContextCompat.getColor(
                requireContext(),
                if (isActive) R.color.login_blue else R.color.text_hint
            )
        )
        binding.tvRoleCodeBadge.setBackgroundResource(
            if (isActive) R.drawable.bg_badge_rounded_blue_border
            else R.drawable.bg_badge_gray_border
        )

        // 세부 권한 목록
        binding.llPermissionsContainer.removeAllViews()
        binding.tvPermTitle.text = "세부 권한 (${role.permissionsByPage.sumOf { it.permissions.size }}개)"

        role.permissionsByPage.forEach { page ->
            val pageBinding = ItemRoleDetailPageBinding.inflate(
                layoutInflater, binding.llPermissionsContainer, false
            )
            pageBinding.tvPageName.text = page.pageDisplayName

            // ChipGroup에 권한 뱃지 동적 추가
            page.permissions.forEach { action ->
                val badge = createActionBadge(action)
                pageBinding.llActions.addView(badge)
            }
            binding.llPermissionsContainer.addView(pageBinding.root)
        }

        // 할당된 팀원 목록
        binding.llUsersContainer.removeAllViews()
        binding.tvUsersTitle.text = "할당된 팀원 (${role.assignedUsers.size}명)"

        role.assignedUsers.forEach { user ->
            val userBinding = ItemRoleAssignedUserBinding.inflate(
                layoutInflater, binding.llUsersContainer, false
            )
            userBinding.tvUserName.text = user.userName
            userBinding.tvUserEmail.text = user.userId
            binding.llUsersContainer.addView(userBinding.root)
        }
    }

    // 복사 템플릿 리스트 바인딩
    private fun bindCopyTemplates(templates: List<Role>) {
        if (templates.isEmpty()) {
            binding.llCopySection.visibility = View.GONE
            return
        }
        binding.llCopySection.visibility = View.VISIBLE

        val adapter = RoleCopyCardAdapter(templates) { selectedRole ->
            showCopyConfirmDialog(selectedRole)
        }
        binding.vpRoleTemplates.adapter = adapter
    }

    // 역할 덮어씌우기 확인 커스텀 다이얼로그 노출
    private fun showCopyConfirmDialog(sourceRole: Role) {
        val dialogView = LayoutInflater.from(requireContext()).inflate(R.layout.dialog_common_confirm, null)
        val dialog = androidx.appcompat.app.AlertDialog.Builder(requireContext())
            .setView(dialogView)
            .create()

        dialog.window?.setBackgroundDrawableResource(android.R.color.transparent)

        val tvTitle = dialogView.findViewById<TextView>(R.id.tv_title)
        val tvMessage = dialogView.findViewById<TextView>(R.id.tv_message)
        val cbConfirm = dialogView.findViewById<View>(R.id.cb_confirm)
        val btnCancel = dialogView.findViewById<View>(R.id.btn_cancel)
        val btnConfirm = dialogView.findViewById<View>(R.id.btn_confirm)

        tvTitle.text = "역할 설정 복사"
        tvMessage.text = "정말 '${sourceRole.displayName}'의 설정을 복사해 덮어씌우겠습니까?\n기존에 설정된 권한은 모두 유실됩니다."
        cbConfirm.visibility = View.GONE

        btnCancel.setOnClickListener {
            dialog.dismiss()
        }

        btnConfirm.setOnClickListener {
            viewModel.copyRolePermissions(currentRoleId, sourceRole.roleId)
            dialog.dismiss()
        }

        dialog.show()
    }

    private fun createActionBadge(action: PermissionAction): TextView {
        return TextView(requireContext()).apply {
            text = action.actionDisplayName
            textSize = 11f
            gravity = android.view.Gravity.CENTER
            includeFontPadding = false
            
            // 칩 패딩 설정 (가로 8dp, 세로 4dp)
            val dp8 = TypedValue.applyDimension(
                TypedValue.COMPLEX_UNIT_DIP, 8f, resources.displayMetrics
            ).toInt()
            val dp4 = TypedValue.applyDimension(
                TypedValue.COMPLEX_UNIT_DIP, 4f, resources.displayMetrics
            ).toInt()
            setPadding(dp8, dp4, dp8, dp4)

            // 권한 동작별 테마 컬러 및 옅은 배경 매칭
            val (bgColorRes, textColorRes) = when (action.actionName.lowercase()) {
                "read"   -> R.color.badge_read_bg   to R.color.badge_read_text
                "create" -> R.color.badge_create_bg to R.color.badge_create_text
                "update" -> R.color.badge_update_bg to R.color.badge_update_text
                "delete" -> R.color.badge_delete_bg to R.color.badge_delete_text
                else     -> R.color.badge_default_bg to R.color.badge_default_text
            }

            val bgColor = ContextCompat.getColor(context, bgColorRes)
            val textColor = ContextCompat.getColor(context, textColorRes)
            
            setTextColor(textColor)

            // 동적으로 둥근 모서리(radius = 6dp) 배경 드로어블 할당
            val radius = TypedValue.applyDimension(
                TypedValue.COMPLEX_UNIT_DIP, 6f, resources.displayMetrics
            )
            background = android.graphics.drawable.GradientDrawable().apply {
                shape = android.graphics.drawable.GradientDrawable.RECTANGLE
                cornerRadius = radius
                setColor(bgColor)
            }

            // 가로 LinearLayout 배치용 LayoutParams 생성 및 우측 마진 10dp 적용
            val dp10 = TypedValue.applyDimension(
                TypedValue.COMPLEX_UNIT_DIP, 10f, resources.displayMetrics
            ).toInt()
            layoutParams = android.widget.LinearLayout.LayoutParams(
                android.widget.LinearLayout.LayoutParams.WRAP_CONTENT,
                android.widget.LinearLayout.LayoutParams.WRAP_CONTENT
            ).apply {
                marginEnd = dp10
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
