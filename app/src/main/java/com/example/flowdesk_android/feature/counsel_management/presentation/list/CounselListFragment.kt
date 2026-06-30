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
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.util.Locale
import androidx.navigation.fragment.findNavController
import com.example.flowdesk_android.databinding.FragmentCounselListBinding

@AndroidEntryPoint
class CounselListFragment : Fragment() {

    private val viewModel: CounselListViewModel by viewModels()

    // Adapters
    private lateinit var statusAdapter: CounselStatusAdapter
    private lateinit var counselAdapter: CounselListAdapter

    // Binding
    private var _binding: FragmentCounselListBinding? = null
    private val binding get() = _binding!!

    // Date formatting
    private val dateFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd", Locale.getDefault())
    private val displayFormatter = DateTimeFormatter.ofPattern("yyyy.MM.dd", Locale.getDefault())

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentCounselListBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setupAdapters()
        setupListeners()
        observeState()
        
        // 상세화면 등에서 상태 변경 후 목록으로 복귀할 때 데이터를 갱신하기 위해 새로고침 수행
        viewModel.triggerRefresh()
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
        binding.rvStatusTabs.layoutManager = LinearLayoutManager(requireContext(), LinearLayoutManager.HORIZONTAL, false)
        binding.rvStatusTabs.adapter = statusAdapter

        // Vertical Counsel List Adapter
        counselAdapter = CounselListAdapter(
            onCopyClick = { phone ->
                val clipboard = requireContext().getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                val clip = ClipData.newPlainText(getString(R.string.label_phone), phone)
                clipboard.setPrimaryClip(clip)
                Toast.makeText(requireContext(), getString(R.string.counsel_toast_phone_copied), Toast.LENGTH_SHORT).show()
            },
            onOptionsClick = { item, anchorView ->
                showItemOptionsMenu(item, anchorView)
            },
            onItemClick = { item ->
                val bundle = android.os.Bundle().apply {
                    putInt("counselSeq", item.counselSeq)
                }
                findNavController().navigate(R.id.counselDetailFragment, bundle)
            }
        )
        binding.rvCounsels.layoutManager = LinearLayoutManager(requireContext())
        binding.rvCounsels.adapter = counselAdapter
    }

    private fun setupListeners() {
        // "Total" Tab Click listener
        binding.layoutTabTotal.setOnClickListener {
            statusAdapter.clearSelection() // Unselect others
            setTotalTabStyle(isSelected = true)
            viewModel.updateStatusFilter(null)
        }

        // Search text change listener (ViewModel 내부에서 300ms 디바운스 및 자동 호출 처리)
        binding.etSearch.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                viewModel.updateSearchQuery(s?.toString())
            }
            override fun afterTextChanged(s: Editable?) {}
        })

        // Infinite Scroll listener on NestedScrollView
        binding.nestedScrollView.setOnScrollChangeListener(NestedScrollView.OnScrollChangeListener { v, _, scrollY, _, _ ->
            val contentHeight = v.getChildAt(0).measuredHeight
            val scrollHeight = v.measuredHeight
            if (scrollY >= contentHeight - scrollHeight - 100) {
                viewModel.loadMore()
            }
        })

        // Open Filter Bottom Sheet Click
        binding.btnFilterDialog.setOnClickListener {
            val filterBottomSheet = CounselFilterBottomSheetFragment()
            filterBottomSheet.show(childFragmentManager, "CounselFilterBottomSheet")
        }
    }

    private fun setTotalTabStyle(isSelected: Boolean) {
        val context = requireContext()
        val countBadge = binding.tvStatusTotalCountBadge
        val card = countBadge.parent as? com.google.android.material.card.MaterialCardView
        if (isSelected) {
            card?.setCardBackgroundColor(androidx.core.content.ContextCompat.getColor(context, R.color.counsel_tab_selected_bg))
            countBadge.setTextColor(androidx.core.content.ContextCompat.getColor(context, R.color.white))
        } else {
            card?.setCardBackgroundColor(androidx.core.content.ContextCompat.getColor(context, R.color.counsel_tab_unselected_bg))
            card?.strokeWidth = (1.5 * context.resources.displayMetrics.density).toInt()
            card?.strokeColor = androidx.core.content.ContextCompat.getColor(context, R.color.counsel_tab_unselected_stroke)
            countBadge.setTextColor(androidx.core.content.ContextCompat.getColor(context, R.color.counsel_tab_unselected_text))
        }
    }

    private fun showItemOptionsMenu(item: CounselItem, view: View) {
        val context = requireContext()
        val popup = PopupMenu(context, view)
        val editTitle = context.getString(R.string.counsel_menu_edit)
        val deleteTitle = context.getString(R.string.counsel_menu_delete)
        popup.menu.add(editTitle)
        popup.menu.add(deleteTitle)
        popup.setOnMenuItemClickListener { menuItem ->
            when (menuItem.title) {
                editTitle -> showEditDialog(item)
                deleteTitle -> showDeleteConfirmDialog(item)
            }
            true
        }
        popup.show()
    }

    private fun showEditDialog(item: CounselItem) {
        val editBottomSheet = CounselEditBottomSheetFragment.newInstance(item.counselSeq, item.name, item.counselHp)
        editBottomSheet.show(childFragmentManager, "CounselEditBottomSheet")
    }

    private fun showDeleteConfirmDialog(item: CounselItem) {
        val dialogView = LayoutInflater.from(requireContext()).inflate(R.layout.dialog_common_confirm, null)
        val dialog = androidx.appcompat.app.AlertDialog.Builder(requireContext())
            .setView(dialogView)
            .create()

        dialog.window?.setBackgroundDrawableResource(android.R.color.transparent)

        val tvTitle = dialogView.findViewById<TextView>(R.id.tv_title)
        val tvMessage = dialogView.findViewById<TextView>(R.id.tv_message)
        val cbConfirm = dialogView.findViewById<View>(R.id.cb_confirm)
        val btnCancel = dialogView.findViewById<View>(R.id.btn_cancel)
        val btnConfirm = dialogView.findViewById<View>(R.id.btn_confirm)

        tvTitle.text = getString(R.string.counsel_dialog_delete_title)
        tvMessage.text = getString(R.string.counsel_dialog_delete_message, item.name)
        cbConfirm.visibility = View.GONE

        btnCancel.setOnClickListener {
            dialog.dismiss()
        }

        btnConfirm.setOnClickListener {
            viewModel.deleteCounsel(item.counselSeq)
            dialog.dismiss()
        }

        dialog.show()
    }

    private fun observeState() {
        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                
                // Observe Counsel List UI State
                launch {
                    viewModel.uiState.collectLatest { state ->
                        when (state) {
                            is CounselListUiState.Loading -> {
                                binding.progressBar.visibility = View.VISIBLE
                                binding.llEmpty.visibility = View.GONE
                            }
                            is CounselListUiState.Success -> {
                                binding.progressBar.visibility = View.GONE
                                counselAdapter.submitList(state.items)
                                binding.tvCounselCount.text = getString(R.string.counsel_total_count, state.totalCount)
                                if (state.items.isEmpty()) {
                                    binding.llEmpty.visibility = View.VISIBLE
                                    binding.rvCounsels.visibility = View.GONE
                                } else {
                                    binding.llEmpty.visibility = View.GONE
                                    binding.rvCounsels.visibility = View.VISIBLE
                                }
                            }
                            is CounselListUiState.Error -> {
                                binding.progressBar.visibility = View.GONE
                                binding.llEmpty.visibility = View.VISIBLE
                                Toast.makeText(requireContext(), state.message, Toast.LENGTH_SHORT).show()
                            }
                        }
                    }
                }

                // Observe Status Counts for circular tabs
                launch {
                    viewModel.statusCounts.collectLatest { list ->
                        statusAdapter.submitList(list)
                        counselAdapter.setStatusColors(list)
                        val totalSum = list.sumOf { it.count }
                        binding.tvStatusTotalCountBadge.text = totalSum.toString()
                    }
                }

                // Observe Filter State to update active badge dot
                launch {
                    viewModel.filterState.collectLatest { filter ->
                        val isAnyFilterActive = filter.startDate != null ||
                                filter.endDate != null ||
                                filter.empSeq != null ||
                                filter.webCode != null
                        binding.viewFilterActiveDot.visibility = if (isAnyFilterActive) View.VISIBLE else View.GONE
                    }
                }
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
