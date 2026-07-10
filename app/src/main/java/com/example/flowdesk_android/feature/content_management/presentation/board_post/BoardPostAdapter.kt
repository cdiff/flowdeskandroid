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

import com.example.flowdesk_android.core.extension.toFormattedDateString

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

            // 확장 함수를 활용하여 끝점이 없는 yyyy. MM. dd 형식으로 일치
            val dateStr = item.createdAt?.toFormattedDateString() ?: "-"
            binding.tvPostInfo.text = dateStr

            // 게시판 뱃지 처리 (게시판 이름 텍스트 설정)
            val boardName = item.boardName ?: "게시판"
            val badgeColor = getDynamicColorForBoard(boardName)

            // 뱃지 디자인 통일: 진한 고유 배경색 + 흰색 글씨
            binding.tvNoticeBadge.background?.setTint(Color.parseColor(badgeColor))
            binding.tvNoticeBadge.setTextColor(Color.WHITE)

            if (item.isNotice) {
                // 중요글 카드 배경
                binding.root.setBackgroundResource(R.drawable.bg_card_rounded_border_notice)
                
                // 중요 글일 때 뱃지 텍스트 앞에 '중요' 추가
                binding.tvNoticeBadge.text = "중요 $boardName"
            } else {
                // 일반글 카드 배경
                binding.root.setBackgroundResource(R.drawable.bg_card_rounded_border)
                
                // 일반 글 뱃지 텍스트
                binding.tvNoticeBadge.text = boardName
            }

            binding.root.setOnClickListener {
                onItemClicked(item)
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
