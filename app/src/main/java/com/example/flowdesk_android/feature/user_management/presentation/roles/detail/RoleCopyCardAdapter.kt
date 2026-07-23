package com.example.flowdesk_android.feature.user_management.presentation.roles.detail

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.flowdesk_android.databinding.ItemRoleCopyCardBinding
import com.example.flowdesk_android.feature.user_management.domain.model.Role

class RoleCopyCardAdapter(
    private val items: List<Role>,
    private val onCopyClick: (Role) -> Unit,
    private val onViewPermissionsClick: (Role) -> Unit = {}
) : RecyclerView.Adapter<RoleCopyCardAdapter.ViewHolder>() {

    class ViewHolder(val binding: ItemRoleCopyCardBinding) : RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding = ItemRoleCopyCardBinding.inflate(
            LayoutInflater.from(parent.context), parent, false
        )
        return ViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val role = items[position]
        val context = holder.itemView.context
        holder.binding.tvRoleTitle.text = role.displayName
        
        // 활성/비활성 여부에 따른 배지 처리
        val isActive = role.isActive
        holder.binding.tvRoleStatus.text = if (isActive) "활성" else "비활성"
        holder.binding.tvRoleStatus.setTextColor(
            androidx.core.content.ContextCompat.getColor(
                context,
                if (isActive) com.example.flowdesk_android.R.color.color_success_active else com.example.flowdesk_android.R.color.text_hint
            )
        )
        holder.binding.tvRoleStatus.setBackgroundResource(
            if (isActive) com.example.flowdesk_android.R.drawable.bg_badge_rounded_green_border
            else com.example.flowdesk_android.R.drawable.bg_badge_gray_border
        )

        holder.binding.tvPermissionCount.text = "${role.permissionCount}개"
        holder.binding.tvUserCount.text = "${role.userCount}명"

        // 카드 전체 클릭 → 권한 복사
        holder.binding.cardView.setOnClickListener { onCopyClick(role) }

        // > 아이콘 클릭 → 해당 역할 세부 권한 읽기 전용 조회
        holder.binding.ivViewPermissions.setOnClickListener { onViewPermissionsClick(role) }
    }

    override fun getItemCount(): Int = items.size
}
