package com.example.flowdesk_android.feature.role.presentation.list

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
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch

@AndroidEntryPoint
class CreateRoleBottomSheetFragment(private val onSuccess: () -> Unit) : BottomSheetDialogFragment() {

    private val viewModel: RolesViewModel by viewModels({ requireParentFragment() })

    private lateinit var btnClose: ImageView
    private lateinit var btnCancel: View
    private lateinit var btnCreate: View
    private lateinit var etDisplayName: EditText
    private lateinit var etRoleName: EditText
    private lateinit var etDescription: EditText
    private lateinit var progressBar: ProgressBar

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        return inflater.inflate(R.layout.dialog_role_create, container, false)
    }

    override fun getTheme(): Int {
        return R.style.CustomBottomSheetDialogTheme
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        dialog?.setOnShowListener { dialogInterface ->
            val bottomSheetDialog = dialogInterface as com.google.android.material.bottomsheet.BottomSheetDialog
            val bottomSheet = bottomSheetDialog.findViewById<View>(com.google.android.material.R.id.design_bottom_sheet)
            bottomSheet?.let {
                val behavior = com.google.android.material.bottomsheet.BottomSheetBehavior.from(it)
                behavior.state = com.google.android.material.bottomsheet.BottomSheetBehavior.STATE_EXPANDED
                behavior.skipCollapsed = true
                behavior.isShouldRemoveExpandedCorners = false
            }
        }

        initViews(view)
        setupListeners()
        observeViewModel()
    }

    private fun initViews(view: View) {
        btnClose = view.findViewById(R.id.btn_close)
        btnCancel = view.findViewById(R.id.btn_cancel)
        btnCreate = view.findViewById(R.id.btn_create)
        etDisplayName = view.findViewById(R.id.et_display_name)
        etRoleName = view.findViewById(R.id.et_role_name)
        etDescription = view.findViewById(R.id.et_description)
        progressBar = view.findViewById(R.id.progress_bar)
    }

    private fun setupListeners() {
        btnClose.setOnClickListener { dismiss() }
        btnCancel.setOnClickListener { dismiss() }

        btnCreate.setOnClickListener {
            val displayName = etDisplayName.text.toString()
            val roleName = etRoleName.text.toString()
            val description = etDescription.text.toString()

            if (displayName.isBlank() || roleName.isBlank() || description.isBlank()) {
                Toast.makeText(context, "모든 항목을 입력해주세요.", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            viewModel.createRole(roleName, displayName, description)
        }
    }

    private fun observeViewModel() {
        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                
                launch {
                    viewModel.uiState.collect { state ->
                        progressBar.isVisible = state is RoleListUiState.Loading
                        btnCreate.isEnabled = state !is RoleListUiState.Loading
                    }
                }
                
                launch {
                    viewModel.event.collect { event ->
                        when (event) {
                            is RoleListEvent.RoleCreated -> {
                                Toast.makeText(context, "새 역할이 생성되었습니다.", Toast.LENGTH_SHORT).show()
                                onSuccess()
                                dismiss()
                            }
                            is RoleListEvent.Error -> {
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
        const val TAG = "CreateRoleBottomSheet"
    }
}
