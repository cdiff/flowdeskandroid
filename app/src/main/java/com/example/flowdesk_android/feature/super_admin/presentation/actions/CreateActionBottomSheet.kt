package com.example.flowdesk_android.feature.super_admin.presentation.actions

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
import com.example.flowdesk_android.databinding.DialogActionCreateBottomSheetBinding
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch

@AndroidEntryPoint
class CreateActionBottomSheet(
    private val onSuccess: () -> Unit
) : BottomSheetDialogFragment() {

    private val viewModel: ActionsViewModel by viewModels({ requireParentFragment() })

    private var _binding: DialogActionCreateBottomSheetBinding? = null
    private val binding get() = _binding!!

    override fun getTheme(): Int = R.style.CustomBottomSheetDialogTheme

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = DialogActionCreateBottomSheetBinding.inflate(inflater, container, false)
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
            val actionName  = binding.etActionName.text.toString().trim()
            val displayName = binding.etDisplayName.text.toString().trim()

            if (actionName.isBlank()) {
                binding.etActionName.error = getString(R.string.action_msg_enter_name)
                return@setOnClickListener
            }
            if (displayName.isBlank()) {
                binding.etDisplayName.error = getString(R.string.action_msg_enter_display_name)
                return@setOnClickListener
            }

            viewModel.createAction(actionName, displayName)
        }
    }

    private fun observeViewModel() {
        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                launch {
                    viewModel.uiState.collect { state ->
                        binding.progressBar.isVisible = state is ActionListUiState.Loading
                        binding.btnCreate.isEnabled   = state !is ActionListUiState.Loading
                    }
                }

                launch {
                    viewModel.event.collect { event ->
                        when (event) {
                            is ActionListEvent.ActionCreated -> {
                                showTopToast(getString(R.string.action_msg_created))
                                onSuccess()
                                dismiss()
                            }
                            is ActionListEvent.Error -> {
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
        const val TAG = "CreateActionBottomSheet"
    }
}
