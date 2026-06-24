package com.example.flowdesk_android.feature.super_admin.presentation.tenants

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
import com.example.flowdesk_android.databinding.ItemSuperAdminTenantBinding
import com.example.flowdesk_android.feature.super_admin.domain.model.Tenant

class TenantAdapter(
    private val onItemClick: (Tenant) -> Unit,
    private val onToggleStatusClick: (Tenant) -> Unit,
    private val onDeleteClick: (Tenant) -> Unit
) : ListAdapter<Tenant, TenantAdapter.TenantViewHolder>(TenantDiffCallback()) {

    private val expandedItems = mutableSetOf<Int>()

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): TenantViewHolder {
        val binding = ItemSuperAdminTenantBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )
        return TenantViewHolder(binding)
    }

    override fun onBindViewHolder(holder: TenantViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    inner class TenantViewHolder(
        private val binding: ItemSuperAdminTenantBinding
    ) : RecyclerView.ViewHolder(binding.root) {

        fun bind(tenant: Tenant) {
            val context = itemView.context
            
            // displayName이 있으면 표시, 없으면 tenantName
            binding.tvTenantName.text   = tenant.displayName.ifBlank { tenant.tenantName }
            binding.tvTenantDomain.text = tenant.domain ?: tenant.tenantName

            // 상태 배지
            if (tenant.isActive) {
                binding.tvTenantStatus.text = context.getString(R.string.label_status_active)
                binding.tvTenantStatus.setTextColor(ContextCompat.getColor(context, R.color.color_success))
                binding.tvTenantStatus.setBackgroundResource(R.drawable.bg_tag_light_green)
            } else {
                binding.tvTenantStatus.text = context.getString(R.string.label_status_inactive)
                binding.tvTenantStatus.setTextColor(ContextCompat.getColor(context, R.color.text_secondary))
                binding.tvTenantStatus.setBackgroundResource(R.drawable.bg_rect_rounded_light_gray)
            }

            // 아이템 클릭 → 상세
            itemView.setOnClickListener { onItemClick(tenant) }

            // 더보기 메뉴 expand/collapse
            val isExpanded = expandedItems.contains(tenant.tenantId)
            binding.llHiddenMenu.visibility = if (isExpanded) View.VISIBLE else View.GONE
            binding.btnToggleStatus.text    = if (tenant.isActive) {
                context.getString(R.string.label_status_inactive) + "화"
            } else {
                context.getString(R.string.label_status_active) + "화"
            }

            binding.ivMore.setOnClickListener {
                val nowExpanded = expandedItems.contains(tenant.tenantId)
                if (nowExpanded) expandedItems.remove(tenant.tenantId)
                else expandedItems.add(tenant.tenantId)

                (itemView.parent as? ViewGroup)?.let {
                    TransitionManager.beginDelayedTransition(it, AutoTransition().apply { duration = 180 })
                }
                binding.llHiddenMenu.visibility =
                    if (expandedItems.contains(tenant.tenantId)) View.VISIBLE else View.GONE
            }

            binding.btnToggleStatus.setOnClickListener {
                onToggleStatusClick(tenant)
                expandedItems.remove(tenant.tenantId)
                binding.llHiddenMenu.visibility = View.GONE
            }

            binding.btnDelete.setOnClickListener {
                onDeleteClick(tenant)
                expandedItems.remove(tenant.tenantId)
                binding.llHiddenMenu.visibility = View.GONE
            }
        }
    }
}

class TenantDiffCallback : DiffUtil.ItemCallback<Tenant>() {
    override fun areItemsTheSame(oldItem: Tenant, newItem: Tenant) = oldItem.tenantId == newItem.tenantId
    override fun areContentsTheSame(oldItem: Tenant, newItem: Tenant) = oldItem == newItem
}
