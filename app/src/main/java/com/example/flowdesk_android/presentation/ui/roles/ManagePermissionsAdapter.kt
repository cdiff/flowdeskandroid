package com.example.flowdesk_android.presentation.ui.roles

import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.CheckBox
import android.widget.LinearLayout
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.flowdesk_android.R
import com.example.flowdesk_android.data.remote.dto.PermissionPageDto
import com.example.flowdesk_android.databinding.ItemManagePermissionGroupBinding

class ManagePermissionsAdapter(
    private val checkedIds: MutableSet<Int>,
    private val onCheckChanged: () -> Unit
) : RecyclerView.Adapter<ManagePermissionsAdapter.ViewHolder>() {

    private val items = mutableListOf<PermissionPageDto>()

    fun submitList(list: List<PermissionPageDto>) {
        items.clear()
        items.addAll(list)
        notifyDataSetChanged()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding = ItemManagePermissionGroupBinding.inflate(
            LayoutInflater.from(parent.context), parent, false
        )
        return ViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(items[position])
    }

    override fun getItemCount() = items.size

    inner class ViewHolder(private val binding: ItemManagePermissionGroupBinding) :
        RecyclerView.ViewHolder(binding.root) {

        fun bind(page: PermissionPageDto) {
            binding.tvCategoryName.text = page.pageDisplayName
            binding.tvCategoryCode.text = page.pageName

            val actions = page.permissions ?: emptyList()

            // 체크박스들을 동적으로 생성
            binding.llActionsContainer.removeAllViews()
            actions.forEach { action ->
                val cb = CheckBox(binding.root.context).apply {
                    text = action.actionDisplayName
                    textSize = 13f
                    buttonTintList = androidx.core.content.ContextCompat.getColorStateList(
                        context, R.color.login_blue
                    )
                    setPadding(4, 0, 0, 0)
                    val params = LinearLayout.LayoutParams(
                        LinearLayout.LayoutParams.WRAP_CONTENT,
                        LinearLayout.LayoutParams.WRAP_CONTENT
                    ).apply { marginEnd = 24 }
                    layoutParams = params
                    isChecked = checkedIds.contains(action.permissionId)
                    setOnCheckedChangeListener { _, isChecked ->
                        if (isChecked) checkedIds.add(action.permissionId)
                        else checkedIds.remove(action.permissionId)
                        // 전체 선택 체크박스 상태 동기화
                        binding.cbSelectAll.setOnCheckedChangeListener(null)
                        binding.cbSelectAll.isChecked = actions.all { checkedIds.contains(it.permissionId) }
                        binding.cbSelectAll.setOnCheckedChangeListener(selectAllListener(actions))
                        onCheckChanged()
                    }
                }
                binding.llActionsContainer.addView(cb)
            }

            // 전체선택 체크박스 세팅
            binding.cbSelectAll.setOnCheckedChangeListener(null)
            binding.cbSelectAll.isChecked = actions.isNotEmpty() && actions.all { checkedIds.contains(it.permissionId) }
            binding.cbSelectAll.setOnCheckedChangeListener(selectAllListener(actions))
        }

        private fun selectAllListener(
            actions: List<com.example.flowdesk_android.data.remote.dto.PermissionActionDto>
        ) = android.widget.CompoundButton.OnCheckedChangeListener { _, isChecked ->
            actions.forEach { action ->
                if (isChecked) checkedIds.add(action.permissionId)
                else checkedIds.remove(action.permissionId)
            }
            // 하위 체크박스 UI 갱신
            val container = binding.llActionsContainer
            for (i in 0 until container.childCount) {
                (container.getChildAt(i) as? CheckBox)?.apply {
                    setOnCheckedChangeListener(null)
                    this.isChecked = isChecked
                    // 리스너는 bind에서 다시 설정되므로 위에서 제거 후 다시 설정 필요
                }
            }
            // 전체 갱신 대신 직접 재바인딩
            bindingAdapterPosition.takeIf { it != RecyclerView.NO_ID.toInt() }?.let {
                notifyItemChanged(it)
            }
            onCheckChanged()
        }
    }
}
