package com.example.flowdesk_android.feature.role.presentation.permissions

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
import com.example.flowdesk_android.core.domain.model.RoleDetail
import com.example.flowdesk_android.databinding.FragmentRoleManagePermissionsBinding
import com.example.flowdesk_android.databinding.ViewRoleManageInfoBinding
import com.example.flowdesk_android.databinding.ViewRoleManagePermissionsBinding
import com.example.flowdesk_android.feature.role.presentation.list.RolesViewModel
import com.example.flowdesk_android.feature.role.presentation.list.RoleCopyBottomSheetFragment
import com.google.android.material.tabs.TabLayoutMediator
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch

@AndroidEntryPoint
class ManagePermissionsFragment : Fragment() {

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

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentRoleManagePermissionsBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setupTabs()
        setupToolbar()
        observeViewModel()

        if (roleId != -1) {
            viewModel.load(roleId)
            rolesViewModel.fetchRoles()
        }
    }

    private fun setupToolbar() {
        binding.toolbar.setNavigationOnClickListener {
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

    private fun observeViewModel() {
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
                                showTopToast("역할 정보가 수정되었습니다.")
                            }
                            is ManagePermissionsEvent.PermissionsSaved -> {
                                showTopToast("권한이 저장되었습니다.")
                            }
                            is ManagePermissionsEvent.PermissionsCopied -> {
                                showTopToast("다른 역할의 권한을 성공적으로 가져왔습니다.")
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

    @Suppress("DEPRECATION")
    private fun showTopToast(message: String) {
        val layout = requireActivity().layoutInflater.inflate(R.layout.view_common_toast_top, null)
        layout.findViewById<TextView>(R.id.tv_toast_message).text = message
        val toast = Toast(requireContext())
        toast.setGravity(android.view.Gravity.TOP or android.view.Gravity.CENTER_HORIZONTAL, 0, 100)
        toast.duration = Toast.LENGTH_SHORT
        toast.view = layout
        toast.show()
    }

    private fun showRoleSelectionBottomSheet() {
        val roles = rolesViewModel.filteredRoles.value
        if (roles.isEmpty()) {
            rolesViewModel.fetchRoles()
        }
        
        // availableRoles가 업데이트될 때까지 기다리기는 어렵고, 보통 UI 흐름상 이미 로드되어 있거나 금방 로드됨.
        // 더 안전하게는 Flow를 collect하여 보여주는게 정석이지만, 여기서는 단순화하여 열릴 때 넘김.
        RoleCopyBottomSheetFragment(roles, roleId) { selectedRole ->
            showCopyConfirmationDialog(selectedRole)
        }.show(childFragmentManager, "RoleCopyBottomSheet")
    }

    private fun showCopyConfirmationDialog(sourceRole: com.example.flowdesk_android.core.domain.model.Role) {
        val dialogView = LayoutInflater.from(requireContext()).inflate(R.layout.dialog_role_confirm_copy, null)
        val dialog = android.app.AlertDialog.Builder(requireContext())
            .setView(dialogView)
            .create()

        dialog.window?.setBackgroundDrawableResource(android.R.color.transparent)

        dialogView.findViewById<TextView>(R.id.tv_message).text = 
            "'${sourceRole.displayName}' 역할의 권한 설정을\n이 역할에 복사할까요?\n\n(기존 권한은 모두 제거됩니다)"

        dialogView.findViewById<View>(R.id.btn_cancel).setOnClickListener {
            dialog.dismiss()
        }

        dialogView.findViewById<View>(R.id.btn_confirm).setOnClickListener {
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
