package com.example.flowdesk_android.feature.user_management.presentation.roles.list

import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
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
import androidx.navigation.fragment.findNavController
import com.example.flowdesk_android.R
import com.example.flowdesk_android.databinding.DialogRoleDeleteBinding
import com.example.flowdesk_android.databinding.FragmentUserManagementRoleListBinding
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch
import com.example.flowdesk_android.core.extension.showTopToast

@AndroidEntryPoint
class RolesFragment : Fragment() {

    private val viewModel: RolesViewModel by viewModels()
    private lateinit var roleAdapter: RoleAdapter

    private var _binding: FragmentUserManagementRoleListBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentUserManagementRoleListBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setupRecyclerView()
        setupListeners()
        observeViewModel()

        viewModel.triggerRefresh()
    }

    private fun setupRecyclerView() {
        roleAdapter = RoleAdapter(
            onEditRoleClick = { role ->
                val bundle = Bundle().apply {
                    putInt("roleId", role.roleId)
                }
                findNavController().navigate(R.id.roleDetailFragment, bundle)
            },
            onToggleStatusClick = { role ->
                viewModel.toggleStatus(role.roleId, role.isActive)
                showTopToast(getString(R.string.toast_status_change_requested))
            },
            onDeleteRoleClick = { role ->
                val dialogBinding = DialogRoleDeleteBinding.inflate(LayoutInflater.from(requireContext()))
                val dialog = androidx.appcompat.app.AlertDialog.Builder(requireContext())
                    .setView(dialogBinding.root)
                    .create()

                dialog.window?.setBackgroundDrawableResource(android.R.color.transparent)

                dialogBinding.tvTitle.text = getString(R.string.dialog_role_delete_title)
                dialogBinding.tvMessage.text = getString(R.string.dialog_role_delete_message, role.displayName)

                dialogBinding.btnCancel.setOnClickListener {
                    dialog.dismiss()
                }

                dialogBinding.btnConfirm.setOnClickListener {
                    viewModel.deleteRole(role.roleId)
                    showTopToast(getString(R.string.toast_delete_requested))
                    dialog.dismiss()
                }

                dialog.show()
            }
        )
        binding.rvRoles.adapter = roleAdapter
    }

    private fun setupListeners() {
        binding.btnCreateRole.setOnClickListener {
            val bottomSheet = CreateRoleBottomSheetFragment()
            bottomSheet.onConfirm = { displayName, roleName, description ->
                viewModel.createRole(roleName, displayName, description) { success, errorMsg ->
                    if (success) {
                        Toast.makeText(context, getString(R.string.success_role_created), Toast.LENGTH_SHORT).show()
                        bottomSheet.dismiss()
                        viewModel.triggerRefresh()
                    } else {
                        Toast.makeText(context, errorMsg ?: "역할 생성 실패", Toast.LENGTH_LONG).show()
                    }
                }
            }
            bottomSheet.show(childFragmentManager, CreateRoleBottomSheetFragment.TAG)
        }

        binding.etSearch.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                viewModel.search(s?.toString() ?: "")
                binding.btnClear.visibility = if (s.isNullOrEmpty()) View.GONE else View.VISIBLE
            }
            override fun afterTextChanged(s: Editable?) {}
        })

        binding.btnClear.setOnClickListener {
            binding.etSearch.text.clear()
        }
    }

    private fun observeViewModel() {
        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                launch {
                    viewModel.uiState.collect { state ->
                        when(state) {
                            is RoleListUiState.Loading -> {
                                binding.progressBar.visibility = View.VISIBLE
                                binding.rvRoles.visibility = View.GONE
                                binding.llEmpty.visibility = View.GONE
                            }
                            is RoleListUiState.Success -> {
                                binding.progressBar.visibility = View.GONE
                                binding.rvRoles.visibility = View.VISIBLE

                                val total = state.roles.size
                                val active = state.roles.count { it.isActive }
                                val inactive = total - active

                                binding.tvTotalCount.text = "${total}"
                                binding.tvActiveCount.text = "${active}"
                                binding.tvInactiveCount.text = "${inactive}"

                                if (state.roles.isEmpty()) {
                                    binding.llEmpty.visibility = View.VISIBLE
                                    binding.rvRoles.visibility = View.GONE
                                } else {
                                    binding.llEmpty.visibility = View.GONE
                                    binding.rvRoles.visibility = View.VISIBLE
                                }
                            }
                            is RoleListUiState.Error -> {
                                binding.progressBar.visibility = View.GONE
                                binding.rvRoles.visibility = View.VISIBLE
                                binding.llEmpty.visibility = View.GONE
                                showTopToast(state.message)
                            }
                            else -> {}
                        }
                    }
                }

                launch {
                    viewModel.filteredRoles.collect { roles ->
                        roleAdapter.submitList(roles)
                    }
                }

                launch {
                    viewModel.event.collect { event ->
                        when(event) {
                            is RoleListEvent.RoleCreated -> {
                                // 생성은 CreateRoleBottomSheetFragment 내에서 처리
                            }
                            is RoleListEvent.RoleDeleted -> {
                                showTopToast(getString(R.string.toast_role_deleted))
                            }
                            is RoleListEvent.StatusToggled -> {
                                showTopToast(getString(R.string.toast_status_changed))
                            }
                            is RoleListEvent.Error -> {
                                showTopToast(event.message)
                            }
                        }
                    }
                }
            }
        }
    }


    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}

