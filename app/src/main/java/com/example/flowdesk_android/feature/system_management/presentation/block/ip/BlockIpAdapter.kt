package com.example.flowdesk_android.feature.system_management.presentation.block.ip

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.example.flowdesk_android.core.util.DateUtils
import com.example.flowdesk_android.databinding.ItemBlockIpBinding
import com.example.flowdesk_android.feature.system_management.domain.model.BlockIpItem

class BlockIpAdapter(
    private val onItemClick: (BlockIpItem) -> Unit
) : ListAdapter<BlockIpItem, BlockIpAdapter.ViewHolder>(BlockIpDiffCallback) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding = ItemBlockIpBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )
        return ViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    inner class ViewHolder(private val binding: ItemBlockIpBinding) :
        RecyclerView.ViewHolder(binding.root) {

        fun bind(item: BlockIpItem) {
            binding.tvIpAddress.text = item.blockIp
            binding.tvBlockedAt.text = DateUtils.formatIsoDate(item.createdAt)
            binding.tvBlockReason.text = item.reason ?: "사유 없음"

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
