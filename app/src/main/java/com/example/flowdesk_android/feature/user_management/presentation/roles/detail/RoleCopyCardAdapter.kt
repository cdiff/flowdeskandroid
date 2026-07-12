package com.example.flowdesk_android.feature.user_management.presentation.roles.detail

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.flowdesk_android.databinding.ItemRoleCopyCardBinding
import com.example.flowdesk_android.feature.user_management.domain.model.Role

class RoleCopyCardAdapter(
    private val items: List<Role>,
    private val onCopyClick: (Role) -> Unit
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
        holder.binding.tvRoleTitle.text = role.displayName
        holder.binding.tvRoleCode.text = role.roleName
        holder.binding.tvPermissionCount.text = "${role.permissionCount}개"
        holder.binding.tvUserCount.text = "${role.userCount}명"

        // 여백이 아닌 실제 카드 영역(cardView) 클릭 시에만 복사 이벤트 유도
        holder.binding.cardView.setOnClickListener { onCopyClick(role) }
    }

    override fun getItemCount(): Int = items.size
}
