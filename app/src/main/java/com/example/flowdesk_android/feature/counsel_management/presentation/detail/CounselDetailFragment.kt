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
import com.example.flowdesk_android.databinding.FragmentCounselDetailBinding
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

    // Binding
    private var _binding: FragmentCounselDetailBinding? = null
    private val binding get() = _binding!!

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

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentCounselDetailBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    override fun getToolbarView(view: View): View {
        return binding.layoutToolbar
    }

    override fun initView() {
        setupTabLayout()
        setupListeners()

        val counselSeq = arguments?.getInt("counselSeq") ?: -1
        viewModel.init(counselSeq)
    }

    private fun setupTabLayout() {
        replaceTabContent(0)

        binding.tabLayout.addOnTabSelectedListener(object : TabLayout.OnTabSelectedListener {
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
        binding.btnBack.setOnClickListener {
            findNavController().navigateUp()
        }

        binding.spinnerStatus.setOnClickListener {
            statusPopup?.let { if (it.isShowing) { it.dismiss(); return@setOnClickListener } }
            showStatusPopup()
        }

        binding.spinnerManager.setOnClickListener {
            managerPopup?.let { if (it.isShowing) { it.dismiss(); return@setOnClickListener } }
            showManagerPopup()
        }

        binding.btnDelete.setOnClickListener {
            showDeleteConfirmDialog()
        }

        binding.btnBlockPhone.setOnClickListener {
            val phone = currentDetail?.counselHp
            val sheet = com.example.flowdesk_android.feature.system_management.presentation.block.phone.PhoneBlockCreateBottomSheet.newInstance(
                phone = phone,
                hideModeSelector = true
            )
            sheet.show(childFragmentManager, "PhoneBlockCreateBottomSheet")
        }

        binding.btnBlockIp.setOnClickListener {
            val ip = currentDetail?.counselIp
            val sheet = com.example.flowdesk_android.feature.system_management.presentation.block.ip.IpBlockCreateBottomSheet.newInstance(
                ip = ip,
                hideModeSelector = true
            )
            sheet.show(childFragmentManager, "IpBlockCreateBottomSheet")
        }

        binding.btnBlockKeyword.setOnClickListener {
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

        tvTitle.text = getString(R.string.counsel_dialog_delete_title)
        tvMessage.text = getString(R.string.counsel_dialog_delete_message, current.name)
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
        statusPopup = binding.spinnerStatus.showCustomDropdown(adapter) { position ->
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
        val labels = mutableListOf(getString(R.string.counsel_label_unassigned)) + employeeList.map { it.empName }
        if (labels.isEmpty()) return
        val adapter = ManagerDropdownAdapter(requireContext(), labels, selectedManagerIndex)
        managerPopup = binding.spinnerManager.showCustomDropdown(adapter) { position ->
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
            binding.tvManagerSelected.text = labels[position]
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
                                Toast.makeText(requireContext(), getString(R.string.counsel_toast_status_changed), Toast.LENGTH_SHORT).show()
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
                                Toast.makeText(requireContext(), getString(R.string.counsel_toast_deleted), Toast.LENGTH_SHORT).show()
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
        binding.tvDetailName.text = detail.name
        binding.tvDetailStatusTag.text = detail.statusName
        binding.tvDetailPhone.text = detail.counselHp
        binding.tvDetailWebsite.text = detail.webTitle
        binding.tvDetailIp.text = detail.counselIp ?: "-"
        binding.tvDetailManager.text = detail.empName ?: getString(R.string.counsel_label_unassigned)
        binding.tvDetailCreatedAt.text = getString(R.string.counsel_label_registered_prefix, formatDateTime(detail.regDtm))
        binding.tvDetailUpdatedAt.text = getString(R.string.counsel_label_updated_prefix, formatDateTime(detail.editDtm))

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
        binding.tvStatusSelected.text = status.statusName
        try {
            binding.vStatusDot.visibility = View.VISIBLE
            binding.vStatusDot.setCardBackgroundColor(Color.parseColor(status.color))
        } catch (e: Exception) {
            binding.vStatusDot.visibility = View.GONE
        }

        try {
            val statusColor = Color.parseColor(status.color)
            val softBg = Color.argb(25, Color.red(statusColor), Color.green(statusColor), Color.blue(statusColor))
            val badgeBg = GradientDrawable().apply {
                setColor(softBg)
                cornerRadius = 4.dpToPx(requireContext()).toFloat()
            }
            binding.tvDetailStatusTag.background = badgeBg
            binding.tvDetailStatusTag.setTextColor(statusColor)
            binding.tvDetailStatusTag.text = status.statusName
        } catch (e: Exception) {
            binding.tvDetailStatusTag.text = status.statusName
            binding.tvDetailStatusTag.setTextColor(androidx.core.content.ContextCompat.getColor(requireContext(), R.color.counsel_detail_purple_badge))
        }
    }

    private fun Int.dpToPx(context: android.content.Context): Int {
        return (this * context.resources.displayMetrics.density).toInt()
    }

    private fun syncManagerButton(empSeq: Int?) {
        val idx = if (empSeq == null) 0
        else employeeList.indexOfFirst { it.empSeq == empSeq }.let { if (it < 0) 0 else it + 1 }
        selectedManagerIndex = idx
        val labels = mutableListOf(getString(R.string.counsel_label_unassigned)) + employeeList.map { it.empName }
        binding.tvManagerSelected.text = labels.getOrNull(idx) ?: getString(R.string.counsel_label_unassigned)
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
                tvText.setTextColor(androidx.core.content.ContextCompat.getColor(context, R.color.counsel_dropdown_selected_text))
                tvText.setTypeface(null, android.graphics.Typeface.BOLD)
            } else {
                tvText.setTextColor(androidx.core.content.ContextCompat.getColor(context, R.color.counsel_dropdown_unselected_text))
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
                tvText.setTextColor(androidx.core.content.ContextCompat.getColor(context, R.color.counsel_dropdown_selected_text))
                tvText.setTypeface(null, android.graphics.Typeface.BOLD)
            } else {
                tvText.setTextColor(androidx.core.content.ContextCompat.getColor(context, R.color.counsel_dropdown_unselected_text))
                tvText.setTypeface(null, android.graphics.Typeface.NORMAL)
            }

            return view
        }
    }
}
