package com.example.flowdesk_android.feature.system_management.presentation.block.ip

import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.core.widget.NestedScrollView
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.flowdesk_android.R
import com.example.flowdesk_android.databinding.FragmentBlockIpBinding
import com.example.flowdesk_android.feature.system_management.domain.model.BlockIpItem
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

@AndroidEntryPoint
class BlockIpFragment : Fragment() {

    private var _binding: FragmentBlockIpBinding? = null
    private val binding get() = _binding!!

    private val viewModel: BlockIpViewModel by activityViewModels()
    private lateinit var adapter: BlockIpAdapter

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentBlockIpBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setupRecyclerView()
        setupListeners()
        observeViewModel()
        
        // Initial load
        viewModel.loadBlockIps(isRefresh = true)
    }

    private fun setupRecyclerView() {
        adapter = BlockIpAdapter { item ->
            showOptionsDialog(item)
        }
        binding.rvIpList.layoutManager = LinearLayoutManager(requireContext())
        binding.rvIpList.adapter = adapter
    }

    private fun setupListeners() {
        binding.btnAddIp.setOnClickListener {
            showAddBottomSheet()
        }

        binding.etIpSearch.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                viewModel.updateSearchQuery(s?.toString() ?: "")
            }
            override fun afterTextChanged(s: Editable?) {}
        })

        // Infinite Scroll
        binding.nsvIpBlock.setOnScrollChangeListener(NestedScrollView.OnScrollChangeListener { v, _, scrollY, _, _ ->
            val contentHeight = v.getChildAt(0).measuredHeight
            val scrollHeight = v.measuredHeight
            if (scrollY >= contentHeight - scrollHeight - 100) {
                viewModel.loadMore()
            }
        })
    }

    private fun observeViewModel() {
        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                
                launch {
                    viewModel.uiState.collectLatest { state ->
                        when (state) {
                            is BlockIpUiState.Loading -> {
                                binding.progressBarIp.visibility = View.VISIBLE
                                binding.llIpEmpty.visibility = View.GONE
                            }
                            is BlockIpUiState.Success -> {
                                binding.progressBarIp.visibility = View.GONE
                                adapter.submitList(state.items)
                                binding.tvIpListCount.text = "  ${state.totalCount}건"
                                
                                if (state.items.isEmpty()) {
                                    binding.llIpEmpty.visibility = View.VISIBLE
                                    binding.rvIpList.visibility = View.GONE
                                } else {
                                    binding.llIpEmpty.visibility = View.GONE
                                    binding.rvIpList.visibility = View.VISIBLE
                                }
                            }
                            is BlockIpUiState.Error -> {
                                binding.progressBarIp.visibility = View.GONE
                                binding.llIpEmpty.visibility = View.VISIBLE
                                binding.rvIpList.visibility = View.GONE
                                Toast.makeText(requireContext(), state.message, Toast.LENGTH_SHORT).show()
                            }
                        }
                    }
                }

                launch {
                    viewModel.errorMessage.collectLatest { message ->
                        Toast.makeText(requireContext(), message, Toast.LENGTH_SHORT).show()
                    }
                }
            }
        }
    }

    private fun showOptionsDialog(item: BlockIpItem) {
        val options = arrayOf("차단 사유 수정", "차단 해제 (삭제)")
        AlertDialog.Builder(requireContext())
            .setTitle(item.blockIp)
            .setItems(options) { _, which ->
                when (which) {
                    0 -> showEditBottomSheet(item)
                    1 -> showDeleteConfirmDialog(item)
                }
            }
            .show()
    }

    private fun showAddBottomSheet() {
        val bottomSheet = IpBlockCreateBottomSheet.newInstance()
        bottomSheet.show(childFragmentManager, "IpBlockCreateBottomSheet")
    }

    private fun showEditBottomSheet(item: BlockIpItem) {
        val bottomSheet = IpBlockCreateBottomSheet.newInstance(item)
        bottomSheet.show(childFragmentManager, "IpBlockEditBottomSheet")
    }

    private fun showDeleteConfirmDialog(item: BlockIpItem) {
        val dialogView = LayoutInflater.from(requireContext()).inflate(R.layout.dialog_common_confirm, null)
        val dialog = AlertDialog.Builder(requireContext())
            .setView(dialogView)
            .create()

        dialog.window?.setBackgroundDrawableResource(android.R.color.transparent)

        val tvTitle = dialogView.findViewById<TextView>(R.id.tv_title)
        val tvMessage = dialogView.findViewById<TextView>(R.id.tv_message)
        val cbConfirm = dialogView.findViewById<View>(R.id.cb_confirm)
        val btnCancel = dialogView.findViewById<View>(R.id.btn_cancel)
        val btnConfirm = dialogView.findViewById<View>(R.id.btn_confirm)

        tvTitle.text = "IP 차단 해제"
        tvMessage.text = "${item.blockIp} 주소의 차단을 해제하시겠습니까?\n해제하면 즉시 접속이 허용됩니다."
        cbConfirm.visibility = View.GONE

        btnCancel.setOnClickListener {
            dialog.dismiss()
        }

        btnConfirm.setOnClickListener {
            dialog.dismiss()
            viewModel.deleteBlockIp(item.dbiIdx) { result ->
                result.onSuccess {
                    Toast.makeText(requireContext(), "IP 차단이 해제되었습니다.", Toast.LENGTH_SHORT).show()
                }.onFailure { err ->
                    Toast.makeText(requireContext(), err.message ?: "차단 해제 실패", Toast.LENGTH_SHORT).show()
                }
            }
        }

        dialog.show()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
