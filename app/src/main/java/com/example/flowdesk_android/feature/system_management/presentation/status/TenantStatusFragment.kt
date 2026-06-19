package com.example.flowdesk_android.feature.system_management.presentation.status

import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.widget.PopupMenu
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.fragment.app.activityViewModels
import androidx.navigation.fragment.findNavController
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.flowdesk_android.R
import com.example.flowdesk_android.databinding.FragmentSystemTenantStatusBinding
import com.example.flowdesk_android.feature.system_management.domain.model.TenantStatus
import com.example.flowdesk_android.feature.system_management.domain.model.TenantStatusGroup
import com.example.flowdesk_android.feature.auth.presentation.dashboard.DashboardViewModel
import com.example.flowdesk_android.feature.auth.presentation.dashboard.DashboardState
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

@AndroidEntryPoint
class TenantStatusFragment : Fragment() {

    // ① ViewBinding 통일 — findViewById 제거
    private var _binding: FragmentSystemTenantStatusBinding? = null
    private val binding get() = _binding!!

    private val viewModel: TenantStatusViewModel by activityViewModels()
    private val dashboardViewModel: DashboardViewModel by activityViewModels()

    // ② Adapter 재사용 — 그룹 key → Adapter 캐시
    private val adapterCache = mutableMapOf<String, TenantStatusAdapter>()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentSystemTenantStatusBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setupListeners()
        observeState()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    // ─── Chip 렌더링 ────────────────────────────────────────────────────────
    private fun renderGroupChips(groups: List<String>) {
        binding.layoutGroupContainer.removeAllViews()
        val inflater = LayoutInflater.from(requireContext())

        // "전체" 칩
        addChip(inflater, "전체", "all")

        // 동적 그룹 칩
        groups.forEach { groupName -> addChip(inflater, groupName, groupName) }

        // 현재 선택 상태 즉시 반영
        updateChipSelection(viewModel.selectedGroup.value)
    }

    private fun addChip(inflater: LayoutInflater, label: String, tag: String) {
        val cardChip = inflater.inflate(
            R.layout.item_status_group_chip,
            binding.layoutGroupContainer,
            false
        ) as com.google.android.material.card.MaterialCardView
        cardChip.tag = tag
        cardChip.findViewById<TextView>(R.id.tv_group_name).text = label
        cardChip.setOnClickListener { viewModel.updateGroup(tag) }
        binding.layoutGroupContainer.addView(cardChip)
    }

    private fun updateChipSelection(selectedGroup: String) {
        val container = binding.layoutGroupContainer
        val selectedBg = androidx.core.content.ContextCompat.getColor(requireContext(), R.color.bg_card_selected)
        val unselectedBg = androidx.core.content.ContextCompat.getColor(requireContext(), R.color.bg_card_unselected)
        val selectedText = androidx.core.content.ContextCompat.getColor(requireContext(), R.color.brand_primary)
        val unselectedText = androidx.core.content.ContextCompat.getColor(requireContext(), R.color.text_secondary)

        for (i in 0 until container.childCount) {
            val card = container.getChildAt(i) as? com.google.android.material.card.MaterialCardView ?: continue
            val tv = card.findViewById<TextView>(R.id.tv_group_name)
            val isSelected = card.tag == selectedGroup
            card.setCardBackgroundColor(
                android.content.res.ColorStateList.valueOf(if (isSelected) selectedBg else unselectedBg)
            )
            tv.setTextColor(if (isSelected) selectedText else unselectedText)
        }
    }

    // ─── Accordion 렌더링 ────────────────────────────────────────────────────
    private fun renderAccordionGroups(groups: List<TenantStatusGroup>) {
        binding.layoutAccordionContainer.removeAllViews()
        val inflater = LayoutInflater.from(requireContext())

        groups.forEach { groupData ->
            val accordionView = inflater.inflate(
                R.layout.item_status_group_accordion,
                binding.layoutAccordionContainer,
                false
            )

            val headerLayout = accordionView.findViewById<View>(R.id.layout_accordion_header)
            val ivArrow = accordionView.findViewById<ImageView>(R.id.iv_arrow_indicator)
            val tvTitle = accordionView.findViewById<TextView>(R.id.tv_accordion_title)
            val tvBadge = accordionView.findViewById<TextView>(R.id.tv_accordion_badge)
            val rvStatuses = accordionView.findViewById<androidx.recyclerview.widget.RecyclerView>(R.id.rv_tenant_statuses)

            tvTitle.text = groupData.statusGroup
            tvBadge.text = "${groupData.items.size}개 상태"

            // ② Adapter 재사용 — 같은 그룹이면 기존 adapter 재활용
            val adapter = adapterCache.getOrPut(groupData.statusGroup) {
                TenantStatusAdapter(
                    onItemClicked = { item ->
                        val bundle = Bundle().apply { putLong("tenantStatusId", item.tenantStatusId) }
                        findNavController().navigate(R.id.statusEditFragment, bundle)
                    },
                    onMoreClicked = { item, anchorView ->
                        showStatusItemMenu(item, anchorView)
                    }
                )
            }

            // ③ NestedScroll 구조 개선 — RecyclerView 중첩 스크롤 비활성화
            rvStatuses.apply {
                layoutManager = LinearLayoutManager(requireContext())
                this.adapter = adapter
                isNestedScrollingEnabled = false   // NestedScrollView와 충돌 방지
                setHasFixedSize(false)
            }
            adapter.submitList(groupData.items)

            // 아코디언 토글
            var isExpanded = true
            headerLayout.setOnClickListener {
                isExpanded = !isExpanded
                rvStatuses.visibility = if (isExpanded) View.VISIBLE else View.GONE
                ivArrow.animate().rotation(if (isExpanded) 90f else 0f).setDuration(200).start()
            }

            binding.layoutAccordionContainer.addView(accordionView)
        }
    }

    // ─── 리스너 ─────────────────────────────────────────────────────────────
    private fun setupListeners() {
        binding.etStatusSearch.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                viewModel.updateSearchQuery(s?.toString() ?: "")
            }
            override fun afterTextChanged(s: Editable?) {}
        })

        binding.btnAddStatus.setOnClickListener {
            val bundle = Bundle().apply {
                putString("defaultGroup", viewModel.selectedGroup.value)
            }
            findNavController().navigate(R.id.statusEditFragment, bundle)
        }
    }

    // ─── 팝업 메뉴 ──────────────────────────────────────────────────────────
    private fun showStatusItemMenu(item: TenantStatus, anchorView: View) {
        val popup = PopupMenu(requireContext(), anchorView)
        popup.menu.add("상태 수정")
        popup.menu.add(if (item.isActive) "상태 비활성화" else "상태 활성화")
        popup.menu.add("상태 삭제")

        popup.setOnMenuItemClickListener { menuItem ->
            when (menuItem.title) {
                "상태 수정" -> {
                    val bundle = Bundle().apply { putLong("tenantStatusId", item.tenantStatusId) }
                    findNavController().navigate(R.id.statusEditFragment, bundle)
                }
                "상태 비활성화", "상태 활성화" -> {
                    viewModel.toggleStatusActive(item.tenantStatusId, item.isActive)
                }
                "상태 삭제" -> {
                    AlertDialog.Builder(requireContext())
                        .setTitle("상태 삭제")
                        .setMessage("정말로 이 상태를 삭제하시겠습니까?\n삭제된 상태는 복구할 수 없습니다.")
                        .setPositiveButton("삭제") { _, _ -> viewModel.deleteStatus(item.tenantStatusId) }
                        .setNegativeButton("취소", null)
                        .show()
                }
            }
            true
        }
        popup.show()
    }

    // ─── 상태 관찰 ──────────────────────────────────────────────────────────
    private fun observeState() {
        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {

                launch { viewModel.statusGroups.collect { renderGroupChips(it) } }

                launch { viewModel.selectedGroup.collect { updateChipSelection(it) } }

                launch { viewModel.filteredGroups.collect { renderAccordionGroups(it) } }

                launch { viewModel.totalGroups.collect { binding.tvStatTotalGroups.text = it.toString() } }

                launch { viewModel.totalStatuses.collect { binding.tvStatTotalStatuses.text = it.toString() } }

                launch { viewModel.activeStatuses.collect { binding.tvStatActiveStatuses.text = it.toString() } }

                launch { viewModel.inactiveStatuses.collect { binding.tvStatInactiveStatuses.text = it.toString() } }

                launch {
                    viewModel.errorMessage.collectLatest { msg ->
                        Toast.makeText(requireContext(), msg, Toast.LENGTH_LONG).show()
                    }
                }

                launch {
                    dashboardViewModel.dashboardState.collect { state ->
                        if (state is DashboardState.Success) {
                            val hasCreate = state.data.permissions["tenants.status.create"] == true
                            binding.btnAddStatus.visibility = if (hasCreate) View.VISIBLE else View.GONE
                        }
                    }
                }
            }
        }
    }
}
