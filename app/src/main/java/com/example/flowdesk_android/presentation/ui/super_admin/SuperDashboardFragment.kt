package com.example.flowdesk_android.presentation.ui.super_admin

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import com.example.flowdesk_android.R
import com.example.flowdesk_android.data.remote.dto.SuperDashboardResponse
import com.example.flowdesk_android.data.remote.dto.TenantStatDto
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch

@AndroidEntryPoint
class SuperDashboardFragment : Fragment() {

    private val viewModel: SuperDashboardViewModel by viewModels()

    // ── Overview
    private lateinit var tvTotalTenants: TextView
    private lateinit var tvActiveTenants: TextView
    private lateinit var tvTotalUsers: TextView
    private lateinit var tvActiveUsers: TextView
    private lateinit var tvTotalCounsels: TextView
    private lateinit var tvTotalPosts: TextView
    private lateinit var tvTotalRoles: TextView
    private lateinit var tvTotalPermissions: TextView

    // ── Today
    private lateinit var tvNewUsers: TextView
    private lateinit var tvNewCounsels: TextView
    private lateinit var tvNewPosts: TextView
    private lateinit var tvActiveSessions: TextView

    // ── Security
    private lateinit var tvBlockedIps: TextView
    private lateinit var tvBlockedHps: TextView
    private lateinit var tvBlockedWords: TextView

    // ── Chart
    private lateinit var lineChart: com.github.mikephil.charting.charts.LineChart

    // ── Containers
    private lateinit var llTenantStats: LinearLayout
    private lateinit var progressBar: View

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_super_dashboard, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        bindViews(view)
        observeState()
    }

    private fun bindViews(view: View) {
        tvTotalTenants     = view.findViewById(R.id.tv_total_tenants)
        tvActiveTenants    = view.findViewById(R.id.tv_active_tenants_label)
        tvTotalUsers       = view.findViewById(R.id.tv_total_users)
        tvActiveUsers      = view.findViewById(R.id.tv_active_users_label)
        tvTotalCounsels    = view.findViewById(R.id.tv_total_counsels)
        tvTotalPosts       = view.findViewById(R.id.tv_total_posts)
        tvTotalRoles       = view.findViewById(R.id.tv_total_roles)
        tvTotalPermissions = view.findViewById(R.id.tv_total_permissions)

        tvNewUsers       = view.findViewById(R.id.tv_new_users)
        tvNewCounsels    = view.findViewById(R.id.tv_new_counsels)
        tvNewPosts       = view.findViewById(R.id.tv_new_posts)
        tvActiveSessions = view.findViewById(R.id.tv_active_sessions)

        tvBlockedIps   = view.findViewById(R.id.tv_blocked_ips)
        tvBlockedHps   = view.findViewById(R.id.tv_blocked_hps)
        tvBlockedWords = view.findViewById(R.id.tv_blocked_words)

        lineChart = view.findViewById(R.id.line_chart)

        llTenantStats = view.findViewById(R.id.ll_tenant_stats)
        progressBar   = view.findViewById(R.id.progress_bar)
    }

    private fun observeState() {
        viewLifecycleOwner.lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.uiState.collect { state ->
                    when (state) {
                        is DashboardUiState.Loading -> showLoading(true)
                        is DashboardUiState.Success -> {
                            showLoading(false)
                            bindData(state.data)
                        }
                        is DashboardUiState.Error -> {
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
    }

    private fun bindData(data: SuperDashboardResponse) {
        // ── Overview
        with(data.overview) {
            tvTotalTenants.text     = totalTenants.toString()
            tvActiveTenants.text    = "활성 $activeTenants"
            tvTotalUsers.text       = totalUsers.toString()
            tvActiveUsers.text      = "활성 $activeUsers"
            tvTotalCounsels.text    = totalCounsels.toString()
            tvTotalPosts.text       = totalPosts.toString()
            tvTotalRoles.text       = totalRoles.toString()
            tvTotalPermissions.text = totalPermissions.toString()
        }

        // ── Today
        with(data.today) {
            tvNewUsers.text       = newUsers.toString()
            tvNewCounsels.text    = newCounsels.toString()
            tvNewPosts.text       = newPosts.toString()
            tvActiveSessions.text = activeSessions.toString()
        }

        // ── Security
        with(data.security) {
            tvBlockedIps.text   = totalBlockedIps.toString()
            tvBlockedHps.text   = totalBlockedHps.toString()
            tvBlockedWords.text = totalBlockedWords.toString()
        }

        // ── Chart 데이터 렌더링 (X, Y축 커스텀 디자인 포함)
        renderLineChart(data.monthlyTrends)

        llTenantStats.removeAllViews()
        data.tenantStats.forEach { tenant ->
            val cardView = LayoutInflater.from(requireContext())
                .inflate(R.layout.item_tenant_stat_card, llTenantStats, false)

            cardView.findViewById<TextView>(R.id.tv_tenant_name).text    = tenant.tenantName
            cardView.findViewById<TextView>(R.id.tv_tenant_id).text      = tenant.tenantId.toString()
            cardView.findViewById<TextView>(R.id.tv_tenant_session).text = tenant.activeSessionCount.toString()
            cardView.findViewById<TextView>(R.id.tv_tenant_users).text   = tenant.userCount.toString()
            cardView.findViewById<TextView>(R.id.tv_tenant_posts).text   = tenant.postCount.toString()
            cardView.findViewById<TextView>(R.id.tv_tenant_roles).text   = tenant.roleCount.toString()

            val tvStatus = cardView.findViewById<TextView>(R.id.tv_tenant_status)
            if (tenant.isActive == 1) {
                tvStatus.text = "활성"
                tvStatus.setTextColor(ContextCompat.getColor(requireContext(), R.color.green_accent))
                tvStatus.setBackgroundResource(R.drawable.bg_tag_light_green)
            } else {
                tvStatus.text = "비활성"
                tvStatus.setTextColor(ContextCompat.getColor(requireContext(), R.color.gray_text))
                tvStatus.setBackgroundResource(R.drawable.bg_rect_rounded_light_gray)
            }

            llTenantStats.addView(cardView)
        }
    }

    /**
     * MPAndroidChart 라이브러리를 사용해 3개의 지표(상담, 사용자, 테넌트)를 선으로 그림
     */
    private fun renderLineChart(trends: com.example.flowdesk_android.data.remote.dto.MonthlyTrendsDto) {
        val last6Users    = trends.userRegistrations.takeLast(6)
        val last6Counsels = trends.counselRegistrations.takeLast(6)
        val last6Tenants  = trends.tenantRegistrations.takeLast(6)

        // X축 레이블 데이터 구성
        val xLabels = last6Users.map { 
            it.month.substringAfter("-").trimStart('0') + "월"
        }

        val userEntries    = last6Users.mapIndexed    { i, e -> com.github.mikephil.charting.data.Entry(i.toFloat(), e.count.toFloat()) }
        val counselEntries = last6Counsels.mapIndexed { i, e -> com.github.mikephil.charting.data.Entry(i.toFloat(), e.count.toFloat()) }
        val tenantEntries  = last6Tenants.mapIndexed  { i, e -> com.github.mikephil.charting.data.Entry(i.toFloat(), e.count.toFloat()) }

        val setUsers = createChartDataSet(userEntries, "사용자", ContextCompat.getColor(requireContext(), R.color.green_accent), R.drawable.bg_chart_gradient_green)
        val setCounsels = createChartDataSet(counselEntries, "상담", ContextCompat.getColor(requireContext(), R.color.login_blue), R.drawable.bg_chart_gradient_blue)
        val setTenants = createChartDataSet(tenantEntries, "테넌트", ContextCompat.getColor(requireContext(), R.color.red), R.drawable.bg_chart_gradient_red)

        lineChart.data = com.github.mikephil.charting.data.LineData(setCounsels, setUsers, setTenants)
        
        // 차트 디자인 커스텀
        with(lineChart) {
            description.isEnabled = false
            legend.isEnabled = false // 커스텀 하단 범례 사용 중
            
            // X축 (하단 월 레이블)
            xAxis.apply {
                isEnabled = true
                position = com.github.mikephil.charting.components.XAxis.XAxisPosition.BOTTOM
                setDrawGridLines(true)
                enableGridDashedLine(10f, 10f, 0f)
                gridColor = 0xFFE5E7EB.toInt()
                textColor = ContextCompat.getColor(requireContext(), R.color.gray_text)
                textSize = 9f
                setDrawAxisLine(true)
                axisLineColor = 0xFFE5E7EB.toInt()
                granularity = 1f
                setLabelCount(6, true)
                valueFormatter = object : com.github.mikephil.charting.formatter.ValueFormatter() {
                    override fun getAxisLabel(value: Float, axis: com.github.mikephil.charting.components.AxisBase?): String {
                        val index = value.toInt()
                        return if (index in xLabels.indices) xLabels[index] else ""
                    }
                }
            }

            // 좌측 Y축
            axisLeft.apply {
                isEnabled = true
                textColor = ContextCompat.getColor(requireContext(), R.color.gray_text)
                textSize = 9f
                setDrawGridLines(true)
                enableGridDashedLine(10f, 10f, 0f)
                gridColor = 0xFFE5E7EB.toInt()
                setDrawAxisLine(false)
                axisMinimum = 0f
                setLabelCount(5, true) // Y축 레이블 개수 고정
            }
            
            // 우측 Y축
            axisRight.isEnabled = false
            
            setTouchEnabled(true)
            isDragEnabled = false
            setScaleEnabled(false)
            setPinchZoom(false)
            
            // 여백 자동 계산 허용 (기본값)
            setExtraOffsets(0f, 0f, 0f, 10f) // 하단 X축 레이블을 위해 약간의 여백 추가

            animateX(800)
            invalidate()
        }
    }

    private fun createChartDataSet(
        entries: List<com.github.mikephil.charting.data.Entry>, 
        label: String, 
        colorInt: Int, 
        gradientDrawableId: Int
    ): com.github.mikephil.charting.data.LineDataSet {
        return com.github.mikephil.charting.data.LineDataSet(entries, label).apply {
            this.color = colorInt
            this.lineWidth = 2f
            this.setDrawCircles(false) // 점 제거 (Image 2 스타일)
            this.setDrawValues(false)
            this.mode = com.github.mikephil.charting.data.LineDataSet.Mode.CUBIC_BEZIER // 부드러운 곡선
            
            // 그라데이션 채우기 활성화
            this.setDrawFilled(true)
            val drawable = ContextCompat.getDrawable(requireContext(), gradientDrawableId)
            this.fillDrawable = drawable
        }
    }
}
