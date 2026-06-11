package com.example.flowdesk_android.feature.counsel_management.presentation.detail

import android.graphics.Color
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.Spinner
import android.widget.TextView
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
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
    private lateinit var btnClose: View
    private lateinit var tvName: TextView
    private lateinit var tvStatusTag: TextView
    private lateinit var tvPhone: TextView
    private lateinit var tvWebsite: TextView
    private lateinit var tvIp: TextView
    private lateinit var tvManager: TextView
    private lateinit var tvCreatedAt: TextView
    private lateinit var tvUpdatedAt: TextView
    private lateinit var spinnerStatus: Spinner
    private lateinit var spinnerManager: Spinner
    private lateinit var tabLayout: TabLayout
    private lateinit var tabContentContainer: ViewGroup

    // Spinner data
    private var statusList: List<CounselStatusStat> = emptyList()
    private var employeeList: List<EmployeeStat> = emptyList()
    private var currentDetail: CounselDetail? = null

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? = inflater.inflate(R.layout.fragment_counsel_detail, container, false)

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        bindViews(view)
        setupTabLayout()
        setupListeners()
        observeViewModel()

        val counselSeq = arguments?.getInt("counselSeq") ?: -1
        viewModel.init(counselSeq)
    }

    private fun bindViews(view: View) {
        btnBack = view.findViewById(R.id.btn_back)
        btnClose = view.findViewById(R.id.btn_close)
        tvName = view.findViewById(R.id.tv_detail_name)
        tvStatusTag = view.findViewById(R.id.tv_detail_status_tag)
        tvPhone = view.findViewById(R.id.tv_detail_phone)
        tvWebsite = view.findViewById(R.id.tv_detail_website)
        tvIp = view.findViewById(R.id.tv_detail_ip)
        tvManager = view.findViewById(R.id.tv_detail_manager)
        tvCreatedAt = view.findViewById(R.id.tv_detail_created_at)
        tvUpdatedAt = view.findViewById(R.id.tv_detail_updated_at)
        spinnerStatus = view.findViewById(R.id.spinner_status)
        spinnerManager = view.findViewById(R.id.spinner_manager)
        tabLayout = view.findViewById(R.id.tab_layout)
        tabContentContainer = view.findViewById(R.id.tab_content_container)
    }

    private fun setupTabLayout() {
        // 초기 탭: 기본 정보
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
        btnClose.setOnClickListener {
            findNavController().navigateUp()
        }
    }

    private fun observeViewModel() {
        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {

                // 상세 데이터 관찰
                launch {
                    viewModel.uiState.collect { state ->
                        when (state) {
                            is CounselDetailUiState.Loading -> { /* 로딩 처리 (필요 시 ProgressBar 추가) */ }
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

                // 담당자 목록 관찰
                launch {
                    viewModel.employeeList.collect { employees ->
                        employeeList = employees
                        setupManagerSpinner(employees)
                    }
                }

                // 상태 목록 관찰
                launch {
                    viewModel.statusList.collect { statuses ->
                        statusList = statuses
                        setupStatusSpinner(statuses)
                    }
                }

                // 상태 변경 결과 관찰
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

    private fun bindDetail(detail: CounselDetail) {
        tvName.text = detail.name
        tvStatusTag.text = detail.statusName
        tvPhone.text = detail.counselHp
        tvWebsite.text = detail.webTitle
        tvIp.text = detail.counselIp ?: "-"
        tvManager.text = detail.empName ?: "미배정"
        tvCreatedAt.text = "등록: ${formatDateTime(detail.regDtm)}"
        tvUpdatedAt.text = "수정: ${formatDateTime(detail.editDtm)}"

        // 상태 Spinner 현재 선택값 동기화
        syncStatusSpinnerSelection(detail.counselStat)

        // 담당자 Spinner 현재 선택값 동기화
        syncManagerSpinnerSelection(detail.empSeq)
    }

    private fun setupStatusSpinner(statuses: List<CounselStatusStat>) {
        val labels = statuses.map { it.statusName }
        val adapter = ArrayAdapter(requireContext(), android.R.layout.simple_spinner_item, labels)
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        spinnerStatus.adapter = adapter

        // 현재 상태 선택
        currentDetail?.let { syncStatusSpinnerSelection(it.counselStat) }

        spinnerStatus.setOnItemSelectedListenerSafe { position ->
            val selected = statuses.getOrNull(position) ?: return@setOnItemSelectedListenerSafe
            val current = currentDetail ?: return@setOnItemSelectedListenerSafe
            if (selected.counselStat != current.counselStat) {
                viewModel.updateCounselStatus(selected.counselStat)
            }
        }
    }

    private fun setupManagerSpinner(employees: List<EmployeeStat>) {
        val labels = mutableListOf("미배정") + employees.map { it.empName }
        val adapter = ArrayAdapter(requireContext(), android.R.layout.simple_spinner_item, labels)
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        spinnerManager.adapter = adapter

        // 현재 담당자 선택
        currentDetail?.let { syncManagerSpinnerSelection(it.empSeq) }

        spinnerManager.setOnItemSelectedListenerSafe { position ->
            val empSeq = if (position == 0) null else employees.getOrNull(position - 1)?.empSeq
            val current = currentDetail ?: return@setOnItemSelectedListenerSafe
            if (empSeq != current.empSeq) {
                viewModel.updateCounsel(
                    com.example.flowdesk_android.feature.counsel_management.data.dto.CounselUpdateRequest(
                        empSeq = empSeq
                    )
                )
            }
        }
    }

    private fun syncStatusSpinnerSelection(counselStat: Int) {
        val idx = statusList.indexOfFirst { it.counselStat == counselStat }
        if (idx >= 0) spinnerStatus.setSelection(idx)
    }

    private fun syncManagerSpinnerSelection(empSeq: Int?) {
        val idx = if (empSeq == null) 0
        else employeeList.indexOfFirst { it.empSeq == empSeq }.let { if (it < 0) 0 else it + 1 }
        spinnerManager.setSelection(idx)
    }

    private fun formatDateTime(iso: String?): String {
        if (iso.isNullOrBlank()) return "-"
        return try {
            "${iso.substring(0, 10)} ${iso.substring(11, 19)}"
        } catch (e: Exception) { iso }
    }
}

// ── Spinner 편의 확장 함수 (초기 onItemSelected 콜백 무시용) ────────────────────

private fun android.widget.Spinner.setOnItemSelectedListenerSafe(
    onSelected: (Int) -> Unit
) {
    var initialized = false
    this.onItemSelectedListener = object : android.widget.AdapterView.OnItemSelectedListener {
        override fun onItemSelected(
            parent: android.widget.AdapterView<*>?, view: View?, position: Int, id: Long
        ) {
            if (!initialized) { initialized = true; return }
            onSelected(position)
        }
        override fun onNothingSelected(parent: android.widget.AdapterView<*>?) {}
    }
}
