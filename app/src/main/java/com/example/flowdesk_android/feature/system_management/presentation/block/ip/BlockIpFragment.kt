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
import androidx.navigation.fragment.findNavController

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
            val bundle = Bundle().apply {
                putLong("blockIpId", item.dbiIdx)
            }
            findNavController().navigate(R.id.blockIpDetailFragment, bundle)
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

    private fun showAddBottomSheet() {
        val bottomSheet = IpBlockCreateBottomSheet.newInstance()
        bottomSheet.show(childFragmentManager, "IpBlockCreateBottomSheet")
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
