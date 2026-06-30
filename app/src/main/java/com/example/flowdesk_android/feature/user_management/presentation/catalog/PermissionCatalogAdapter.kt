package com.example.flowdesk_android.feature.user_management.presentation.catalog

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import com.example.flowdesk_android.R
import com.example.flowdesk_android.feature.user_management.domain.model.PermissionPage
import com.example.flowdesk_android.databinding.ItemRolePermissionCatalogBinding
import com.example.flowdesk_android.databinding.ItemRolePermissionCatalogGroupBinding

class PermissionCatalogAdapter() : RecyclerView.Adapter<PermissionCatalogAdapter.GroupViewHolder>() {

    private val items = mutableListOf<PermissionPage>()

    fun submitList(list: List<PermissionPage>) {
        items.clear()
        items.addAll(list)
        notifyDataSetChanged()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): GroupViewHolder {
        val binding = ItemRolePermissionCatalogGroupBinding.inflate(
            LayoutInflater.from(parent.context), parent, false
        )
        return GroupViewHolder(binding)
    }

    override fun onBindViewHolder(holder: GroupViewHolder, position: Int) {
        holder.bind(items[position])
    }

    override fun getItemCount() = items.size

    inner class GroupViewHolder(private val binding: ItemRolePermissionCatalogGroupBinding) :
        RecyclerView.ViewHolder(binding.root) {

        fun bind(page: PermissionPage) {
            binding.tvCategoryName.text = page.pageDisplayName
            binding.tvCategoryCode.text = page.pageName

            val permissions = page.permissions
            
            // 컨텐츠 컨테이너 비우기
            binding.layoutActionsContainer.removeAllViews()

            if (permissions.isEmpty()) {
                val noneTv = TextView(binding.root.context).apply {
                    text = "해당 페이지에 할당된 권한이 없습니다."
                    textSize = 12f
                    setTextColor(itemView.context.getColor(R.color.gray_text))
                    setPadding(0, 8, 0, 8)
                }
                binding.layoutActionsContainer.addView(noneTv)
            } else {
                permissions.forEachIndexed { index, permissionAction ->
                    val actionRow = ItemRolePermissionCatalogBinding.inflate(
                        LayoutInflater.from(binding.root.context),
                        binding.layoutActionsContainer,
                        false
                    )

                    val actionName = permissionAction.actionName

                    actionRow.tvActionName.text = permissionAction.actionDisplayName
                    
                    // 액션별 색상 적용 (배경 틴트 및 글자색)
                    val (bgColor, textColor) = when (actionName) {
                        "read" -> R.color.badge_read_bg to R.color.badge_read_text
                        "create" -> R.color.badge_create_bg to R.color.badge_create_text
                        "update" -> R.color.badge_update_bg to R.color.badge_update_text
                        "delete" -> R.color.badge_delete_bg to R.color.badge_delete_text
                        else -> R.color.badge_default_bg to R.color.badge_default_text
                    }
                    
                    actionRow.tvActionName.backgroundTintList = ContextCompat.getColorStateList(binding.root.context, bgColor)
                    actionRow.tvActionName.setTextColor(ContextCompat.getColor(binding.root.context, textColor))

                    actionRow.tvPermissionId.text = "ID: ${permissionAction.permissionId}"
                    actionRow.tvPermissionTitle.text = permissionAction.displayName
                    actionRow.tvPermissionKey.text = "${page.pageName}@${actionName}"
                    
                    val desc = permissionAction.description
                    if (!desc.isNullOrBlank()) {
                        actionRow.tvPermissionDesc.text = desc
                        actionRow.tvPermissionDesc.visibility = android.view.View.VISIBLE
                    } else {
                        actionRow.tvPermissionDesc.visibility = android.view.View.GONE
                    }

                    // 마지막 아이템이면 구분선 제거
                    if (index == permissions.lastIndex) {
                        actionRow.divItem.visibility = android.view.View.GONE
                    }

                    binding.layoutActionsContainer.addView(actionRow.root)
                }
            }
        }
    }
}
