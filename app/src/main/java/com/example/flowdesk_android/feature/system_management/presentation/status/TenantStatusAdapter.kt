package com.example.flowdesk_android.feature.system_management.presentation.status

import android.graphics.Color
import android.graphics.drawable.GradientDrawable
import android.transition.AutoTransition
import android.transition.TransitionManager
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
    private val onToggleStatusClicked: (TenantStatus) -> Unit,
    private val onDeleteClicked: (TenantStatus) -> Unit
) : ListAdapter<TenantStatus, TenantStatusAdapter.ViewHolder>(DiffCallback()) {

    // 확장 상태인 아이템들의 tenantStatusId 관리
    private val expandedItems = mutableSetOf<Long>()

    private var canUpdate: Boolean = true
    private var canDelete: Boolean = true

    fun setPermissions(canUpdate: Boolean, canDelete: Boolean) {
        this.canUpdate = canUpdate
        this.canDelete = canDelete
        notifyDataSetChanged()
    }

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
                binding.tvActiveStatus.setTextColor(Color.parseColor("#22C55E"))
            } else {
                binding.tvActiveStatus.text = "• 비활성"
                binding.tvActiveStatus.setTextColor(Color.parseColor("#94A3B8"))
            }

            // 상태 토글 텍스트 세팅
            binding.btnToggleStatus.text = if (item.isActive) "비활성화" else "활성화"

            // Dot Color parsing dynamically
            try {
                val parsedColor = Color.parseColor(item.color)
                val drawable = GradientDrawable().apply {
                    shape = GradientDrawable.OVAL
                    setColor(parsedColor)
                }
                binding.viewStatusDot.background = drawable
            } catch (e: Exception) {
                binding.viewStatusDot.setBackgroundResource(R.drawable.bg_circle_blue)
            }

            // 권한 제어
            binding.btnToggleStatus.visibility = if (canUpdate) View.VISIBLE else View.GONE
            binding.btnDelete.visibility = if (canDelete) View.VISIBLE else View.GONE
            if (!canUpdate && !canDelete) {
                binding.ivMore.visibility = View.GONE
            } else {
                binding.ivMore.visibility = View.VISIBLE
            }

            // 아코디언 확장/축소 상태 적용
            val isExpanded = expandedItems.contains(item.tenantStatusId) && (canUpdate || canDelete)
            binding.llHiddenMenu.visibility = if (isExpanded) View.VISIBLE else View.GONE

            // 카드 클릭 시 상세 페이지로 이동
            binding.root.setOnClickListener {
                onItemClicked(item)
            }

            // 더보기 버튼 - 카드 아코디언 확장/축소 애니메이션
            binding.ivMore.setOnClickListener {
                val isCurrentlyExpanded = expandedItems.contains(item.tenantStatusId)
                if (isCurrentlyExpanded) {
                    expandedItems.remove(item.tenantStatusId)
                } else {
                    expandedItems.add(item.tenantStatusId)
                }

                val parent = itemView.parent as? ViewGroup
                if (parent != null) {
                    TransitionManager.beginDelayedTransition(
                        parent,
                        AutoTransition().apply { duration = 200 }
                    )
                }

                binding.llHiddenMenu.visibility =
                    if (expandedItems.contains(item.tenantStatusId)) View.VISIBLE else View.GONE
            }

            // 활성화/비활성화 버튼 클릭
            binding.btnToggleStatus.setOnClickListener {
                onToggleStatusClicked(item)
                expandedItems.remove(item.tenantStatusId)

                val parent = itemView.parent as? ViewGroup
                if (parent != null) {
                    TransitionManager.beginDelayedTransition(
                        parent,
                        AutoTransition().apply { duration = 200 }
                    )
                }
                binding.llHiddenMenu.visibility = View.GONE
            }

            // 삭제 버튼 클릭
            binding.btnDelete.setOnClickListener {
                onDeleteClicked(item)
                expandedItems.remove(item.tenantStatusId)

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
