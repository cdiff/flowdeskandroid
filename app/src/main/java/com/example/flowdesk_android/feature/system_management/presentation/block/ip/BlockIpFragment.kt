package com.example.flowdesk_android.feature.system_management.presentation.block.ip

import android.os.Bundle
import android.widget.Toast
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.ListAdapter
import com.example.flowdesk_android.R
import com.example.flowdesk_android.feature.system_management.domain.model.BlockIpItem
import com.example.flowdesk_android.feature.system_management.presentation.block.BaseBlockListFragment
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

@AndroidEntryPoint
class BlockIpFragment : BaseBlockListFragment<BlockIpItem>() {

    private val viewModel: BlockIpViewModel by activityViewModels()
    
    private lateinit var _adapter: BlockIpAdapter
    override val adapter: ListAdapter<BlockIpItem, *> get() = _adapter

    // UI Configuration
    override val titleText: String = "IP 차단 목록"
    override val searchHint: String = "IP 주소 검색..."
    override val emptyTitleText: String = "차단된 IP가 없습니다"
    override val emptySubtitleText: String = "위의 IP 추가 버튼으로 차단 IP를 등록하세요"

    override fun setupRecyclerView() {
        _adapter = BlockIpAdapter { item ->
            val bundle = Bundle().apply {
                putLong("blockIpId", item.dbiIdx)
            }
            findNavController().navigate(R.id.blockIpDetailFragment, bundle)
        }
        binding.rvList.layoutManager = LinearLayoutManager(requireContext())
        binding.rvList.adapter = _adapter
    }

    override fun onSearchQueryChanged(query: String) {
        viewModel.updateSearchQuery(query)
    }

    override fun onLoadMore() {
        viewModel.loadMore()
    }

    override fun onAddClicked() {
        showAddBottomSheet()
    }

    override fun observeViewModel() {
        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                launch {
                    viewModel.uiState.collectLatest { state ->
                        when (state) {
                            is BlockIpUiState.Loading -> {
                                showLoading()
                            }
                            is BlockIpUiState.Success -> {
                                showSuccess(state.items, state.totalCount)
                            }
                            is BlockIpUiState.Error -> {
                                showError(state.message)
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

    override fun onResume() {
        super.onResume()
        // 리스트 새로고침
        viewModel.loadBlockIps(isRefresh = true)
    }
}
