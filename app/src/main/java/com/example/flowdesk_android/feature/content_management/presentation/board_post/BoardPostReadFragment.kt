package com.example.flowdesk_android.feature.content_management.presentation.board_post

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.widget.PopupMenu
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.updatePadding
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.navigation.fragment.findNavController
import com.example.flowdesk_android.R
import com.example.flowdesk_android.databinding.FragmentContentBoardPostReadBinding
import com.example.flowdesk_android.databinding.DialogCommonConfirmBinding
import com.example.flowdesk_android.core.extension.showTopToast
import com.example.flowdesk_android.core.extension.toFormattedDateString
import com.example.flowdesk_android.feature.system_management.domain.model.BoardPost
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

@AndroidEntryPoint
class BoardPostReadFragment : Fragment() {

    private var _binding: FragmentContentBoardPostReadBinding? = null
    private val binding get() = _binding!!

    private val viewModel: BoardPostDetailViewModel by viewModels()
    private var boardId: Long = -1L
    private var postId: Long = -1L

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {
            boardId = it.getLong("boardId", -1L)
            postId = it.getLong("postId", -1L)
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentContentBoardPostReadBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // Window insets 처리 (상태바/카메라 홀 침범 예방)
        ViewCompat.setOnApplyWindowInsetsListener(binding.root) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.updatePadding(top = systemBars.top)
            insets
        }

        setupListeners()
        observeViewModel()

        if (postId != -1L) {
            viewModel.loadPostDetail(boardId, postId)
        } else {
            showTopToast("올바르지 않은 게시글 접근입니다.")
            findNavController().popBackStack()
        }
    }

    private fun setupListeners() {
        binding.btnBack.setOnClickListener {
            findNavController().popBackStack()
        }
        binding.btnClose.setOnClickListener {
            findNavController().popBackStack()
        }
    }

    private fun observeViewModel() {
        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                launch {
                    viewModel.uiState.collectLatest { state ->
                        when (state) {
                            is BoardPostDetailUiState.Idle -> {
                                binding.progressBar.visibility = View.GONE
                            }
                            is BoardPostDetailUiState.Loading -> {
                                binding.progressBar.visibility = View.VISIBLE
                                binding.scrollView.visibility = View.INVISIBLE
                            }
                            is BoardPostDetailUiState.Success -> {
                                binding.progressBar.visibility = View.GONE
                                binding.scrollView.visibility = View.VISIBLE
                                bindData(state.post)
                                setupOptionsMenu(state.canUpdate, state.canDelete)
                            }
                            is BoardPostDetailUiState.DeleteSuccess -> {
                                binding.progressBar.visibility = View.GONE
                                showTopToast(getString(R.string.post_toast_delete_success))
                                findNavController().previousBackStackEntry?.savedStateHandle?.set("refresh", true)
                                findNavController().popBackStack()
                            }
                            is BoardPostDetailUiState.Error -> {
                                binding.progressBar.visibility = View.GONE
                                showTopToast(state.message)
                            }
                            else -> {
                                // SaveSuccess 등은 읽기 전용 화면에서 발생하지 않음
                            }
                        }
                    }
                }

                launch {
                    viewModel.toastMessage.collectLatest { msg ->
                        showTopToast(msg)
                    }
                }
            }
        }
    }

    private fun bindData(post: BoardPost) {
        val boardName = post.boardName ?: "게시판"
        binding.tvToolbarTitle.text = boardName
        
        // 중요글 여부에 따른 뱃지 텍스트 분기 ("중요 [게시판명]" vs "[게시판명]")
        if (post.isNotice) {
            binding.tvBoardBadge.text = "중요 $boardName"
        } else {
            binding.tvBoardBadge.text = boardName
        }

        // 상세 페이지 뱃지 디자인: 흰색 바탕 + 각 카테고리별 테두리 및 텍스트 색상 적용
        val badgeColor = getDynamicColorForBoard(boardName)
        val strokeColorInt = android.graphics.Color.parseColor(badgeColor)
        
        binding.tvBoardBadge.setTextColor(strokeColorInt)
        
        val backgroundDrawable = binding.tvBoardBadge.background?.mutate() as? android.graphics.drawable.GradientDrawable
        if (backgroundDrawable != null) {
            val strokeWidthPx = (1 * requireContext().resources.displayMetrics.density).toInt()
            backgroundDrawable.setStroke(strokeWidthPx, strokeColorInt)
        }

        binding.tvPostTitle.text = post.title
        
        // HTML 태그 파싱 렌더링 적용 (HtmlCompat.fromHtml)
        val contentHtml = post.content ?: ""
        binding.tvPostContent.text = androidx.core.text.HtmlCompat.fromHtml(
            contentHtml,
            androidx.core.text.HtmlCompat.FROM_HTML_MODE_LEGACY
        )

        // 기간 정보 유무에 따른 작성자 및 날짜/기간 출력 분기
        val startDate = post.startDtm?.toFormattedDateString()
        val endDate = post.endDtm?.toFormattedDateString()
        val createdDate = post.createdAt?.toFormattedDateString() ?: "-"

        if (!startDate.isNullOrEmpty() && !endDate.isNullOrEmpty()) {
            binding.tvPostDate.text = "작성자 ID: ${post.userSeq}   /   $startDate ~ $endDate"
        } else {
            binding.tvPostDate.text = "작성자 ID: ${post.userSeq}   /   $createdDate"
        }
    }

    // 각 게시판 유형별로 고유 진한 색상을 부여하는 헬퍼 함수
    private fun getDynamicColorForBoard(boardName: String): String {
        return when {
            boardName.contains("공지") -> "#EF4444" // Red
            boardName.contains("자유") -> "#3B82F6" // Blue
            boardName.contains("질문") || boardName.contains("Q&A") || boardName.contains("문의") -> "#10B981" // Green
            boardName.contains("이벤트") || boardName.contains("행사") -> "#F59E0B" // Orange
            boardName.contains("자료") || boardName.contains("가이드") -> "#8B5CF6" // Purple
            else -> {
                val colors = listOf("#8B5CF6", "#EC4899", "#06B6D4", "#6366F1", "#14B8A6")
                val index = Math.abs(boardName.hashCode()) % colors.size
                colors[index]
            }
        }
    }

    private fun setupOptionsMenu(canUpdate: Boolean, canDelete: Boolean) {
        if (!canUpdate && !canDelete) {
            binding.btnMore.visibility = View.GONE
            return
        }

        binding.btnMore.visibility = View.VISIBLE
        binding.btnMore.setOnClickListener { view ->
            val popup = PopupMenu(requireContext(), view)
            
            // 메뉴 옵션 동적 추가
            if (canUpdate) {
                popup.menu.add(0, 1, 0, "수정")
            }
            if (canDelete) {
                popup.menu.add(0, 2, 1, "삭제")
            }

            popup.setOnMenuItemClickListener { menuItem ->
                when (menuItem.itemId) {
                    1 -> {
                        // 수정 페이지(BoardPostDetailFragment)로 이동
                        val bundle = Bundle().apply {
                            putLong("boardId", boardId)
                            putLong("postId", postId)
                        }
                        findNavController().navigate(R.id.boardPostDetailFragment, bundle)
                    }
                    2 -> {
                        showDeleteConfirmDialog()
                    }
                }
                true
            }
            popup.show()
        }
    }

    private fun showDeleteConfirmDialog() {
        val dialogBinding = DialogCommonConfirmBinding.inflate(layoutInflater)
        val dialog = AlertDialog.Builder(requireContext())
            .setView(dialogBinding.root)
            .create()

        dialog.window?.setBackgroundDrawableResource(android.R.color.transparent)

        dialogBinding.tvTitle.text = getString(R.string.post_dialog_delete_title)
        dialogBinding.tvMessage.text = getString(R.string.post_dialog_delete_message)
        dialogBinding.cbConfirm.visibility = View.GONE

        dialogBinding.btnCancel.setOnClickListener {
            dialog.dismiss()
        }

        dialogBinding.btnConfirm.setOnClickListener {
            dialog.dismiss()
            viewModel.deletePost(boardId, postId)
        }

        dialog.show()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
