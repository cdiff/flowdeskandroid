package com.example.flowdesk_android.presentation.ui.roles

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
import java.text.SimpleDateFormat
import java.util.Locale

class RoleAdapter(
    private val onManagePermissionsClick: (RoleDto) -> Unit,
    private val onEditRoleClick: (RoleDto) -> Unit,
    private val onMoreOptionsClick: (RoleDto, View) -> Unit
) : ListAdapter<RoleDto, RoleAdapter.RoleViewHolder>(RoleDiffCallback()) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RoleViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_role, parent, false)
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

        fun bind(role: RoleDto) {
            tvDisplayName.text = role.displayName
            tvRoleName.text = role.roleName
            tvDescription.text = role.description ?: ""
            
            if (role.isActive == 1) {
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

            btnManagePermissions.setOnClickListener { onManagePermissionsClick(role) }
            btnEditRole.setOnClickListener { onEditRoleClick(role) }
            ivMoreOptions.setOnClickListener { onMoreOptionsClick(role, it) }
        }
    }
}

class RoleDiffCallback : DiffUtil.ItemCallback<RoleDto>() {
    override fun areItemsTheSame(oldItem: RoleDto, newItem: RoleDto): Boolean {
        return oldItem.roleId == newItem.roleId
    }

    override fun areContentsTheSame(oldItem: RoleDto, newItem: RoleDto): Boolean {
        return oldItem == newItem
    }
}
