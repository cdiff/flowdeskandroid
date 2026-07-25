package com.example.flowdesk_android.feature.super_admin.presentation.tenants

import android.os.Bundle
import android.view.View
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.navigation.fragment.findNavController
import com.example.flowdesk_android.R
import com.example.flowdesk_android.core.extension.showTopToast
import com.example.flowdesk_android.databinding.FragmentSuperAdminTenantDetailBinding
import com.example.flowdesk_android.feature.super_admin.domain.model.TenantDetail
import com.example.flowdesk_android.core.base.BaseFragment
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Locale
import java.util.TimeZone

@AndroidEntryPoint
class TenantDetailFragment : BaseFragment(R.layout.fragment_super_admin_tenant_detail) {

    private val viewModel: TenantDetailViewModel by viewModels()

    private var _binding: FragmentSuperAdminTenantDetailBinding? = null
    private val binding get() = _binding!!

    private var isEditMode = false
    private var tenantId: Int = -1
    private var currentTenant: TenantDetail? = null

    override fun getToolbarView(view: View): View? = null

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        _binding = FragmentSuperAdminTenantDetailBinding.bind(view)
        super.onViewCreated(view, savedInstanceState)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    override fun initView() {
        tenantId = arguments?.getInt("tenant_id", -1) ?: -1
        setupListeners()
        if (tenantId != -1) viewModel.fetchDetail(tenantId)
    }

    private fun setupListeners() {
        binding.btnAction.setOnClickListener {
            if (isEditMode) {
                val tenant = currentTenant ?: return@setOnClickListener
                viewModel.saveTenant(
                    tenantId    = tenant.tenantId,
                    tenantName  = binding.etNameEdit.text?.toString() ?: "",
                    displayName = binding.etDisplayNameEdit.text?.toString() ?: "",
                    domain      = binding.etDomainEdit.text?.toString() ?: ""
                )
            } else {
                enterEditMode()
            }
        }

        binding.btnRetry.setOnClickListener {
            if (tenantId != -1) viewModel.fetchDetail(tenantId)
        }
    }

    override fun observeViewModel() {
        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {

                launch {
                    viewModel.uiState.collect { state ->
                        when (state) {
                            is TenantDetailUiState.Loading -> showLoading()
                            is TenantDetailUiState.Success -> {
                                currentTenant = state.tenant
                                bindData(state.tenant)
                                showContent()
                            }
                            is TenantDetailUiState.Error -> {
                                binding.tvErrorMessage.text = state.message
                                showError()
                            }
                        }
                    }
                }

                launch {
                    viewModel.event.collect { event ->
                        when (event) {
                            is TenantDetailEvent.SaveSuccess -> {
                                showTopToast(getString(R.string.tenant_msg_updated))
                                findNavController().popBackStack()
                            }
                            is TenantDetailEvent.Error -> {
                                showTopToast(event.message)
                            }
                        }
                    }
                }

                launch {
                    viewModel.isSaving.collect { saving ->
                        binding.btnAction.isEnabled = !saving
                        binding.btnAction.alpha = if (saving) 0.5f else 1.0f
                        binding.btnAction.text = when {
                            saving -> getString(R.string.label_action_saving)
                            isEditMode -> getString(R.string.label_action_save)
                            else -> getString(R.string.label_action_edit)
                        }
                    }
                }
            }
        }
    }

    private fun bindData(tenant: TenantDetail) {
        binding.tvTenantId.text = "#${tenant.tenantId}"

        if (tenant.isActive) {
            binding.tvStatusView.text = getString(R.string.label_status_active)
            binding.tvStatusView.setTextColor(requireContext().getColor(R.color.color_success))
            binding.tvStatusView.setBackgroundResource(R.drawable.bg_tag_light_green)
        } else {
            binding.tvStatusView.text = getString(R.string.label_status_inactive)
            binding.tvStatusView.setTextColor(requireContext().getColor(R.color.text_secondary))
            binding.tvStatusView.setBackgroundResource(R.drawable.bg_rect_rounded_light_gray)
        }

        binding.tvNameView.text = tenant.tenantName
        binding.tvDisplayNameView.text = tenant.displayName
        binding.tvDomainView.text = tenant.domain ?: "-"
        binding.tvCreatedAt.text = formatDate(tenant.createdAt)
        binding.tvUpdatedAt.text = formatDate(tenant.updatedAt)
    }

    private fun enterEditMode() {
        val tenant = currentTenant ?: return
        isEditMode = true
        binding.btnAction.text = getString(R.string.label_action_save)

        binding.tvNameView.visibility = View.GONE
        binding.tvDisplayNameView.visibility = View.GONE
        binding.tvDomainView.visibility = View.GONE

        binding.etNameEdit.setText(tenant.tenantName)
        binding.tilName.visibility = View.VISIBLE

        binding.etDisplayNameEdit.setText(tenant.displayName)
        binding.tilDisplayName.visibility = View.VISIBLE

        binding.etDomainEdit.setText(tenant.domain ?: "")
        binding.tilDomain.visibility = View.VISIBLE
    }

    private fun showLoading() {
        binding.progressBar.visibility = View.VISIBLE
        binding.llContent.visibility = View.GONE
        binding.llError.visibility = View.GONE
    }

    private fun showContent() {
        binding.progressBar.visibility = View.GONE
        binding.llContent.visibility = View.VISIBLE
        binding.llError.visibility = View.GONE
    }

    private fun showError() {
        binding.progressBar.visibility = View.GONE
        binding.llContent.visibility = View.GONE
        binding.llError.visibility = View.VISIBLE
    }

    private fun formatDate(raw: String?): String {
        if (raw.isNullOrBlank()) return "-"
        return try {
            val input  = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'", Locale.getDefault())
                .apply { timeZone = TimeZone.getTimeZone("UTC") }
            val output = SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.getDefault())
                .apply { timeZone = TimeZone.getDefault() }
            output.format(input.parse(raw)!!)
        } catch (e: Exception) {
            raw
        }
    }
}
