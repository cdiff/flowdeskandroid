package com.example.flowdesk_android.feature.counsel_management.presentation.list

import android.app.DatePickerDialog
import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.graphics.Color
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import android.widget.ProgressBar
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.widget.PopupMenu
import androidx.core.widget.NestedScrollView
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.flowdesk_android.R
import com.example.flowdesk_android.feature.counsel_management.domain.model.CounselItem
import com.example.flowdesk_android.feature.counsel_management.domain.model.EmployeeStat
import com.example.flowdesk_android.feature.counsel_management.domain.model.TopWebsite
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.util.Locale

@AndroidEntryPoint
class CounselListFragment : Fragment() {

    private val viewModel: CounselListViewModel by viewModels()

    // Adapters
    private lateinit var statusAdapter: CounselStatusAdapter
    private lateinit var counselAdapter: CounselListAdapter

    // Views
    private lateinit var layoutTabTotal: View
    private lateinit var tvStatusTotalCountBadge: TextView
    private lateinit var rvStatusTabs: RecyclerView
    private lateinit var etSearch: EditText
    private lateinit var btnFilterDate: View
    private lateinit var tvFilterDate: TextView
    private lateinit var btnFilterManager: View
    private lateinit var tvFilterManager: TextView
    private lateinit var btnFilterWebsite: View
    private lateinit var tvFilterWebsite: TextView
    private lateinit var tvCounselCount: TextView
    private lateinit var rvCounsels: RecyclerView
    private lateinit var progressBar: ProgressBar
    private lateinit var llEmpty: View
    private lateinit var nestedScrollView: NestedScrollView

    // Dynamic Lists for Popup Filters
    private val employeeList = mutableListOf<EmployeeStat>()
    private val websiteList = mutableListOf<TopWebsite>()

    // Search Job
    private var searchJob: Job? = null

    // Date formatting
    private val dateFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd", Locale.getDefault())
    private val displayFormatter = DateTimeFormatter.ofPattern("yyyy.MM.dd", Locale.getDefault())

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? = inflater.inflate(R.layout.fragment_counsel_list, container, false)

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        bindViews(view)
        setupAdapters()
        setupListeners()
        observeState()
    }

    private fun bindViews(view: View) {
        layoutTabTotal = view.findViewById(R.id.layout_tab_total)
        tvStatusTotalCountBadge = view.findViewById(R.id.tv_status_total_count_badge)
        rvStatusTabs = view.findViewById(R.id.rv_status_tabs)
        etSearch = view.findViewById(R.id.et_search)
        btnFilterDate = view.findViewById(R.id.btn_filter_date)
        tvFilterDate = view.findViewById(R.id.tv_filter_date)
        btnFilterManager = view.findViewById(R.id.btn_filter_manager)
        tvFilterManager = view.findViewById(R.id.tv_filter_manager)
        btnFilterWebsite = view.findViewById(R.id.btn_filter_website)
        tvFilterWebsite = view.findViewById(R.id.tv_filter_website)
        tvCounselCount = view.findViewById(R.id.tv_counsel_count)
        rvCounsels = view.findViewById(R.id.rv_counsels)
        progressBar = view.findViewById(R.id.progress_bar)
        llEmpty = view.findViewById(R.id.ll_empty)
        nestedScrollView = view.findViewById(R.id.nested_scroll_view)
    }

    private fun setupAdapters() {
        // Horizontal Status Tab Adapter
        statusAdapter = CounselStatusAdapter { status ->
            if (status == null) {
                viewModel.updateStatusFilter(null)
            } else {
                setTotalTabStyle(isSelected = false)
                viewModel.updateStatusFilter(status.counselStat)
            }
        }
        rvStatusTabs.layoutManager = LinearLayoutManager(requireContext(), LinearLayoutManager.HORIZONTAL, false)
        rvStatusTabs.adapter = statusAdapter

        // Vertical Counsel List Adapter
        counselAdapter = CounselListAdapter(
            onCopyClick = { phone ->
                val clipboard = requireContext().getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                val clip = ClipData.newPlainText("전화번호", phone)
                clipboard.setPrimaryClip(clip)
                Toast.makeText(requireContext(), "전화번호가 복사되었습니다.", Toast.LENGTH_SHORT).show()
            },
            onOptionsClick = { item, anchorView ->
                showItemOptionsMenu(item, anchorView)
            }
        )
        rvCounsels.layoutManager = LinearLayoutManager(requireContext())
        rvCounsels.adapter = counselAdapter
    }

    private fun setupListeners() {
        // "Total" Tab Click listener
        layoutTabTotal.setOnClickListener {
            statusAdapter.clearSelection() // Unselect others
            setTotalTabStyle(isSelected = true)
            viewModel.updateStatusFilter(null)
        }

        // Search text change listener (Debounced)
        etSearch.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                searchJob?.cancel()
                searchJob = viewLifecycleOwner.lifecycleScope.launch {
                    delay(300)
                    viewModel.updateSearchQuery(s?.toString())
                }
            }
            override fun afterTextChanged(s: Editable?) {}
        })

        // Infinite Scroll listener on NestedScrollView
        nestedScrollView.setOnScrollChangeListener(NestedScrollView.OnScrollChangeListener { v, _, scrollY, _, _ ->
            val contentHeight = v.getChildAt(0).measuredHeight
            val scrollHeight = v.measuredHeight
            if (scrollY >= contentHeight - scrollHeight - 100) {
                viewModel.loadMore()
            }
        })

        // Date Filter menu click
        btnFilterDate.setOnClickListener { view ->
            showDateFilterMenu(view)
        }

        // Manager Filter menu click
        btnFilterManager.setOnClickListener { view ->
            showManagerFilterMenu(view)
        }

        // Website Filter menu click
        btnFilterWebsite.setOnClickListener { view ->
            showWebsiteFilterMenu(view)
        }
    }

    private fun setTotalTabStyle(isSelected: Boolean) {
        val countBadge = tvStatusTotalCountBadge
        val card = countBadge.parent as? com.google.android.material.card.MaterialCardView
        if (isSelected) {
            card?.setCardBackgroundColor(Color.parseColor("#1E293B"))
            countBadge.setTextColor(Color.WHITE)
        } else {
            card?.setCardBackgroundColor(Color.WHITE)
            card?.strokeWidth = (1.5 * requireContext().resources.displayMetrics.density).toInt()
            card?.strokeColor = Color.parseColor("#E2E8F0")
            countBadge.setTextColor(Color.parseColor("#374151"))
        }
    }

    private fun showItemOptionsMenu(item: CounselItem, view: View) {
        val popup = PopupMenu(requireContext(), view)
        popup.menu.add("상태 변경")
        popup.menu.add("담당자 지정")
        popup.menu.add("삭제")
        popup.setOnMenuItemClickListener { menuItem ->
            Toast.makeText(requireContext(), "${menuItem.title} - 준비 중인 기능입니다.", Toast.LENGTH_SHORT).show()
            true
        }
        popup.show()
    }

    private fun showDateFilterMenu(anchorView: View) {
        val popup = PopupMenu(requireContext(), anchorView)
        popup.menu.add(0, 1, 0, "전체 기간")
        popup.menu.add(0, 2, 0, "오늘")
        popup.menu.add(0, 3, 0, "최근 7일")
        popup.menu.add(0, 4, 0, "직접 선택")
        popup.setOnMenuItemClickListener { menuItem ->
            when (menuItem.itemId) {
                1 -> {
                    tvFilterDate.text = "전체 기간"
                    viewModel.updateDateFilter(null, null)
                }
                2 -> {
                    val today = LocalDate.now().format(dateFormatter)
                    tvFilterDate.text = "오늘"
                    viewModel.updateDateFilter(today, today)
                }
                3 -> {
                    val start = LocalDate.now().minusDays(7).format(dateFormatter)
                    val end = LocalDate.now().format(dateFormatter)
                    tvFilterDate.text = "최근 7일"
                    viewModel.updateDateFilter(start, end)
                }
                4 -> {
                    showCustomDatePickerFlow()
                }
            }
            true
        }
        popup.show()
    }

    private fun showCustomDatePickerFlow() {
        val now = LocalDate.now()
        DatePickerDialog(
            requireContext(),
            { _, year, month, dayOfMonth ->
                val startDate = LocalDate.of(year, month + 1, dayOfMonth)
                DatePickerDialog(
                    requireContext(),
                    { _, endYear, endMonth, endDayOfMonth ->
                        val endDate = LocalDate.of(endYear, endMonth + 1, endDayOfMonth)
                        if (startDate.isAfter(endDate)) {
                            Toast.makeText(requireContext(), "시작일은 종료일보다 이전이어야 합니다.", Toast.LENGTH_SHORT).show()
                        } else {
                            val startStr = startDate.format(dateFormatter)
                            val endStr = endDate.format(dateFormatter)
                            tvFilterDate.text = "${startDate.format(displayFormatter)} ~ ${endDate.format(displayFormatter)}"
                            viewModel.updateDateFilter(startStr, endStr)
                        }
                    },
                    now.year, now.monthValue - 1, now.dayOfMonth
                ).apply {
                    setTitle("종료일 선택")
                    show()
                }
            },
            now.year, now.monthValue - 1, now.dayOfMonth
        ).apply {
            setTitle("시작일 선택")
            show()
        }
    }

    private fun showManagerFilterMenu(anchorView: View) {
        val popup = PopupMenu(requireContext(), anchorView)
        popup.menu.add(0, 0, 0, "담당자: 전체")
        employeeList.forEachIndexed { index, emp ->
            popup.menu.add(0, emp.empSeq, index + 1, emp.empName)
        }
        popup.setOnMenuItemClickListener { menuItem ->
            if (menuItem.itemId == 0) {
                tvFilterManager.text = "담당자: 전체"
                viewModel.updateManagerFilter(null)
            } else {
                tvFilterManager.text = "담당자: ${menuItem.title}"
                viewModel.updateManagerFilter(menuItem.itemId)
            }
            true
        }
        popup.show()
    }

    private fun showWebsiteFilterMenu(anchorView: View) {
        val popup = PopupMenu(requireContext(), anchorView)
        popup.menu.add(0, 0, 0, "웹사이트: 전체")
        websiteList.forEachIndexed { index, web ->
            popup.menu.add(0, index + 1, index + 1, web.webTitle)
        }
        popup.setOnMenuItemClickListener { menuItem ->
            if (menuItem.itemId == 0) {
                tvFilterWebsite.text = "웹사이트: 전체"
                viewModel.updateWebsiteFilter(null)
            } else {
                val selectedWeb = websiteList[menuItem.itemId - 1]
                tvFilterWebsite.text = "웹사이트: ${selectedWeb.webTitle}"
                viewModel.updateWebsiteFilter(selectedWeb.webCode)
            }
            true
        }
        popup.show()
    }

    private fun observeState() {
        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                
                // Observe Counsel List UI State
                launch {
                    viewModel.uiState.collectLatest { state ->
                        when (state) {
                            is CounselListUiState.Loading -> {
                                progressBar.visibility = View.VISIBLE
                                llEmpty.visibility = View.GONE
                            }
                            is CounselListUiState.Success -> {
                                progressBar.visibility = View.GONE
                                counselAdapter.submitList(state.items)
                                tvCounselCount.text = "총 ${state.totalCount}건"
                                if (state.items.isEmpty()) {
                                    llEmpty.visibility = View.VISIBLE
                                    rvCounsels.visibility = View.GONE
                                } else {
                                    llEmpty.visibility = View.GONE
                                    rvCounsels.visibility = View.VISIBLE
                                }
                            }
                            is CounselListUiState.Error -> {
                                progressBar.visibility = View.GONE
                                llEmpty.visibility = View.VISIBLE
                                Toast.makeText(requireContext(), state.message, Toast.LENGTH_SHORT).show()
                            }
                        }
                    }
                }

                // Observe Status Counts for circular tabs
                launch {
                    viewModel.statusCounts.collectLatest { list ->
                        statusAdapter.submitList(list)
                        val totalSum = list.sumOf { it.count }
                        tvStatusTotalCountBadge.text = totalSum.toString()
                    }
                }

                // Observe Manager List to refresh dropdown menu options
                launch {
                    viewModel.employeeList.collectLatest { list ->
                        employeeList.clear()
                        employeeList.addAll(list)
                    }
                }

                // Observe Website List to refresh dropdown menu options
                launch {
                    viewModel.websiteList.collectLatest { list ->
                        websiteList.clear()
                        websiteList.addAll(list)
                    }
                }
            }
        }
    }
}
