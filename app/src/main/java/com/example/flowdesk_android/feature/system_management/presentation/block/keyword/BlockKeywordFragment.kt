package com.example.flowdesk_android.feature.system_management.presentation.block.keyword

import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.example.flowdesk_android.R
import com.example.flowdesk_android.feature.system_management.presentation.block.BaseBlockListFragment
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class BlockKeywordFragment : BaseBlockListFragment<Any>() {

    // 금칙어 기능이 아직 구현되지 않았으므로 임시로 빈 리스트 어댑터와 Callback 설정
    private val dummyAdapter = object : ListAdapter<Any, RecyclerView.ViewHolder>(AnyDiffCallback) {
        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
            return object : RecyclerView.ViewHolder(parent) {}
        }
        override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {}
    }

    override val adapter: ListAdapter<Any, *> get() = dummyAdapter

    // UI Configuration
    override val titleText: String = "금칙어 목록"
    override val searchHint: String = "금칙어 검색..."
    override val emptyTitleText: String = "등록된 금칙어가 없습니다"
    override val emptySubtitleText: String = "금칙어 추가 버튼으로 필터 단어를 등록하세요"
    override val bannerText: String = "금칙어는 채팅·상담 등 고객 입력 필드에서 자동으로 필터링됩니다."

    override fun setupRecyclerView() {
        binding.rvList.layoutManager = LinearLayoutManager(requireContext())
        binding.rvList.adapter = dummyAdapter
    }

    override fun onSearchQueryChanged(query: String) {
        // TODO: 검색어 변경 시 처리
    }

    override fun onLoadMore() {
        // TODO: 무한 스크롤 처리
    }

    override fun onAddClicked() {
        // TODO: 금칙어 추가 다이얼로그 호출
    }

    override fun observeViewModel() {
        // TODO: 뷰모델 연동 및 showSuccess, showLoading, showError 호출
        showSuccess(emptyList(), 0) // 임시로 빈 화면 노출
    }

    companion object {
        private val AnyDiffCallback = object : DiffUtil.ItemCallback<Any>() {
            override fun areItemsTheSame(oldItem: Any, newItem: Any): Boolean = oldItem == newItem
            override fun areContentsTheSame(oldItem: Any, newItem: Any): Boolean = oldItem == newItem
        }
    }
}
