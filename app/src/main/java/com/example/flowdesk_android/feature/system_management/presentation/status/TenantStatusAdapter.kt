package com.example.flowdesk_android.feature.system_management.presentation.status

import android.graphics.Color
import android.graphics.drawable.GradientDrawable
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.example.flowdesk_android.R
import com.example.flowdesk_android.databinding.ItemSystemTenantStatusBinding
import com.example.flowdesk_android.feature.system_management.domain.model.TenantStatus

class TenantStatusAdapter(
    private val onItemClicked: (TenantStatus) -> Unit,
    private val onMoreClicked: (TenantStatus, View) -> Unit
) : ListAdapter<TenantStatus, TenantStatusAdapter.ViewHolder>(DiffCallback()) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding = ItemSystemTenantStatusBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )
        return ViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    inner class ViewHolder(private val binding: ItemSystemTenantStatusBinding) :
        RecyclerView.ViewHolder(binding.root) {

        fun bind(item: TenantStatus) {
            binding.tvStatusName.text = item.statusName
            binding.tvStatusKey.text = item.statusKey
            binding.tvStatusDesc.text = item.description

            // Active/Inactive Label & Color setup
            if (item.isActive) {
                binding.tvActiveStatus.text = "• 활성"
                binding.tvActiveStatus.setTextColor(Color.parseColor("#22C55E")) // Green
            } else {
                binding.tvActiveStatus.text = "• 비활성"
                binding.tvActiveStatus.setTextColor(Color.parseColor("#94A3B8")) // Slate Gray
            }

            // Dot Color parsing dynamically
            try {
                val parsedColor = Color.parseColor(item.color)
                val drawable = GradientDrawable().apply {
                    shape = GradientDrawable.OVAL
                    setColor(parsedColor)
                }
                binding.viewStatusDot.background = drawable
            } catch (e: Exception) {
                // Fallback to default indicator if invalid hex string
                binding.viewStatusDot.setBackgroundResource(R.drawable.bg_circle_blue)
            }

            binding.root.setOnClickListener {
                onItemClicked(item)
            }

            binding.ivMore.setOnClickListener { view ->
                onMoreClicked(item, view)
            }
        }
    }

    private class DiffCallback : DiffUtil.ItemCallback<TenantStatus>() {
        override fun areItemsTheSame(oldItem: TenantStatus, newItem: TenantStatus): Boolean {
            return oldItem.tenantStatusId == newItem.tenantStatusId
        }

        override fun areContentsTheSame(oldItem: TenantStatus, newItem: TenantStatus): Boolean {
            return oldItem == newItem
        }
    }
}
