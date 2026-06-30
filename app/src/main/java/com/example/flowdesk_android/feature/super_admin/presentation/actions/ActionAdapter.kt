package com.example.flowdesk_android.feature.super_admin.presentation.actions

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
import com.example.flowdesk_android.databinding.ItemSuperAdminActionBinding
import com.example.flowdesk_android.feature.super_admin.domain.model.Action

class ActionAdapter(
    private val onToggleStatusClick: (Action) -> Unit,
    private val onDeleteClick: (Action) -> Unit
) : ListAdapter<Action, ActionAdapter.ActionViewHolder>(ActionDiffCallback()) {

    private val expandedHiddenMenu = mutableSetOf<Int>()

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ActionViewHolder {
        val binding = ItemSuperAdminActionBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )
        return ActionViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ActionViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    inner class ActionViewHolder(
        private val binding: ItemSuperAdminActionBinding
    ) : RecyclerView.ViewHolder(binding.root) {

        fun bind(action: Action) {
            val context = itemView.context

            binding.tvActionName.text  = action.actionName
            binding.tvActionDisplayName.text = action.displayName
            binding.tvActionPermissionCount.text   = action.permissionCount.toString()

            // 상태 뱃지
            if (action.isActive) {
                binding.tvActionStatus.text = context.getString(R.string.label_status_active)
                binding.tvActionStatus.setTextColor(ContextCompat.getColor(context, R.color.color_success))
                binding.tvActionStatus.setBackgroundResource(R.drawable.bg_tag_light_green)
            } else {
                binding.tvActionStatus.text = context.getString(R.string.label_status_inactive)
                binding.tvActionStatus.setTextColor(ContextCompat.getColor(context, R.color.text_secondary))
                binding.tvActionStatus.setBackgroundResource(R.drawable.bg_rect_rounded_light_gray)
            }

            // 더보기 expand/collapse
            val isMenuExpanded = expandedHiddenMenu.contains(action.actionId)
            binding.llHiddenMenu.visibility = if (isMenuExpanded) View.VISIBLE else View.GONE
            binding.btnToggleStatus.text    = if (action.isActive) {
                context.getString(R.string.label_status_inactive) + "화"
            } else {
                context.getString(R.string.label_status_active) + "화"
            }

            binding.ivMore.setOnClickListener {
                val nowExpanded = expandedHiddenMenu.contains(action.actionId)
                if (nowExpanded) expandedHiddenMenu.remove(action.actionId)
                else expandedHiddenMenu.add(action.actionId)

                (itemView.parent as? ViewGroup)?.let {
                    TransitionManager.beginDelayedTransition(it, AutoTransition().apply { duration = 180 })
                }
                binding.llHiddenMenu.visibility =
                    if (expandedHiddenMenu.contains(action.actionId)) View.VISIBLE else View.GONE
            }

            binding.btnToggleStatus.setOnClickListener {
                onToggleStatusClick(action)
                expandedHiddenMenu.remove(action.actionId)
                binding.llHiddenMenu.visibility = View.GONE
            }

            binding.btnDelete.setOnClickListener {
                onDeleteClick(action)
                expandedHiddenMenu.remove(action.actionId)
                binding.llHiddenMenu.visibility = View.GONE
            }
        }
    }
}

class ActionDiffCallback : DiffUtil.ItemCallback<Action>() {
    override fun areItemsTheSame(oldItem: Action, newItem: Action) = oldItem.actionId == newItem.actionId
    override fun areContentsTheSame(oldItem: Action, newItem: Action) = oldItem == newItem
}
