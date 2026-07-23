package com.example.flowdesk_android.feature.counsel_management.presentation.dashboard

import android.graphics.Color
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import android.widget.ProgressBar
import android.widget.TextView
import android.widget.Toast
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import com.example.flowdesk_android.R
import com.example.flowdesk_android.feature.counsel_management.domain.model.CounselDashboard
import com.example.flowdesk_android.feature.counsel_management.domain.model.CounselStatusStat
import com.example.flowdesk_android.feature.counsel_management.domain.model.DailyTrend
import com.example.flowdesk_android.feature.counsel_management.domain.model.EmployeeStat
import com.example.flowdesk_android.feature.counsel_management.domain.model.HourlyDistribution
import com.example.flowdesk_android.feature.counsel_management.domain.model.TopWebsite
import com.example.flowdesk_android.feature.counsel_management.domain.model.UpcomingReservation
import com.github.mikephil.charting.charts.BarChart
import com.github.mikephil.charting.charts.LineChart
import com.github.mikephil.charting.data.Entry
import com.github.mikephil.charting.data.LineData
import com.github.mikephil.charting.data.LineDataSet
import com.github.mikephil.charting.components.XAxis
import com.github.mikephil.charting.data.BarData
import com.github.mikephil.charting.data.BarDataSet
import com.github.mikephil.charting.data.BarEntry
import com.github.mikephil.charting.formatter.ValueFormatter
import com.github.mikephil.charting.charts.RadarChart
import com.github.mikephil.charting.data.RadarData
import com.github.mikephil.charting.data.RadarDataSet
import com.github.mikephil.charting.data.RadarEntry
import android.graphics.Canvas
import android.content.Context
import android.util.AttributeSet
import android.view.MotionEvent
import com.github.mikephil.charting.renderer.XAxisRendererRadarChart
import com.github.mikephil.charting.utils.MPPointF
import com.github.mikephil.charting.utils.Utils
import com.github.mikephil.charting.utils.ViewPortHandler
import com.github.mikephil.charting.listener.OnChartGestureListener
import com.github.mikephil.charting.listener.ChartTouchListener
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch
import java.time.LocalDateTime
import java.time.ZonedDateTime
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.util.Locale
import com.example.flowdesk_android.feature.counsel_management.presentation.list.CustomCalendarDialogFragment

@AndroidEntryPoint
class CounselDashboardFragment : Fragment() {

    private val viewModel: CounselDashboardViewModel by viewModels()

    private lateinit var tvHeaderTitle: TextView
    private lateinit var tvHeaderSubtitle: TextView

    // ── 파이프라인 분석 ──
    private lateinit var btnSummaryInfo: View
    private lateinit var btnDailyTrendInfo: View
    private lateinit var gaugeNew: SingleCircularProgressView
    private lateinit var gaugeProgress: SingleCircularProgressView
    private lateinit var gaugeCompleted: SingleCircularProgressView
    private lateinit var tvGaugeNewCount: TextView
    private lateinit var tvGaugeNewPercent: TextView
    private lateinit var tvGaugeProgressCount: TextView
    private lateinit var tvGaugeProgressPercent: TextView
    private lateinit var tvGaugeCompletedCount: TextView
    private lateinit var tvGaugeCompletedPercent: TextView

    // ── 섹션 컨테이너 ──
    private lateinit var llStatusDistribution: LinearLayout
    private lateinit var lineChartEmployee: CounselEmployeeLineChartView
    private lateinit var barChartDaily: DailyBarChartView
    private lateinit var llTopWebsites: LinearLayout
    private lateinit var lineChartHourly: CounselHourlyLineChartView
    private lateinit var llUpcomingReservations: LinearLayout

    // ── 상태 분포 차트 ──
    private lateinit var tvStatusTotalCount: TextView
    private lateinit var segmentedBarView: SegmentedBarView
    private lateinit var btnStatusMore: TextView
    private var isStatusExpanded = false

    // ── 날짜 필터 ──
    private lateinit var btnDateFilter: View
    private lateinit var tvDateRange: TextView
    private var selectedStartDate = LocalDate.now().minusDays(30)
    private var selectedEndDate = LocalDate.now()
    private var isDateFilterApplied = false
    private val dateFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd", Locale.getDefault())
    private val displayFormatter = DateTimeFormatter.ofPattern("yyyy.MM.dd", Locale.getDefault())

    // ── 일별 추이 1주/2주/한달 전환 ──
    private var fullDailyTrends: List<DailyTrend> = emptyList()
    private lateinit var vSegmentSelector: View
    private lateinit var btnPeriod1w: TextView
    private lateinit var btnPeriod2w: TextView
    private lateinit var btnPeriod1m: TextView
    private var currentPeriodIndex = 0

    // ── 로딩 / 콘텐츠 ──
    private lateinit var progressBar: ProgressBar
    private lateinit var llContent: View

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? = inflater.inflate(R.layout.fragment_counsel_dashboard, container, false)

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        bindViews(view)
        observeState()
        viewModel.loadDashboard(null, null) // Load all-time data initially
    }

    private fun bindViews(view: View) {
        tvHeaderTitle           = view.findViewById(R.id.tv_header_title)
        tvHeaderSubtitle        = view.findViewById(R.id.tv_header_subtitle)
        llStatusDistribution    = view.findViewById(R.id.ll_status_distribution)
        lineChartEmployee       = view.findViewById(R.id.line_chart_employee)
        barChartDaily           = view.findViewById(R.id.bar_chart_daily)
        llTopWebsites           = view.findViewById(R.id.ll_top_websites)
        lineChartHourly         = view.findViewById(R.id.line_chart_hourly)
        llUpcomingReservations  = view.findViewById(R.id.ll_upcoming_reservations)

        gaugeNew                = view.findViewById(R.id.gauge_new)
        gaugeProgress           = view.findViewById(R.id.gauge_progress)
        gaugeCompleted          = view.findViewById(R.id.gauge_completed)
        tvGaugeNewCount         = view.findViewById(R.id.tv_gauge_new_count)
        tvGaugeNewPercent       = view.findViewById(R.id.tv_gauge_new_percent)
        tvGaugeProgressCount    = view.findViewById(R.id.tv_gauge_progress_count)
        tvGaugeProgressPercent  = view.findViewById(R.id.tv_gauge_progress_percent)
        tvGaugeCompletedCount   = view.findViewById(R.id.tv_gauge_completed_count)
        tvGaugeCompletedPercent = view.findViewById(R.id.tv_gauge_completed_percent)

        gaugeNew.setActiveColor(Color.parseColor("#3B82F6"))
        gaugeCompleted.setActiveColor(Color.parseColor("#3B82F6"))
        gaugeProgress.setActiveColor(Color.parseColor("#3B82F6")) // Blue color matching photo
        gaugeProgress.setUseGapArc(true) // Gap-arc gauge design

        tvStatusTotalCount      = view.findViewById(R.id.tv_status_total_count)
        segmentedBarView        = view.findViewById(R.id.segmented_bar_view)
        btnStatusMore           = view.findViewById(R.id.btn_status_more)

        btnDateFilter           = view.findViewById(R.id.btn_date_filter)
        tvDateRange             = view.findViewById(R.id.tv_date_range)

        updateDateTextViews()
        setupDatePickerListeners()

        progressBar = view.findViewById(R.id.progress_bar)
        llContent   = view.findViewById(R.id.ll_content)

        btnSummaryInfo          = view.findViewById(R.id.btn_summary_info)
        setupSummaryInfoTooltip()

        btnDailyTrendInfo       = view.findViewById(R.id.btn_daily_trend_info)
        setupDailyTrendInfoTooltip()

        vSegmentSelector        = view.findViewById(R.id.v_segment_selector)
        btnPeriod1w             = view.findViewById(R.id.btn_period_1w)
        btnPeriod2w             = view.findViewById(R.id.btn_period_2w)
        btnPeriod1m             = view.findViewById(R.id.btn_period_1m)
        setupDailyPeriodTabs()
    }

    private fun setupSummaryInfoTooltip() {
        btnSummaryInfo.setOnClickListener { v ->
            val inflater = LayoutInflater.from(requireContext())
            val popupView = inflater.inflate(R.layout.layout_tooltip_dashboard_summary, null)
            val popupWindow = android.widget.PopupWindow(
                popupView,
                ViewGroup.LayoutParams.WRAP_CONTENT,
                ViewGroup.LayoutParams.WRAP_CONTENT,
                true
            ).apply {
                elevation = 16f
                setBackgroundDrawable(android.graphics.drawable.ColorDrawable(Color.TRANSPARENT))
                isOutsideTouchable = true
            }
            popupWindow.showAsDropDown(v, 0, 8)
        }
    }

    private fun setupDailyTrendInfoTooltip() {
        btnDailyTrendInfo.setOnClickListener { v ->
            val inflater = LayoutInflater.from(requireContext())
            val popupView = inflater.inflate(R.layout.layout_tooltip_dashboard_daily_trend, null)
            val popupWindow = android.widget.PopupWindow(
                popupView,
                ViewGroup.LayoutParams.WRAP_CONTENT,
                ViewGroup.LayoutParams.WRAP_CONTENT,
                true
            ).apply {
                elevation = 16f
                setBackgroundDrawable(android.graphics.drawable.ColorDrawable(Color.TRANSPARENT))
                isOutsideTouchable = true
            }
            popupWindow.showAsDropDown(v, 0, 8)
        }
    }

    private fun observeState() {
        viewLifecycleOwner.lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.uiState.collect { state ->
                    when (state) {
                        is CounselDashboardUiState.Loading -> showLoading(true)
                        is CounselDashboardUiState.Success -> {
                            showLoading(false)
                            bindData(state.data)
                            
                            // canAdmin에 따른 화면 제어 적용
                            tvHeaderTitle.text = if (state.canAdmin) "상담 대시보드" else "내 상담 대시보드"
                            val employeeCard = lineChartEmployee.parent as? View
                            employeeCard?.visibility = if (state.canAdmin) View.VISIBLE else View.GONE
                        }
                        is CounselDashboardUiState.Error -> {
                            showLoading(false)
                            Toast.makeText(requireContext(), state.message, Toast.LENGTH_SHORT).show()
                        }
                    }
                }
            }
        }
    }

    private fun showLoading(isLoading: Boolean) {
        progressBar.visibility = if (isLoading) View.VISIBLE else View.GONE
        llContent.visibility   = if (isLoading) View.GONE   else View.VISIBLE
    }

    private fun bindData(data: CounselDashboard) {
        // ① 요약 및 파이프라인 3단 게이지 바인딩
        with(data.summary) {
            val total = totalCounsels.toFloat()
            val newPercent = if (total > 0) (newCounsels.toFloat() / total * 100f) else 0f
            val completedPercent = if (total > 0) (completedCounsels.toFloat() / total * 100f) else 0f

            // 1. 신규 접수 (건수만 표기)
            tvGaugeNewCount.text = "${newCounsels}건"
            gaugeNew.setProgress(newPercent)

            // 2. 처리 완료 (건수만 표기)
            tvGaugeCompletedCount.text = "${completedCounsels}건"
            gaugeCompleted.setProgress(completedPercent)

            // 3. 완료율 (백분율 표기)
            tvGaugeProgressCount.text = String.format(Locale.getDefault(), "%.1f%%", completionRate)
            gaugeProgress.setProgress(completionRate.toFloat())

            // 완료율 상태 분석 텍스트 및 컬러 매핑 (예: 안정 / 보통 / 주의)
            val (statusText, statusColor) = when {
                completionRate >= 50.0 -> "안정" to Color.parseColor("#10B981") // 초록색
                completionRate >= 20.0 -> "보통" to Color.parseColor("#F59E0B") // 주황색
                else -> "주의" to Color.parseColor("#EF4444") // 빨간색
            }
            tvGaugeProgressPercent.text = statusText
            tvGaugeProgressPercent.setTextColor(statusColor)
            gaugeProgress.setActiveColor(statusColor)
        }

        // ② 상태별 분포
        renderStatusDistribution(data.statusDistribution)

        // ③ 담당자별 현황
        renderEmployeeStats(data.employeeStats)

        // ④ 일별 추이 데이터 백업 및 기간 필터링 차트 노출
        this.fullDailyTrends = data.dailyTrends
        updateDailyChartByPeriod()

        // ⑤ 웹사이트 Top 5
        renderTopWebsites(data.topWebsites)

        // ⑥ 시간대별 분포
        renderHourlyChart(data.hourlyDistribution)

        // ⑦ 예정된 예약
        renderUpcomingReservations(data.upcomingReservations)
    }

    // ─────────────────────────────────────────────────────────
    // ② 상태별 분포
    // ─────────────────────────────────────────────────────────
    private fun renderStatusDistribution(list: List<CounselStatusStat>) {
        llStatusDistribution.removeAllViews()
        val totalCount = list.sumOf { it.count }
        tvStatusTotalCount.text = "${totalCount}건"

        if (list.isEmpty()) {
            segmentedBarView.setSegments(emptyList())
            btnStatusMore.visibility = View.GONE
            addEmptyView(llStatusDistribution, "데이터 없음")
            return
        }

        // 1. Segmented Bar View 데이터 설정
        val segments = list.map { stat ->
            val color = try {
                Color.parseColor(stat.color)
            } catch (e: Exception) {
                ContextCompat.getColor(requireContext(), R.color.login_blue)
            }
            SegmentedBarView.Segment(stat.count.toFloat(), color)
        }
        segmentedBarView.setSegments(segments)

        // 2. 리스트 행 렌더링
        val safeTotal = totalCount.coerceAtLeast(1)
        list.forEachIndexed { idx, stat ->
            val row = LayoutInflater.from(requireContext())
                .inflate(R.layout.item_counsel_stat_bar_row, llStatusDistribution, false)

            val tvName      = row.findViewById<TextView>(R.id.tv_stat_name)
            val tvPercent   = row.findViewById<TextView>(R.id.tv_stat_percent)
            val tvCount     = row.findViewById<TextView>(R.id.tv_stat_count)
            val vColorDot   = row.findViewById<View>(R.id.v_status_color_dot)

            tvName.text  = stat.statusName
            tvCount.text = "${stat.count}건"

            val ratio = stat.count.toFloat() / safeTotal
            tvPercent.text = String.format(Locale.getDefault(), "%.1f%%", ratio * 100f)

            try {
                val color = Color.parseColor(stat.color)
                vColorDot.backgroundTintList = android.content.res.ColorStateList.valueOf(color)
            } catch (e: Exception) {
                vColorDot.backgroundTintList = android.content.res.ColorStateList.valueOf(
                    ContextCompat.getColor(requireContext(), R.color.login_blue)
                )
            }

            // Top 3만 먼저 노출
            if (idx >= 3) {
                row.visibility = if (isStatusExpanded) View.VISIBLE else View.GONE
            } else {
                row.visibility = View.VISIBLE
            }

            llStatusDistribution.addView(row)
        }

        // 3. 더보기 버튼 설정
        if (list.size > 3) {
            btnStatusMore.visibility = View.VISIBLE
            btnStatusMore.text = if (isStatusExpanded) "접기" else "자세히 보기"
            btnStatusMore.setOnClickListener {
                isStatusExpanded = !isStatusExpanded
                btnStatusMore.text = if (isStatusExpanded) "접기" else "자세히 보기"
                val count = llStatusDistribution.childCount
                for (i in 3 until count) {
                    llStatusDistribution.getChildAt(i).visibility =
                        if (isStatusExpanded) View.VISIBLE else View.GONE
                }
            }
        } else {
            btnStatusMore.visibility = View.GONE
        }
    }

    // ─────────────────────────────────────────────────────────
    // ③ 담당자별 현황
    // ─────────────────────────────────────────────────────────
    private fun renderEmployeeStats(list: List<EmployeeStat>) {
        if (list.isEmpty()) {
            lineChartEmployee.visibility = View.GONE
            return
        }
        lineChartEmployee.visibility = View.VISIBLE
        lineChartEmployee.setData(list)
    }

    // ─────────────────────────────────────────────────────────
    // ④ 일별 추이 Bar Chart
    // ─────────────────────────────────────────────────────────
    private fun renderDailyChart(trends: List<DailyTrend>) {
        barChartDaily.setData(trends)
    }

    private fun setupDailyPeriodTabs() {
        btnPeriod1w.setOnClickListener { selectPeriod(0) }
        btnPeriod2w.setOnClickListener { selectPeriod(1) }
        btnPeriod1m.setOnClickListener { selectPeriod(2) }

        btnPeriod1w.addOnLayoutChangeListener(object : View.OnLayoutChangeListener {
            override fun onLayoutChange(
                v: View, left: Int, top: Int, right: Int, bottom: Int,
                oldLeft: Int, oldTop: Int, oldRight: Int, oldBottom: Int
            ) {
                val btnWidth = btnPeriod1w.width
                if (btnWidth > 0) {
                    btnPeriod1w.removeOnLayoutChangeListener(this)
                    val lp = vSegmentSelector.layoutParams
                    lp.width = btnWidth
                    vSegmentSelector.layoutParams = lp
                    vSegmentSelector.translationX = when (currentPeriodIndex) {
                        0 -> btnPeriod1w.left.toFloat()
                        1 -> btnPeriod2w.left.toFloat()
                        else -> btnPeriod1m.left.toFloat()
                    }
                }
            }
        })
    }

    private fun selectPeriod(index: Int) {
        if (currentPeriodIndex == index) return
        currentPeriodIndex = index

        // 1. Sliding animation for selector background View
        val targetX = when (index) {
            0 -> btnPeriod1w.left.toFloat()
            1 -> btnPeriod2w.left.toFloat()
            else -> btnPeriod1m.left.toFloat()
        }
        vSegmentSelector.animate()
            .translationX(targetX)
            .setDuration(250)
            .setInterpolator(android.view.animation.DecelerateInterpolator())
            .start()

        // 2. Active/Inactive text color switching
        val activeColor = Color.parseColor("#3B82F6")
        val inactiveColor = Color.parseColor("#64748B")

        btnPeriod1w.setTextColor(if (index == 0) activeColor else inactiveColor)
        btnPeriod2w.setTextColor(if (index == 1) activeColor else inactiveColor)
        btnPeriod1m.setTextColor(if (index == 2) activeColor else inactiveColor)

        // 3. Re-render daily bar chart with updated list size
        updateDailyChartByPeriod()
    }

    private fun updateDailyChartByPeriod() {
        if (fullDailyTrends.isEmpty()) return

        val count = when (currentPeriodIndex) {
            0 -> 6   // 1주
            1 -> 14  // 2주
            else -> 30 // 한달
        }

        // 1. Determine base anchor date (the latest date in fullDailyTrends, fallback to today)
        val sortedTrends = fullDailyTrends.sortedBy { it.date }
        val lastItem = sortedTrends.lastOrNull()
        val anchorDate = if (lastItem != null) {
            try {
                LocalDate.parse(lastItem.date)
            } catch (e: Exception) {
                LocalDate.now()
            }
        } else {
            LocalDate.now()
        }

        val formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd", Locale.getDefault())

        // 2. Generate target consecutive dates backward from anchorDate
        val targetDates = (0 until count).map { offset ->
            anchorDate.minusDays(offset.toLong())
        }.reversed()

        // 3. Convert to map for quick lookup
        val trendMap = fullDailyTrends.associateBy { it.date }

        // 4. Map dates to DailyTrend objects, padding missing dates with 0 count
        val processedData = targetDates.map { date ->
            val dateStr = date.format(formatter)
            trendMap[dateStr] ?: DailyTrend(
                date = dateStr,
                count = 0
            )
        }

        renderDailyChart(processedData)
    }

    // ─────────────────────────────────────────────────────────
    // ⑤ 웹사이트 Top 5
    // ─────────────────────────────────────────────────────────
    private fun renderTopWebsites(list: List<TopWebsite>) {
        llTopWebsites.removeAllViews()
        if (list.isEmpty()) {
            addEmptyView(llTopWebsites, "데이터 없음")
            return
        }
        val maxCount = list.maxOfOrNull { it.count }?.coerceAtLeast(1) ?: 1
        list.forEachIndexed { idx, site ->
            val row = LayoutInflater.from(requireContext())
                .inflate(R.layout.item_counsel_rank_bar_row, llTopWebsites, false)

            row.findViewById<TextView>(R.id.tv_rank).text  = "${idx + 1}"
            row.findViewById<TextView>(R.id.tv_name).text  = site.webTitle
            row.findViewById<TextView>(R.id.tv_count).text = "${site.count}건"

            val vBar   = row.findViewById<View>(R.id.v_rank_bar)
            val vBarBg = row.findViewById<View>(R.id.v_rank_bar_bg)
            val ratio  = site.count.toFloat() / maxCount

            vBarBg.post {
                val lp = vBar.layoutParams
                lp.width = (vBarBg.width * ratio).toInt().coerceAtLeast(4)
                vBar.layoutParams = lp
            }

            llTopWebsites.addView(row)
        }
    }

    // ─────────────────────────────────────────────────────────
    // ⑥ 시간대별 분포
    // ─────────────────────────────────────────────────────────
    private fun renderHourlyChart(list: List<HourlyDistribution>) {
        lineChartHourly.setData(list)
    }

    // ─────────────────────────────────────────────────────────
    // ⑦ 예정된 예약
    // ─────────────────────────────────────────────────────────
    private fun renderUpcomingReservations(list: List<UpcomingReservation>) {
        llUpcomingReservations.removeAllViews()
        if (list.isEmpty()) {
            addEmptyView(llUpcomingReservations, "예정된 예약 없음")
            return
        }
        list.forEach { reservation ->
            val row = LayoutInflater.from(requireContext())
                .inflate(R.layout.item_counsel_reservation, llUpcomingReservations, false)

            row.findViewById<TextView>(R.id.tv_res_name).text   = reservation.name
            row.findViewById<TextView>(R.id.tv_res_phone).text  = reservation.counselHp
            row.findViewById<TextView>(R.id.tv_res_emp).text    = "담당: ${reservation.empName ?: "미배정"}"
            row.findViewById<TextView>(R.id.tv_res_status).text = reservation.statusName

            // 날짜 포맷팅
            val dtmText = try {
                val zdt = ZonedDateTime.parse(reservation.counselResvDtm)
                val fmt = DateTimeFormatter.ofPattern("MM/dd HH:mm", Locale.KOREAN)
                zdt.format(fmt)
            } catch (e: Exception) {
                reservation.counselResvDtm.take(16).replace("T", " ")
            }
            row.findViewById<TextView>(R.id.tv_res_datetime).text = dtmText

            llUpcomingReservations.addView(row)
        }
    }

    // ─────────────────────────────────────────────────────────
    // 공통 — 빈 데이터 안내
    // ─────────────────────────────────────────────────────────
    private fun addEmptyView(container: LinearLayout, message: String) {
        val tv = TextView(requireContext()).apply {
            text      = message
            textSize  = 13f
            setTextColor(ContextCompat.getColor(requireContext(), R.color.gray_text))
            val pad = (8 * resources.displayMetrics.density).toInt()
            setPadding(0, pad, 0, pad)
        }
        container.addView(tv)
    }

    private fun updateDateTextViews() {
        if (!isDateFilterApplied) {
            tvDateRange.text = "전체 기간"
        } else {
            val start = selectedStartDate.format(displayFormatter)
            val end = selectedEndDate.format(displayFormatter)
            tvDateRange.text = "$start ~ $end"
        }
    }

    private fun setupDatePickerListeners() {
        btnDateFilter.setOnClickListener { showDatePickerFlow() }
    }

    private fun showDatePickerFlow() {
        val customCalendar = CustomCalendarDialogFragment()
        customCalendar.setInitialRange(selectedStartDate, selectedEndDate)
        customCalendar.setOnDateRangeSelectedListener { start, end ->
            selectedStartDate = start
            selectedEndDate = end
            isDateFilterApplied = true
            updateDateTextViews()
            reloadDashboardData()
        }
        customCalendar.show(childFragmentManager, "custom_calendar_dialog")
    }

    private fun reloadDashboardData() {
        viewModel.loadDashboard(
            startDate = selectedStartDate.format(dateFormatter),
            endDate = selectedEndDate.format(dateFormatter)
        )
    }

    companion object {
        fun newInstance() = CounselDashboardFragment()
    }
}
