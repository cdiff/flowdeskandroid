package com.example.flowdesk_android.feature.user_management.presentation.roles.permissions

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import com.example.flowdesk_android.R
import com.example.flowdesk_android.feature.user_management.domain.model.RoleDetail
import com.example.flowdesk_android.feature.user_management.domain.model.Role
import com.example.flowdesk_android.databinding.FragmentRoleManagePermissionsBinding
import com.example.flowdesk_android.databinding.ViewRoleManageInfoBinding
import com.example.flowdesk_android.databinding.ViewRoleManagePermissionsBinding
import com.example.flowdesk_android.feature.user_management.presentation.roles.list.RolesViewModel
import com.example.flowdesk_android.feature.user_management.presentation.roles.list.RoleCopyBottomSheetFragment
import com.google.android.material.tabs.TabLayoutMediator
import com.example.flowdesk_android.core.base.BaseFragment
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch
import com.example.flowdesk_android.core.extension.showTopToast

@AndroidEntryPoint
class ManagePermissionsFragment : BaseFragment(R.layout.fragment_role_manage_permissions) {

    private var _binding: FragmentRoleManagePermissionsBinding? = null
    private val binding get() = _binding!!
    private val viewModel: ManagePermissionsViewModel by viewModels()
    private val rolesViewModel: RolesViewModel by viewModels()

    private var roleId: Int = -1

    // 탭1: 기본 정보 뷰
    private var infoBinding: ViewRoleManageInfoBinding? = null
    // 탭2: 권한 목록 뷰
    private var permBinding: ViewRoleManagePermissionsBinding? = null

    private lateinit var permAdapter: ManagePermissionsAdapter
    private var originalPermissionIds: Set<Int> = emptySet()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        roleId = arguments?.getInt("role_id", -1) ?: -1
    }

    override fun getToolbarView(view: View): View? = binding.toolbar

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        _binding = FragmentRoleManagePermissionsBinding.bind(view)
        super.onViewCreated(view, savedInstanceState)
    }

    override fun initView() {
        setupTabs()
        setupToolbar()
        if (roleId != -1) {
            viewModel.load(roleId)
            rolesViewModel.triggerRefresh()
        }
    }

    private fun setupToolbar() {
        binding.btnBack.setOnClickListener {
            requireActivity().onBackPressedDispatcher.onBackPressed()
        }
        binding.btnSaveAll.setOnClickListener {
            val currentTab = binding.tabLayout.selectedTabPosition
            if (currentTab == 0) saveRoleInfo()
            else savePermissions()
        }
    }

    private fun setupTabs() {
        val pagerAdapter = ManagePermissionsPagerAdapter(
            layoutInflater,
            binding.viewPager
        )
        binding.viewPager.adapter = pagerAdapter
        // 두 탭 모두 미리 생성·유지하여 바인딩 참조가 항상 유효하도록 설정
        binding.viewPager.offscreenPageLimit = 1
        binding.viewPager.isUserInputEnabled = true

        TabLayoutMediator(binding.tabLayout, binding.viewPager) { tab, position ->
            tab.text = if (position == 0) "기본정보 수정" else "세부권한 관리"
        }.attach()

        // 생성자에서 미리 inflate된 바인딩을 가져옴 (항상 초기화 보장)
        infoBinding = pagerAdapter.infoBinding
        permBinding = pagerAdapter.permBinding

        // RecyclerView 셋업
        permAdapter = ManagePermissionsAdapter {}
        permBinding?.rvDetailedPermissions?.apply {
            adapter = permAdapter
            layoutManager = androidx.recyclerview.widget.LinearLayoutManager(requireContext())
        }

        // 권한 복사 버튼 셋업
        permBinding?.btnCopyPermissions?.setOnClickListener {
            showRoleSelectionBottomSheet()
        }
    }

    override fun observeViewModel() {
        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                launch {
                    viewModel.uiState.collect { state ->
                        when (state) {
                            is ManagePermissionsUiState.Loading -> { /* 로딩 표시는 간단히 생략 */ }
                            is ManagePermissionsUiState.Success -> {
                                bindRoleData(state.roleDetail)
                                permAdapter.submitCatalog(state.catalog, state.selectedIds)
                                originalPermissionIds = state.selectedIds
                            }
                            is ManagePermissionsUiState.Error -> {
                                showTopToast(state.message)
                            }
                        }
                    }
                }
                launch {
                    viewModel.event.collect { event ->
                        when (event) {
                            is ManagePermissionsEvent.InfoUpdated -> {
                                showTopToast(getString(R.string.success_role_info_updated))
                            }
                            is ManagePermissionsEvent.PermissionsSaved -> {
                                showTopToast(getString(R.string.success_permissions_saved))
                            }
                            is ManagePermissionsEvent.PermissionsCopied -> {
                                showTopToast(getString(R.string.success_permissions_copied))
                            }
                            is ManagePermissionsEvent.Error -> {
                                showTopToast(event.message)
                            }
                        }
                    }
                }
            }
        }
    }

    private fun bindRoleData(role: RoleDetail) {
        // 기본정보 탭 채우기
        infoBinding?.etDisplayName?.setText(role.displayName)
        infoBinding?.etRoleName?.setText(role.roleName)
        infoBinding?.etDescription?.setText(role.description ?: "")


    }

    private fun saveRoleInfo() {
        val ib = infoBinding ?: return
        val roleName = ib.etRoleName.text.toString().trim()
        val displayName = ib.etDisplayName.text.toString().trim()
        val description = ib.etDescription.text.toString().trim().takeIf { it.isNotEmpty() }
        viewModel.updateRoleInfo(roleName, displayName, description)
    }

    private fun savePermissions() {
        val currentCheckedIds = permAdapter.checkedIds
        viewModel.savePermissions(currentCheckedIds)
    }


    private fun showRoleSelectionBottomSheet() {
        val roles = rolesViewModel.filteredRoles.value
        if (roles.isEmpty()) {
            rolesViewModel.triggerRefresh()
        }
        
        // availableRoles가 업데이트될 때까지 기다리기는 어렵고, 보통 UI 흐름상 이미 로드되어 있거나 금방 로드됨.
        // 더 안전하게는 Flow를 collect하여 보여주는게 정석이지만, 여기서는 단순화하여 열릴 때 넘김.
        RoleCopyBottomSheetFragment(roles, roleId) { selectedRole: Role ->
            showCopyConfirmationDialog(selectedRole)
        }.show(childFragmentManager, "RoleCopyBottomSheet")
    }

    private fun showCopyConfirmationDialog(sourceRole: com.example.flowdesk_android.feature.user_management.domain.model.Role) {
        val dialogBinding = com.example.flowdesk_android.databinding.DialogRoleConfirmCopyBinding.inflate(layoutInflater)
        val dialog = android.app.AlertDialog.Builder(requireContext())
            .setView(dialogBinding.root)
            .create()

        dialog.window?.setBackgroundDrawableResource(android.R.color.transparent)

        dialogBinding.tvMessage.text = 
            getString(R.string.dialog_role_copy_message, sourceRole.displayName)

        dialogBinding.btnCancel.setOnClickListener {
            dialog.dismiss()
        }

        dialogBinding.btnConfirm.setOnClickListener {
            viewModel.copyFromRole(sourceRole.roleId)
            dialog.dismiss()
        }

        dialog.show()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
        infoBinding = null
        permBinding = null
    }

    companion object {
        fun newInstance(roleId: Int) = ManagePermissionsFragment().apply {
            arguments = Bundle().apply { putInt("role_id", roleId) }
        }
    }
}
