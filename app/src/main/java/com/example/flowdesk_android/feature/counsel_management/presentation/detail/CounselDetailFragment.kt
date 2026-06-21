package com.example.flowdesk_android.feature.counsel_management.presentation.detail

import android.graphics.Color
import android.graphics.drawable.GradientDrawable
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.ListPopupWindow
import android.widget.TextView
import android.widget.Toast
import androidx.cardview.widget.CardView
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.navigation.fragment.findNavController
import com.example.flowdesk_android.R
import com.example.flowdesk_android.feature.counsel_management.domain.model.CounselDetail
import com.example.flowdesk_android.feature.counsel_management.domain.model.CounselStatusStat
import com.example.flowdesk_android.feature.counsel_management.domain.model.EmployeeStat
import com.google.android.material.tabs.TabLayout
import com.example.flowdesk_android.core.base.BaseFragment
import com.example.flowdesk_android.core.extension.showCustomDropdown
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch

@AndroidEntryPoint
class CounselDetailFragment : BaseFragment(R.layout.fragment_counsel_detail) {

    private val viewModel: CounselDetailViewModel by viewModels()

    // Views
    private lateinit var btnBack: View
    private lateinit var btnDelete: View
    private lateinit var btnBlockPhone: View
    private lateinit var btnBlockIp: View
    private lateinit var btnBlockKeyword: View

    private lateinit var tvName: TextView
    private lateinit var tvStatusTag: TextView
    private lateinit var tvPhone: TextView
    private lateinit var tvWebsite: TextView
    private lateinit var tvIp: TextView
    private lateinit var tvManager: TextView
    private lateinit var tvCreatedAt: TextView
    private lateinit var tvUpdatedAt: TextView

    // 커스텀 스피너 버튼 뷰
    private lateinit var spinnerStatusAnchor: View
    private lateinit var tvStatusSelected: TextView
    private lateinit var vStatusDot: CardView

    private lateinit var spinnerManagerAnchor: View
    private lateinit var tvManagerSelected: TextView

    private lateinit var tabLayout: TabLayout
    private lateinit var tabContentContainer: ViewGroup

    // 팝업 윈도우
    private var statusPopup: ListPopupWindow? = null
    private var managerPopup: ListPopupWindow? = null

    // Spinner data
    private var statusList: List<CounselStatusStat> = emptyList()
    private var employeeList: List<EmployeeStat> = emptyList()
    private var currentDetail: CounselDetail? = null

    // 현재 선택된 인덱스
    private var selectedStatusIndex: Int = 0
    private var selectedManagerIndex: Int = 0

    override fun getToolbarView(view: View): View? {
        return view.findViewById(R.id.layout_toolbar)
    }

    override fun initView() {
        val view = requireView()
        bindViews(view)
        setupTabLayout()
        setupListeners()

        val counselSeq = arguments?.getInt("counselSeq") ?: -1
        viewModel.init(counselSeq)
    }

    private fun bindViews(view: View) {
        btnBack = view.findViewById(R.id.btn_back)

        tvName = view.findViewById(R.id.tv_detail_name)
        tvStatusTag = view.findViewById(R.id.tv_detail_status_tag)
        tvPhone = view.findViewById(R.id.tv_detail_phone)
        tvWebsite = view.findViewById(R.id.tv_detail_website)
        tvIp = view.findViewById(R.id.tv_detail_ip)
        tvManager = view.findViewById(R.id.tv_detail_manager)
        tvCreatedAt = view.findViewById(R.id.tv_detail_created_at)
        tvUpdatedAt = view.findViewById(R.id.tv_detail_updated_at)

        // 커스텀 스피너 버튼 뷰 바인딩
        spinnerStatusAnchor = view.findViewById(R.id.spinner_status)
        tvStatusSelected = view.findViewById(R.id.tv_status_selected)
        vStatusDot = view.findViewById(R.id.v_status_dot)

        spinnerManagerAnchor = view.findViewById(R.id.spinner_manager)
        tvManagerSelected = view.findViewById(R.id.tv_manager_selected)

        tabLayout = view.findViewById(R.id.tab_layout)
        tabContentContainer = view.findViewById(R.id.tab_content_container)
        btnDelete = view.findViewById(R.id.btn_delete)
        btnBlockPhone = view.findViewById(R.id.btn_block_phone)
        btnBlockIp = view.findViewById(R.id.btn_block_ip)
        btnBlockKeyword = view.findViewById(R.id.btn_block_keyword)
    }

    private fun setupTabLayout() {
        replaceTabContent(0)

        tabLayout.addOnTabSelectedListener(object : TabLayout.OnTabSelectedListener {
            override fun onTabSelected(tab: TabLayout.Tab?) {
                replaceTabContent(tab?.position ?: 0)
            }
            override fun onTabUnselected(tab: TabLayout.Tab?) {}
            override fun onTabReselected(tab: TabLayout.Tab?) {}
        })
    }

    private fun replaceTabContent(position: Int) {
        val fragment = when (position) {
            0 -> CounselDetailInfoFragment()
            1 -> CounselDetailMemoFragment()
            2 -> CounselDetailHistoryFragment()
            else -> CounselDetailInfoFragment()
        }
        childFragmentManager.beginTransaction()
            .replace(R.id.tab_content_container, fragment)
            .commit()
    }

    private fun setupListeners() {
        btnBack.setOnClickListener {
            findNavController().navigateUp()
        }

        spinnerStatusAnchor.setOnClickListener {
            statusPopup?.let { if (it.isShowing) { it.dismiss(); return@setOnClickListener } }
            showStatusPopup()
        }

        spinnerManagerAnchor.setOnClickListener {
            managerPopup?.let { if (it.isShowing) { it.dismiss(); return@setOnClickListener } }
            showManagerPopup()
        }

        btnDelete.setOnClickListener {
            showDeleteConfirmDialog()
        }

        btnBlockPhone.setOnClickListener {
            val phone = currentDetail?.counselHp
            val sheet = com.example.flowdesk_android.feature.system_management.presentation.block.phone.PhoneBlockCreateBottomSheet.newInstance(
                phone = phone,
                hideModeSelector = true
            )
            sheet.show(childFragmentManager, "PhoneBlockCreateBottomSheet")
        }

        btnBlockIp.setOnClickListener {
            val ip = currentDetail?.counselIp
            val sheet = com.example.flowdesk_android.feature.system_management.presentation.block.ip.IpBlockCreateBottomSheet.newInstance(
                ip = ip,
                hideModeSelector = true
            )
            sheet.show(childFragmentManager, "IpBlockCreateBottomSheet")
        }

        btnBlockKeyword.setOnClickListener {
            val sheet = com.example.flowdesk_android.feature.system_management.presentation.block.keyword.KeywordBlockCreateBottomSheet.newInstance(
                keyword = null,
                hideModeSelector = true
            )
            sheet.show(childFragmentManager, "KeywordBlockCreateBottomSheet")
        }
    }

    private fun showDeleteConfirmDialog() {
        val current = currentDetail ?: return
        val dialogView = LayoutInflater.from(requireContext()).inflate(R.layout.dialog_common_confirm, null)
        val dialog = androidx.appcompat.app.AlertDialog.Builder(requireContext())
            .setView(dialogView)
            .create()

        dialog.window?.setBackgroundDrawableResource(android.R.color.transparent)

        val tvTitle = dialogView.findViewById<TextView>(R.id.tv_title)
        val tvMessage = dialogView.findViewById<TextView>(R.id.tv_message)
        val cbConfirm = dialogView.findViewById<View>(R.id.cb_confirm)
        val btnCancel = dialogView.findViewById<View>(R.id.btn_cancel)
        val btnConfirm = dialogView.findViewById<View>(R.id.btn_confirm)

        tvTitle.text = "상담 삭제"
        tvMessage.text = "'${current.name}' 고객의 상담 정보를 정말 삭제하시겠습니까?\n이 작업은 되돌릴 수 없습니다."
        cbConfirm.visibility = View.GONE

        btnCancel.setOnClickListener {
            dialog.dismiss()
        }

        btnConfirm.setOnClickListener {
            viewModel.deleteCounsel()
            dialog.dismiss()
        }

        dialog.show()
    }

    // ── 상태 변경 팝업 ──────────────────────────────────────────────────────────

    private fun showStatusPopup() {
        if (statusList.isEmpty()) return
        val adapter = StatusDropdownAdapter(requireContext(), statusList, selectedStatusIndex)
        statusPopup = spinnerStatusAnchor.showCustomDropdown(adapter) { position ->
            val selected = statusList.getOrNull(position) ?: return@showCustomDropdown
            val current = currentDetail ?: return@showCustomDropdown
            if (selected.counselStat != current.counselStat) {
                // 서버 스펙상 counselResvDtm 필수 → 기존 값 유지하여 전달
                viewModel.updateCounselStatus(selected.counselStat, current.counselResvDtm)
            }
            selectedStatusIndex = position
            bindStatusButton(selected)
        }
    }

    private fun showManagerPopup() {
        val labels = mutableListOf("미배정") + employeeList.map { it.empName }
        if (labels.isEmpty()) return
        val adapter = ManagerDropdownAdapter(requireContext(), labels, selectedManagerIndex)
        managerPopup = spinnerManagerAnchor.showCustomDropdown(adapter) { position ->
            val empSeq = if (position == 0) null else employeeList.getOrNull(position - 1)?.empSeq
            val current = currentDetail ?: return@showCustomDropdown
            if (empSeq != current.empSeq) {
                viewModel.updateCounsel(
                    com.example.flowdesk_android.feature.counsel_management.data.dto.CounselUpdateRequest(
                        empSeq = empSeq
                    )
                )
            }
            selectedManagerIndex = position
            tvManagerSelected.text = labels[position]
        }
    }

    // ── ViewModel 관찰 ──────────────────────────────────────────────────────────

    override fun observeViewModel() {
        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {

                launch {
                    viewModel.uiState.collect { state ->
                        when (state) {
                            is CounselDetailUiState.Loading -> {}
                            is CounselDetailUiState.Success -> {
                                currentDetail = state.detail
                                bindDetail(state.detail)
                            }
                            is CounselDetailUiState.Error -> {
                                Toast.makeText(requireContext(), state.message, Toast.LENGTH_SHORT).show()
                            }
                        }
                    }
                }

                launch {
                    viewModel.employeeList.collect { employees ->
                        employeeList = employees
                        // 현재 선택된 담당자 표시 갱신
                        currentDetail?.let { syncManagerButton(it.empSeq) }
                    }
                }

                launch {
                    viewModel.statusList.collect { statuses ->
                        statusList = statuses
                        // 현재 선택된 상태 표시 갱신
                        currentDetail?.let { syncStatusButton(it.counselStat) }
                    }
                }

                launch {
                    viewModel.statusUpdateState.collect { state ->
                        when (state) {
                            is CounselUpdateState.Success -> {
                                Toast.makeText(requireContext(), "상태가 변경되었습니다.", Toast.LENGTH_SHORT).show()
                                viewModel.resetStatusUpdateState()
                            }
                            is CounselUpdateState.Error -> {
                                Toast.makeText(requireContext(), state.message, Toast.LENGTH_SHORT).show()
                                viewModel.resetStatusUpdateState()
                            }
                            else -> {}
                        }
                    }
                }

                launch {
                    viewModel.deleteState.collect { state ->
                        when (state) {
                            is CounselUpdateState.Success -> {
                                Toast.makeText(requireContext(), "상담 정보가 삭제되었습니다.", Toast.LENGTH_SHORT).show()
                                viewModel.resetDeleteState()
                                findNavController().navigateUp()
                            }
                            is CounselUpdateState.Error -> {
                                Toast.makeText(requireContext(), state.message, Toast.LENGTH_SHORT).show()
                                viewModel.resetDeleteState()
                            }
                            else -> {}
                        }
                    }
                }
            }
        }
    }

    // ── 상세 데이터 바인딩 ─────────────────────────────────────────────────────

    private fun bindDetail(detail: CounselDetail) {
        tvName.text = detail.name
        tvStatusTag.text = detail.statusName
        tvPhone.text = detail.counselHp
        tvWebsite.text = detail.webTitle
        tvIp.text = detail.counselIp ?: "-"
        tvManager.text = detail.empName ?: "미배정"
        tvCreatedAt.text = "등록: ${formatDateTime(detail.regDtm)}"
        tvUpdatedAt.text = "수정: ${formatDateTime(detail.editDtm)}"

        syncStatusButton(detail.counselStat)
        syncManagerButton(detail.empSeq)
    }

    private fun syncStatusButton(counselStat: Int) {
        val idx = statusList.indexOfFirst { it.counselStat == counselStat }
        if (idx >= 0) {
            selectedStatusIndex = idx
            bindStatusButton(statusList[idx])
        }
    }

    private fun bindStatusButton(status: CounselStatusStat) {
        tvStatusSelected.text = status.statusName
        try {
            vStatusDot.visibility = View.VISIBLE
            vStatusDot.setCardBackgroundColor(Color.parseColor(status.color))
        } catch (e: Exception) {
            vStatusDot.visibility = View.GONE
        }

        try {
            val statusColor = Color.parseColor(status.color)
            val softBg = Color.argb(25, Color.red(statusColor), Color.green(statusColor), Color.blue(statusColor))
            val badgeBg = GradientDrawable().apply {
                setColor(softBg)
                cornerRadius = 4.dpToPx(requireContext()).toFloat()
            }
            tvStatusTag.background = badgeBg
            tvStatusTag.setTextColor(statusColor)
            tvStatusTag.text = status.statusName
        } catch (e: Exception) {
            tvStatusTag.text = status.statusName
            tvStatusTag.setTextColor(Color.parseColor("#8B5CF6"))
        }
    }

    private fun Int.dpToPx(context: android.content.Context): Int {
        return (this * context.resources.displayMetrics.density).toInt()
    }

    private fun syncManagerButton(empSeq: Int?) {
        val idx = if (empSeq == null) 0
        else employeeList.indexOfFirst { it.empSeq == empSeq }.let { if (it < 0) 0 else it + 1 }
        selectedManagerIndex = idx
        val labels = mutableListOf("미배정") + employeeList.map { it.empName }
        tvManagerSelected.text = labels.getOrNull(idx) ?: "미배정"
    }

    private fun formatDateTime(iso: String?): String {
        if (iso.isNullOrBlank()) return "-"
        return try {
            "${iso.substring(0, 10)} ${iso.substring(11, 19)}"
        } catch (e: Exception) { iso }
    }

    // ── 드롭다운 어댑터 ────────────────────────────────────────────────────────

    inner class StatusDropdownAdapter(
        context: android.content.Context,
        private val items: List<CounselStatusStat>,
        private val selectedIndex: Int
    ) : ArrayAdapter<CounselStatusStat>(context, R.layout.item_spinner_dropdown, items) {

        override fun getView(position: Int, convertView: View?, parent: ViewGroup): View {
            return getDropDownView(position, convertView, parent)
        }

        override fun getDropDownView(position: Int, convertView: View?, parent: ViewGroup): View {
            val view = convertView ?: LayoutInflater.from(context)
                .inflate(R.layout.item_spinner_dropdown, parent, false)
            val item = getItem(position) ?: return view
            val tvText = view.findViewById<TextView>(android.R.id.text1)
            val vDot = view.findViewById<CardView>(R.id.v_spinner_dot)
            val layoutRoot = view.findViewById<View>(R.id.layout_dropdown_root)

            tvText.text = item.statusName

            try {
                vDot.visibility = View.VISIBLE
                vDot.setCardBackgroundColor(Color.parseColor(item.color))
            } catch (e: Exception) {
                vDot.visibility = View.GONE
            }

            val isSelected = position == selectedIndex
            layoutRoot.isSelected = isSelected
            if (isSelected) {
                tvText.setTextColor(Color.parseColor("#1D4ED8"))
                tvText.setTypeface(null, android.graphics.Typeface.BOLD)
            } else {
                tvText.setTextColor(Color.parseColor("#475569"))
                tvText.setTypeface(null, android.graphics.Typeface.NORMAL)
            }

            return view
        }
    }

    inner class ManagerDropdownAdapter(
        context: android.content.Context,
        private val items: List<String>,
        private val selectedIndex: Int
    ) : ArrayAdapter<String>(context, R.layout.item_spinner_dropdown, items) {

        override fun getView(position: Int, convertView: View?, parent: ViewGroup): View {
            return getDropDownView(position, convertView, parent)
        }

        override fun getDropDownView(position: Int, convertView: View?, parent: ViewGroup): View {
            val view = convertView ?: LayoutInflater.from(context)
                .inflate(R.layout.item_spinner_dropdown, parent, false)
            val tvText = view.findViewById<TextView>(android.R.id.text1)
            val vDot = view.findViewById<View>(R.id.v_spinner_dot)
            val layoutRoot = view.findViewById<View>(R.id.layout_dropdown_root)

            tvText.text = getItem(position)
            vDot.visibility = View.GONE

            val isSelected = position == selectedIndex
            layoutRoot.isSelected = isSelected
            if (isSelected) {
                tvText.setTextColor(Color.parseColor("#1D4ED8"))
                tvText.setTypeface(null, android.graphics.Typeface.BOLD)
            } else {
                tvText.setTextColor(Color.parseColor("#475569"))
                tvText.setTypeface(null, android.graphics.Typeface.NORMAL)
            }

            return view
        }
    }
}
