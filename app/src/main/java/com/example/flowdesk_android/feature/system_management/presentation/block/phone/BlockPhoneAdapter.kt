package com.example.flowdesk_android.feature.system_management.presentation.block.phone

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.example.flowdesk_android.R
import com.example.flowdesk_android.core.extension.toFormattedDateString
import com.example.flowdesk_android.databinding.ItemBlockListBinding
import com.example.flowdesk_android.feature.system_management.domain.model.BlockPhoneItem

class BlockPhoneAdapter(
    private val onItemClick: (BlockPhoneItem) -> Unit
) : ListAdapter<BlockPhoneItem, BlockPhoneAdapter.ViewHolder>(BlockPhoneDiffCallback) {

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

        fun bind(item: BlockPhoneItem) {
            // 휴대폰 번호 렌더링 (포맷팅 적용)
            binding.tvTargetValue.text = formatPhoneNumber(item.blockHp)
            binding.tvTargetValue.typeface = android.graphics.Typeface.DEFAULT // 기본 폰트
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

    private fun formatPhoneNumber(number: String): String {
        val cleanNumber = number.replace(Regex("[^0-9]"), "")
        return when {
            cleanNumber.length == 11 -> {
                "${cleanNumber.substring(0, 3)}-${cleanNumber.substring(3, 7)}-${cleanNumber.substring(7)}"
            }
            cleanNumber.length == 10 -> {
                if (cleanNumber.startsWith("02")) {
                    "${cleanNumber.substring(0, 2)}-${cleanNumber.substring(2, 6)}-${cleanNumber.substring(6)}"
                } else {
                    "${cleanNumber.substring(0, 3)}-${cleanNumber.substring(3, 6)}-${cleanNumber.substring(6)}"
                }
            }
            cleanNumber.length == 9 && cleanNumber.startsWith("02") -> {
                "${cleanNumber.substring(0, 2)}-${cleanNumber.substring(2, 5)}-${cleanNumber.substring(5)}"
            }
            else -> number
        }
    }

    companion object {
        private val BlockPhoneDiffCallback = object : DiffUtil.ItemCallback<BlockPhoneItem>() {
            override fun areItemsTheSame(oldItem: BlockPhoneItem, newItem: BlockPhoneItem): Boolean =
                oldItem.dbhIdx == newItem.dbhIdx

            override fun areContentsTheSame(oldItem: BlockPhoneItem, newItem: BlockPhoneItem): Boolean =
                oldItem == newItem
        }
    }
}
