package com.example.flowdesk_android.feature.user_management.presentation.roles.list

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import android.transition.AutoTransition
import android.transition.TransitionManager
import com.example.flowdesk_android.R
import com.example.flowdesk_android.databinding.ItemRoleListBinding
import com.example.flowdesk_android.feature.user_management.domain.model.Role
import java.text.SimpleDateFormat
import java.util.Locale

class RoleAdapter(
    private val onEditRoleClick: (Role) -> Unit,
    private val onToggleStatusClick: (Role) -> Unit,
    private val onDeleteRoleClick: (Role) -> Unit
) : ListAdapter<Role, RoleAdapter.RoleViewHolder>(RoleDiffCallback()) {

    private val expandedRoles = mutableSetOf<Int>()

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RoleViewHolder {
        val binding = ItemRoleListBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return RoleViewHolder(binding)
    }

    override fun onBindViewHolder(holder: RoleViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    inner class RoleViewHolder(private val binding: ItemRoleListBinding) : RecyclerView.ViewHolder(binding.root) {

        fun bind(role: Role) {
            binding.tvRoleDisplayName.text = role.displayName
            binding.tvRoleDescription.text = role.description ?: ""
            
            // 활성 상태 뱃지 텍스트 및 색상 매핑 (배경 제거)
            if (role.isActive) {
                binding.tvRoleStatus.text = "• 활성"
                binding.tvRoleStatus.setTextColor(itemView.context.getColor(R.color.color_success_active))
            } else {
                binding.tvRoleStatus.text = "• 비활성"
                binding.tvRoleStatus.setTextColor(itemView.context.getColor(R.color.text_hint))
            }

            // 생성일자 포맷팅
            val formattedDate = try {
                val parser = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'", Locale.getDefault())
                val formatter = SimpleDateFormat("yyyy.MM.dd", Locale.getDefault())
                val date = role.createdAt?.let { parser.parse(it) }
                if (date != null) formatter.format(date) else ""
            } catch (e: Exception) {
                role.createdAt ?: ""
            }
            binding.tvCreatedAt.text = formattedDate

            binding.tvUserCount.text = "${role.userCount}명"
            binding.tvPermissionCount.text = "${role.permissionCount}개"

            // 아코디언 확장 메뉴 처리 로직
            val isExpanded = expandedRoles.contains(role.roleId)
            binding.llHiddenMenu.visibility = if (isExpanded) View.VISIBLE else View.GONE
            
            binding.btnToggleStatus.text = if (role.isActive) "비활성화" else "활성화"

            binding.ivMore.setOnClickListener { 
                val isCurrentlyExpanded = expandedRoles.contains(role.roleId)
                if (isCurrentlyExpanded) {
                    expandedRoles.remove(role.roleId)
                } else {
                    expandedRoles.add(role.roleId)
                }
                
                val parent = itemView.parent as? ViewGroup
                if (parent != null) {
                    TransitionManager.beginDelayedTransition(parent, AutoTransition().apply { duration = 200 })
                }
                
                binding.llHiddenMenu.visibility = if (expandedRoles.contains(role.roleId)) View.VISIBLE else View.GONE
            }
            
            binding.btnToggleStatus.setOnClickListener {
                onToggleStatusClick(role)
                expandedRoles.remove(role.roleId)
                
                val parent = itemView.parent as? ViewGroup
                if (parent != null) {
                    TransitionManager.beginDelayedTransition(parent, AutoTransition().apply { duration = 200 })
                }
                binding.llHiddenMenu.visibility = View.GONE
            }
            
            binding.btnDeleteRole.setOnClickListener {
                onDeleteRoleClick(role)
                expandedRoles.remove(role.roleId)
                
                val parent = itemView.parent as? ViewGroup
                if (parent != null) {
                    TransitionManager.beginDelayedTransition(parent, AutoTransition().apply { duration = 200 })
                }
                binding.llHiddenMenu.visibility = View.GONE
            }

            itemView.setOnClickListener { onEditRoleClick(role) }
        }
    }
}

class RoleDiffCallback : DiffUtil.ItemCallback<Role>() {
    override fun areItemsTheSame(oldItem: Role, newItem: Role): Boolean {
        return oldItem.roleId == newItem.roleId
    }

    override fun areContentsTheSame(oldItem: Role, newItem: Role): Boolean {
        return oldItem == newItem
    }
}

