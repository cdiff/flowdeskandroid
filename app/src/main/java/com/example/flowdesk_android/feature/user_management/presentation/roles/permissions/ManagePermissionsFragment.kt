package com.example.flowdesk_android.feature.user_management.presentation.roles.permissions

import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.navigation.fragment.findNavController
import com.example.flowdesk_android.R
import com.example.flowdesk_android.databinding.FragmentUserManagementRoleManagePermissionsBinding
import com.example.flowdesk_android.core.base.BaseFragment
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch
import com.example.flowdesk_android.core.extension.showTopToast

@AndroidEntryPoint
class ManagePermissionsFragment : BaseFragment(R.layout.fragment_user_management_role_manage_permissions) {

    private var _binding: FragmentUserManagementRoleManagePermissionsBinding? = null
    private val binding get() = _binding!!
    private val viewModel: ManagePermissionsViewModel by viewModels()

    private var roleId: Int = -1
    private var readOnly: Boolean = false
    private lateinit var permAdapter: ManagePermissionsAdapter
    private var originalPermissionIds: Set<Int> = emptySet()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        roleId = arguments?.getInt("role_id", -1) ?: -1
        readOnly = arguments?.getBoolean("read_only", false) ?: false
    }

    override fun getToolbarView(view: View): View? = binding.toolbar

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        _binding = FragmentUserManagementRoleManagePermissionsBinding.bind(view)
        super.onViewCreated(view, savedInstanceState)
    }

    override fun initView() {
        setupToolbar()
        setupPermissionsList()
        if (roleId != -1) {
            viewModel.load(roleId)
        }
        // 읽기 전용 모드면 저장 버튼 숨김
        binding.btnSave.visibility = if (readOnly) View.GONE else View.VISIBLE
    }

    private fun setupToolbar() {
        binding.btnBack.setOnClickListener {
            requireActivity().onBackPressedDispatcher.onBackPressed()
        }
        binding.btnSave.setOnClickListener {
            savePermissions()
        }
    }

    private fun setupPermissionsList() {
        // RecyclerView 셋업
        permAdapter = ManagePermissionsAdapter {}
        binding.rvDetailedPermissions.apply {
            adapter = permAdapter
            layoutManager = androidx.recyclerview.widget.LinearLayoutManager(requireContext())
        }
    }

    override fun observeViewModel() {
        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                launch {
                    viewModel.uiState.collect { state ->
                        when (state) {
                            is ManagePermissionsUiState.Loading -> { /* 로딩 처리 생략 */ }
                            is ManagePermissionsUiState.Success -> {
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
                            is ManagePermissionsEvent.PermissionsSaved -> {
                                showTopToast(getString(R.string.success_permissions_saved))
                                // 목록 및 상세화면 갱신을 위해 refresh 시그널을 전달하고 뒤로가기
                                findNavController().previousBackStackEntry?.savedStateHandle?.set("refresh", true)
                                findNavController().popBackStack()
                            }
                            is ManagePermissionsEvent.Error -> {
                                showTopToast(event.message)
                            }
                            else -> {}
                        }
                    }
                }
            }
        }
    }

    private fun savePermissions() {
        val currentCheckedIds = permAdapter.checkedIds
        viewModel.savePermissions(currentCheckedIds)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    companion object {
        fun newInstance(roleId: Int) = ManagePermissionsFragment().apply {
            arguments = Bundle().apply { putInt("role_id", roleId) }
        }
    }
}
