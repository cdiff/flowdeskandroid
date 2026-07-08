package com.example.flowdesk_android.feature.system_management.presentation.board_type

import android.transition.AutoTransition
import android.transition.TransitionManager
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.example.flowdesk_android.R
import com.example.flowdesk_android.databinding.ItemSystemBoardTypeCardBinding
import com.example.flowdesk_android.feature.system_management.domain.model.BoardType

class BoardTypeAdapter(
    private val onItemClicked: (BoardType) -> Unit,
    private val onToggleStatusClicked: (BoardType) -> Unit,
    private val onDeleteClicked: (BoardType) -> Unit
) : ListAdapter<BoardType, BoardTypeAdapter.ViewHolder>(DiffCallback()) {

    // 아코디언 메뉴가 확장된 아이템들의 boardId 저장
    private val expandedItems = mutableSetOf<Long>()

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding = ItemSystemBoardTypeCardBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )
        return ViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    inner class ViewHolder(private val binding: ItemSystemBoardTypeCardBinding) :
        RecyclerView.ViewHolder(binding.root) {

        fun bind(item: BoardType) {
            binding.tvBoardName.text = item.name
            binding.tvBoardKey.text = item.boardKey
            binding.tvSortOrder.text = "우선순위: ${item.sortOrder}"
            binding.tvBoardDesc.text = item.description ?: "설명 없음"

            // 상태 배지 설정
            if (item.isActive) {
                binding.tvStatusBadge.text = "• 활성"
                binding.tvStatusBadge.setTextColor(ContextCompat.getColor(binding.root.context, R.color.color_success_active))
            } else {
                binding.tvStatusBadge.text = "• 비활성"
                binding.tvStatusBadge.setTextColor(ContextCompat.getColor(binding.root.context, R.color.text_hint))
            }

            // 활성/비활성 텍스트 매핑
            binding.btnToggleStatus.text = if (item.isActive) "비활성화" else "활성화"

            // 아코디언 메뉴 가시성 설정
            val isExpanded = expandedItems.contains(item.boardId)
            binding.llHiddenMenu.visibility = if (isExpanded) View.VISIBLE else View.GONE

            // 아이템 자체 클릭 -> 상세 페이지로 이동
            binding.root.setOnClickListener {
                onItemClicked(item)
            }

            // 더보기(⋮) 클릭 -> 아코디언 확장/축소 애니메이션 수행
            binding.btnMore.setOnClickListener {
                val currentlyExpanded = expandedItems.contains(item.boardId)
                if (currentlyExpanded) {
                    expandedItems.remove(item.boardId)
                } else {
                    expandedItems.add(item.boardId)
                }

                val parent = itemView.parent as? ViewGroup
                if (parent != null) {
                    TransitionManager.beginDelayedTransition(
                        parent,
                        AutoTransition().apply { duration = 200 }
                    )
                }

                binding.llHiddenMenu.visibility =
                    if (expandedItems.contains(item.boardId)) View.VISIBLE else View.GONE
            }

            // 아코디언 메뉴 내 [수정] 클릭
            binding.btnEdit.setOnClickListener {
                onItemClicked(item)
                collapseMenu(item.boardId)
            }

            // 아코디언 메뉴 내 [활성/비활성] 클릭
            binding.btnToggleStatus.setOnClickListener {
                onToggleStatusClicked(item)
                collapseMenu(item.boardId)
            }

            // 아코디언 메뉴 내 [삭제] 클릭
            binding.btnDelete.setOnClickListener {
                onDeleteClicked(item)
                collapseMenu(item.boardId)
            }
        }

        private fun collapseMenu(boardId: Long) {
            expandedItems.remove(boardId)
            val parent = itemView.parent as? ViewGroup
            if (parent != null) {
                TransitionManager.beginDelayedTransition(
                    parent,
                    AutoTransition().apply { duration = 200 }
                )
            }
            binding.llHiddenMenu.visibility = View.GONE
        }
    }

    private class DiffCallback : DiffUtil.ItemCallback<BoardType>() {
        override fun areItemsTheSame(oldItem: BoardType, newItem: BoardType): Boolean {
            return oldItem.boardId == newItem.boardId
        }

        override fun areContentsTheSame(oldItem: BoardType, newItem: BoardType): Boolean {
            return oldItem == newItem
        }
    }
}
