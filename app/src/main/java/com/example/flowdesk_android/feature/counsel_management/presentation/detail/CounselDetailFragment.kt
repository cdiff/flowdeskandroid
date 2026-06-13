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
import com.example.flowdesk_android.feature.counsel_management.domain.model.CounselDetail
import com.example.flowdesk_android.feature.counsel_management.domain.model.CounselStatusStat
import com.example.flowdesk_android.feature.counsel_management.domain.model.EmployeeStat
import com.google.android.material.tabs.TabLayout
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch

@AndroidEntryPoint
class CounselDetailFragment : Fragment() {

    private val viewModel: CounselDetailViewModel by viewModels()

    // Views
    private lateinit var btnBack: View

    private lateinit var tvName: TextView
    private lateinit var tvStatusTag: TextView
    private lateinit var tvPhone: TextView
    private lateinit var tvWebsite: TextView
    private lateinit var tvIp: TextView
    private lateinit var tvManager: TextView
    private lateinit var tvCreatedAt: TextView
    private lateinit var tvUpdatedAt: TextView

    // 커스텀 스피너 버튼 뷰
    private lateinit var spinnerStatusAnchor: View
    private lateinit var tvStatusSelected: TextView
    private lateinit var vStatusDot: CardView

    private lateinit var spinnerManagerAnchor: View
    private lateinit var tvManagerSelected: TextView

    private lateinit var tabLayout: TabLayout
    private lateinit var tabContentContainer: ViewGroup

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
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? = inflater.inflate(R.layout.fragment_counsel_detail, container, false)

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        bindViews(view)

        // Handle window insets for status bar
        val toolbarLayout = view.findViewById<View>(R.id.layout_toolbar)
        androidx.core.view.ViewCompat.setOnApplyWindowInsetsListener(toolbarLayout) { v, insets ->
            val systemBars = insets.getInsets(androidx.core.view.WindowInsetsCompat.Type.systemBars())
            v.setPadding(v.paddingLeft, systemBars.top, v.paddingRight, v.paddingBottom)
            insets
        }

        setupTabLayout()
        setupListeners()
        observeViewModel()

        val counselSeq = arguments?.getInt("counselSeq") ?: -1
        viewModel.init(counselSeq)
    }

    private fun bindViews(view: View) {
        btnBack = view.findViewById(R.id.btn_back)

        tvName = view.findViewById(R.id.tv_detail_name)
        tvStatusTag = view.findViewById(R.id.tv_detail_status_tag)
        tvPhone = view.findViewById(R.id.tv_detail_phone)
        tvWebsite = view.findViewById(R.id.tv_detail_website)
        tvIp = view.findViewById(R.id.tv_detail_ip)
        tvManager = view.findViewById(R.id.tv_detail_manager)
        tvCreatedAt = view.findViewById(R.id.tv_detail_created_at)
        tvUpdatedAt = view.findViewById(R.id.tv_detail_updated_at)

        // 커스텀 스피너 버튼 뷰 바인딩
        spinnerStatusAnchor = view.findViewById(R.id.spinner_status)
        tvStatusSelected = view.findViewById(R.id.tv_status_selected)
        vStatusDot = view.findViewById(R.id.v_status_dot)

        spinnerManagerAnchor = view.findViewById(R.id.spinner_manager)
        tvManagerSelected = view.findViewById(R.id.tv_manager_selected)

        tabLayout = view.findViewById(R.id.tab_layout)
        tabContentContainer = view.findViewById(R.id.tab_content_container)
    }

    private fun setupTabLayout() {
        replaceTabContent(0)

        tabLayout.addOnTabSelectedListener(object : TabLayout.OnTabSelectedListener {
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
        btnBack.setOnClickListener {
            findNavController().navigateUp()
        }

        spinnerStatusAnchor.setOnClickListener {
            statusPopup?.let { if (it.isShowing) { it.dismiss(); return@setOnClickListener } }
            showStatusPopup()
        }

        spinnerManagerAnchor.setOnClickListener {
            managerPopup?.let { if (it.isShowing) { it.dismiss(); return@setOnClickListener } }
            showManagerPopup()
        }
    }

    // ── 상태 변경 팝업 ──────────────────────────────────────────────────────────

    private fun showStatusPopup() {
        if (statusList.isEmpty()) return

        val popup = ListPopupWindow(requireContext())
        popup.anchorView = spinnerStatusAnchor

        // ① 너비 1:1 동기화
        popup.width = spinnerStatusAnchor.width
        // ② 무조건 아래 방향으로만
        popup.isModal = true
        popup.setDropDownGravity(android.view.Gravity.START)
        // ③ 스피너와 겹치지 않는 정렬 — yOffset=0 이면 앵커 바로 밑에 붙음
        popup.verticalOffset = 0
        popup.horizontalOffset = 0

        popup.setBackgroundDrawable(
            androidx.core.content.ContextCompat.getDrawable(requireContext(), R.drawable.bg_spinner_popup)
        )
        popup.height = ListPopupWindow.WRAP_CONTENT

        val adapter = StatusDropdownAdapter(requireContext(), statusList, selectedStatusIndex)
        popup.setAdapter(adapter)

        popup.setOnItemClickListener { _, _, position, _ ->
            val selected = statusList.getOrNull(position) ?: return@setOnItemClickListener
            val current = currentDetail ?: return@setOnItemClickListener
            if (selected.counselStat != current.counselStat) {
                // 서버 스펙상 counselResvDtm 필수 → 기존 값 유지하여 전달
                viewModel.updateCounselStatus(selected.counselStat, current.counselResvDtm)
            }
            selectedStatusIndex = position
            bindStatusButton(selected)
            popup.dismiss()
        }

        popup.show()
        // 팝업 ListView 에 클립 해제 (둥근 모서리 잘림 방지) + elevation 적용
        popup.listView?.clipToOutline = false
        popup.listView?.elevation = resources.displayMetrics.density * 8
        statusPopup = popup
    }

    private fun showManagerPopup() {
        val labels = mutableListOf("미배정") + employeeList.map { it.empName }
        if (labels.isEmpty()) return

        val popup = ListPopupWindow(requireContext())
        popup.anchorView = spinnerManagerAnchor

        // ① 너비 1:1 동기화
        popup.width = spinnerManagerAnchor.width
        // ② 무조건 아래 방향으로만
        popup.isModal = true
        popup.setDropDownGravity(android.view.Gravity.START)
        // ③ 스피너와 겹치지 않는 정렬
        popup.verticalOffset = 0
        popup.horizontalOffset = 0

        popup.setBackgroundDrawable(
            androidx.core.content.ContextCompat.getDrawable(requireContext(), R.drawable.bg_spinner_popup)
        )
        popup.height = ListPopupWindow.WRAP_CONTENT

        val adapter = ManagerDropdownAdapter(requireContext(), labels, selectedManagerIndex)
        popup.setAdapter(adapter)

        popup.setOnItemClickListener { _, _, position, _ ->
            val empSeq = if (position == 0) null else employeeList.getOrNull(position - 1)?.empSeq
            val current = currentDetail ?: return@setOnItemClickListener
            if (empSeq != current.empSeq) {
                viewModel.updateCounsel(
                    com.example.flowdesk_android.feature.counsel_management.data.dto.CounselUpdateRequest(
                        empSeq = empSeq
                    )
                )
            }
            selectedManagerIndex = position
            tvManagerSelected.text = labels[position]
            popup.dismiss()
        }

        popup.show()
        popup.listView?.clipToOutline = false
        popup.listView?.elevation = resources.displayMetrics.density * 8
        managerPopup = popup
    }

    // ── ViewModel 관찰 ──────────────────────────────────────────────────────────

    private fun observeViewModel() {
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
                                Toast.makeText(requireContext(), "상태가 변경되었습니다.", Toast.LENGTH_SHORT).show()
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
            }
        }
    }

    // ── 상세 데이터 바인딩 ─────────────────────────────────────────────────────

    private fun bindDetail(detail: CounselDetail) {
        tvName.text = detail.name
        tvStatusTag.text = detail.statusName
        tvPhone.text = detail.counselHp
        tvWebsite.text = detail.webTitle
        tvIp.text = detail.counselIp ?: "-"
        tvManager.text = detail.empName ?: "미배정"
        tvCreatedAt.text = "등록: ${formatDateTime(detail.regDtm)}"
        tvUpdatedAt.text = "수정: ${formatDateTime(detail.editDtm)}"

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
        tvStatusSelected.text = status.statusName
        try {
            vStatusDot.visibility = View.VISIBLE
            vStatusDot.setCardBackgroundColor(Color.parseColor(status.color))
        } catch (e: Exception) {
            vStatusDot.visibility = View.GONE
        }

        try {
            val statusColor = Color.parseColor(status.color)
            val softBg = Color.argb(25, Color.red(statusColor), Color.green(statusColor), Color.blue(statusColor))
            val badgeBg = GradientDrawable().apply {
                setColor(softBg)
                cornerRadius = 4.dpToPx(requireContext()).toFloat()
            }
            tvStatusTag.background = badgeBg
            tvStatusTag.setTextColor(statusColor)
            tvStatusTag.text = status.statusName
        } catch (e: Exception) {
            tvStatusTag.text = status.statusName
            tvStatusTag.setTextColor(Color.parseColor("#8B5CF6"))
        }
    }

    private fun Int.dpToPx(context: android.content.Context): Int {
        return (this * context.resources.displayMetrics.density).toInt()
    }

    private fun syncManagerButton(empSeq: Int?) {
        val idx = if (empSeq == null) 0
        else employeeList.indexOfFirst { it.empSeq == empSeq }.let { if (it < 0) 0 else it + 1 }
        selectedManagerIndex = idx
        val labels = mutableListOf("미배정") + employeeList.map { it.empName }
        tvManagerSelected.text = labels.getOrNull(idx) ?: "미배정"
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
                tvText.setTextColor(Color.parseColor("#1D4ED8"))
                tvText.setTypeface(null, android.graphics.Typeface.BOLD)
            } else {
                tvText.setTextColor(Color.parseColor("#475569"))
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
                tvText.setTextColor(Color.parseColor("#1D4ED8"))
                tvText.setTypeface(null, android.graphics.Typeface.BOLD)
            } else {
                tvText.setTextColor(Color.parseColor("#475569"))
                tvText.setTypeface(null, android.graphics.Typeface.NORMAL)
            }

            return view
        }
    }
}
