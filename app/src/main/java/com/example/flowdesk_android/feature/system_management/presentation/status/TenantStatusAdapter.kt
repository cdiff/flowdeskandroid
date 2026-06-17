package com.example.flowdesk_android.feature.system_management.presentation.status

import android.graphics.Color
import android.graphics.drawable.GradientDrawable
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.example.flowdesk_android.R
import com.example.flowdesk_android.feature.system_management.domain.model.TenantStatus

class TenantStatusAdapter(
    private val onItemClicked: (TenantStatus) -> Unit,
    private val onMoreClicked: (TenantStatus, View) -> Unit
) : ListAdapter<TenantStatus, TenantStatusAdapter.ViewHolder>(DiffCallback()) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_system_tenant_status, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    inner class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val viewStatusDot: View = itemView.findViewById(R.id.view_status_dot)
        private val tvStatusName: TextView = itemView.findViewById(R.id.tv_status_name)
        private val tvStatusKey: TextView = itemView.findViewById(R.id.tv_status_key)
        private val tvActiveStatus: TextView = itemView.findViewById(R.id.tv_active_status)
        private val ivMore: ImageView = itemView.findViewById(R.id.iv_more)
        private val tvStatusDesc: TextView = itemView.findViewById(R.id.tv_status_desc)

        fun bind(item: TenantStatus) {
            tvStatusName.text = item.statusName
            tvStatusKey.text = item.statusKey
            tvStatusDesc.text = item.description

            // Active/Inactive Label & Color setup
            if (item.isActive) {
                tvActiveStatus.text = "• 활성"
                tvActiveStatus.setTextColor(Color.parseColor("#22C55E")) // Green
            } else {
                tvActiveStatus.text = "• 비활성"
                tvActiveStatus.setTextColor(Color.parseColor("#94A3B8")) // Slate Gray
            }

            // Dot Color parsing dynamically
            try {
                val parsedColor = Color.parseColor(item.color)
                val drawable = GradientDrawable().apply {
                    shape = GradientDrawable.OVAL
                    setColor(parsedColor)
                }
                viewStatusDot.background = drawable
            } catch (e: Exception) {
                // Fallback to default indicator if invalid hex string
                viewStatusDot.setBackgroundResource(R.drawable.bg_circle_blue)
            }

            itemView.setOnClickListener {
                onItemClicked(item)
            }

            ivMore.setOnClickListener { view ->
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
