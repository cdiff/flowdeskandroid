package com.example.flowdesk_android.feature.user_management.presentation.users.invite

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.example.flowdesk_android.databinding.ItemRoleSelectionGridBinding
import com.example.flowdesk_android.feature.user_management.domain.model.Role

class RoleSelectionAdapter(
    private val onSelectionChanged: (Set<Int>) -> Unit
) : ListAdapter<Role, RoleSelectionAdapter.RoleViewHolder>(DiffCallback) {

    private val selectedRoleIds = mutableSetOf<Int>()

    fun getSelectedRoleIds(): Set<Int> = selectedRoleIds.toSet()

    fun setSelectedRoleIds(ids: Set<Int>) {
        selectedRoleIds.clear()
        selectedRoleIds.addAll(ids)
        notifyDataSetChanged()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RoleViewHolder {
        val binding = ItemRoleSelectionGridBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return RoleViewHolder(binding)
    }

    override fun onBindViewHolder(holder: RoleViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    inner class RoleViewHolder(private val binding: ItemRoleSelectionGridBinding) : RecyclerView.ViewHolder(binding.root) {

        fun bind(role: Role) {
            binding.tvTitle.text = role.displayName

            updateStyle(selectedRoleIds.contains(role.roleId))

            binding.clMain.setOnClickListener {
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
            binding.clMain.isSelected = isSelected
            binding.ivRadio.isSelected = isSelected
            binding.tvTitle.isSelected = isSelected
        }
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
