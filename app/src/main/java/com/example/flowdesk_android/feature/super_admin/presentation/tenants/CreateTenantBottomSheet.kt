package com.example.flowdesk_android.feature.super_admin.presentation.tenants

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.view.isVisible
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import com.example.flowdesk_android.R
import com.example.flowdesk_android.core.extension.showTopToast
import com.example.flowdesk_android.databinding.DialogTenantCreateBottomSheetBinding
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch

@AndroidEntryPoint
class CreateTenantBottomSheet(private val onSuccess: () -> Unit) : BottomSheetDialogFragment() {

    private val viewModel: TenantsViewModel by viewModels({ requireParentFragment() })

    private var _binding: DialogTenantCreateBottomSheetBinding? = null
    private val binding get() = _binding!!

    override fun getTheme(): Int = R.style.CustomBottomSheetDialogTheme

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = DialogTenantCreateBottomSheetBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        dialog?.setOnShowListener { dialogInterface ->
            val bottomSheetDialog = dialogInterface as BottomSheetDialog
            val bottomSheet = bottomSheetDialog.findViewById<View>(com.google.android.material.R.id.design_bottom_sheet)
            bottomSheet?.let {
                val behavior = BottomSheetBehavior.from(it)
                behavior.state = BottomSheetBehavior.STATE_EXPANDED
                behavior.skipCollapsed = true
                behavior.isShouldRemoveExpandedCorners = false
            }
        }

        setupListeners()
        observeViewModel()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    private fun setupListeners() {
        binding.btnClose.setOnClickListener { dismiss() }

        binding.btnCreate.setOnClickListener {
            val tenantName  = binding.etTenantName.text.toString().trim()
            val displayName = binding.etDisplayName.text.toString().trim()
            val domain      = binding.etDomain.text.toString().trim()

            if (tenantName.isBlank()) {
                binding.etTenantName.error = getString(R.string.tenant_msg_enter_name)
                return@setOnClickListener
            }
            if (displayName.isBlank()) {
                binding.etDisplayName.error = getString(R.string.tenant_msg_enter_display_name)
                return@setOnClickListener
            }
            if (domain.isBlank()) {
                binding.etDomain.error = getString(R.string.tenant_msg_enter_domain)
                return@setOnClickListener
            }

            viewModel.createTenant(tenantName, displayName, domain)
        }
    }

    private fun observeViewModel() {
        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                launch {
                    viewModel.uiState.collect { state ->
                        binding.progressBar.isVisible = state is TenantListUiState.Loading
                        binding.btnCreate.isEnabled   = state !is TenantListUiState.Loading
                    }
                }

                launch {
                    viewModel.event.collect { event ->
                        when (event) {
                            is TenantListEvent.TenantCreated -> {
                                showTopToast(getString(R.string.tenant_msg_created))
                                onSuccess()
                                dismiss()
                            }
                            is TenantListEvent.Error -> {
                                showTopToast(event.message)
                            }
                            else -> {}
                        }
                    }
                }
            }
        }
    }

    companion object {
        const val TAG = "CreateTenantBottomSheet"
    }
}
