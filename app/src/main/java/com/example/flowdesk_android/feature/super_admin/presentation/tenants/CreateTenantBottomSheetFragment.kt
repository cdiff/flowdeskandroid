package com.example.flowdesk_android.feature.super_admin.presentation.tenants

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import android.widget.ImageView
import android.widget.ProgressBar
import android.widget.Toast
import androidx.core.view.isVisible
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import com.example.flowdesk_android.R
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import com.google.android.material.bottomsheet.BottomSheetBehavior
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch

@AndroidEntryPoint
class CreateTenantBottomSheetFragment(private val onSuccess: () -> Unit) : BottomSheetDialogFragment() {

    private val viewModel: TenantsViewModel by viewModels({ requireParentFragment() })

    private lateinit var btnClose: ImageView
    private lateinit var btnCancel: View
    private lateinit var btnCreate: View
    private lateinit var etTenantName: EditText
    private lateinit var etDisplayName: EditText
    private lateinit var etDomain: EditText
    private lateinit var progressBar: ProgressBar

    override fun getTheme(): Int = R.style.CustomBottomSheetDialogTheme

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View = inflater.inflate(R.layout.dialog_super_admin_create_tenant, container, false)

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

        initViews(view)
        setupListeners()
        observeViewModel()
    }

    private fun initViews(view: View) {
        btnClose       = view.findViewById(R.id.btn_close)
        btnCancel      = view.findViewById(R.id.btn_cancel)
        btnCreate      = view.findViewById(R.id.btn_create)
        etTenantName   = view.findViewById(R.id.et_tenant_name)
        etDisplayName  = view.findViewById(R.id.et_display_name)
        etDomain       = view.findViewById(R.id.et_domain)
        progressBar    = view.findViewById(R.id.progress_bar)
    }

    private fun setupListeners() {
        btnClose.setOnClickListener { dismiss() }
        btnCancel.setOnClickListener { dismiss() }

        btnCreate.setOnClickListener {
            val tenantName  = etTenantName.text.toString().trim()
            val displayName = etDisplayName.text.toString().trim()
            val domain      = etDomain.text.toString().trim()

            if (tenantName.isBlank()) {
                etTenantName.error = "테넌트명을 입력해주세요."
                return@setOnClickListener
            }
            if (displayName.isBlank()) {
                etDisplayName.error = "표시 이름을 입력해주세요."
                return@setOnClickListener
            }
            if (domain.isBlank()) {
                etDomain.error = "도메인을 입력해주세요."
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
                        progressBar.isVisible = state is TenantListUiState.Loading
                        btnCreate.isEnabled   = state !is TenantListUiState.Loading
                    }
                }

                launch {
                    viewModel.event.collect { event ->
                        when (event) {
                            is TenantListEvent.TenantCreated -> {
                                Toast.makeText(context, "테넌트가 생성되었습니다.", Toast.LENGTH_SHORT).show()
                                onSuccess()
                                dismiss()
                            }
                            is TenantListEvent.Error -> {
                                Toast.makeText(context, event.message, Toast.LENGTH_LONG).show()
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
