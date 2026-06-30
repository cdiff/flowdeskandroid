package com.example.flowdesk_android.feature.counsel_management.presentation.list

import android.content.Context
import android.graphics.Color
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.fragment.app.DialogFragment
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.flowdesk_android.R
import com.example.flowdesk_android.databinding.DialogCustomCalendarBinding
import com.example.flowdesk_android.databinding.ItemCalendarDayBinding
import com.example.flowdesk_android.databinding.ItemCalendarMonthBinding
import java.time.LocalDate
import java.time.YearMonth
import java.time.format.DateTimeFormatter
import java.util.Locale

class CustomCalendarDialogFragment : DialogFragment() {

    private var _binding: DialogCustomCalendarBinding? = null
    private val binding get() = _binding!!

    // Date formatting
    private val displayFormatter = DateTimeFormatter.ofPattern("yyyy. MM. dd.", Locale.getDefault())

    // Date selection states
    private var selectedStartDate: LocalDate? = null
    private var selectedEndDate: LocalDate? = null

    // Callback
    private var onDateRangeSelectedListener: ((LocalDate, LocalDate) -> Unit)? = null

    fun setOnDateRangeSelectedListener(listener: (LocalDate, LocalDate) -> Unit) {
        this.onDateRangeSelectedListener = listener
    }

    // Set initial values if preset
    fun setInitialRange(start: LocalDate?, end: LocalDate?) {
        this.selectedStartDate = start
        this.selectedEndDate = end
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        // Set full-screen dialog theme
        setStyle(STYLE_NORMAL, android.R.style.Theme_Material_Light_NoActionBar_Fullscreen)
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = DialogCustomCalendarBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setupToolbar()
        updateSummaryUi()
        setupCalendarList()

        binding.btnConfirm.setOnClickListener {
            val start = selectedStartDate
            val end = selectedEndDate
            if (start != null && end != null) {
                onDateRangeSelectedListener?.invoke(start, end)
                dismiss()
            } else {
                Toast.makeText(requireContext(), "시작일과 종료일을 모두 선택해 주세요.", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun setupToolbar() {
        binding.btnBack.setOnClickListener {
            dismiss()
        }
    }

    private fun updateSummaryUi() {
        binding.tvStartDateVal.text = selectedStartDate?.format(displayFormatter) ?: "시작일 선택"
        binding.tvEndDateVal.text = selectedEndDate?.format(displayFormatter) ?: "종료일 선택"

        // Enable confirm button only when both dates are selected
        val hasSelection = selectedStartDate != null && selectedEndDate != null
        binding.btnConfirm.isEnabled = hasSelection
        binding.btnConfirm.alpha = if (hasSelection) 1.0f else 0.5f
    }

    private fun setupCalendarList() {
        // Generate months (e.g., from 6 months ago to 6 months in the future)
        val monthsList = mutableListOf<YearMonth>()
        val startMonth = YearMonth.now().minusMonths(6)
        for (i in 0..12) {
            monthsList.add(startMonth.plusMonths(i.toLong()))
        }

        val monthAdapter = MonthAdapter(monthsList) { date ->
            handleDateSelection(date)
        }

        binding.rvCalendarMonths.layoutManager = LinearLayoutManager(requireContext())
        binding.rvCalendarMonths.adapter = monthAdapter

        // Scroll to current month
        val currentMonthIndex = monthsList.indexOf(YearMonth.now())
        if (currentMonthIndex != -1) {
            binding.rvCalendarMonths.scrollToPosition(currentMonthIndex)
        }
    }

    private fun handleDateSelection(date: LocalDate) {
        val start = selectedStartDate
        val end = selectedEndDate

        when {
            start == null -> {
                selectedStartDate = date
                selectedEndDate = null
            }
            end == null -> {
                if (date.isBefore(start)) {
                    selectedStartDate = date
                } else {
                    selectedEndDate = date
                }
            }
            else -> {
                selectedStartDate = date
                selectedEndDate = null
            }
        }

        updateSummaryUi()
        // Refresh calendar data to redraw highlights
        binding.rvCalendarMonths.adapter?.notifyDataSetChanged()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    // --- Month Adapter ---
    inner class MonthAdapter(
        private val months: List<YearMonth>,
        private val onDateClick: (LocalDate) -> Unit
    ) : RecyclerView.Adapter<MonthAdapter.MonthViewHolder>() {

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MonthViewHolder {
            val binding = ItemCalendarMonthBinding.inflate(LayoutInflater.from(parent.context), parent, false)
            return MonthViewHolder(binding)
        }

        override fun onBindViewHolder(holder: MonthViewHolder, position: Int) {
            holder.bind(months[position])
        }

        override fun getItemCount(): Int = months.size

        inner class MonthViewHolder(private val monthBinding: ItemCalendarMonthBinding) :
            RecyclerView.ViewHolder(monthBinding.root) {

            fun bind(yearMonth: YearMonth) {
                val formatter = DateTimeFormatter.ofPattern("yyyy.MM", Locale.getDefault())
                monthBinding.tvMonthTitle.text = yearMonth.format(formatter)

                // Generate dates list for grid (null for empty cells)
                val daysInMonth = yearMonth.lengthOfMonth()
                val firstDayOfWeek = yearMonth.atDay(1).dayOfWeek.value % 7 // 0=Sunday, 1=Monday, etc.

                val daysList = mutableListOf<LocalDate?>()
                // Fill leading empty days
                for (i in 0 until firstDayOfWeek) {
                    daysList.add(null)
                }
                // Fill actual days
                for (day in 1..daysInMonth) {
                    daysList.add(yearMonth.atDay(day))
                }

                val dayAdapter = DayAdapter(daysList, onDateClick)
                monthBinding.rvDays.layoutManager = GridLayoutManager(itemView.context, 7)
                monthBinding.rvDays.adapter = dayAdapter
            }
        }
    }

    // --- Day Adapter ---
    inner class DayAdapter(
        private val days: List<LocalDate?>,
        private val onDateClick: (LocalDate) -> Unit
    ) : RecyclerView.Adapter<DayAdapter.DayViewHolder>() {

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): DayViewHolder {
            val binding = ItemCalendarDayBinding.inflate(LayoutInflater.from(parent.context), parent, false)
            return DayViewHolder(binding)
        }

        override fun onBindViewHolder(holder: DayViewHolder, position: Int) {
            holder.bind(days[position])
        }

        override fun getItemCount(): Int = days.size

        inner class DayViewHolder(private val dayBinding: ItemCalendarDayBinding) :
            RecyclerView.ViewHolder(dayBinding.root) {

            fun bind(date: LocalDate?) {
                if (date == null) {
                    dayBinding.tvDayNumber.text = ""
                    dayBinding.viewSelectedCircle.visibility = View.GONE
                    dayBinding.viewRangeLeft.visibility = View.GONE
                    dayBinding.viewRangeRight.visibility = View.GONE
                    itemView.setOnClickListener(null)
                    itemView.isClickable = false
                    return
                }

                dayBinding.tvDayNumber.text = date.dayOfMonth.toString()
                itemView.isClickable = true

                // Selection check
                val isStart = date == selectedStartDate
                val isEnd = date == selectedEndDate
                val isInRange = selectedStartDate != null && selectedEndDate != null &&
                        date.isAfter(selectedStartDate) && date.isBefore(selectedEndDate)

                // Background & Text Style binding
                when {
                    isStart -> {
                        dayBinding.viewSelectedCircle.visibility = View.VISIBLE
                        dayBinding.tvDayNumber.setTextColor(Color.WHITE)
                        dayBinding.tvDayNumber.paint.isFakeBoldText = true

                        // Highlight right side only if end date exists
                        dayBinding.viewRangeLeft.visibility = View.GONE
                        dayBinding.viewRangeRight.visibility = if (selectedEndDate != null) View.VISIBLE else View.GONE
                    }
                    isEnd -> {
                        dayBinding.viewSelectedCircle.visibility = View.VISIBLE
                        dayBinding.tvDayNumber.setTextColor(Color.WHITE)
                        dayBinding.tvDayNumber.paint.isFakeBoldText = true

                        // Highlight left side only
                        dayBinding.viewRangeLeft.visibility = View.VISIBLE
                        dayBinding.viewRangeRight.visibility = View.GONE
                    }
                    isInRange -> {
                        dayBinding.viewSelectedCircle.visibility = View.GONE
                        dayBinding.tvDayNumber.setTextColor(Color.parseColor("#374151"))
                        dayBinding.tvDayNumber.paint.isFakeBoldText = false

                        // Highlight entire width
                        dayBinding.viewRangeLeft.visibility = View.VISIBLE
                        dayBinding.viewRangeRight.visibility = View.VISIBLE
                    }
                    else -> {
                        dayBinding.viewSelectedCircle.visibility = View.GONE
                        dayBinding.tvDayNumber.setTextColor(Color.parseColor("#374151"))
                        dayBinding.tvDayNumber.paint.isFakeBoldText = false

                        dayBinding.viewRangeLeft.visibility = View.GONE
                        dayBinding.viewRangeRight.visibility = View.GONE
                    }
                }

                itemView.setOnClickListener {
                    onDateClick(date)
                }
            }
        }
    }
}
