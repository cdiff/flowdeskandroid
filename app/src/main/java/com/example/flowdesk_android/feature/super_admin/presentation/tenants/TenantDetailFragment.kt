package com.example.flowdesk_android.feature.super_admin.presentation.tenants

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import android.widget.ProgressBar
import android.widget.TextView
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.navigation.fragment.findNavController
import com.example.flowdesk_android.R
import com.example.flowdesk_android.feature.super_admin.domain.model.TenantDetail
import com.google.android.material.textfield.TextInputEditText
import com.google.android.material.textfield.TextInputLayout
import com.example.flowdesk_android.core.base.BaseFragment
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Locale
import java.util.TimeZone

@AndroidEntryPoint
class TenantDetailFragment : BaseFragment(R.layout.fragment_super_admin_tenant_detail) {

    private val viewModel: TenantDetailViewModel by viewModels()

    // ── Views ──────────────────────────────────────────────
    private lateinit var clHeader: View
    private lateinit var btnBack: View
    private lateinit var btnAction: TextView
    private lateinit var progressBar: ProgressBar
    private lateinit var llContent: LinearLayout
    private lateinit var llError: LinearLayout
    private lateinit var tvErrorMessage: TextView
    private lateinit var btnRetry: View

    // 보기 모드
    private lateinit var tvTenantId: TextView
    private lateinit var tvStatusView: TextView
    private lateinit var tvNameView: TextView
    private lateinit var tvDisplayNameView: TextView
    private lateinit var tvDomainView: TextView
    private lateinit var tvCreatedAt: TextView
    private lateinit var tvUpdatedAt: TextView


    private lateinit var tilName: TextInputLayout
    private lateinit var etNameEdit: TextInputEditText
    private lateinit var tilDisplayName: TextInputLayout
    private lateinit var etDisplayNameEdit: TextInputEditText
    private lateinit var tilDomain: TextInputLayout
    private lateinit var etDomainEdit: TextInputEditText

    // ── State ──────────────────────────────────────────────
    private var isEditMode = false
    private var tenantId: Int = -1
    private var currentTenant: TenantDetail? = null

    // ──────────────────────────────────────────────────────
    override fun getToolbarView(view: View): View? = view.findViewById(R.id.cl_header)
 
    override fun initView() {
        val view = requireView()
        tenantId = arguments?.getInt("tenant_id", -1) ?: -1
        initViews(view)
        setupListeners()
        if (tenantId != -1) viewModel.fetchDetail(tenantId)
    }

    // ── View 초기화 ────────────────────────────────────────
    private fun initViews(v: View) {
        clHeader         = v.findViewById(R.id.cl_header)
        btnBack          = v.findViewById(R.id.btn_back)
        btnAction        = v.findViewById(R.id.btn_action)
        progressBar      = v.findViewById(R.id.progress_bar)
        llContent        = v.findViewById(R.id.ll_content)
        llError          = v.findViewById(R.id.ll_error)
        tvErrorMessage   = v.findViewById(R.id.tv_error_message)
        btnRetry         = v.findViewById(R.id.btn_retry)

        tvTenantId       = v.findViewById(R.id.tv_tenant_id)
        tvStatusView     = v.findViewById(R.id.tv_status_view)
        tvNameView       = v.findViewById(R.id.tv_name_view)
        tvDisplayNameView= v.findViewById(R.id.tv_display_name_view)
        tvDomainView     = v.findViewById(R.id.tv_domain_view)
        tvCreatedAt      = v.findViewById(R.id.tv_created_at)
        tvUpdatedAt      = v.findViewById(R.id.tv_updated_at)


        tilName          = v.findViewById(R.id.til_name)
        etNameEdit       = v.findViewById(R.id.et_name_edit)
        tilDisplayName   = v.findViewById(R.id.til_display_name)
        etDisplayNameEdit= v.findViewById(R.id.et_display_name_edit)
        tilDomain        = v.findViewById(R.id.til_domain)
        etDomainEdit     = v.findViewById(R.id.et_domain_edit)
    }

    // ── 리스너 ─────────────────────────────────────────────
    private fun setupListeners() {
        btnBack.setOnClickListener {
            findNavController().popBackStack()
        }

        btnAction.setOnClickListener {
            if (isEditMode) {
                // 저장
                val tenant = currentTenant ?: return@setOnClickListener
                viewModel.saveTenant(
                    tenantId    = tenant.tenantId,
                    tenantName  = etNameEdit.text?.toString() ?: "",
                    displayName = etDisplayNameEdit.text?.toString() ?: "",
                    domain      = etDomainEdit.text?.toString() ?: ""
                )
            } else {
                // 수정 모드 진입
                enterEditMode()
            }
        }

        btnRetry.setOnClickListener {
            if (tenantId != -1) viewModel.fetchDetail(tenantId)
        }
    }

    // ── ViewModel 관찰 ─────────────────────────────────────
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
                                tvErrorMessage.text = state.message
                                showError()
                            }
                        }
                    }
                }

                launch {
                    viewModel.event.collect { event ->
                        when (event) {
                            is TenantDetailEvent.SaveSuccess -> {
                                showTopToast("테넌트 정보가 수정되었습니다.")
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
                        btnAction.isEnabled = !saving
                        btnAction.alpha = if (saving) 0.5f else 1.0f
                        btnAction.text = when {
                            saving -> "저장 중..."
                            isEditMode -> "저장"
                            else -> "수정"
                        }
                    }
                }
            }
        }
    }

    // ── 데이터 바인딩 ──────────────────────────────────────
    private fun bindData(tenant: TenantDetail) {
        tvTenantId.text = "#${tenant.tenantId}"

        if (tenant.isActive) {
            tvStatusView.text = "활성"
            tvStatusView.setTextColor(requireContext().getColor(R.color.green_accent))
            tvStatusView.setBackgroundResource(R.drawable.bg_tag_light_green)
        } else {
            tvStatusView.text = "비활성"
            tvStatusView.setTextColor(requireContext().getColor(R.color.gray_text))
            tvStatusView.setBackgroundResource(R.drawable.bg_rect_rounded_light_gray)
        }

        tvNameView.text = tenant.tenantName
        tvDisplayNameView.text = tenant.displayName
        tvDomainView.text = tenant.domain ?: "-"
        tvCreatedAt.text = formatDate(tenant.createdAt)
        tvUpdatedAt.text = formatDate(tenant.updatedAt)
    }

    // ── 모드 전환 ──────────────────────────────────────────
    private fun enterEditMode() {
        val tenant = currentTenant ?: return
        isEditMode = true
        btnAction.text = "저장"

        // 보기 → 숨김 (상태는 변경할 수 없으므로 tvStatusView는 숨기지 않고 그대로 표시합니다)
        tvNameView.visibility = View.GONE
        tvDisplayNameView.visibility = View.GONE
        tvDomainView.visibility = View.GONE

        // 편집 필드 → 표시 및 현재 값 세팅
        etNameEdit.setText(tenant.tenantName)
        tilName.visibility = View.VISIBLE

        etDisplayNameEdit.setText(tenant.displayName)
        tilDisplayName.visibility = View.VISIBLE

        etDomainEdit.setText(tenant.domain ?: "")
        tilDomain.visibility = View.VISIBLE
    }

    // ── 상태 표시 ──────────────────────────────────────────
    private fun showLoading() {
        progressBar.visibility = View.VISIBLE
        llContent.visibility = View.GONE
        llError.visibility = View.GONE
    }

    private fun showContent() {
        progressBar.visibility = View.GONE
        llContent.visibility = View.VISIBLE
        llError.visibility = View.GONE
    }

    private fun showError() {
        progressBar.visibility = View.GONE
        llContent.visibility = View.GONE
        llError.visibility = View.VISIBLE
    }

    // ── 날짜 포맷 ──────────────────────────────────────────
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

    // ── Toast ──────────────────────────────────────────────
    @Suppress("DEPRECATION")
    private fun showTopToast(message: String) {
        val layout = requireActivity().layoutInflater
            .inflate(R.layout.view_common_toast_top, null)
        layout.findViewById<TextView>(R.id.tv_toast_message).text = message
        val toast = Toast(requireContext())
        toast.setGravity(android.view.Gravity.TOP or android.view.Gravity.CENTER_HORIZONTAL, 0, 100)
        toast.duration = Toast.LENGTH_SHORT
        toast.view = layout
        toast.show()
    }
}
