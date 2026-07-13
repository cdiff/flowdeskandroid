package com.example.flowdesk_android.feature.system_management.presentation.block.phone

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
import com.example.flowdesk_android.feature.system_management.domain.model.BlockPhoneItem
import com.example.flowdesk_android.feature.system_management.presentation.block.BaseBlockListFragment
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

@AndroidEntryPoint
class BlockPhoneFragment : BaseBlockListFragment<BlockPhoneItem>() {

    private val viewModel: BlockPhoneViewModel by activityViewModels()

    private lateinit var _adapter: BlockPhoneAdapter
    override val adapter: ListAdapter<BlockPhoneItem, *> get() = _adapter

    // UI Configuration
    override val titleText: String = "휴대폰 차단 목록"
    override val searchHint: String = "휴대폰 번호를 검색하세요."
    override val emptyTitleText: String = "차단된 번호가 없습니다"
    override val emptySubtitleText: String = "번호 추가 버튼으로 차단할 번호를 등록하세요"

    override fun setupRecyclerView() {
        _adapter = BlockPhoneAdapter { item ->
            val bundle = Bundle().apply {
                putLong("blockHpId", item.dbhIdx)
            }
            findNavController().navigate(R.id.blockPhoneDetailFragment, bundle)
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
                            is BlockPhoneUiState.Loading -> {
                                showLoading()
                            }
                            is BlockPhoneUiState.Success -> {
                                showSuccess(state.items, state.totalCount)
                                updateWritePermission(state.canWrite)
                            }
                            is BlockPhoneUiState.Error -> {
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
        val bottomSheet = PhoneBlockCreateBottomSheet.newInstance()
        bottomSheet.show(childFragmentManager, "PhoneBlockCreateBottomSheet")
    }

    override fun onResume() {
        super.onResume()
        // 리스트 새로고침
        viewModel.triggerRefresh()
    }
}
