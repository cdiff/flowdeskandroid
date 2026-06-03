package com.example.flowdesk_android.feature.user_management.presentation.roles.list

import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import android.widget.PopupMenu
import android.widget.ProgressBar
import android.widget.TextView
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.RecyclerView
import com.example.flowdesk_android.R
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch

@AndroidEntryPoint
class RolesFragment : Fragment() {

    private val viewModel: RolesViewModel by viewModels()
    private lateinit var roleAdapter: RoleAdapter

    private lateinit var rvRoles: RecyclerView
    private lateinit var progressBar: ProgressBar
    private lateinit var etSearch: EditText
    private lateinit var tvTotalCount: TextView
    private lateinit var tvActiveCount: TextView
    private lateinit var tvInactiveCount: TextView
    private lateinit var btnCreateRole: View
    private lateinit var bannerInfo: View
    private lateinit var btnCloseBanner: View

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_role_list, container, false)

        initViews(view)
        setupRecyclerView()
        setupListeners()
        observeViewModel()

        viewModel.fetchRoles()

        return view
    }

    private fun initViews(view: View) {
        rvRoles = view.findViewById(R.id.rv_roles)
        progressBar = view.findViewById(R.id.progress_bar)
        etSearch = view.findViewById(R.id.et_search)
        tvTotalCount = view.findViewById(R.id.tv_total_count)
        tvActiveCount = view.findViewById(R.id.tv_active_count)
        tvInactiveCount = view.findViewById(R.id.tv_inactive_count)
        btnCreateRole = view.findViewById(R.id.btn_create_role)
        bannerInfo = view.findViewById(R.id.banner_info)
        btnCloseBanner = view.findViewById(R.id.btn_close_banner)
    }

    private fun setupRecyclerView() {
        roleAdapter = RoleAdapter(
            onManagePermissionsClick = { role ->
                val bundle = Bundle().apply { putInt("role_id", role.roleId) }
                findNavController().navigate(R.id.managePermissionsFragment, bundle)
            },
            onEditRoleClick = { role ->
                val bundle = Bundle().apply {
                    putInt("roleId", role.roleId)
                }
                findNavController().navigate(R.id.roleDetailFragment, bundle)
            },
            onToggleStatusClick = { role ->
                viewModel.toggleStatus(role.roleId, role.isActive)
                showTopToast("상태 변경을 요청했습니다.")
            },
            onDeleteRoleClick = { role ->
                val dialogView = LayoutInflater.from(requireContext()).inflate(R.layout.dialog_role_delete, null)
                val dialog = androidx.appcompat.app.AlertDialog.Builder(requireContext())
                    .setView(dialogView)
                    .create()

                dialog.window?.setBackgroundDrawableResource(android.R.color.transparent)

                val tvTitle = dialogView.findViewById<TextView>(R.id.tv_title)
                val tvMessage = dialogView.findViewById<TextView>(R.id.tv_message)
                val btnCancel = dialogView.findViewById<View>(R.id.btn_cancel)
                val btnConfirm = dialogView.findViewById<View>(R.id.btn_confirm)

                tvTitle.text = "역할 삭제"
                tvMessage.text = "'${role.displayName}' 역할을 정말 삭제하시겠습니까?\n이 작업은 되돌릴 수 없습니다."

                btnCancel.setOnClickListener {
                    dialog.dismiss()
                }

                btnConfirm.setOnClickListener {
                    viewModel.deleteRole(role.roleId)
                    showTopToast("삭제 요청을 보냈습니다.")
                    dialog.dismiss()
                }

                dialog.show()
            }
        )
        rvRoles.adapter = roleAdapter
    }

    private fun setupListeners() {
        btnCloseBanner.setOnClickListener {
            bannerInfo.visibility = View.GONE
        }

        btnCreateRole.setOnClickListener {
            val bottomSheet = CreateRoleBottomSheetFragment {
                viewModel.fetchRoles()
            }
            bottomSheet.show(childFragmentManager, CreateRoleBottomSheetFragment.TAG)
        }

        etSearch.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                viewModel.search(s?.toString() ?: "")
            }
            override fun afterTextChanged(s: Editable?) {}
        })
    }

    private fun observeViewModel() {
        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                launch {
                    viewModel.uiState.collect { state ->
                        when(state) {
                            is RoleListUiState.Loading -> {
                                progressBar.visibility = View.VISIBLE
                                rvRoles.visibility = View.GONE
                            }
                            is RoleListUiState.Success -> {
                                progressBar.visibility = View.GONE
                                rvRoles.visibility = View.VISIBLE

                                val total = state.roles.size
                                val active = state.roles.count { it.isActive }
                                val inactive = total - active

                                tvTotalCount.text = "총 ${total}개"
                                tvActiveCount.text = "활성 ${active}개"
                                tvInactiveCount.text = "비활성 ${inactive}개"
                            }
                            is RoleListUiState.Error -> {
                                progressBar.visibility = View.GONE
                                rvRoles.visibility = View.VISIBLE
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
            }
        }
    }

    @Suppress("DEPRECATION")
    private fun showTopToast(message: String) {
        val inflater = requireActivity().layoutInflater
        val layout = inflater.inflate(R.layout.view_common_toast_top, null)
        layout.findViewById<TextView>(R.id.tv_toast_message).text = message

        val toast = Toast(requireContext())
        toast.setGravity(android.view.Gravity.TOP or android.view.Gravity.CENTER_HORIZONTAL, 0, 100)
        toast.duration = Toast.LENGTH_SHORT
        toast.view = layout
        toast.show()
    }
}
