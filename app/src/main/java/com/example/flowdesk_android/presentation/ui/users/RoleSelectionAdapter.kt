package com.example.flowdesk_android.presentation.ui.users

import android.graphics.Color
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.example.flowdesk_android.R
import com.example.flowdesk_android.data.remote.dto.RoleDto

class RoleSelectionAdapter(
    private val onSelectionChanged: (Set<Int>) -> Unit
) : ListAdapter<RoleDto, RoleSelectionAdapter.RoleViewHolder>(DiffCallback) {

    private val selectedRoleIds = mutableSetOf<Int>()

    fun getSelectedRoleIds(): Set<Int> = selectedRoleIds.toSet()

    fun setSelectedRoleIds(ids: Set<Int>) {
        selectedRoleIds.clear()
        selectedRoleIds.addAll(ids)
        notifyDataSetChanged()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RoleViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_role_selection_grid, parent, false)
        return RoleViewHolder(view)
    }

    override fun onBindViewHolder(holder: RoleViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    inner class RoleViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val clMain: View = itemView.findViewById(R.id.cl_main)
        private val ivRadio: ImageView = itemView.findViewById(R.id.iv_radio)
        private val tvTitle: TextView = itemView.findViewById(R.id.tv_title)
        private val tvDesc: TextView = itemView.findViewById(R.id.tv_desc)

        fun bind(role: RoleDto) {
            tvTitle.text = role.displayName
            tvDesc.text = role.description

            updateStyle(selectedRoleIds.contains(role.roleId))

            clMain.setOnClickListener {
                if (selectedRoleIds.contains(role.roleId)) {
                    selectedRoleIds.remove(role.roleId)
                } else {
                    selectedRoleIds.add(role.roleId)
                }
                updateStyle(selectedRoleIds.contains(role.roleId))
                onSelectionChanged(selectedRoleIds.toSet())
            }
        }

        private fun updateStyle(isSelected: Boolean) {
            if (isSelected) {
                clMain.setBackgroundResource(R.drawable.bg_card_rounded_border_selected)
                ivRadio.setImageResource(R.drawable.ic_radio_selected)
                tvTitle.setTextColor(itemView.context.getColor(R.color.login_blue))
            } else {
                clMain.setBackgroundResource(R.drawable.bg_card_rounded_border)
                ivRadio.setImageResource(R.drawable.ic_radio_unselected)
                tvTitle.setTextColor(Color.parseColor("#3A485A"))
            }
        }
    }

    companion object {
        private val DiffCallback = object : DiffUtil.ItemCallback<RoleDto>() {
            override fun areItemsTheSame(oldItem: RoleDto, newItem: RoleDto) =
                oldItem.roleId == newItem.roleId
            override fun areContentsTheSame(oldItem: RoleDto, newItem: RoleDto) =
                oldItem == newItem
        }
    }
}
