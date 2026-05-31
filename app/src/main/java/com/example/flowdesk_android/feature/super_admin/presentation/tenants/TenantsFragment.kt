package com.example.flowdesk_android.feature.super_admin.presentation.tenants

import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import android.widget.ProgressBar
import android.widget.TextView
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.recyclerview.widget.RecyclerView
import com.example.flowdesk_android.R
import com.example.flowdesk_android.feature.super_admin.domain.model.Tenant
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch

@AndroidEntryPoint
class TenantsFragment : Fragment() {

    private val viewModel: TenantsViewModel by viewModels()
    private lateinit var tenantAdapter: TenantAdapter

    private lateinit var rvTenants: RecyclerView
    private lateinit var progressBar: ProgressBar
    private lateinit var etSearch: EditText
    private lateinit var tvTotalCount: TextView
    private lateinit var tvActiveCount: TextView
    private lateinit var tvInactiveCount: TextView
    private lateinit var btnCreateTenant: View

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_super_admin_tenants, container, false)
        initViews(view)
        setupRecyclerView()
        setupListeners()
        observeViewModel()
        return view
    }

    private fun initViews(view: View) {
        rvTenants       = view.findViewById(R.id.rv_tenants)
        progressBar     = view.findViewById(R.id.progress_bar)
        etSearch        = view.findViewById(R.id.et_search)
        tvTotalCount    = view.findViewById(R.id.tv_total_count)
        tvActiveCount   = view.findViewById(R.id.tv_active_count)
        tvInactiveCount = view.findViewById(R.id.tv_inactive_count)
        btnCreateTenant = view.findViewById(R.id.btn_create_tenant)
    }

    private fun setupRecyclerView() {
        tenantAdapter = TenantAdapter(
            onItemClick = { tenant ->
                // TODO: Navigation Graph에 tenantDetailFragment 등록 후 활성화
                // val bundle = Bundle().apply { putInt("tenant_id", tenant.tenantId) }
                // findNavController().navigate(R.id.tenantDetailFragment, bundle)
                showTopToast("${tenant.tenantName} 상세 (준비 중)")
            },
            onToggleStatusClick = { tenant ->
                viewModel.toggleStatus(tenant)
                showTopToast("상태 변경을 요청했습니다.")
            },
            onDeleteClick = { tenant ->
                showDeleteDialog(tenant)
            }
        )
        rvTenants.adapter = tenantAdapter
    }

    private fun setupListeners() {
        btnCreateTenant.setOnClickListener {
            val bottomSheet = CreateTenantBottomSheetFragment {
                viewModel.fetchTenants()
            }
            bottomSheet.show(childFragmentManager, CreateTenantBottomSheetFragment.TAG)
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
                        when (state) {
                            is TenantListUiState.Loading -> {
                                progressBar.visibility = View.VISIBLE
                                rvTenants.visibility = View.GONE
                            }
                            is TenantListUiState.Success -> {
                                progressBar.visibility = View.GONE
                                rvTenants.visibility = View.VISIBLE

                                val total    = state.tenants.size
                                val active   = state.tenants.count { it.isActive }
                                val inactive = total - active
                                tvTotalCount.text    = "${total}개"
                                tvActiveCount.text   = active.toString()
                                tvInactiveCount.text = inactive.toString()
                            }
                            is TenantListUiState.Error -> {
                                progressBar.visibility = View.GONE
                                rvTenants.visibility = View.VISIBLE
                                showTopToast(state.message)
                            }
                            else -> {}
                        }
                    }
                }

                launch {
                    viewModel.filteredTenants.collect { tenants ->
                        tenantAdapter.submitList(tenants)
                    }
                }

                launch {
                    viewModel.event.collect { event ->
                        when (event) {
                            is TenantListEvent.TenantCreated -> showTopToast("테넌트가 생성되었습니다.")
                            is TenantListEvent.TenantDeleted -> showTopToast("테넌트가 삭제되었습니다.")
                            is TenantListEvent.StatusToggled -> showTopToast("상태가 변경되었습니다.")
                            is TenantListEvent.Error         -> showTopToast(event.message)
                        }
                    }
                }
            }
        }
    }

    private fun showDeleteDialog(tenant: Tenant) {
        val dialogView = LayoutInflater.from(requireContext())
            .inflate(R.layout.dialog_role_delete, null)
        val dialog = androidx.appcompat.app.AlertDialog.Builder(requireContext())
            .setView(dialogView)
            .create()
        dialog.window?.setBackgroundDrawableResource(android.R.color.transparent)

        dialogView.findViewById<TextView>(R.id.tv_title).text = "테넌트 삭제"
        dialogView.findViewById<TextView>(R.id.tv_message).text =
            "'${tenant.tenantName}' 테넌트를 정말 삭제하시겠습니까?\n이 작업은 되돌릴 수 없습니다."
        dialogView.findViewById<View>(R.id.btn_cancel).setOnClickListener { dialog.dismiss() }
        dialogView.findViewById<View>(R.id.btn_confirm).setOnClickListener {
            viewModel.deleteTenant(tenant.tenantId)
            dialog.dismiss()
        }
        dialog.show()
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
