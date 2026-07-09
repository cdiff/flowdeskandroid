package com.example.flowdesk_android.feature.system_management.presentation.website

import android.graphics.Color
import android.transition.AutoTransition
import android.transition.TransitionManager
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.example.flowdesk_android.R
import com.example.flowdesk_android.databinding.ItemWebsiteCardBinding
import com.example.flowdesk_android.feature.system_management.domain.model.Website

class WebsiteAdapter(
    private val onItemClicked: (Website) -> Unit,
    private val onToggleStatusClicked: (Website) -> Unit,
    private val onDeleteClicked: (Website) -> Unit
) : ListAdapter<Website, WebsiteAdapter.ViewHolder>(DiffCallback()) {

    // 확장 상태인 아이템들의 webCode 관리
    private val expandedWebsites = mutableSetOf<String>()

    private var canUpdate: Boolean = true
    private var canDelete: Boolean = true

    fun setPermissions(canUpdate: Boolean, canDelete: Boolean) {
        this.canUpdate = canUpdate
        this.canDelete = canDelete
        notifyDataSetChanged()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding = ItemWebsiteCardBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )
        return ViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    inner class ViewHolder(private val binding: ItemWebsiteCardBinding) :
        RecyclerView.ViewHolder(binding.root) {

        fun bind(item: Website) {
            binding.tvWebTitle.text = item.webTitle
            binding.tvWebCode.text = item.webCode
            binding.tvWebUrl.text = item.webUrl
            binding.tvUserName.text = item.userName ?: "-"
            binding.tvDuplicateDays.text = binding.root.context.getString(
                R.string.website_label_duplicate_days,
                item.duplicateAllowAfterDays
            )

            // 상태 배지 (• 활성 / • 비활성)
            if (item.isActive) {
                binding.tvStatusBadge.text = "• 활성"
                binding.tvStatusBadge.setTextColor(Color.parseColor("#22C55E")) // Green
            } else {
                binding.tvStatusBadge.text = "• 비활성"
                binding.tvStatusBadge.setTextColor(Color.parseColor("#94A3B8")) // Slate Gray
            }

            // 권한 제어
            binding.btnToggleStatus.visibility = if (canUpdate) View.VISIBLE else View.GONE
            binding.btnDelete.visibility = if (canDelete) View.VISIBLE else View.GONE
            if (!canUpdate && !canDelete) {
                binding.btnMore.visibility = View.GONE
            } else {
                binding.btnMore.visibility = View.VISIBLE
            }

            // 아코디언 메뉴 확장/축소 상태 제어
            val isExpanded = expandedWebsites.contains(item.webCode) && (canUpdate || canDelete)
            binding.llHiddenMenu.visibility = if (isExpanded) View.VISIBLE else View.GONE
            
            // 상태 토글 텍스트 세팅
            binding.btnToggleStatus.text = if (item.isActive) "비활성화" else "활성화"

            // 더보기 버튼 누를 시 카드 확장/축소 애니메이션 처리
            binding.btnMore.setOnClickListener {
                val isCurrentlyExpanded = expandedWebsites.contains(item.webCode)
                if (isCurrentlyExpanded) {
                    expandedWebsites.remove(item.webCode)
                } else {
                    expandedWebsites.add(item.webCode)
                }
                
                val parent = itemView.parent as? ViewGroup
                if (parent != null) {
                    TransitionManager.beginDelayedTransition(
                        parent,
                        AutoTransition().apply { duration = 200 }
                    )
                }
                
                binding.llHiddenMenu.visibility = if (expandedWebsites.contains(item.webCode)) View.VISIBLE else View.GONE
            }

            // 카드 클릭 시 상세 페이지로 이동
            binding.root.setOnClickListener {
                onItemClicked(item)
            }

            // 아코디언 메뉴 내 상태 토글 버튼 클릭 리스너
            binding.btnToggleStatus.setOnClickListener {
                onToggleStatusClicked(item)
                expandedWebsites.remove(item.webCode)
                
                val parent = itemView.parent as? ViewGroup
                if (parent != null) {
                    TransitionManager.beginDelayedTransition(
                        parent,
                        AutoTransition().apply { duration = 200 }
                    )
                }
                binding.llHiddenMenu.visibility = View.GONE
            }

            // 아코디언 메뉴 내 삭제 버튼 클릭 리스너
            binding.btnDelete.setOnClickListener {
                onDeleteClicked(item)
                expandedWebsites.remove(item.webCode)
                
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

    private class DiffCallback : DiffUtil.ItemCallback<Website>() {
        override fun areItemsTheSame(oldItem: Website, newItem: Website): Boolean {
            return oldItem.webCode == newItem.webCode
        }

        override fun areContentsTheSame(oldItem: Website, newItem: Website): Boolean {
            return oldItem == newItem
        }
    }
}
