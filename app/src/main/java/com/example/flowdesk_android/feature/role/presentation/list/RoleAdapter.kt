package com.example.flowdesk_android.feature.role.presentation.list

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import android.transition.AutoTransition
import android.transition.TransitionManager
import com.example.flowdesk_android.R
import com.example.flowdesk_android.core.domain.model.Role
import java.text.SimpleDateFormat
import java.util.Locale

class RoleAdapter(
    private val onManagePermissionsClick: (Role) -> Unit,
    private val onEditRoleClick: (Role) -> Unit,
    private val onToggleStatusClick: (Role) -> Unit,
    private val onDeleteRoleClick: (Role) -> Unit
) : ListAdapter<Role, RoleAdapter.RoleViewHolder>(RoleDiffCallback()) {

    private val expandedRoles = mutableSetOf<Int>()

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RoleViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_role_list, parent, false)
        return RoleViewHolder(view)
    }

    override fun onBindViewHolder(holder: RoleViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    inner class RoleViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val tvDisplayName: TextView = itemView.findViewById(R.id.tv_role_display_name)
        private val tvRoleName: TextView = itemView.findViewById(R.id.tv_role_name)
        private val tvDescription: TextView = itemView.findViewById(R.id.tv_role_description)
        private val tvStatus: TextView = itemView.findViewById(R.id.tv_role_status)
        private val tvCreatedAt: TextView = itemView.findViewById(R.id.tv_created_at)
        private val tvUserCount: TextView = itemView.findViewById(R.id.tv_user_count)
        private val tvPermissionCount: TextView = itemView.findViewById(R.id.tv_permission_count)
        
        private val btnManagePermissions: TextView = itemView.findViewById(R.id.btn_manage_permissions)
        private val btnEditRole: TextView = itemView.findViewById(R.id.btn_edit_role)
        private val ivMoreOptions: ImageView = itemView.findViewById(R.id.iv_more_options)
        
        private val llHiddenMenu: View = itemView.findViewById(R.id.ll_hidden_menu)
        private val btnToggleStatus: TextView = itemView.findViewById(R.id.btn_toggle_status)
        private val btnDeleteRole: TextView = itemView.findViewById(R.id.btn_delete_role)

        fun bind(role: Role) {
            tvDisplayName.text = role.displayName
            tvRoleName.text = role.roleName
            tvDescription.text = role.description ?: ""
            
            if (role.isActive) {
                tvStatus.text = "활성"
                tvStatus.setTextColor(itemView.context.getColor(R.color.green_accent))
                tvStatus.setBackgroundColor(android.graphics.Color.parseColor("#E8F5E9"))
            } else {
                tvStatus.text = "비활성"
                tvStatus.setTextColor(itemView.context.getColor(R.color.red))
                tvStatus.setBackgroundColor(android.graphics.Color.parseColor("#FFEBEE"))
            }

            // formatting createdAt
            val formattedDate = try {
                val parser = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'", Locale.getDefault())
                val formatter = SimpleDateFormat("yyyy.MM.dd 생성", Locale.getDefault())
                val date = role.createdAt?.let { parser.parse(it) }
                if (date != null) formatter.format(date) else ""
            } catch (e: Exception) {
                role.createdAt ?: ""
            }
            tvCreatedAt.text = formattedDate

            tvUserCount.text = "${role.userCount}명"
            tvPermissionCount.text = "${role.permissionCount}개"

            // Hidden Menu Logic
            val isExpanded = expandedRoles.contains(role.roleId)
            llHiddenMenu.visibility = if (isExpanded) View.VISIBLE else View.GONE
            
            btnToggleStatus.text = if (role.isActive) "비활성화" else "활성화"

            ivMoreOptions.setOnClickListener { 
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
                
                llHiddenMenu.visibility = if (expandedRoles.contains(role.roleId)) View.VISIBLE else View.GONE
            }
            
            btnToggleStatus.setOnClickListener {
                onToggleStatusClick(role)
                expandedRoles.remove(role.roleId)
                
                val parent = itemView.parent as? ViewGroup
                if (parent != null) {
                    TransitionManager.beginDelayedTransition(parent, AutoTransition().apply { duration = 200 })
                }
                llHiddenMenu.visibility = View.GONE
            }
            
            btnDeleteRole.setOnClickListener {
                onDeleteRoleClick(role)
                expandedRoles.remove(role.roleId)
                
                val parent = itemView.parent as? ViewGroup
                if (parent != null) {
                    TransitionManager.beginDelayedTransition(parent, AutoTransition().apply { duration = 200 })
                }
                llHiddenMenu.visibility = View.GONE
            }

            btnManagePermissions.setOnClickListener { onManagePermissionsClick(role) }
            btnEditRole.setOnClickListener { onEditRoleClick(role) }
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
