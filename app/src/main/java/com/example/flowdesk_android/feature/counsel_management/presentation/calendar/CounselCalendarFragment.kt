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
import android.widget.Spinner
import android.widget.TextView
import android.widget.Toast
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

    // Views
    private lateinit var btnPrevMonth: ImageButton
    private lateinit var tvSelectedMonth: TextView
    private lateinit var btnNextMonth: ImageButton
    private lateinit var spinnerManager: Spinner
    private lateinit var tvMonthlyCount: TextView
    private lateinit var vpCalendar: ViewPager2
    private lateinit var progressBar: ProgressBar

    // 하단 패널 Views
    private lateinit var llSelectedDayPanel: LinearLayout
    private lateinit var llSelectedDateHeader: LinearLayout
    private lateinit var tvSelectedDate: TextView
    private lateinit var tvSelectedCount: TextView
    private lateinit var llNoSelection: LinearLayout
    private lateinit var llEmptyReservations: LinearLayout
    private lateinit var rvDayReservations: RecyclerView

    private lateinit var pagerAdapter: MonthPagerAdapter
    private lateinit var dayReservationAdapter: DayReservationAdapter
    private var employeeList: List<EmployeeStat> = emptyList()
    private var displayEmployeeList: List<EmployeeStat> = emptyList()
    private var isFirstSpinnerSelection = true
    private var selectedDate: LocalDate? = null
    private var allReservations: Map<LocalDate, List<CounselItem>> = emptyMap()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_counsel_calendar, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        bindViews(view)
        setupViewPager()
        setupDayReservationList()
        setupListeners()
        observeState()
    }

    private fun bindViews(view: View) {
        btnPrevMonth = view.findViewById(R.id.btn_prev_month)
        tvSelectedMonth = view.findViewById(R.id.tv_selected_month)
        btnNextMonth = view.findViewById(R.id.btn_next_month)
        spinnerManager = view.findViewById(R.id.spinner_manager)
        tvMonthlyCount = view.findViewById(R.id.tv_monthly_count)
        vpCalendar = view.findViewById(R.id.vp_calendar)
        progressBar = view.findViewById(R.id.progress_bar)
        llSelectedDayPanel = view.findViewById(R.id.ll_selected_day_panel)
        llSelectedDateHeader = view.findViewById(R.id.ll_selected_date_header)
        tvSelectedDate = view.findViewById(R.id.tv_selected_date)
        tvSelectedCount = view.findViewById(R.id.tv_selected_count)
        llNoSelection = view.findViewById(R.id.ll_no_selection)
        llEmptyReservations = view.findViewById(R.id.ll_empty_reservations)
        rvDayReservations = view.findViewById(R.id.rv_day_reservations)
    }

    private fun setupViewPager() {
        pagerAdapter = MonthPagerAdapter(startMonth, endMonth) { date ->
            onDateSelected(date)
        }
        vpCalendar.adapter = pagerAdapter

        // 현재 달로 초기 스크롤 세팅
        val initialPosition = pagerAdapter.getPosition(YearMonth.now())
        vpCalendar.setCurrentItem(initialPosition, false)

        // 가로 스와이프 페이지 변경 콜백
        vpCalendar.registerOnPageChangeCallback(object : ViewPager2.OnPageChangeCallback() {
            override fun onPageSelected(position: Int) {
                val targetMonth = pagerAdapter.getYearMonth(position)
                if (viewModel.selectedMonth.value != targetMonth) {
                    viewModel.selectMonth(targetMonth)
                }
            }
        })

        // NestedScrollView 터치 간섭 방지: 가로 스와이프 우선 처리 적용
        val child = vpCalendar.getChildAt(0)
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
        rvDayReservations.layoutManager = LinearLayoutManager(requireContext())
        rvDayReservations.adapter = dayReservationAdapter
    }

    private fun onDateSelected(date: LocalDate) {
        selectedDate = date
        pagerAdapter.activeAdapters.values.forEach { it.setSelectedDate(date) }

        val dayName = date.dayOfWeek.getDisplayName(TextStyle.SHORT, Locale.KOREAN)
        tvSelectedDate.text = "${date.monthValue}월 ${date.dayOfMonth}일 $dayName"
        llSelectedDateHeader.visibility = View.VISIBLE

        val reservations = allReservations[date] ?: emptyList()
        tvSelectedCount.text = "${reservations.size}건"

        if (reservations.isEmpty()) {
            rvDayReservations.visibility = View.GONE
            llNoSelection.visibility = View.GONE
            llEmptyReservations.visibility = View.VISIBLE
        } else {
            llNoSelection.visibility = View.GONE
            llEmptyReservations.visibility = View.GONE
            rvDayReservations.visibility = View.VISIBLE
            dayReservationAdapter.updateList(reservations)
        }
    }

    private fun setupListeners() {
        btnPrevMonth.setOnClickListener {
            if (vpCalendar.currentItem > 0) {
                vpCalendar.currentItem = vpCalendar.currentItem - 1
            }
        }
        btnNextMonth.setOnClickListener {
            if (vpCalendar.currentItem < pagerAdapter.itemCount - 1) {
                vpCalendar.currentItem = vpCalendar.currentItem + 1
            }
        }

        spinnerManager.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
                if (isFirstSpinnerSelection) {
                    isFirstSpinnerSelection = false
                    return
                }
                val empSeq = if (position == 0) null else displayEmployeeList.getOrNull(position - 1)?.empSeq
                viewModel.selectEmpSeq(empSeq)
            }

            override fun onNothingSelected(parent: AdapterView<*>?) {}
        }
    }

    private fun observeState() {
        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {

                launch {
                    viewModel.selectedMonth.collect { month ->
                        tvSelectedMonth.text = "${month.year}년 ${month.monthValue}월"
                        
                        // 뷰페이저 현재 페이지와 selectedMonth 불일치 시 페이지 이동 동기화
                        val currentMonth = pagerAdapter.getYearMonth(vpCalendar.currentItem)
                        if (currentMonth != month) {
                            val targetPosition = pagerAdapter.getPosition(month)
                            vpCalendar.setCurrentItem(targetPosition, false)
                        }

                        // 월 변경 시 선택 초기화
                        selectedDate = null
                        pagerAdapter.activeAdapters.values.forEach { it.setSelectedDate(null) }
                        llSelectedDateHeader.visibility = View.GONE
                        llNoSelection.visibility = View.VISIBLE
                        llEmptyReservations.visibility = View.GONE
                        rvDayReservations.visibility = View.GONE
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
                        tvMonthlyCount.text = "이번 달 예약: ${count}건"
                    }
                }

                launch {
                    viewModel.uiState.collect { state ->
                        when (state) {
                            is CalendarUiState.Loading -> {
                                progressBar.visibility = View.VISIBLE
                                vpCalendar.alpha = 0.5f
                            }
                            is CalendarUiState.Success -> {
                                progressBar.visibility = View.GONE
                                vpCalendar.alpha = 1.0f
                                allReservations = state.reservations
                                pagerAdapter.activeAdapters.values.forEach { it.updateReservations(state.reservations) }
                                // 선택된 날짜 있으면 갱신
                                selectedDate?.let { onDateSelected(it) }
                            }
                            is CalendarUiState.Error -> {
                                progressBar.visibility = View.GONE
                                vpCalendar.alpha = 1.0f
                                Toast.makeText(requireContext(), state.message, Toast.LENGTH_SHORT).show()
                            }
                        }
                    }
                }
            }
        }
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
            list.add(EmployeeStat(empSeq = 0, empName = "미배정", count = 0))
        }
        list.addAll(employeeList.map {
            if (it.empSeq == 0 || it.empName.isBlank()) {
                it.copy(empName = "미배정")
            } else {
                it
            }
        })
        displayEmployeeList = list.distinctBy { it.empSeq }

        val labels = mutableListOf("모든 담당자") + displayEmployeeList.map { it.empName }
        val adapter = object : ArrayAdapter<String>(requireContext(), android.R.layout.simple_spinner_item, labels) {
            override fun getView(position: Int, convertView: View?, parent: ViewGroup): View {
                val view = convertView ?: LayoutInflater.from(context)
                    .inflate(R.layout.item_spinner_selected, parent, false)
                val tvText = view.findViewById<TextView>(android.R.id.text1)
                tvText?.text = getItem(position)
                return view
            }
            override fun getDropDownView(position: Int, convertView: View?, parent: ViewGroup): View {
                val view = convertView ?: LayoutInflater.from(context)
                    .inflate(R.layout.item_spinner_dropdown, parent, false)
                val tvText = view.findViewById<TextView>(android.R.id.text1)
                val vDot = view.findViewById<View>(R.id.v_spinner_dot)
                val layoutRoot = view.findViewById<View>(R.id.layout_dropdown_root)

                tvText.text = getItem(position)
                vDot.visibility = View.GONE

                val selectedPos = spinnerManager.selectedItemPosition
                val isSelected = position == selectedPos
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
        adapter.setDropDownViewResource(R.layout.item_spinner_dropdown)
        spinnerManager.adapter = adapter

        val selectedEmpSeq = viewModel.selectedEmpSeq.value
        if (selectedEmpSeq == null) {
            spinnerManager.setSelection(0)
        } else {
            val idx = displayEmployeeList.indexOfFirst { it.empSeq == selectedEmpSeq }
            if (idx >= 0) {
                spinnerManager.setSelection(idx + 1)
            } else {
                spinnerManager.setSelection(0)
            }
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
            val view = LayoutInflater.from(parent.context)
                .inflate(R.layout.item_calendar_month_page, parent, false)
            return MonthViewHolder(view)
        }

        override fun onBindViewHolder(holder: MonthViewHolder, position: Int) {
            val month = getYearMonth(position)
            holder.bind(month)
        }

        override fun onViewRecycled(holder: MonthViewHolder) {
            super.onViewRecycled(holder)
            activeAdapters.remove(holder.bindingAdapterPosition)
        }

        inner class MonthViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
            private val rvGrid: RecyclerView = itemView.findViewById(R.id.rv_calendar_grid)

            fun bind(month: YearMonth) {
                val days = generateCalendarDays(month)
                val adapter = CalendarAdapter(days, allReservations) { date ->
                    onDateClick(date)
                }
                
                selectedDate?.let { adapter.setSelectedDate(it) }

                rvGrid.layoutManager = GridLayoutManager(itemView.context, 7)
                rvGrid.adapter = adapter
                rvGrid.itemAnimator = null

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
            val view = LayoutInflater.from(parent.context)
                .inflate(R.layout.item_calendar_grid_day, parent, false)

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
            return DayViewHolder(view)
        }

        override fun onBindViewHolder(holder: DayViewHolder, position: Int) {
            holder.bind(days[position])
        }

        override fun getItemCount(): Int = days.size

        inner class DayViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
            private val tvDayNumber: TextView = itemView.findViewById(R.id.tv_day_number)
            private val layoutIndicators: LinearLayout = itemView.findViewById(R.id.layout_reservations)
            private val layoutDayCell: View = itemView.findViewById(R.id.layout_day_cell)

            fun bind(day: CalendarDay) {
                val date = day.date
                tvDayNumber.text = date.dayOfMonth.toString()

                val isToday = date == LocalDate.now()
                val isSelected = date == selectedDate
                val hasReservation = (reservations[date]?.isNotEmpty()) == true

                when {
                    isSelected -> {
                        // 선택된 날: 테두리+연보라 배경만 유지
                        tvDayNumber.setTextColor(Color.parseColor("#7C3AED"))
                        layoutDayCell.setBackgroundResource(R.drawable.bg_day_cell_selected)
                    }
                    isToday -> {
                        // 오늘: 파란 테두리+연파랑 배경만 유지
                        tvDayNumber.setTextColor(Color.parseColor("#1D4ED8"))
                        layoutDayCell.setBackgroundResource(R.drawable.bg_day_cell_today)
                    }
                    day.isCurrentMonth -> {
                        val dayOfWeek = date.dayOfWeek.value
                        tvDayNumber.setTextColor(
                            when (dayOfWeek) {
                                7 -> Color.parseColor("#EF4444")
                                6 -> Color.parseColor("#3B82F6")
                                else -> Color.parseColor("#374151")
                            }
                        )
                        layoutDayCell.setBackgroundResource(R.drawable.bg_day_cell_normal)
                    }
                    else -> {
                        // 이전/다음 달 날짜: 흐리게
                        tvDayNumber.setTextColor(Color.parseColor("#C7D2E2"))
                        layoutDayCell.setBackgroundResource(R.drawable.bg_day_cell_normal)
                    }
                }

                // 예약 인디케이터 (하단 바 가로 꽉 채우기)
                layoutIndicators.removeAllViews()
                if (hasReservation) {
                    val dot = View(itemView.context).apply {
                        val params = LinearLayout.LayoutParams(
                            LinearLayout.LayoutParams.MATCH_PARENT,
                            4.dpToPx(itemView.context)
                        )
                        layoutParams = params
                        setBackgroundResource(R.drawable.bg_reservation_indicator)
                    }
                    layoutIndicators.addView(dot)
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
            val view = LayoutInflater.from(parent.context)
                .inflate(R.layout.item_calendar_reservation_card, parent, false)
            return CardViewHolder(view)
        }

        override fun onBindViewHolder(holder: CardViewHolder, position: Int) {
            holder.bind(items[position])
        }

        override fun getItemCount(): Int = items.size

        inner class CardViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
            private val tvName: TextView = itemView.findViewById(R.id.tv_name)
            private val tvTime: TextView = itemView.findViewById(R.id.tv_time)
            private val tvPhone: TextView = itemView.findViewById(R.id.tv_phone)

            fun bind(item: CounselItem) {
                tvName.text = item.name
                tvTime.text = formatTime(item.counselResvDtm)
                tvPhone.text = formatPhone(item.counselHp)

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
