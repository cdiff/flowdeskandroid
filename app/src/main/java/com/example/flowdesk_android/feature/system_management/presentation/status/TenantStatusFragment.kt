package com.example.flowdesk_android.feature.system_management.presentation.status

import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
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
import androidx.recyclerview.widget.RecyclerView
import com.example.flowdesk_android.R
import com.example.flowdesk_android.feature.system_management.domain.model.TenantStatus
import com.example.flowdesk_android.feature.auth.presentation.dashboard.DashboardViewModel
import com.example.flowdesk_android.feature.auth.presentation.dashboard.DashboardState
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

@AndroidEntryPoint
class TenantStatusFragment : Fragment() {
 
     private val viewModel: TenantStatusViewModel by activityViewModels()
     private val dashboardViewModel: DashboardViewModel by activityViewModels()
 
     // Views
     private lateinit var etSearch: EditText
     private lateinit var btnAddStatus: View
     
     private lateinit var tvStatTotalGroups: TextView
     private lateinit var tvStatTotalStatuses: TextView
     private lateinit var tvStatActiveStatuses: TextView
     private lateinit var tvStatInactiveStatuses: TextView
 
     // Group Containers (동적 칩 및 동적 아코디언 컨테이너)
     private lateinit var layoutGroupContainer: android.widget.LinearLayout
     private lateinit var layoutAccordionContainer: android.widget.LinearLayout
 
     override fun onCreateView(
         inflater: LayoutInflater, container: ViewGroup?,
         savedInstanceState: Bundle?
     ): View? = inflater.inflate(R.layout.fragment_system_tenant_status, container, false)
 
     override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
         super.onViewCreated(view, savedInstanceState)
         bindViews(view)
         setupListeners()
         observeState()
     }
 
     private fun bindViews(view: View) {
         etSearch = view.findViewById(R.id.et_status_search)
         btnAddStatus = view.findViewById(R.id.btn_add_status)
         
         tvStatTotalGroups = view.findViewById(R.id.tv_stat_total_groups)
         tvStatTotalStatuses = view.findViewById(R.id.tv_stat_total_statuses)
         tvStatActiveStatuses = view.findViewById(R.id.tv_stat_active_statuses)
         tvStatInactiveStatuses = view.findViewById(R.id.tv_stat_inactive_statuses)
 
         // Bind Dynamic Containers
         layoutGroupContainer = view.findViewById(R.id.layout_group_container)
         layoutAccordionContainer = view.findViewById(R.id.layout_accordion_container)
     }
 
     private fun renderGroupChips(groups: List<String>) {
         layoutGroupContainer.removeAllViews()
         val inflater = LayoutInflater.from(requireContext())
 
         // 1. "전체" 칩 생성
         val allChip = inflater.inflate(R.layout.item_status_group_chip, layoutGroupContainer, false) as TextView
         allChip.text = "전체"
         allChip.tag = "all"
         allChip.setOnClickListener {
             viewModel.updateGroup("all")
         }
         layoutGroupContainer.addView(allChip)
 
         // 2. 동적 그룹 칩 생성
         groups.forEach { groupName ->
             val chip = inflater.inflate(R.layout.item_status_group_chip, layoutGroupContainer, false) as TextView
             chip.text = groupName
             chip.tag = groupName
             chip.setOnClickListener {
                 viewModel.updateGroup(groupName)
             }
             layoutGroupContainer.addView(chip)
         }
 
         // 현재 선택 상태 즉시 반영
         updateChipSelection(viewModel.selectedGroup.value)
     }
 
     private fun updateChipSelection(selectedGroup: String) {
         for (i in 0 until layoutGroupContainer.childCount) {
             val child = layoutGroupContainer.getChildAt(i) as? TextView ?: continue
             val isSelected = child.tag == selectedGroup
             child.setBackgroundResource(
                 if (isSelected) R.drawable.bg_card_rounded_border_selected
                 else R.drawable.bg_card_rounded_border
             )
         }
     }
 
     private fun renderAccordionGroups(groups: List<com.example.flowdesk_android.feature.system_management.domain.model.TenantStatusGroup>) {
         layoutAccordionContainer.removeAllViews()
         val inflater = LayoutInflater.from(requireContext())
 
         groups.forEach { groupData ->
             // 1. 아코디언 컴포넌트 인플레이팅
             val accordionView = inflater.inflate(R.layout.item_status_group_accordion, layoutAccordionContainer, false)
 
             val headerLayout = accordionView.findViewById<View>(R.id.layout_accordion_header)
             val ivArrow = accordionView.findViewById<ImageView>(R.id.iv_arrow_indicator)
             val tvTitle = accordionView.findViewById<TextView>(R.id.tv_accordion_title)
             val tvBadge = accordionView.findViewById<TextView>(R.id.tv_accordion_badge)
             val rvStatuses = accordionView.findViewById<RecyclerView>(R.id.rv_tenant_statuses)
 
             // 2. 타이틀 & 상태 개수 매핑 (아코디언 타이틀 텍스트 갱신)
             tvTitle.text = groupData.statusGroup
             tvBadge.text = "${groupData.items.size}개 상태"
 
             // 3. Adapter 및 LayoutManager 바인딩
             val adapter = TenantStatusAdapter(
                onItemClicked = { item ->
                    val bundle = Bundle().apply {
                        putLong("tenantStatusId", item.tenantStatusId)
                    }
                    findNavController().navigate(R.id.statusEditFragment, bundle)
                },
                onMoreClicked = { item, anchorView ->
                    showStatusItemMenu(item, anchorView)
                }
            )
             rvStatuses.layoutManager = LinearLayoutManager(requireContext())
             rvStatuses.adapter = adapter
             adapter.submitList(groupData.items)
 
             // 4. 개별 아코디언 토글(접기/펴기) 처리
             var isExpanded = true
             headerLayout.setOnClickListener {
                 isExpanded = !isExpanded
                 if (isExpanded) {
                     rvStatuses.visibility = View.VISIBLE
                     ivArrow.animate().rotation(90f).setDuration(200).start()
                 } else {
                     rvStatuses.visibility = View.GONE
                     ivArrow.animate().rotation(0f).setDuration(200).start()
                 }
             }
 
             layoutAccordionContainer.addView(accordionView)
         }
     }
 
     private fun setupListeners() {
         // Search Input TextWatcher
         etSearch.addTextChangedListener(object : TextWatcher {
             override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
             override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                 viewModel.updateSearchQuery(s?.toString() ?: "")
             }
             override fun afterTextChanged(s: Editable?) {}
         })
 
        // "+ 상태 추가" Button Click - StatusEditFragment 오픈
        btnAddStatus.setOnClickListener {
            val bundle = Bundle().apply {
                putString("defaultGroup", viewModel.selectedGroup.value)
            }
            findNavController().navigate(R.id.statusEditFragment, bundle)
        }
     }
 
     private fun showStatusItemMenu(item: TenantStatus, anchorView: View) {
         val popup = PopupMenu(requireContext(), anchorView)
         popup.menu.add("상태 수정")
         popup.menu.add(if (item.isActive) "상태 비활성화" else "상태 활성화")
         popup.menu.add("상태 삭제")
         
         popup.setOnMenuItemClickListener { menuItem ->
             when (menuItem.title) {
                  "상태 수정" -> {
                      val bundle = Bundle().apply {
                          putLong("tenantStatusId", item.tenantStatusId)
                      }
                      findNavController().navigate(R.id.statusEditFragment, bundle)
                  }
                 "상태 비활성화", "상태 활성화" -> {
                     viewModel.toggleStatusActive(item.tenantStatusId, item.isActive)
                 }
                 "상태 삭제" -> {
                     // 삭제 확인 AlertDialog
                     AlertDialog.Builder(requireContext())
                         .setTitle("상태 삭제")
                         .setMessage("정말로 이 상태를 삭제하시겠습니까?\n삭제된 상태는 복구할 수 없습니다.")
                         .setPositiveButton("삭제") { _, _ ->
                             viewModel.deleteStatus(item.tenantStatusId)
                         }
                         .setNegativeButton("취소", null)
                         .show()
                 }
             }
             true
         }
         popup.show()
     }
 
     private fun observeState() {
         viewLifecycleOwner.lifecycleScope.launch {
             viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                 // 1. Group Chips List mapping
                 launch {
                     viewModel.statusGroups.collect { groups ->
                         renderGroupChips(groups)
                     }
                 }
 
                 // 2. Selected Group selection state mapping
                 launch {
                     viewModel.selectedGroup.collect { selected ->
                         updateChipSelection(selected)
                     }
                 }
 
                 // 3. Dynamic Group Accordions mapping
                 launch {
                     viewModel.filteredGroups.collect { list ->
                         renderAccordionGroups(list)
                     }
                 }
 
                 // 4. Groups count
                 launch {
                     viewModel.totalGroups.collect { count ->
                         tvStatTotalGroups.text = count.toString()
                     }
                 }
 
                 // 5. Statuses count
                 launch {
                     viewModel.totalStatuses.collect { count ->
                         tvStatTotalStatuses.text = count.toString()
                     }
                 }
 
                 // 6. Active statuses count
                 launch {
                     viewModel.activeStatuses.collect { count ->
                         tvStatActiveStatuses.text = count.toString()
                     }
                 }
 
                 // 7. Inactive statuses count
                 launch {
                     viewModel.inactiveStatuses.collect { count ->
                         tvStatInactiveStatuses.text = count.toString()
                     }
                 }
 
                 // 8. Error messages Flow
                 launch {
                     viewModel.errorMessage.collectLatest { msg ->
                         Toast.makeText(requireContext(), msg, Toast.LENGTH_LONG).show()
                     }
                 }

                 // 9. 권한(tenants.status.create)에 따른 등록 버튼 제어
                 launch {
                     dashboardViewModel.dashboardState.collect { state ->
                         if (state is DashboardState.Success) {
                             val hasCreatePermission = state.data.permissions["tenants.status.create"] == true
                             btnAddStatus.visibility = if (hasCreatePermission) View.VISIBLE else View.GONE
                         }
                     }
                 }
             }
         }
     }
 }
