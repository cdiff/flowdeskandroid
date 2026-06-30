package com.example.flowdesk_android.feature.super_admin.presentation.dashboard

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import com.example.flowdesk_android.R
import com.example.flowdesk_android.core.extension.showTopToast
import com.example.flowdesk_android.databinding.FragmentSuperAdminDashboardBinding
import com.example.flowdesk_android.databinding.ItemSuperAdminTenantStatCardBinding
import com.example.flowdesk_android.feature.super_admin.domain.model.DashboardStats
import com.example.flowdesk_android.feature.super_admin.domain.model.MonthlyTrends
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch

@AndroidEntryPoint
class SuperDashboardFragment : Fragment() {

    private val viewModel: SuperDashboardViewModel by viewModels()

    private var _binding: FragmentSuperAdminDashboardBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentSuperAdminDashboardBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        observeState()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    private fun observeState() {
        viewLifecycleOwner.lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.uiState.collect { state ->
                    when (state) {
                        is SuperDashboardUiState.Loading -> showLoading(true)
                        is SuperDashboardUiState.Success -> {
                            showLoading(false)
                            bindData(state.stats)
                        }
                        is SuperDashboardUiState.Error -> {
                            showLoading(false)
                            showTopToast(state.message)
                        }
                    }
                }
            }
        }
    }

    private fun showLoading(isLoading: Boolean) {
        binding.progressBar.visibility = if (isLoading) View.VISIBLE else View.GONE
    }

    private fun bindData(data: DashboardStats) {
        with(binding) {
            with(data.overview) {
                layoutOverview.tvTotalTenants.text     = totalTenants.toString()
                layoutOverview.tvActiveTenantsLabel.text = getString(R.string.label_status_active_short, activeTenants.toString())
                layoutOverview.tvTotalUsers.text       = totalUsers.toString()
                layoutOverview.tvActiveUsersLabel.text  = getString(R.string.label_status_active_short, activeUsers.toString())
                layoutOverview.tvTotalCounsels.text    = totalCounsels.toString()
                layoutOverview.tvTotalPosts.text       = totalPosts.toString()
                layoutOverview.tvTotalRoles.text       = totalRoles.toString()
                layoutOverview.tvTotalPermissions.text = totalPermissions.toString()
            }

            with(data.today) {
                layoutToday.tvNewUsers.text       = newUsers.toString()
                layoutToday.tvNewCounsels.text    = newCounsels.toString()
                layoutToday.tvNewPosts.text       = newPosts.toString()
                layoutToday.tvActiveSessions.text = activeSessions.toString()
            }

            with(data.security) {
                layoutSecurity.tvBlockedIps.text   = totalBlockedIps.toString()
                layoutSecurity.tvBlockedHps.text   = totalBlockedHps.toString()
                layoutSecurity.tvBlockedWords.text = totalBlockedWords.toString()
            }
        }

        renderLineChart(data.monthlyTrends)

        binding.llTenantStats.removeAllViews()
        data.tenantStats.forEach { tenant ->
            val cardBinding = ItemSuperAdminTenantStatCardBinding.inflate(
                LayoutInflater.from(requireContext()),
                binding.llTenantStats,
                false
            )

            cardBinding.tvTenantName.text    = tenant.tenantName
            cardBinding.tvTenantId.text      = tenant.tenantId.toString()
            cardBinding.tvTenantSession.text = tenant.activeSessionCount.toString()
            cardBinding.tvTenantUsers.text   = tenant.userCount.toString()
            cardBinding.tvTenantPosts.text   = tenant.postCount.toString()
            cardBinding.tvTenantRoles.text   = tenant.roleCount.toString()

            if (tenant.isActive) {
                cardBinding.tvTenantStatus.text = getString(R.string.label_status_active)
                cardBinding.tvTenantStatus.setTextColor(ContextCompat.getColor(requireContext(), R.color.color_success))
                cardBinding.tvTenantStatus.setBackgroundResource(R.drawable.bg_tag_light_green)
            } else {
                cardBinding.tvTenantStatus.text = getString(R.string.label_status_inactive)
                cardBinding.tvTenantStatus.setTextColor(ContextCompat.getColor(requireContext(), R.color.text_secondary))
                cardBinding.tvTenantStatus.setBackgroundResource(R.drawable.bg_rect_rounded_light_gray)
            }

            binding.llTenantStats.addView(cardBinding.root)
        }
    }

    private fun renderLineChart(trends: MonthlyTrends) {
        val last6Users    = trends.userRegistrations.takeLast(6)
        val last6Counsels = trends.counselRegistrations.takeLast(6)
        val last6Tenants  = trends.tenantRegistrations.takeLast(6)

        val xLabels = last6Users.map { 
            val monthVal = it.month.substringAfter("-").trimStart('0')
            getString(R.string.chart_label_month_suffix, monthVal)
        }

        val userEntries    = last6Users.mapIndexed    { i, e -> com.github.mikephil.charting.data.Entry(i.toFloat(), e.count.toFloat()) }
        val counselEntries = last6Counsels.mapIndexed { i, e -> com.github.mikephil.charting.data.Entry(i.toFloat(), e.count.toFloat()) }
        val tenantEntries  = last6Tenants.mapIndexed  { i, e -> com.github.mikephil.charting.data.Entry(i.toFloat(), e.count.toFloat()) }

        val setUsers = createChartDataSet(
            userEntries,
            getString(R.string.chart_label_user),
            ContextCompat.getColor(requireContext(), R.color.color_success_active),
            R.drawable.bg_chart_gradient_green
        )
        val setCounsels = createChartDataSet(
            counselEntries,
            getString(R.string.chart_label_counsel),
            ContextCompat.getColor(requireContext(), R.color.brand_primary),
            R.drawable.bg_chart_gradient_blue
        )
        val setTenants = createChartDataSet(
            tenantEntries,
            getString(R.string.chart_label_tenant),
            ContextCompat.getColor(requireContext(), R.color.color_error),
            R.drawable.bg_chart_gradient_red
        )

        binding.layoutChart.lineChart.data = com.github.mikephil.charting.data.LineData(setCounsels, setUsers, setTenants)
        
        with(binding.layoutChart.lineChart) {
            description.isEnabled = false
            legend.isEnabled = false
            
            xAxis.apply {
                isEnabled = true
                position = com.github.mikephil.charting.components.XAxis.XAxisPosition.BOTTOM
                setDrawGridLines(true)
                enableGridDashedLine(10f, 10f, 0f)
                gridColor = ContextCompat.getColor(requireContext(), R.color.bg_divider)
                textColor = ContextCompat.getColor(requireContext(), R.color.text_secondary)
                textSize = 9f
                setDrawAxisLine(true)
                axisLineColor = ContextCompat.getColor(requireContext(), R.color.bg_divider)
                granularity = 1f
                setLabelCount(6, true)
                valueFormatter = object : com.github.mikephil.charting.formatter.ValueFormatter() {
                    override fun getAxisLabel(value: Float, axis: com.github.mikephil.charting.components.AxisBase?): String {
                        val index = value.toInt()
                        return if (index in xLabels.indices) xLabels[index] else ""
                    }
                }
            }

            axisLeft.apply {
                isEnabled = true
                textColor = ContextCompat.getColor(requireContext(), R.color.text_secondary)
                textSize = 9f
                setDrawGridLines(true)
                enableGridDashedLine(10f, 10f, 0f)
                gridColor = ContextCompat.getColor(requireContext(), R.color.bg_divider)
                setDrawAxisLine(false)
                axisMinimum = 0f
                setLabelCount(5, true)
            }
            
            axisRight.isEnabled = false
            
            setTouchEnabled(true)
            isDragEnabled = false
            setScaleEnabled(false)
            setPinchZoom(false)
            
            setExtraOffsets(0f, 0f, 0f, 10f)

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
            this.setDrawCircles(false)
            this.setDrawValues(false)
            this.mode = com.github.mikephil.charting.data.LineDataSet.Mode.CUBIC_BEZIER
            
            this.setDrawFilled(true)
            val drawable = ContextCompat.getDrawable(requireContext(), gradientDrawableId)
            this.fillDrawable = drawable
        }
    }
}
