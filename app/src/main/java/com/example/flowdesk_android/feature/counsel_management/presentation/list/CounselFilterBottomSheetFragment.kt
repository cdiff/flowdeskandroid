package com.example.flowdesk_android.feature.counsel_management.presentation.list

import android.app.Dialog
import android.app.DatePickerDialog
import android.graphics.Color
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.RadioButton
import android.widget.TextView
import android.widget.Toast
import androidx.core.content.ContextCompat
import androidx.fragment.app.viewModels
import com.example.flowdesk_android.R
import com.example.flowdesk_android.databinding.DialogCounselFilterBinding
import com.example.flowdesk_android.feature.counsel_management.domain.model.EmployeeStat
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import dagger.hilt.android.AndroidEntryPoint
import java.time.LocalDate
import java.time.Instant
import java.time.ZoneOffset
import java.time.format.DateTimeFormatter
import java.util.Locale


import com.example.flowdesk_android.core.extension.observePermission
import javax.inject.Inject

@AndroidEntryPoint
class CounselFilterBottomSheetFragment : BottomSheetDialogFragment() {

    @Inject
    lateinit var sessionManager: com.example.flowdesk_android.data.local.SessionManager

    private val viewModel: CounselListViewModel by viewModels({ requireParentFragment() })

    override fun getTheme(): Int {
        return R.style.FilterBottomSheetDialogTheme
    }

    // Date formatting
    private val dateFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd", Locale.getDefault())
    private val displayFormatter = DateTimeFormatter.ofPattern("yyyy.MM.dd", Locale.getDefault())

    // Temporary selections
    private var tempStartDate: String? = null
    private var tempEndDate: String? = null
    private var tempEmpSeq: Int? = null
    private var tempWebCode: String? = null

    // Date preset selected state
    private var selectedPreset: String? = null // "today", "7days", "30days", "custom", null

    // Binding
    private var _binding: DialogCounselFilterBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = DialogCounselFilterBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        dialog?.setOnShowListener { dialogInterface ->
            val bottomSheetDialog = dialogInterface as BottomSheetDialog
            val bottomSheet = bottomSheetDialog.findViewById<View>(com.google.android.material.R.id.design_bottom_sheet)
            bottomSheet?.let {
                val behavior = com.google.android.material.bottomsheet.BottomSheetBehavior.from(it)
                behavior.isShouldRemoveExpandedCorners = false
            }
        }

        initStates()
        setupListeners()
    }

    private fun initStates() {
        val filter = viewModel.filterState.value
        tempStartDate = filter.startDate
        tempEndDate = filter.endDate
        tempEmpSeq = filter.empSeq
        tempWebCode = filter.webCode

        // Determine date preset based on current values
        determineDatePreset()
        updateDatePresetUi()
        updateDateRangeDisplay()

        // counsels.admin 권한에 따라 담당자 필터 가시성 제어 (observePermission 확장함수 사용)
        observePermission(sessionManager, "counsels.admin") { isAdmin ->
            binding.tvManagerLabel.visibility = if (isAdmin) View.VISIBLE else View.GONE
            binding.containerManager.visibility = if (isAdmin) View.VISIBLE else View.GONE
            if (isAdmin) {
                populateManagerOptions()
            }
        }
        
        populateWebsiteOptions()
    }

    private fun determineDatePreset() {
        if (tempStartDate == null && tempEndDate == null) {
            selectedPreset = null
            return
        }
        val today = LocalDate.now().format(dateFormatter)
        if (tempStartDate == today && tempEndDate == today) {
            selectedPreset = "today"
            return
        }
        val sevenDaysAgo = LocalDate.now().minusDays(7).format(dateFormatter)
        if (tempStartDate == sevenDaysAgo && tempEndDate == today) {
            selectedPreset = "7days"
            return
        }
        val thirtyDaysAgo = LocalDate.now().minusDays(30).format(dateFormatter)
        if (tempStartDate == thirtyDaysAgo && tempEndDate == today) {
            selectedPreset = "30days"
            return
        }
        selectedPreset = "custom"
    }

    private fun updateDatePresetUi() {
        val presetViews = mapOf(
            "today" to binding.tvPresetToday,
            "7days" to binding.tvPreset7days,
            "30days" to binding.tvPreset30days,
            "custom" to binding.tvPresetCustom
        )

        presetViews.forEach { (key, view) ->
            if (key == selectedPreset) {
                view.setBackgroundResource(R.drawable.bg_chip_filter_selected)
                view.setTextColor(Color.WHITE)
            } else {
                view.setBackgroundResource(R.drawable.bg_chip_filter)
                view.setTextColor(ContextCompat.getColor(requireContext(), R.color.gray_text))
            }
        }
    }

    private fun updateDateRangeDisplay() {
        if (tempStartDate != null && tempEndDate != null) {
            try {
                val start = LocalDate.parse(tempStartDate, dateFormatter).format(displayFormatter)
                val end = LocalDate.parse(tempEndDate, dateFormatter).format(displayFormatter)
                binding.tvDateRangeDisplay.text = "$start ~ $end"
                binding.tvDateRangeDisplay.visibility = View.VISIBLE
            } catch (e: Exception) {
                binding.tvDateRangeDisplay.text = ""
                binding.tvDateRangeDisplay.visibility = View.GONE
            }
        } else {
            binding.tvDateRangeDisplay.text = getString(R.string.counsel_filter_all_period)
            binding.tvDateRangeDisplay.visibility = View.VISIBLE
        }
    }

    private fun <T> populateGridOptions(
        container: LinearLayout,
        items: List<T>,
        getItemText: (T) -> String,
        getItemTag: (T) -> Any?,
        currentSelectedTag: Any?,
        onSelected: (Any?) -> Unit
    ) {
        container.removeAllViews()
        val allRadioButtons = mutableListOf<RadioButton>()

        // 1. "전체" 라디오 버튼 추가
        val totalRb = layoutInflater.inflate(R.layout.item_filter_radio, container, false) as RadioButton
        totalRb.text = getString(R.string.counsel_filter_all)
        totalRb.tag = null
        totalRb.isChecked = (currentSelectedTag == null)
        allRadioButtons.add(totalRb)

        // 2. 개별 아이템 라디오 버튼 추가
        items.forEach { item ->
            val rb = layoutInflater.inflate(R.layout.item_filter_radio, container, false) as RadioButton
            rb.text = getItemText(item)
            val tagValue = getItemTag(item)
            rb.tag = tagValue
            rb.isChecked = (currentSelectedTag == tagValue)
            allRadioButtons.add(rb)
        }

        // 3. 단일 선택(Single-Selection)을 구현하기 위한 클릭 리스너 바인딩
        allRadioButtons.forEach { rb ->
            rb.setOnClickListener { view ->
                val selectedRb = view as RadioButton
                allRadioButtons.forEach { otherRb ->
                    otherRb.isChecked = (otherRb == selectedRb)
                }
                onSelected(selectedRb.tag)
            }
        }

        // 4. 리니어 레이아웃 가로 배치로 2열 그리드 구성
        var currentRow: LinearLayout? = null
        allRadioButtons.forEachIndexed { index, rb ->
            if (index % 2 == 0) {
                currentRow = LinearLayout(requireContext()).apply {
                    layoutParams = LinearLayout.LayoutParams(
                        LinearLayout.LayoutParams.MATCH_PARENT,
                        LinearLayout.LayoutParams.WRAP_CONTENT
                    )
                    orientation = LinearLayout.HORIZONTAL
                    setPadding(0, 4, 0, 4)
                }
                container.addView(currentRow)
            }

            rb.layoutParams = LinearLayout.LayoutParams(
                0,
                LinearLayout.LayoutParams.WRAP_CONTENT,
                1.0f
            )
            currentRow?.addView(rb)
        }

        // 2열을 맞추기 위해 홀수 개의 항목인 경우 마지막 행에 투명 View 추가
        if (allRadioButtons.size % 2 != 0) {
            val emptyView = View(requireContext()).apply {
                layoutParams = LinearLayout.LayoutParams(
                    0,
                    LinearLayout.LayoutParams.WRAP_CONTENT,
                    1.0f
                )
            }
            currentRow?.addView(emptyView)
        }
    }

    private fun populateManagerOptions() {
        val rawList = viewModel.employeeList.value
        val hasUnassigned = rawList.any { it.empSeq == 0 }

        val list = mutableListOf<EmployeeStat>()
        if (!hasUnassigned) {
            list.add(EmployeeStat(empSeq = 0, empName = getString(R.string.counsel_label_unassigned), count = 0))
        }

        list.addAll(rawList.map {
            if (it.empSeq == 0 || it.empName.isBlank()) {
                it.copy(empName = getString(R.string.counsel_label_unassigned))
            } else {
                it
            }
        })

        val uniqueList = list.distinctBy { it.empSeq }

        populateGridOptions(
            container = binding.containerManager,
            items = uniqueList,
            getItemText = { it.empName },
            getItemTag = { it.empSeq },
            currentSelectedTag = tempEmpSeq,
            onSelected = { tag -> tempEmpSeq = tag as? Int }
        )
    }

    private fun populateWebsiteOptions() {
        populateGridOptions(
            container = binding.containerWebsite,
            items = viewModel.websiteList.value,
            getItemText = { it.webTitle },
            getItemTag = { it.webCode },
            currentSelectedTag = tempWebCode,
            onSelected = { tag -> tempWebCode = tag as? String }
        )
    }

    private fun setupListeners() {
        // Close Button Click
        binding.btnClose.setOnClickListener {
            dismiss()
        }

        // Reset Click
        binding.btnReset.setOnClickListener {
            tempStartDate = null
            tempEndDate = null
            tempEmpSeq = null
            tempWebCode = null
            selectedPreset = null

            updateDatePresetUi()
            updateDateRangeDisplay()
            populateManagerOptions()
            populateWebsiteOptions()
        }

        // Today preset Click
        binding.tvPresetToday.setOnClickListener {
            val today = LocalDate.now().format(dateFormatter)
            tempStartDate = today
            tempEndDate = today
            selectedPreset = "today"
            updateDatePresetUi()
            updateDateRangeDisplay()
        }

        // 7 Days preset Click
        binding.tvPreset7days.setOnClickListener {
            val start = LocalDate.now().minusDays(7).format(dateFormatter)
            val end = LocalDate.now().format(dateFormatter)
            tempStartDate = start
            tempEndDate = end
            selectedPreset = "7days"
            updateDatePresetUi()
            updateDateRangeDisplay()
        }

        // 30 Days preset Click
        binding.tvPreset30days.setOnClickListener {
            val start = LocalDate.now().minusDays(30).format(dateFormatter)
            val end = LocalDate.now().format(dateFormatter)
            tempStartDate = start
            tempEndDate = end
            selectedPreset = "30days"
            updateDatePresetUi()
            updateDateRangeDisplay()
        }

        // Custom preset Click
        binding.tvPresetCustom.setOnClickListener {
            showCustomDatePickerFlow()
        }

        // Apply Click
        binding.btnApply.setOnClickListener {
            viewModel.updateDateFilter(tempStartDate, tempEndDate)
            viewModel.updateManagerFilter(tempEmpSeq)
            viewModel.updateWebsiteFilter(tempWebCode)
            dismiss()
        }
    }

    private fun showCustomDatePickerFlow() {
        val customCalendar = CustomCalendarDialogFragment()
        
        // Parse current temp dates if they exist
        val startLocalDate = tempStartDate?.let {
            try { LocalDate.parse(it, dateFormatter) } catch (e: Exception) { null }
        }
        val endLocalDate = tempEndDate?.let {
            try { LocalDate.parse(it, dateFormatter) } catch (e: Exception) { null }
        }
        
        customCalendar.setInitialRange(startLocalDate, endLocalDate)
        customCalendar.setOnDateRangeSelectedListener { start, end ->
            tempStartDate = start.format(dateFormatter)
            tempEndDate = end.format(dateFormatter)
            selectedPreset = "custom"
            updateDatePresetUi()
            updateDateRangeDisplay()
        }
        
        customCalendar.show(childFragmentManager, "custom_calendar_dialog")
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
