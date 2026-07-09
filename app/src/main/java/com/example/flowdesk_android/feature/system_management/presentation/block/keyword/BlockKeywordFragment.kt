package com.example.flowdesk_android.feature.system_management.presentation.block.keyword

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
import com.example.flowdesk_android.feature.system_management.domain.model.BlockWordItem
import com.example.flowdesk_android.feature.system_management.presentation.block.BaseBlockListFragment
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

@AndroidEntryPoint
class BlockKeywordFragment : BaseBlockListFragment<BlockWordItem>() {

    private val viewModel: BlockKeywordViewModel by activityViewModels()

    private lateinit var _adapter: BlockKeywordAdapter
    override val adapter: ListAdapter<BlockWordItem, *> get() = _adapter

    // UI Configuration
    override val titleText: String = "금칙어 목록"
    override val searchHint: String = "금칙어 검색..."
    override val emptyTitleText: String = "등록된 금칙어가 없습니다"
    override val emptySubtitleText: String = "금칙어 추가 버튼으로 필터 단어를 등록하세요"
    override val bannerText: String = "금칙어는 채팅·상담 등 고객 입력 필드에서 자동으로 필터링됩니다."

    override fun setupRecyclerView() {
        _adapter = BlockKeywordAdapter { item ->
            val bundle = Bundle().apply {
                putLong("blockWordId", item.dbwIdx)
            }
            findNavController().navigate(R.id.blockKeywordDetailFragment, bundle)
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
                            is BlockWordUiState.Loading -> {
                                showLoading()
                            }
                            is BlockWordUiState.Success -> {
                                showSuccess(state.items, state.totalCount)
                                updateWritePermission(state.canWrite)
                            }
                            is BlockWordUiState.Error -> {
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
        val bottomSheet = KeywordBlockCreateBottomSheet.newInstance()
        bottomSheet.show(childFragmentManager, "KeywordBlockCreateBottomSheet")
    }

    override fun onResume() {
        super.onResume()
        viewModel.triggerRefresh()
    }
}
