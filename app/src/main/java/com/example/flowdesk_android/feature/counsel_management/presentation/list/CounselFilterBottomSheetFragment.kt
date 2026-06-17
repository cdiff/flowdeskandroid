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
import com.example.flowdesk_android.feature.counsel_management.domain.model.EmployeeStat
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import dagger.hilt.android.AndroidEntryPoint
import java.time.LocalDate
import java.time.Instant
import java.time.ZoneOffset
import java.time.format.DateTimeFormatter
import java.util.Locale


@AndroidEntryPoint
class CounselFilterBottomSheetFragment : BottomSheetDialogFragment() {

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

    // Views
    private lateinit var btnClose: ImageView
    private lateinit var btnReset: TextView
    private lateinit var tvPresetToday: TextView
    private lateinit var tvPreset7days: TextView
    private lateinit var tvPreset30days: TextView
    private lateinit var tvPresetCustom: TextView
    private lateinit var tvDateRangeDisplay: TextView
    private lateinit var containerManager: LinearLayout
    private lateinit var containerWebsite: LinearLayout
    private lateinit var btnApply: View

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? = inflater.inflate(R.layout.dialog_counsel_filter, container, false)

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

        bindViews(view)
        initStates()
        setupListeners()
    }

    private fun bindViews(view: View) {
        btnClose = view.findViewById(R.id.btn_close)
        btnReset = view.findViewById(R.id.btn_reset)
        tvPresetToday = view.findViewById(R.id.tv_preset_today)
        tvPreset7days = view.findViewById(R.id.tv_preset_7days)
        tvPreset30days = view.findViewById(R.id.tv_preset_30days)
        tvPresetCustom = view.findViewById(R.id.tv_preset_custom)
        tvDateRangeDisplay = view.findViewById(R.id.tv_date_range_display)
        containerManager = view.findViewById(R.id.container_manager)
        containerWebsite = view.findViewById(R.id.container_website)
        btnApply = view.findViewById(R.id.btn_apply)
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

        // Populate Choice Options in 2 Columns
        populateManagerOptions()
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
            "today" to tvPresetToday,
            "7days" to tvPreset7days,
            "30days" to tvPreset30days,
            "custom" to tvPresetCustom
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
                tvDateRangeDisplay.text = "$start ~ $end"
                tvDateRangeDisplay.visibility = View.VISIBLE
            } catch (e: Exception) {
                tvDateRangeDisplay.text = ""
                tvDateRangeDisplay.visibility = View.GONE
            }
        } else {
            tvDateRangeDisplay.text = "전체 기간"
            tvDateRangeDisplay.visibility = View.VISIBLE
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
        totalRb.text = "전체"
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
            list.add(EmployeeStat(empSeq = 0, empName = "미배정", count = 0))
        }

        list.addAll(rawList.map {
            if (it.empSeq == 0 || it.empName.isBlank()) {
                it.copy(empName = "미배정")
            } else {
                it
            }
        })

        val uniqueList = list.distinctBy { it.empSeq }

        populateGridOptions(
            container = containerManager,
            items = uniqueList,
            getItemText = { it.empName },
            getItemTag = { it.empSeq },
            currentSelectedTag = tempEmpSeq,
            onSelected = { tag -> tempEmpSeq = tag as? Int }
        )
    }

    private fun populateWebsiteOptions() {
        populateGridOptions(
            container = containerWebsite,
            items = viewModel.websiteList.value,
            getItemText = { it.webTitle },
            getItemTag = { it.webCode },
            currentSelectedTag = tempWebCode,
            onSelected = { tag -> tempWebCode = tag as? String }
        )
    }

    private fun setupListeners() {
        // Close Button Click
        btnClose.setOnClickListener {
            dismiss()
        }

        // Reset Click
        btnReset.setOnClickListener {
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
        tvPresetToday.setOnClickListener {
            val today = LocalDate.now().format(dateFormatter)
            tempStartDate = today
            tempEndDate = today
            selectedPreset = "today"
            updateDatePresetUi()
            updateDateRangeDisplay()
        }

        // 7 Days preset Click
        tvPreset7days.setOnClickListener {
            val start = LocalDate.now().minusDays(7).format(dateFormatter)
            val end = LocalDate.now().format(dateFormatter)
            tempStartDate = start
            tempEndDate = end
            selectedPreset = "7days"
            updateDatePresetUi()
            updateDateRangeDisplay()
        }

        // 30 Days preset Click
        tvPreset30days.setOnClickListener {
            val start = LocalDate.now().minusDays(30).format(dateFormatter)
            val end = LocalDate.now().format(dateFormatter)
            tempStartDate = start
            tempEndDate = end
            selectedPreset = "30days"
            updateDatePresetUi()
            updateDateRangeDisplay()
        }

        // Custom preset Click
        tvPresetCustom.setOnClickListener {
            showCustomDatePickerFlow()
        }

        // Apply Click
        btnApply.setOnClickListener {
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
}
