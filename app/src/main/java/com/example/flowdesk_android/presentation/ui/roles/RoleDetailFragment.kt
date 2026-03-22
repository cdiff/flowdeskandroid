package com.example.flowdesk_android.presentation.ui.roles

import android.graphics.Color
import android.os.Bundle
import android.util.TypedValue
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.navigation.fragment.findNavController
import com.example.flowdesk_android.R
import com.example.flowdesk_android.data.remote.dto.PermissionActionDto
import com.example.flowdesk_android.data.remote.dto.RoleDetailResponse
import com.example.flowdesk_android.databinding.FragmentRoleDetailBinding
import com.example.flowdesk_android.presentation.viewmodel.RoleDetailState
import com.example.flowdesk_android.presentation.viewmodel.RoleDetailViewModel
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch

@AndroidEntryPoint
class RoleDetailFragment : Fragment() {

    private var _binding: FragmentRoleDetailBinding? = null
    private val binding get() = _binding!!

    private val viewModel: RoleDetailViewModel by viewModels()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentRoleDetailBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val roleId = arguments?.getInt("roleId") ?: return

        setupToolbar()
        observeViewModel()
        
        viewModel.fetchRoleDetail(roleId)
    }

    private fun setupToolbar() {
        binding.toolbar.setNavigationOnClickListener {
            requireActivity().onBackPressedDispatcher.onBackPressed()
        }
    }

    private fun observeViewModel() {
        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.state.collect { state ->
                    binding.progressBar.isVisible = state is RoleDetailState.Loading

                    when (state) {
                        is RoleDetailState.Success -> {
                            bindRoleData(state.role)
                        }
                        is RoleDetailState.Error -> {
                            Toast.makeText(requireContext(), state.message, Toast.LENGTH_LONG).show()
                        }
                        else -> {}
                    }
                }
            }
        }
    }

    private fun bindRoleData(role: RoleDetailResponse) {
        // Upper metadata
        binding.tvRoleDisplayName.text = role.displayName
        binding.tvRoleName.text = "@${role.roleName}"
        binding.tvRoleDesc.text = role.description ?: "설명이 없습니다."
        
        // Permissions
        binding.llPermissionsContainer.removeAllViews()
        val permissions = role.permissionsByPage ?: emptyList()
        binding.tvPermTitle.text = "세부 권한 (${permissions.sumOf { it.permissions?.size ?: 0 }}개)"
        
        permissions.forEach { page ->
            val pageView = layoutInflater.inflate(R.layout.item_role_detail_page, binding.llPermissionsContainer, false)
            
            pageView.findViewById<TextView>(R.id.tv_page_name).text = page.pageDisplayName
            pageView.findViewById<TextView>(R.id.tv_page_code).text = page.pageName
            
            val llActions = pageView.findViewById<LinearLayout>(R.id.ll_actions)
            page.permissions?.forEach { action ->
                val badge = createActionBadge(action)
                llActions.addView(badge)
            }
            
            binding.llPermissionsContainer.addView(pageView)
        }

        // Assigned Users
        binding.llUsersContainer.removeAllViews()
        val users = role.assignedUsers ?: emptyList()
        binding.tvUsersTitle.text = "할당된 팀원 (${users.size}명)"
        
        users.forEach { user ->
            val userView = layoutInflater.inflate(R.layout.item_role_assigned_user, binding.llUsersContainer, false)
            val tvAvatar = userView.findViewById<TextView>(R.id.tv_avatar)
            val tvName = userView.findViewById<TextView>(R.id.tv_user_name)
            val tvEmail = userView.findViewById<TextView>(R.id.tv_user_email)
            
            tvAvatar.text = user.userName.firstOrNull()?.toString() ?: "?"
            tvName.text = user.userName
            tvEmail.text = user.email ?: ""
            
            binding.llUsersContainer.addView(userView)
        }
    }

    private fun createActionBadge(action: PermissionActionDto): TextView {
        val dp8 = TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 8f, resources.displayMetrics).toInt()
        val dp4 = TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 4f, resources.displayMetrics).toInt()
        
        val bg = android.graphics.drawable.GradientDrawable()
        bg.cornerRadius = dp4.toFloat()
        
        return TextView(requireContext()).apply {
            text = action.actionDisplayName
            textSize = 11f
            setPadding(dp8, dp4, dp8, dp4)
            
            // Set colors based on actionName (read, create, update, delete)
            when (action.actionName.lowercase()) {
                "read" -> {
                    bg.setColor(Color.parseColor("#E1F0FF"))
                    setTextColor(Color.parseColor("#0052CC"))
                }
                "create" -> {
                    bg.setColor(Color.parseColor("#E3FCEF"))
                    setTextColor(Color.parseColor("#006644"))
                }
                "update" -> {
                    bg.setColor(Color.parseColor("#FFF0B3"))
                    setTextColor(Color.parseColor("#FF8B00"))
                }
                "delete" -> {
                    bg.setColor(Color.parseColor("#FFEBE6"))
                    setTextColor(Color.parseColor("#DE350B"))
                }
                else -> {
                    bg.setColor(Color.parseColor("#F4F5F7"))
                    setTextColor(Color.parseColor("#42526E"))
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
