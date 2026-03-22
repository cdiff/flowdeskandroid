package com.example.flowdesk_android.presentation.ui.roles

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
import com.example.flowdesk_android.data.remote.dto.RoleDetailResponse
import com.example.flowdesk_android.databinding.FragmentManagePermissionsBinding
import com.example.flowdesk_android.databinding.LayoutManageRoleInfoBinding
import com.example.flowdesk_android.databinding.LayoutManageRolePermissionsBinding
import com.example.flowdesk_android.presentation.viewmodel.ManagePermissionsState
import com.example.flowdesk_android.presentation.viewmodel.ManagePermissionsViewModel
import com.google.android.material.tabs.TabLayoutMediator
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch

@AndroidEntryPoint
class ManagePermissionsFragment : Fragment() {

    private var _binding: FragmentManagePermissionsBinding? = null
    private val binding get() = _binding!!
    private val viewModel: ManagePermissionsViewModel by viewModels()

    private var roleId: Int = -1

    // 탭1: 기본 정보 뷰
    private var infoBinding: LayoutManageRoleInfoBinding? = null
    // 탭2: 권한 목록 뷰
    private var permBinding: LayoutManageRolePermissionsBinding? = null

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
        _binding = FragmentManagePermissionsBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setupTabs()
        setupToolbar()
        observeViewModel()

        if (roleId != -1) viewModel.loadRoleDetail(roleId)
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
        permAdapter = ManagePermissionsAdapter(viewModel.checkedPermissionIds) {}
        permBinding?.rvDetailedPermissions?.apply {
            adapter = permAdapter
            layoutManager = androidx.recyclerview.widget.LinearLayoutManager(requireContext())
        }
    }

    private fun observeViewModel() {
        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                launch {
                    viewModel.state.collect { state ->
                        when (state) {
                            is ManagePermissionsState.Loading -> { /* 로딩 표시는 간단히 생략 */ }
                            is ManagePermissionsState.Loaded -> bindRoleData(state.role)
                            is ManagePermissionsState.InfoUpdateSuccess -> {
                                showTopToast("역할 정보가 수정되었습니다.")
                                viewModel.resetState()
                                viewModel.loadRoleDetail(roleId)
                            }
                            is ManagePermissionsState.PermissionsUpdateSuccess -> {
                                showTopToast("권한이 저장되었습니다.")
                                viewModel.resetState()
                                viewModel.loadRoleDetail(roleId)
                            }
                            is ManagePermissionsState.Error -> {
                                showTopToast(state.message)
                                viewModel.resetState()
                            }
                            else -> {}
                        }
                    }
                }
                launch {
                    viewModel.permissionPages.collect { pages ->
                        permAdapter.submitList(pages)
                    }
                }
            }
        }
    }

    private fun bindRoleData(role: RoleDetailResponse) {
        // 기본정보 탭 채우기
        infoBinding?.etDisplayName?.setText(role.displayName)
        infoBinding?.etRoleName?.setText(role.roleName)
        infoBinding?.etDescription?.setText(role.description ?: "")

        // 원본 권한 id 목록 저장 (변경분 계산용)
        originalPermissionIds = role.permissionsByPage
            ?.flatMap { it.permissions ?: emptyList() }
            ?.map { it.permissionId }
            ?.toSet() ?: emptySet()
    }

    private fun saveRoleInfo() {
        val ib = infoBinding ?: return
        val roleName = ib.etRoleName.text.toString().trim()
        val displayName = ib.etDisplayName.text.toString().trim()
        val description = ib.etDescription.text.toString().trim().takeIf { it.isNotEmpty() }
        viewModel.updateRoleInfo(roleId, roleName, displayName, description)
    }

    private fun savePermissions() {
        viewModel.savePermissions(roleId, originalPermissionIds)
    }

    @Suppress("DEPRECATION")
    private fun showTopToast(message: String) {
        val layout = requireActivity().layoutInflater.inflate(R.layout.custom_top_toast, null)
        layout.findViewById<TextView>(R.id.tv_toast_message).text = message
        val toast = Toast(requireContext())
        toast.setGravity(android.view.Gravity.TOP or android.view.Gravity.CENTER_HORIZONTAL, 0, 100)
        toast.duration = Toast.LENGTH_SHORT
        toast.view = layout
        toast.show()
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
