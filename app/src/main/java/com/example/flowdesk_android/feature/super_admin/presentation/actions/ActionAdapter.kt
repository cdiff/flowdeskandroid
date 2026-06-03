package com.example.flowdesk_android.feature.super_admin.presentation.actions

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
import com.example.flowdesk_android.feature.super_admin.domain.model.Action

class ActionAdapter(
    private val onToggleStatusClick: (Action) -> Unit,
    private val onDeleteClick: (Action) -> Unit
) : ListAdapter<Action, ActionAdapter.ActionViewHolder>(ActionDiffCallback()) {

    private val expandedHiddenMenu = mutableSetOf<Int>()

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ActionViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_super_admin_action, parent, false)
        return ActionViewHolder(view)
    }

    override fun onBindViewHolder(holder: ActionViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    inner class ActionViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val tvActionName: TextView        = itemView.findViewById(R.id.tv_action_name)
        private val tvDisplayName: TextView       = itemView.findViewById(R.id.tv_action_display_name)
        private val tvStatus: TextView            = itemView.findViewById(R.id.tv_action_status)
        private val tvPermCount: TextView         = itemView.findViewById(R.id.tv_action_permission_count)
        private val ivMore: View                  = itemView.findViewById(R.id.iv_more)
        private val llHiddenMenu: View?           = itemView.findViewById(R.id.ll_hidden_menu)
        private val btnToggleStatus: TextView?    = itemView.findViewById(R.id.btn_toggle_status)
        private val btnDelete: View?              = itemView.findViewById(R.id.btn_delete)

        fun bind(action: Action) {
            tvActionName.text  = action.actionName
            tvDisplayName.text = action.displayName
            tvPermCount.text   = action.permissionCount.toString()

            // 상태 뱃지
            if (action.isActive) {
                tvStatus.text = "활성"
                tvStatus.setTextColor(ContextCompat.getColor(itemView.context, R.color.green_accent))
                tvStatus.setBackgroundResource(R.drawable.bg_tag_light_green)
            } else {
                tvStatus.text = "비활성"
                tvStatus.setTextColor(ContextCompat.getColor(itemView.context, R.color.gray_text))
                tvStatus.setBackgroundResource(R.drawable.bg_rect_rounded_light_gray)
            }

            // 더보기 expand/collapse
            val isMenuExpanded = expandedHiddenMenu.contains(action.actionId)
            llHiddenMenu?.visibility = if (isMenuExpanded) View.VISIBLE else View.GONE
            btnToggleStatus?.text    = if (action.isActive) "비활성화" else "활성화"

            ivMore.setOnClickListener {
                val nowExpanded = expandedHiddenMenu.contains(action.actionId)
                if (nowExpanded) expandedHiddenMenu.remove(action.actionId)
                else expandedHiddenMenu.add(action.actionId)

                (itemView.parent as? ViewGroup)?.let {
                    TransitionManager.beginDelayedTransition(it, AutoTransition().apply { duration = 180 })
                }
                llHiddenMenu?.visibility =
                    if (expandedHiddenMenu.contains(action.actionId)) View.VISIBLE else View.GONE
            }

            btnToggleStatus?.setOnClickListener {
                onToggleStatusClick(action)
                expandedHiddenMenu.remove(action.actionId)
                llHiddenMenu?.visibility = View.GONE
            }

            btnDelete?.setOnClickListener {
                onDeleteClick(action)
                expandedHiddenMenu.remove(action.actionId)
                llHiddenMenu?.visibility = View.GONE
            }
        }
    }
}

class ActionDiffCallback : DiffUtil.ItemCallback<Action>() {
    override fun areItemsTheSame(oldItem: Action, newItem: Action) = oldItem.actionId == newItem.actionId
    override fun areContentsTheSame(oldItem: Action, newItem: Action) = oldItem == newItem
}
