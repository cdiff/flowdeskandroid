package com.example.flowdesk_android.feature.super_admin.presentation.tenants

import android.transition.AutoTransition
import android.transition.TransitionManager
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.example.flowdesk_android.R
import com.example.flowdesk_android.feature.super_admin.domain.model.Tenant

class TenantAdapter(
    private val onItemClick: (Tenant) -> Unit,
    private val onToggleStatusClick: (Tenant) -> Unit,
    private val onDeleteClick: (Tenant) -> Unit
) : ListAdapter<Tenant, TenantAdapter.TenantViewHolder>(TenantDiffCallback()) {

    private val expandedItems = mutableSetOf<Int>()

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): TenantViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_super_admin_tenant, parent, false)
        return TenantViewHolder(view)
    }

    override fun onBindViewHolder(holder: TenantViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    inner class TenantViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val tvInitial: TextView     = itemView.findViewById(R.id.tv_tenant_initial)
        private val tvName: TextView        = itemView.findViewById(R.id.tv_tenant_name)
        private val tvDomain: TextView      = itemView.findViewById(R.id.tv_tenant_domain)
        private val tvStatus: TextView      = itemView.findViewById(R.id.tv_tenant_status)
        private val ivMore: View            = itemView.findViewById(R.id.iv_more)
        private val llHiddenMenu: View?     = itemView.findViewById(R.id.ll_hidden_menu)
        private val btnToggleStatus: TextView? = itemView.findViewById(R.id.btn_toggle_status)
        private val btnDelete: View?        = itemView.findViewById(R.id.btn_delete)

        fun bind(tenant: Tenant) {
            // displayName이 있으면 표시, 없으면 tenantName
            tvInitial.text = (tenant.displayName.ifBlank { tenant.tenantName })
                .firstOrNull()?.uppercase() ?: "T"
            tvName.text   = tenant.displayName.ifBlank { tenant.tenantName }
            tvDomain.text = tenant.domain ?: tenant.tenantName

            // 상태 배지
            if (tenant.isActive) {
                tvStatus.text = "활성"
                tvStatus.setTextColor(ContextCompat.getColor(itemView.context, R.color.green_accent))
                tvStatus.setBackgroundResource(R.drawable.bg_tag_light_green)
            } else {
                tvStatus.text = "비활성"
                tvStatus.setTextColor(ContextCompat.getColor(itemView.context, R.color.gray_text))
                tvStatus.setBackgroundResource(R.drawable.bg_rect_rounded_light_gray)
            }

            // 아이템 클릭 → 상세
            itemView.setOnClickListener { onItemClick(tenant) }

            // 더보기 메뉴 expand/collapse
            val isExpanded = expandedItems.contains(tenant.tenantId)
            llHiddenMenu?.visibility = if (isExpanded) View.VISIBLE else View.GONE
            btnToggleStatus?.text    = if (tenant.isActive) "비활성화" else "활성화"

            ivMore.setOnClickListener {
                val nowExpanded = expandedItems.contains(tenant.tenantId)
                if (nowExpanded) expandedItems.remove(tenant.tenantId)
                else expandedItems.add(tenant.tenantId)

                (itemView.parent as? ViewGroup)?.let {
                    TransitionManager.beginDelayedTransition(it, AutoTransition().apply { duration = 180 })
                }
                llHiddenMenu?.visibility =
                    if (expandedItems.contains(tenant.tenantId)) View.VISIBLE else View.GONE
            }

            btnToggleStatus?.setOnClickListener {
                onToggleStatusClick(tenant)
                expandedItems.remove(tenant.tenantId)
                llHiddenMenu?.visibility = View.GONE
            }

            btnDelete?.setOnClickListener {
                onDeleteClick(tenant)
                expandedItems.remove(tenant.tenantId)
                llHiddenMenu?.visibility = View.GONE
            }
        }
    }
}

class TenantDiffCallback : DiffUtil.ItemCallback<Tenant>() {
    override fun areItemsTheSame(oldItem: Tenant, newItem: Tenant) = oldItem.tenantId == newItem.tenantId
    override fun areContentsTheSame(oldItem: Tenant, newItem: Tenant) = oldItem == newItem
}
