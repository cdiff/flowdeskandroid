package com.example.flowdesk_android.feature.user_management.presentation.roles.detail

import android.os.Bundle
import android.util.TypedValue
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.core.content.ContextCompat
import androidx.core.view.isVisible
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import com.example.flowdesk_android.R
import com.example.flowdesk_android.feature.user_management.domain.model.RoleDetail
import com.example.flowdesk_android.feature.user_management.domain.model.PermissionAction
import com.example.flowdesk_android.databinding.FragmentRoleDetailBinding
import com.example.flowdesk_android.databinding.ItemRoleDetailPageBinding
import com.example.flowdesk_android.databinding.ItemRoleAssignedUserBinding
import com.example.flowdesk_android.core.base.BaseFragment
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch

@AndroidEntryPoint
class RoleDetailFragment : BaseFragment(R.layout.fragment_role_detail) {

    private var _binding: FragmentRoleDetailBinding? = null
    private val binding get() = _binding!!

    private val viewModel: RoleDetailViewModel by viewModels()

    override fun getToolbarView(view: View): View? = binding.toolbar
 
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        _binding = FragmentRoleDetailBinding.bind(view)
        super.onViewCreated(view, savedInstanceState)
    }
 
    override fun initView() {
        val roleId = arguments?.getInt("roleId") ?: return
        setupToolbar()
        viewModel.loadRoleDetail(roleId)
    }

    private fun setupToolbar() {
        binding.btnBack.setOnClickListener {
            requireActivity().onBackPressedDispatcher.onBackPressed()
        }
    }

    override fun observeViewModel() {
        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.uiState.collect { state ->
                    binding.progressBar.isVisible = state is RoleDetailUiState.Loading

                    when (state) {
                        is RoleDetailUiState.Success -> {
                            bindRoleData(state.role)
                        }
                        is RoleDetailUiState.Error -> {
                            Toast.makeText(requireContext(), state.message, Toast.LENGTH_LONG).show()
                        }
                        else -> {}
                    }
                }
            }
        }
    }

    private fun bindRoleData(role: RoleDetail) {
        binding.tvRoleDisplayName.text = role.displayName
        binding.tvRoleName.text = "@${role.roleName}"
        binding.tvRoleDesc.text = role.description ?: "설명이 없습니다."

        binding.llPermissionsContainer.removeAllViews()
        binding.tvPermTitle.text = "세부 권한 (${role.permissionsByPage.sumOf { it.permissions.size }}개)"

        role.permissionsByPage.forEach { page ->
            val pageBinding = ItemRoleDetailPageBinding.inflate(layoutInflater, binding.llPermissionsContainer, false)
            pageBinding.tvPageName.text = page.pageDisplayName
            pageBinding.tvPageCode.text = page.pageName

            page.permissions.forEach { action ->
                val badge = createActionBadge(action)
                pageBinding.llActions.addView(badge)
            }
            binding.llPermissionsContainer.addView(pageBinding.root)
        }

        binding.llUsersContainer.removeAllViews()
        binding.tvUsersTitle.text = "할당된 팀원 (${role.assignedUsers.size}명)"

        role.assignedUsers.forEach { user ->
            val userBinding = ItemRoleAssignedUserBinding.inflate(layoutInflater, binding.llUsersContainer, false)
            userBinding.tvAvatar.text = user.userName.firstOrNull()?.toString() ?: "?"
            userBinding.tvUserName.text = user.userName
            userBinding.tvUserEmail.text = user.userId
            binding.llUsersContainer.addView(userBinding.root)
        }
    }

    private fun createActionBadge(action: PermissionAction): android.widget.TextView {
        val dp8 = TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 8f, resources.displayMetrics).toInt()
        val dp4 = TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 4f, resources.displayMetrics).toInt()
        
        val bg = android.graphics.drawable.GradientDrawable()
        bg.cornerRadius = dp4.toFloat()
        
        return TextView(requireContext()).apply {
            text = action.actionDisplayName
            textSize = 11f
            setPadding(dp8, dp4, dp8, dp4)
            
            // Set colors based on actionName using centralized colors.xml resources
            when (action.actionName.lowercase()) {
                "read" -> {
                    bg.setColor(ContextCompat.getColor(context, R.color.badge_read_bg))
                    setTextColor(ContextCompat.getColor(context, R.color.badge_read_text))
                }
                "create" -> {
                    bg.setColor(ContextCompat.getColor(context, R.color.badge_create_bg))
                    setTextColor(ContextCompat.getColor(context, R.color.badge_create_text))
                }
                "update" -> {
                    bg.setColor(ContextCompat.getColor(context, R.color.badge_update_bg))
                    setTextColor(ContextCompat.getColor(context, R.color.badge_update_text))
                }
                "delete" -> {
                    bg.setColor(ContextCompat.getColor(context, R.color.badge_delete_bg))
                    setTextColor(ContextCompat.getColor(context, R.color.badge_delete_text))
                }
                else -> {
                    bg.setColor(ContextCompat.getColor(context, R.color.badge_default_bg))
                    setTextColor(ContextCompat.getColor(context, R.color.badge_default_text))
                }
            }
            background = bg
            
            val params = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.WRAP_CONTENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
            )
            params.marginEnd = dp4
            layoutParams = params
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}

