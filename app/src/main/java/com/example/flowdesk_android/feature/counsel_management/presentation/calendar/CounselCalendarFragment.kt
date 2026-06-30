package com.example.flowdesk_android.feature.counsel_management.presentation.calendar

import android.graphics.Color
import android.os.Bundle
import android.view.LayoutInflater
import android.view.MotionEvent
import android.view.View
import android.view.ViewGroup
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.FrameLayout
import android.widget.LinearLayout
import android.widget.ProgressBar
import android.widget.TextView
import android.widget.Toast
import android.widget.ListPopupWindow
import com.example.flowdesk_android.core.extension.showCustomDropdown
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.viewpager2.widget.ViewPager2
import com.example.flowdesk_android.R
import com.example.flowdesk_android.databinding.FragmentCounselCalendarBinding
import com.example.flowdesk_android.databinding.ItemCalendarMonthPageBinding
import com.example.flowdesk_android.databinding.ItemCalendarGridDayBinding
import com.example.flowdesk_android.databinding.ItemCalendarReservationCardBinding
import com.example.flowdesk_android.feature.counsel_management.domain.model.CounselItem
import com.example.flowdesk_android.feature.counsel_management.domain.model.EmployeeStat
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch
import java.time.LocalDate
import java.time.YearMonth
import java.time.format.TextStyle
import java.time.temporal.ChronoUnit
import java.util.Locale
import android.widget.ImageButton

@AndroidEntryPoint
class CounselCalendarFragment : Fragment() {

    private val viewModel: CounselCalendarViewModel by viewModels()

    private val startMonth = YearMonth.now().minusMonths(100)
    private val endMonth = YearMonth.now().plusMonths(100)

    // Binding
    private var _binding: FragmentCounselCalendarBinding? = null
    private val binding get() = _binding!!

    private lateinit var pagerAdapter: MonthPagerAdapter
    private lateinit var dayReservationAdapter: DayReservationAdapter
    private var employeeList: List<EmployeeStat> = emptyList()
    private var displayEmployeeList: List<EmployeeStat> = emptyList()
    private var isFirstSpinnerSelection = true
    private var selectedDate: LocalDate? = null
    private var allReservations: Map<LocalDate, List<CounselItem>> = emptyMap()
    private var selectedManagerIndex = 0
    private var managerPopup: ListPopupWindow? = null

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentCounselCalendarBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setupViewPager()
        setupDayReservationList()
        setupListeners()
        observeState()
    }

    private fun setupViewPager() {
        pagerAdapter = MonthPagerAdapter(startMonth, endMonth) { date ->
            onDateSelected(date)
        }
        binding.vpCalendar.adapter = pagerAdapter

        // 현재 달로 초기 스크롤 세팅
        val initialPosition = pagerAdapter.getPosition(YearMonth.now())
        binding.vpCalendar.setCurrentItem(initialPosition, false)

        // 가로 스와이프 페이지 변경 콜백
        binding.vpCalendar.registerOnPageChangeCallback(object : ViewPager2.OnPageChangeCallback() {
            override fun onPageSelected(position: Int) {
                val targetMonth = pagerAdapter.getYearMonth(position)
                if (viewModel.selectedMonth.value != targetMonth) {
                    viewModel.selectMonth(targetMonth)
                }
            }
        })

        // NestedScrollView 터치 간섭 방지: 가로 스와이프 우선 처리 적용
        val child = binding.vpCalendar.getChildAt(0)
        if (child is RecyclerView) {
            child.addOnItemTouchListener(object : RecyclerView.SimpleOnItemTouchListener() {
                private var startX = 0f
                private var startY = 0f

                override fun onInterceptTouchEvent(rv: RecyclerView, e: MotionEvent): Boolean {
                    when (e.action) {
                        MotionEvent.ACTION_DOWN -> {
                            startX = e.x
                            startY = e.y
                            rv.parent.requestDisallowInterceptTouchEvent(true)
                        }
                        MotionEvent.ACTION_MOVE -> {
                            val diffX = Math.abs(e.x - startX)
                            val diffY = Math.abs(e.y - startY)
                            if (diffX > diffY) {
                                // 가로 드래그 시 부모 NestedScrollView가 터치를 가로채지 못하게 락 설정
                                rv.parent.requestDisallowInterceptTouchEvent(true)
                            } else {
                                // 세로 드래그 시 부모가 터치를 가져가서 스크롤할 수 있도록 허용
                                rv.parent.requestDisallowInterceptTouchEvent(false)
                            }
                        }
                    }
                    return false
                }
            })
        }
    }

    private fun setupDayReservationList() {
        dayReservationAdapter = DayReservationAdapter(emptyList()) { item ->
            val bundle = Bundle().apply {
                putInt("counselSeq", item.counselSeq)
            }
            findNavController().navigate(R.id.counselDetailFragment, bundle)
        }
        binding.rvDayReservations.layoutManager = LinearLayoutManager(requireContext())
        binding.rvDayReservations.adapter = dayReservationAdapter
    }

    private fun onDateSelected(date: LocalDate) {
        selectedDate = date
        pagerAdapter.activeAdapters.values.forEach { it.setSelectedDate(date) }

        val dayName = date.dayOfWeek.getDisplayName(TextStyle.SHORT, Locale.KOREAN)
        binding.tvSelectedDate.text = getString(R.string.counsel_calendar_date_format, date.monthValue, date.dayOfMonth, dayName)
        binding.llSelectedDateHeader.visibility = View.VISIBLE

        val reservations = allReservations[date] ?: emptyList()
        binding.tvSelectedCount.text = getString(R.string.counsel_count_suffix, reservations.size)

        if (reservations.isEmpty()) {
            binding.rvDayReservations.visibility = View.GONE
            binding.llNoSelection.visibility = View.GONE
            binding.llEmptyReservations.visibility = View.VISIBLE
        } else {
            binding.llNoSelection.visibility = View.GONE
            binding.llEmptyReservations.visibility = View.GONE
            binding.rvDayReservations.visibility = View.VISIBLE
            dayReservationAdapter.updateList(reservations)
        }
    }

    private fun setupListeners() {
        binding.btnPrevMonth.setOnClickListener {
            if (binding.vpCalendar.currentItem > 0) {
                binding.vpCalendar.currentItem = binding.vpCalendar.currentItem - 1
            }
        }
        binding.btnNextMonth.setOnClickListener {
            if (binding.vpCalendar.currentItem < pagerAdapter.itemCount - 1) {
                binding.vpCalendar.currentItem = binding.vpCalendar.currentItem + 1
            }
        }
    }

    private fun observeState() {
        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {

                launch {
                    viewModel.selectedMonth.collect { month ->
                        binding.tvSelectedMonth.text = getString(R.string.counsel_calendar_month_format, month.year, month.monthValue)
                        
                        val currentMonth = pagerAdapter.getYearMonth(binding.vpCalendar.currentItem)
                        if (currentMonth != month) {
                            val targetPosition = pagerAdapter.getPosition(month)
                            binding.vpCalendar.setCurrentItem(targetPosition, false)
                        }

                        selectedDate = null
                        pagerAdapter.activeAdapters.values.forEach { it.setSelectedDate(null) }
                        binding.llSelectedDateHeader.visibility = View.GONE
                        binding.llNoSelection.visibility = View.VISIBLE
                        binding.llEmptyReservations.visibility = View.GONE
                        binding.rvDayReservations.visibility = View.GONE
                    }
                }

                launch {
                    viewModel.employeeList.collect { list ->
                        employeeList = list
                        setupManagerSpinner()
                    }
                }

                launch {
                    viewModel.monthlyReservationCount.collect { count ->
                        binding.tvMonthlyCount.text = getString(R.string.counsel_label_monthly_reservation_count, count)
                    }
                }

                launch {
                    viewModel.uiState.collect { state ->
                        when (state) {
                            is CalendarUiState.Loading -> {
                                binding.progressBar.visibility = View.VISIBLE
                                binding.vpCalendar.alpha = 0.5f
                            }
                            is CalendarUiState.Success -> {
                                binding.progressBar.visibility = View.GONE
                                binding.vpCalendar.alpha = 1.0f
                                allReservations = state.reservations
                                pagerAdapter.activeAdapters.values.forEach { it.updateReservations(state.reservations) }
                                selectedDate?.let { onDateSelected(it) }
                            }
                            is CalendarUiState.Error -> {
                                binding.progressBar.visibility = View.GONE
                                binding.vpCalendar.alpha = 1.0f
                                Toast.makeText(requireContext(), state.message, Toast.LENGTH_SHORT).show()
                            }
                        }
                    }
                }
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        binding.vpCalendar.adapter = null
        _binding = null
    }

    private fun generateCalendarDays(month: YearMonth): List<CalendarDay> {
        val days = mutableListOf<CalendarDay>()
        val firstDay = month.atDay(1)
        val firstDayOfWeek = firstDay.dayOfWeek.value % 7

        val prevMonth = month.minusMonths(1)
        val daysInPrevMonth = prevMonth.lengthOfMonth()
        for (i in firstDayOfWeek - 1 downTo 0) {
            val dayVal = daysInPrevMonth - i
            days.add(CalendarDay(prevMonth.atDay(dayVal), false))
        }

        val daysInMonth = month.lengthOfMonth()
        for (i in 1..daysInMonth) {
            days.add(CalendarDay(month.atDay(i), true))
        }

        val totalCells = if (days.size <= 35) 35 else 42
        val nextMonth = month.plusMonths(1)
        val remaining = totalCells - days.size
        for (i in 1..remaining) {
            days.add(CalendarDay(nextMonth.atDay(i), false))
        }

        return days
    }

    private fun setupManagerSpinner() {
        val hasUnassigned = employeeList.any { it.empSeq == 0 }
        val list = mutableListOf<EmployeeStat>()
        if (!hasUnassigned) {
            list.add(EmployeeStat(empSeq = 0, empName = getString(R.string.counsel_label_unassigned), count = 0))
        }
        list.addAll(employeeList.map {
            if (it.empSeq == 0 || it.empName.isBlank()) {
                it.copy(empName = getString(R.string.counsel_label_unassigned))
            } else {
                it
            }
        })
        displayEmployeeList = list.distinctBy { it.empSeq }

        val labels = mutableListOf(getString(R.string.counsel_label_all_managers)) + displayEmployeeList.map { it.empName }

        val selectedEmpSeq = viewModel.selectedEmpSeq.value
        val defaultIdx = if (selectedEmpSeq == null) 0 else {
            val idx = displayEmployeeList.indexOfFirst { it.empSeq == selectedEmpSeq }
            if (idx >= 0) idx + 1 else 0
        }
        selectedManagerIndex = defaultIdx
        binding.tvManagerSelected.text = labels.getOrNull(defaultIdx) ?: getString(R.string.counsel_label_all_managers)

        binding.spinnerManager.setOnClickListener {
            showManagerDropdown(labels)
        }
    }

    private fun showManagerDropdown(labels: List<String>) {
        val adapter = object : ArrayAdapter<String>(requireContext(), R.layout.item_spinner_dropdown, labels) {
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

                val isSelected = position == selectedManagerIndex
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

        managerPopup?.dismiss()
        managerPopup = binding.spinnerManager.showCustomDropdown(adapter) { position ->
            selectedManagerIndex = position
            binding.tvManagerSelected.text = labels[position]

            val empSeq = if (position == 0) null else displayEmployeeList.getOrNull(position - 1)?.empSeq
            viewModel.selectEmpSeq(empSeq)
        }
    }

    // --- ViewPager2 Month Adapter ---
    inner class MonthPagerAdapter(
        private val startMonth: YearMonth,
        private val endMonth: YearMonth,
        private val onDateClick: (LocalDate) -> Unit
    ) : RecyclerView.Adapter<MonthPagerAdapter.MonthViewHolder>() {

        val activeAdapters = mutableMapOf<Int, CalendarAdapter>()

        override fun getItemCount(): Int {
            return ChronoUnit.MONTHS.between(startMonth, endMonth).toInt() + 1
        }

        fun getYearMonth(position: Int): YearMonth {
            return startMonth.plusMonths(position.toLong())
        }

        fun getPosition(month: YearMonth): Int {
            return ChronoUnit.MONTHS.between(startMonth, month).toInt()
        }

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MonthViewHolder {
            val binding = ItemCalendarMonthPageBinding.inflate(
                LayoutInflater.from(parent.context), parent, false
            )
            return MonthViewHolder(binding)
        }

        override fun onBindViewHolder(holder: MonthViewHolder, position: Int) {
            val month = getYearMonth(position)
            holder.bind(month)
        }

        override fun onViewRecycled(holder: MonthViewHolder) {
            super.onViewRecycled(holder)
            activeAdapters.remove(holder.bindingAdapterPosition)
        }

        inner class MonthViewHolder(
            private val binding: ItemCalendarMonthPageBinding
        ) : RecyclerView.ViewHolder(binding.root) {
            fun bind(month: YearMonth) {
                val days = generateCalendarDays(month)
                val adapter = CalendarAdapter(days, allReservations) { date ->
                    onDateClick(date)
                }
                
                selectedDate?.let { adapter.setSelectedDate(it) }

                binding.rvCalendarGrid.layoutManager = GridLayoutManager(itemView.context, 7)
                binding.rvCalendarGrid.adapter = adapter
                binding.rvCalendarGrid.itemAnimator = null

                activeAdapters[bindingAdapterPosition] = adapter
            }
        }
    }

    // --- Models ---
    data class CalendarDay(
        val date: LocalDate,
        val isCurrentMonth: Boolean
    )

    // --- Calendar Grid Adapter ---
    inner class CalendarAdapter(
        private var days: List<CalendarDay>,
        private var reservations: Map<LocalDate, List<CounselItem>>,
        private val onDayClick: (LocalDate) -> Unit
    ) : RecyclerView.Adapter<CalendarAdapter.DayViewHolder>() {

        private var selectedDate: LocalDate? = null

        fun setSelectedDate(date: LocalDate?) {
            val old = selectedDate
            selectedDate = date
            // 이전 선택 갱신
            old?.let { oldDate ->
                val oldIdx = days.indexOfFirst { it.date == oldDate }
                if (oldIdx >= 0) notifyItemChanged(oldIdx)
            }
            // 새 선택 갱신
            date?.let { newDate ->
                val newIdx = days.indexOfFirst { it.date == newDate }
                if (newIdx >= 0) notifyItemChanged(newIdx)
            }
        }

        fun updateDays(newDays: List<CalendarDay>) {
            this.days = newDays
            notifyDataSetChanged()
        }

        fun updateReservations(newReservations: Map<LocalDate, List<CounselItem>>) {
            this.reservations = newReservations
            notifyDataSetChanged()
        }

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): DayViewHolder {
            val binding = ItemCalendarGridDayBinding.inflate(
                LayoutInflater.from(parent.context), parent, false
            )
            val view = binding.root

            val rowCount = if (days.size <= 35) 5 else 6
            val density = parent.context.resources.displayMetrics.density
            
            // ViewPager2의 고정 높이 340dp 및 셀 마진 2dp(상하 4dp)를 완벽하게 고려한 정밀 계산값
            val cellHeightDp = if (rowCount == 5) 64 else 52
            val cellHeightPx = (cellHeightDp * density).toInt()

            val params = view.layoutParams as? ViewGroup.MarginLayoutParams
            if (params != null) {
                params.height = cellHeightPx
                view.layoutParams = params
            } else {
                view.layoutParams = RecyclerView.LayoutParams(
                    ViewGroup.LayoutParams.MATCH_PARENT,
                    cellHeightPx
                )
            }
            return DayViewHolder(binding)
        }

        override fun onBindViewHolder(holder: DayViewHolder, position: Int) {
            holder.bind(days[position])
        }

        override fun getItemCount(): Int = days.size

        inner class DayViewHolder(
            private val binding: ItemCalendarGridDayBinding
        ) : RecyclerView.ViewHolder(binding.root) {

            fun bind(day: CalendarDay) {
                val date = day.date
                binding.tvDayNumber.text = date.dayOfMonth.toString()

                val isToday = date == LocalDate.now()
                val isSelected = date == selectedDate
                val hasReservation = (reservations[date]?.isNotEmpty()) == true

                when {
                    isSelected -> {
                        // 선택된 날: 테두리+연보라 배경만 유지
                        binding.tvDayNumber.setTextColor(androidx.core.content.ContextCompat.getColor(itemView.context, R.color.counsel_calendar_selected_day))
                        binding.layoutDayCell.setBackgroundResource(R.drawable.bg_day_cell_selected)
                    }
                    isToday -> {
                        // 오늘: 파란 테두리+연파랑 배경만 유지
                        binding.tvDayNumber.setTextColor(androidx.core.content.ContextCompat.getColor(itemView.context, R.color.counsel_calendar_today))
                        binding.layoutDayCell.setBackgroundResource(R.drawable.bg_day_cell_today)
                    }
                    day.isCurrentMonth -> {
                        val dayOfWeek = date.dayOfWeek.value
                        binding.tvDayNumber.setTextColor(
                            when (dayOfWeek) {
                                7 -> androidx.core.content.ContextCompat.getColor(itemView.context, R.color.counsel_calendar_sunday)
                                6 -> androidx.core.content.ContextCompat.getColor(itemView.context, R.color.counsel_calendar_saturday)
                                else -> androidx.core.content.ContextCompat.getColor(itemView.context, R.color.counsel_calendar_day_normal)
                            }
                        )
                        binding.layoutDayCell.setBackgroundResource(R.drawable.bg_day_cell_normal)
                    }
                    else -> {
                        // 이전/다음 달 날짜: 흐리게
                        binding.tvDayNumber.setTextColor(androidx.core.content.ContextCompat.getColor(itemView.context, R.color.counsel_calendar_day_disabled))
                        binding.layoutDayCell.setBackgroundResource(R.drawable.bg_day_cell_normal)
                    }
                }

                // 예약 인디케이터 (하단 바 가로 꽉 채우기)
                binding.layoutReservations.removeAllViews()
                if (hasReservation) {
                    val dot = View(itemView.context).apply {
                        val params = LinearLayout.LayoutParams(
                            LinearLayout.LayoutParams.MATCH_PARENT,
                            4.dpToPx(itemView.context)
                        )
                        layoutParams = params
                        setBackgroundResource(R.drawable.bg_calendar_reservation_bar)
                    }
                    binding.layoutReservations.addView(dot)
                }

                itemView.setOnClickListener {
                    if (day.isCurrentMonth) {
                        onDayClick(date)
                    }
                }
            }

            private fun Int.dpToPx(context: android.content.Context): Int =
                (this * context.resources.displayMetrics.density).toInt()
        }
    }

    // --- 하단 패널 예약 목록 Adapter ---
    inner class DayReservationAdapter(
        private var items: List<CounselItem>,
        private val onItemClick: (CounselItem) -> Unit
    ) : RecyclerView.Adapter<DayReservationAdapter.CardViewHolder>() {

        fun updateList(newItems: List<CounselItem>) {
            this.items = newItems
            notifyDataSetChanged()
        }

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CardViewHolder {
            val binding = ItemCalendarReservationCardBinding.inflate(
                LayoutInflater.from(parent.context), parent, false
            )
            return CardViewHolder(binding)
        }

        override fun onBindViewHolder(holder: CardViewHolder, position: Int) {
            holder.bind(items[position])
        }

        override fun getItemCount(): Int = items.size

        inner class CardViewHolder(
            private val binding: ItemCalendarReservationCardBinding
        ) : RecyclerView.ViewHolder(binding.root) {

            fun bind(item: CounselItem) {
                binding.tvName.text = item.name
                binding.tvTime.text = formatTime(item.counselResvDtm)
                binding.tvPhone.text = formatPhone(item.counselHp)

                itemView.setOnClickListener { onItemClick(item) }
            }

            private fun formatTime(dtm: String?): String {
                if (dtm.isNullOrBlank()) return ""
                return try {
                    if (dtm.contains("T")) dtm.substringAfter("T").substring(0, 5)
                    else dtm.substringAfter(" ").substring(0, 5)
                } catch (e: Exception) { "" }
            }

            private fun formatPhone(phone: String?): String {
                if (phone.isNullOrBlank()) return ""
                val clean = phone.replace("-", "").replace(" ", "")
                return when (clean.length) {
                    11 -> "${clean.substring(0, 3)}-${clean.substring(3, 7)}-${clean.substring(7)}"
                    10 -> "${clean.substring(0, 3)}-${clean.substring(3, 6)}-${clean.substring(6)}"
                    else -> phone
                }
            }
        }
    }
}
