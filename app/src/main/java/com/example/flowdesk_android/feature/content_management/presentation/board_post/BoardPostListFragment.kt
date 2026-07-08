package com.example.flowdesk_android.feature.content_management.presentation.board_post

import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.flowdesk_android.R
import com.example.flowdesk_android.databinding.FragmentContentBoardPostListBinding
import com.example.flowdesk_android.databinding.ItemStatusGroupChipBinding
import com.example.flowdesk_android.core.extension.showTopToast
import com.example.flowdesk_android.feature.system_management.domain.model.BoardType
import com.example.flowdesk_android.feature.system_management.domain.model.BoardPost
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

@AndroidEntryPoint
class BoardPostListFragment : Fragment() {

    private var _binding: FragmentContentBoardPostListBinding? = null
    private val binding get() = _binding!!

    private val viewModel: BoardPostListViewModel by viewModels()
    private lateinit var adapter: BoardPostAdapter

    // 동적으로 생성한 칩 뷰들의 참조 저장 (선택 상태 변경 시 빠른 업데이트용)
    private val chipViews = mutableMapOf<Long, View>()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentContentBoardPostListBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setupRecyclerView()
        setupListeners()
        observeViewModel()

        // 상세/추가 복귀 시 목록 새로고침 감지
        findNavController().currentBackStackEntry?.savedStateHandle?.getLiveData<Boolean>("refresh")
            ?.observe(viewLifecycleOwner) { refresh ->
                if (refresh == true) {
                    viewModel.triggerRefresh()
                    findNavController().currentBackStackEntry?.savedStateHandle?.remove<Boolean>("refresh")
                }
            }
    }

    private fun setupRecyclerView() {
        adapter = BoardPostAdapter { post ->
            val bundle = Bundle().apply {
                putLong("boardId", post.boardId)
                putLong("postId", post.postId)
            }
            findNavController().navigate(R.id.boardPostDetailFragment, bundle)
        }
        binding.rvPosts.layoutManager = LinearLayoutManager(requireContext())
        binding.rvPosts.adapter = adapter
    }

    private fun setupListeners() {
        binding.fabWrite.setOnClickListener {
            val currentBoardId = viewModel.selectedBoardId.value
            if (currentBoardId == -1L) {
                showTopToast(getString(R.string.post_toast_select_board_first))
                return@setOnClickListener
            }
            // 신규 작성 모드로 이동 (boardId 전달, postId = -1L)
            val bundle = Bundle().apply {
                putLong("boardId", currentBoardId)
                putLong("postId", -1L)
            }
            findNavController().navigate(R.id.boardPostDetailFragment, bundle)
        }

        binding.btnClearSearch.setOnClickListener {
            binding.etSearch.text?.clear()
        }

        binding.etSearch.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                val text = s?.toString() ?: ""
                viewModel.updateSearchQuery(text)
                binding.btnClearSearch.visibility = if (text.isNotEmpty()) View.VISIBLE else View.GONE
            }
            override fun afterTextChanged(s: Editable?) {}
        })
    }

    private fun observeViewModel() {
        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                launch {
                    viewModel.uiState.collectLatest { state ->
                        when (state) {
                            is BoardPostListUiState.Loading -> {
                                binding.progressBar.visibility = View.VISIBLE
                                binding.llEmpty.visibility = View.GONE
                            }
                            is BoardPostListUiState.Success -> {
                                binding.progressBar.visibility = View.GONE
                                adapter.submitList(state.posts)
                                binding.tvListCount.text = "  ${state.totalCount}건"

                                // 칩 탭 드로잉 (최초 1회 또는 보드 타입 변경 시)
                                renderBoardChips(state.boardTypes)

                                if (state.posts.isEmpty()) {
                                    binding.llEmpty.visibility = View.VISIBLE
                                    binding.rvPosts.visibility = View.GONE
                                } else {
                                    binding.llEmpty.visibility = View.GONE
                                    binding.rvPosts.visibility = View.VISIBLE
                                }
                            }
                            is BoardPostListUiState.Error -> {
                                binding.progressBar.visibility = View.GONE
                                showTopToast(state.message)
                            }
                        }
                    }
                }

                launch {
                    viewModel.selectedBoardId.collectLatest { selectedId ->
                        updateChipSelection(selectedId)
                    }
                }
            }
        }
    }

    // Toss 스타일의 슬림 칩 탭 렌더링
    private fun renderBoardChips(boards: List<BoardType>) {
        if (chipViews.size == boards.size && chipViews.keys.containsAll(boards.map { it.boardId })) {
            return // 기존에 그려진 칩과 개수/구성이 같으면 드로잉 건너뛰고 선택 상태만 업데이트
        }
        binding.layoutBoardTabs.removeAllViews()
        chipViews.clear()

        val inflater = LayoutInflater.from(requireContext())
        boards.forEach { board ->
            val chipBinding = ItemStatusGroupChipBinding.inflate(inflater, binding.layoutBoardTabs, false)
            chipBinding.tvGroupName.text = board.name
            chipBinding.root.setOnClickListener {
                viewModel.selectBoard(board.boardId)
            }
            binding.layoutBoardTabs.addView(chipBinding.root)
            chipViews[board.boardId] = chipBinding.root
        }

        // 현재 활성화된 칩 선택
        updateChipSelection(viewModel.selectedBoardId.value)
    }

    // 칩 선택 상태에 따른 색상 및 드로어블 실시간 변경
    private fun updateChipSelection(selectedId: Long) {
        chipViews.forEach { (boardId, view) ->
            val isSelected = boardId == selectedId
            val tvChipName = view.findViewById<TextView>(R.id.tv_group_name)

            if (isSelected) {
                view.setBackgroundResource(R.drawable.bg_chip_filter_selected)
                tvChipName.setTextColor(ContextCompat.getColor(requireContext(), R.color.white))
            } else {
                view.setBackgroundResource(R.drawable.bg_chip_filter)
                tvChipName.setTextColor(ContextCompat.getColor(requireContext(), R.color.text_hint))
            }
        }
    }

    override fun onDestroyView() {
        binding.rvPosts.adapter = null
        super.onDestroyView()
        _binding = null
        chipViews.clear()
    }
}
