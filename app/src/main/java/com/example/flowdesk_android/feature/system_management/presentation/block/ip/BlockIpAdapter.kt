package com.example.flowdesk_android.feature.system_management.presentation.block.ip

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.example.flowdesk_android.R
import com.example.flowdesk_android.core.extension.toFormattedDateString
import com.example.flowdesk_android.databinding.ItemBlockListBinding
import com.example.flowdesk_android.feature.system_management.domain.model.BlockIpItem

class BlockIpAdapter(
    private val onItemClick: (BlockIpItem) -> Unit
) : ListAdapter<BlockIpItem, BlockIpAdapter.ViewHolder>(BlockIpDiffCallback) {

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

        fun bind(item: BlockIpItem) {
            // IP 주소 렌더링
            binding.tvTargetValue.text = item.blockIp
            binding.tvTargetValue.typeface = android.graphics.Typeface.MONOSPACE // monospace 적용
            binding.tvBlockedAt.text = item.createdAt?.toFormattedDateString() ?: "-"
            binding.tvBlockReason.text = item.reason ?: "사유 없음"

            // 뷰 가시성 및 색상 조정
            binding.tvBlockedAt.visibility = View.VISIBLE
            binding.tvMatchingType.visibility = View.GONE
            binding.vAccentBar.setBackgroundResource(R.color.color_error)

            binding.root.setOnClickListener {
                onItemClick(item)
            }
        }
    }

    companion object {
        private val BlockIpDiffCallback = object : DiffUtil.ItemCallback<BlockIpItem>() {
            override fun areItemsTheSame(oldItem: BlockIpItem, newItem: BlockIpItem): Boolean =
                oldItem.dbiIdx == newItem.dbiIdx

            override fun areContentsTheSame(oldItem: BlockIpItem, newItem: BlockIpItem): Boolean =
                oldItem == newItem
        }
    }
}
