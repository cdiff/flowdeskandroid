package com.example.flowdesk_android.feature.super_admin.presentation.tenants

import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.navigation.fragment.findNavController
import com.example.flowdesk_android.R
import com.example.flowdesk_android.core.extension.showTopToast
import com.example.flowdesk_android.databinding.FragmentSuperAdminTenantsBinding
import com.example.flowdesk_android.feature.super_admin.domain.model.Tenant
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch

@AndroidEntryPoint
class TenantsFragment : Fragment() {

    private val viewModel: TenantsViewModel by viewModels()
    private lateinit var tenantAdapter: TenantAdapter

    private var _binding: FragmentSuperAdminTenantsBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentSuperAdminTenantsBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setupRecyclerView()
        setupListeners()
        observeViewModel()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    private fun setupRecyclerView() {
        tenantAdapter = TenantAdapter(
            onItemClick = { tenant ->
                val bundle = Bundle().apply { putInt("tenant_id", tenant.tenantId) }
                findNavController().navigate(R.id.tenantDetailFragment, bundle)
            },
            onToggleStatusClick = { tenant ->
                viewModel.toggleStatus(tenant)
                showTopToast(getString(R.string.tenant_msg_status_change_requested))
            },
            onDeleteClick = { tenant ->
                showDeleteDialog(tenant)
            }
        )
        binding.rvTenants.adapter = tenantAdapter
    }

    private fun setupListeners() {
        binding.btnCloseBanner.setOnClickListener {
            binding.bannerInfo.visibility = View.GONE
        }

        binding.btnCreateTenant.setOnClickListener {
            val bottomSheet = CreateTenantBottomSheet {
                viewModel.triggerRefresh()
            }
            bottomSheet.show(childFragmentManager, CreateTenantBottomSheet.TAG)
        }

        binding.etSearch.addTextChangedListener(object : TextWatcher {
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
                                binding.progressBar.visibility = View.VISIBLE
                                binding.rvTenants.visibility = View.GONE
                                binding.llEmpty.visibility = View.GONE
                            }
                            is TenantListUiState.Success -> {
                                binding.progressBar.visibility = View.GONE
                                
                                val total    = state.tenants.size
                                val active   = state.tenants.count { it.isActive }
                                val inactive = total - active
                                binding.tvBadgeTotal.text    = getString(R.string.label_status_count_total, total)
                                binding.tvBadgeActive.text   = getString(R.string.label_status_count_active, active)
                                binding.tvBadgeInactive.text = getString(R.string.label_status_count_inactive, inactive)

                                if (state.tenants.isEmpty()) {
                                    binding.rvTenants.visibility = View.GONE
                                    binding.llEmpty.visibility = View.VISIBLE
                                } else {
                                    binding.rvTenants.visibility = View.VISIBLE
                                    binding.llEmpty.visibility = View.GONE
                                }
                            }
                            is TenantListUiState.Error -> {
                                binding.progressBar.visibility = View.GONE
                                binding.rvTenants.visibility = View.VISIBLE
                                binding.llEmpty.visibility = View.GONE
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
                            is TenantListEvent.TenantCreated -> showTopToast(getString(R.string.tenant_msg_created))
                            is TenantListEvent.TenantDeleted -> showTopToast(getString(R.string.tenant_msg_deleted))
                            is TenantListEvent.StatusToggled -> showTopToast(getString(R.string.tenant_msg_status_changed))
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

        dialogView.findViewById<TextView>(R.id.tv_title).text = getString(R.string.tenant_delete_title)
        dialogView.findViewById<TextView>(R.id.tv_message).text =
            getString(R.string.tenant_delete_message, tenant.tenantName)
        dialogView.findViewById<View>(R.id.btn_cancel).setOnClickListener { dialog.dismiss() }
        dialogView.findViewById<View>(R.id.btn_confirm).setOnClickListener {
            viewModel.deleteTenant(tenant.tenantId)
            dialog.dismiss()
        }
        dialog.show()
    }
}
