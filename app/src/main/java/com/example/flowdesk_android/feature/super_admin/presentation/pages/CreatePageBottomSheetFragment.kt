package com.example.flowdesk_android.feature.super_admin.presentation.pages

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
class CreatePageBottomSheetFragment(
    private val onSuccess: () -> Unit
) : BottomSheetDialogFragment() {

    private val viewModel: PagesViewModel by viewModels({ requireParentFragment() })

    private lateinit var etPageName: EditText
    private lateinit var etDisplayName: EditText
    private lateinit var etPath: EditText
    private lateinit var etDescription: EditText
    private lateinit var etSortOrder: EditText
    private lateinit var btnCreate: View
    private lateinit var btnCancel: View
    private lateinit var btnClose: ImageView
    private lateinit var progressBar: ProgressBar

    override fun getTheme(): Int = R.style.CustomBottomSheetDialogTheme

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View = inflater.inflate(R.layout.dialog_super_admin_create_page, container, false)

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
        etPageName    = view.findViewById(R.id.et_page_name)
        etDisplayName = view.findViewById(R.id.et_display_name)
        etPath        = view.findViewById(R.id.et_path)
        etDescription = view.findViewById(R.id.et_description)
        etSortOrder   = view.findViewById(R.id.et_sort_order)
        btnCreate     = view.findViewById(R.id.btn_create)
        btnCancel     = view.findViewById(R.id.btn_cancel)
        btnClose      = view.findViewById(R.id.btn_close)
        progressBar   = view.findViewById(R.id.progress_bar)
    }

    private fun setupListeners() {
        btnClose.setOnClickListener { dismiss() }
        btnCancel.setOnClickListener { dismiss() }

        btnCreate.setOnClickListener {
            val pageName    = etPageName.text.toString().trim()
            val displayName = etDisplayName.text.toString().trim()
            val path        = etPath.text.toString().trim()
            val description = etDescription.text.toString().trim().ifBlank { null }
            val sortOrder   = etSortOrder.text.toString().trim().toIntOrNull() ?: 1

            if (pageName.isBlank()) {
                etPageName.error = "페이지 이름을 입력해주세요."
                return@setOnClickListener
            }
            if (displayName.isBlank()) {
                etDisplayName.error = "표시 이름을 입력해주세요."
                return@setOnClickListener
            }
            if (path.isBlank()) {
                etPath.error = "경로를 입력해주세요."
                return@setOnClickListener
            }

            viewModel.createPage(pageName, path, displayName, description, null, sortOrder)
        }
    }

    private fun observeViewModel() {
        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                launch {
                    viewModel.uiState.collect { state ->
                        progressBar.isVisible = state is PageListUiState.Loading
                        btnCreate.isEnabled   = state !is PageListUiState.Loading
                    }
                }

                launch {
                    viewModel.event.collect { event ->
                        when (event) {
                            is PageListEvent.PageCreated -> {
                                Toast.makeText(context, "페이지가 생성되었습니다.", Toast.LENGTH_SHORT).show()
                                onSuccess()
                                dismiss()
                            }
                            is PageListEvent.Error -> {
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
        const val TAG = "CreatePageBottomSheet"
    }
}
