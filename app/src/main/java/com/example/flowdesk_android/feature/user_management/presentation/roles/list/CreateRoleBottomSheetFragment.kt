package com.example.flowdesk_android.feature.user_management.presentation.roles.list

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.core.view.isVisible
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import com.example.flowdesk_android.R
import com.example.flowdesk_android.databinding.DialogRoleCreateBinding
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch

@AndroidEntryPoint
class CreateRoleBottomSheetFragment : BottomSheetDialogFragment() {

    var onSuccess: (() -> Unit)? = null

    private val viewModel: RolesViewModel by viewModels({ requireParentFragment() })

    private var _binding: DialogRoleCreateBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = DialogRoleCreateBinding.inflate(inflater, container, false)
        return binding.root
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

        setupListeners()
        observeViewModel()
    }

    private fun setupListeners() {
        binding.btnClose.setOnClickListener { dismiss() }
        binding.btnCancel.setOnClickListener { dismiss() }

        binding.btnCreate.setOnClickListener {
            val displayName = binding.etDisplayName.text.toString()
            val roleName = binding.etRoleName.text.toString()
            val description = binding.etDescription.text.toString()

            if (displayName.isBlank() || roleName.isBlank() || description.isBlank()) {
                Toast.makeText(context, getString(R.string.error_fill_all_fields), Toast.LENGTH_SHORT).show()
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
                        binding.progressBar.isVisible = state is RoleListUiState.Loading
                        binding.btnCreate.isEnabled = state !is RoleListUiState.Loading
                    }
                }
                
                launch {
                    viewModel.event.collect { event ->
                        when (event) {
                            is RoleListEvent.RoleCreated -> {
                                Toast.makeText(context, getString(R.string.success_role_created), Toast.LENGTH_SHORT).show()
                                onSuccess?.invoke()
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

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    companion object {
        const val TAG = "CreateRoleBottomSheet"
    }
}

