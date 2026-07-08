package com.example.flowdesk_android.feature.content_management.presentation.board_post

import android.graphics.Color
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.example.flowdesk_android.R
import com.example.flowdesk_android.databinding.ItemContentBoardPostCardBinding
import com.example.flowdesk_android.feature.system_management.domain.model.BoardPost

class BoardPostAdapter(
    private val onItemClicked: (BoardPost) -> Unit
) : ListAdapter<BoardPost, BoardPostAdapter.ViewHolder>(DiffCallback()) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding = ItemContentBoardPostCardBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )
        return ViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    inner class ViewHolder(private val binding: ItemContentBoardPostCardBinding) :
        RecyclerView.ViewHolder(binding.root) {

        fun bind(item: BoardPost) {
            binding.tvPostTitle.text = item.title

            // KST 형식에 맞게 날짜 출력 (2026. 03. 01.)
            val dateStr = item.createdAt?.substringBefore("T")?.replace("-", ". ")?.let { "$it." } ?: "-"
            binding.tvPostInfo.text = "작성자 일련번호: ${item.userSeq}   /   $dateStr"

            // 게시판 뱃지 처리 (게시판 이름 텍스트 설정)
            val boardName = item.boardName ?: "게시판"
            binding.tvNoticeBadge.text = boardName

            // 각 게시판 타입별 뱃지 배경색 다르게 주기 (해시 함수를 활용한 파스텔톤 다이내믹 컬러 부여)
            val badgeColor = getDynamicColorForBoard(boardName)
            binding.tvNoticeBadge.background?.setTint(Color.parseColor(badgeColor))

            // 공지글 스타일링: 공지글인 경우 카드 배경을 은은한 배경으로 강조 설정
            if (item.isNotice) {
                // 공지글 전용 소프트 주황/적색 톤 테두리 또는 배경 지정
                binding.root.setBackgroundResource(R.drawable.bg_card_rounded_border)
                binding.root.background?.setTint(Color.parseColor("#FEF2F2")) // Light red tinted background for notices
                binding.tvNoticeBadge.text = "공지"
                binding.tvNoticeBadge.background?.setTint(Color.parseColor("#EF4444")) // Red color for actual notice tag
            } else {
                binding.root.setBackgroundResource(R.drawable.bg_card_rounded_border)
                binding.root.background?.setTintList(null) // Restore default white
            }

            binding.root.setOnClickListener {
                onItemClicked(item)
            }
        }

        // 게시판 유형별로 일정한 색상을 부여하는 헬퍼 함수
        private fun getDynamicColorForBoard(boardName: String): String {
            return when {
                boardName.contains("공지") -> "#EF4444" // Red
                boardName.contains("자유") -> "#3B82F6" // Blue
                boardName.contains("질문") || boardName.contains("Q&A") || boardName.contains("문의") -> "#10B981" // Green
                boardName.contains("이벤트") || boardName.contains("행사") -> "#F59E0B" // Orange
                boardName.contains("자료") || boardName.contains("가이드") -> "#8B5CF6" // Purple
                else -> {
                    // 해시 코드를 기반으로 기본 5개 중 색상 하나 선택
                    val colors = listOf("#8B5CF6", "#EC4899", "#06B6D4", "#6366F1", "#14B8A6")
                    val index = Math.abs(boardName.hashCode()) % colors.size
                    colors[index]
                }
            }
        }
    }

    private class DiffCallback : DiffUtil.ItemCallback<BoardPost>() {
        override fun areItemsTheSame(oldItem: BoardPost, newItem: BoardPost): Boolean {
            return oldItem.postId == newItem.postId
        }

        override fun areContentsTheSame(oldItem: BoardPost, newItem: BoardPost): Boolean {
            return oldItem == newItem
        }
    }
}
