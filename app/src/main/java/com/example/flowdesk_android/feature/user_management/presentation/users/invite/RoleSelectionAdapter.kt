package com.example.flowdesk_android.feature.user_management.presentation.users.invite

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.example.flowdesk_android.R
import com.example.flowdesk_android.databinding.ItemRoleSelectionGridBinding
import com.example.flowdesk_android.feature.user_management.domain.model.Role

class RoleSelectionAdapter(
    private val onSelectionChanged: (Set<Int>) -> Unit = {},
    private val showOrderBadges: Boolean = false
) : ListAdapter<Role, RoleSelectionAdapter.RoleViewHolder>(DiffCallback) {

    private var onAddRoleClickedListener: (() -> Unit)? = null

    fun setOnAddRoleClickedListener(listener: () -> Unit) {
        this.onAddRoleClickedListener = listener
    }

    // 선택된 roleId → 선택 순서(1-based) 매핑
    private val selectionOrder = linkedMapOf<Int, Int>()
    private var nextOrder = 1

    fun getSelectedRoleIds(): Set<Int> = selectionOrder.keys.toSet()

    fun setSelectedRoleIds(ids: Set<Int>) {
        selectionOrder.clear()
        nextOrder = 1
        ids.forEach { id -> selectionOrder[id] = nextOrder++ }
        notifyDataSetChanged()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RoleViewHolder {
        val binding = ItemRoleSelectionGridBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return RoleViewHolder(binding)
    }

    override fun onBindViewHolder(holder: RoleViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    inner class RoleViewHolder(private val binding: ItemRoleSelectionGridBinding) :
        RecyclerView.ViewHolder(binding.root) {

        fun bind(role: Role) {
            binding.tvTitle.text = role.displayName

            if (role.roleId == -999) {
                // "+ 추가하기" 더미 칩 스타일 설정 (항상 미선택 상태 및 회색 배경)
                binding.clMain.setBackgroundResource(R.drawable.bg_role_add_chip)
                binding.clMain.isSelected = false
                binding.tvTitle.isSelected = false
                binding.tvTitle.setTextColor(android.graphics.Color.parseColor("#64748B")) // Slate 500
                binding.tvBadge.visibility = View.GONE

                binding.clMain.setOnClickListener {
                    onAddRoleClickedListener?.invoke()
                }
            } else {
                binding.clMain.setBackgroundResource(R.drawable.selector_custom_radio_bg)
                // selector_custom_radio_text 컬러 리소스 복원
                val colorStateList = androidx.core.content.ContextCompat.getColorStateList(
                    itemView.context, 
                    R.color.selector_custom_radio_text
                )
                binding.tvTitle.setTextColor(colorStateList)
                updateStyle(role.roleId)

                binding.clMain.setOnClickListener {
                    if (selectionOrder.containsKey(role.roleId)) {
                        // 선택 해제: 해당 항목 제거 후 순서 재정렬
                        selectionOrder.remove(role.roleId)
                        reorderBadges()
                    } else {
                        // 선택: 다음 순서 부여
                        selectionOrder[role.roleId] = nextOrder++
                    }
                    // 전체 목록 UI 갱신 (배지 번호 재정렬 반영)
                    notifyDataSetChanged()
                    onSelectionChanged(selectionOrder.keys.toSet())
                }
            }
        }

        private fun updateStyle(roleId: Int) {
            val order = selectionOrder[roleId]
            val isSelected = order != null

            binding.clMain.isSelected = isSelected
            binding.tvTitle.isSelected = isSelected

            if (showOrderBadges && isSelected && order != null) {
                binding.tvBadge.visibility = View.VISIBLE
                binding.tvBadge.text = order.toString()
            } else {
                binding.tvBadge.visibility = View.GONE
            }
        }
    }

    /**
     * 선택 해제 후 남은 항목들의 순서를 1부터 재정렬
     */
    private fun reorderBadges() {
        val reordered = linkedMapOf<Int, Int>()
        var order = 1
        selectionOrder.keys.forEach { id -> reordered[id] = order++ }
        selectionOrder.clear()
        selectionOrder.putAll(reordered)
        nextOrder = order
    }

    companion object {
        private val DiffCallback = object : DiffUtil.ItemCallback<Role>() {
            override fun areItemsTheSame(oldItem: Role, newItem: Role) =
                oldItem.roleId == newItem.roleId
            override fun areContentsTheSame(oldItem: Role, newItem: Role) =
                oldItem == newItem
        }
    }
}
