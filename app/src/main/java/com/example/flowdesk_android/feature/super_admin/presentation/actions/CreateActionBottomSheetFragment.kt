package com.example.flowdesk_android.feature.super_admin.presentation.actions

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
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch

@AndroidEntryPoint
class CreateActionBottomSheetFragment(
    private val onSuccess: () -> Unit
) : BottomSheetDialogFragment() {

    private val viewModel: ActionsViewModel by viewModels({ requireParentFragment() })

    private lateinit var etActionName: EditText
    private lateinit var etDisplayName: EditText
    private lateinit var btnCreate: View
    private lateinit var btnCancel: View
    private lateinit var btnClose: ImageView
    private lateinit var progressBar: ProgressBar

    override fun getTheme(): Int = R.style.CustomBottomSheetDialogTheme

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View = inflater.inflate(R.layout.dialog_super_admin_create_action, container, false)

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
        etActionName  = view.findViewById(R.id.et_action_name)
        etDisplayName = view.findViewById(R.id.et_display_name)
        btnCreate     = view.findViewById(R.id.btn_create)
        btnCancel     = view.findViewById(R.id.btn_cancel)
        btnClose      = view.findViewById(R.id.btn_close)
        progressBar   = view.findViewById(R.id.progress_bar)
    }

    private fun setupListeners() {
        btnClose.setOnClickListener { dismiss() }
        btnCancel.setOnClickListener { dismiss() }

        btnCreate.setOnClickListener {
            val actionName  = etActionName.text.toString().trim()
            val displayName = etDisplayName.text.toString().trim()

            if (actionName.isBlank()) {
                etActionName.error = "액션 이름을 입력해주세요."
                return@setOnClickListener
            }
            if (displayName.isBlank()) {
                etDisplayName.error = "표시 이름을 입력해주세요."
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
                        progressBar.isVisible = state is ActionListUiState.Loading
                        btnCreate.isEnabled   = state !is ActionListUiState.Loading
                    }
                }

                launch {
                    viewModel.event.collect { event ->
                        when (event) {
                            is ActionListEvent.ActionCreated -> {
                                Toast.makeText(context, "액션이 생성되었습니다.", Toast.LENGTH_SHORT).show()
                                onSuccess()
                                dismiss()
                            }
                            is ActionListEvent.Error -> {
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
        const val TAG = "CreateActionBottomSheet"
    }
}
