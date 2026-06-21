package com.example.flowdesk_android.feature.system_management.presentation.block.keyword

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.example.flowdesk_android.R
import com.example.flowdesk_android.databinding.ItemBlockListBinding
import com.example.flowdesk_android.feature.system_management.domain.model.BlockWordItem

class BlockKeywordAdapter(
    private val onItemClick: (BlockWordItem) -> Unit
) : ListAdapter<BlockWordItem, BlockKeywordAdapter.ViewHolder>(BlockKeywordDiffCallback) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding = ItemBlockListBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )
        return ViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    inner class ViewHolder(private val binding: ItemBlockListBinding) :
        RecyclerView.ViewHolder(binding.root) {

        fun bind(item: BlockWordItem) {
            // 금칙어 텍스트 및 기본 정보 바인딩
            binding.tvTargetValue.text = item.blockWord
            binding.tvTargetValue.typeface = android.graphics.Typeface.DEFAULT
            binding.tvBlockReason.text = item.reason ?: "사유 없음"

            // 금칙어 전용 매칭 타입 뱃지 활성화 및 날짜 감추기
            val friendlyMatchType = when (item.matchType) {
                "EXACT" -> "완전 일치"
                "REGEX" -> "패턴 차단"
                "CONTAINS" -> "포함 차단"
                else -> item.matchType
            }
            binding.tvMatchingType.text = friendlyMatchType
            binding.tvMatchingType.visibility = View.VISIBLE
            binding.tvBlockedAt.visibility = View.GONE

            // 금칙어 전용 주황색 액센트 바 색상 지정
            binding.vAccentBar.setBackgroundResource(R.color.color_warning)

            binding.root.setOnClickListener {
                onItemClick(item)
            }
        }
    }

    companion object {
        private val BlockKeywordDiffCallback = object : DiffUtil.ItemCallback<BlockWordItem>() {
            override fun areItemsTheSame(oldItem: BlockWordItem, newItem: BlockWordItem): Boolean =
                oldItem.dbwIdx == newItem.dbwIdx

            override fun areContentsTheSame(oldItem: BlockWordItem, newItem: BlockWordItem): Boolean =
                oldItem == newItem
        }
    }
}
