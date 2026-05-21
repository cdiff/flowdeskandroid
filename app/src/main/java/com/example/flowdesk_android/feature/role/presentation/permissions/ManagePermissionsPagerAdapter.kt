package com.example.flowdesk_android.feature.role.presentation.permissions

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.flowdesk_android.databinding.ViewRoleManageInfoBinding
import com.example.flowdesk_android.databinding.ViewRoleManagePermissionsBinding

class ManagePermissionsPagerAdapter(
    inflater: LayoutInflater,
    parent: ViewGroup
) : RecyclerView.Adapter<ManagePermissionsPagerAdapter.PageViewHolder>() {

    // 생성자 시점에 미리 두 뷰를 inflate해 두어 lateinit 크래시 방지
    val infoBinding: ViewRoleManageInfoBinding =
        ViewRoleManageInfoBinding.inflate(inflater, parent, false)

    val permBinding: ViewRoleManagePermissionsBinding =
        ViewRoleManagePermissionsBinding.inflate(inflater, parent, false)

    private val pages: List<View> = listOf(infoBinding.root, permBinding.root)

    override fun getItemCount(): Int = pages.size

    override fun getItemViewType(position: Int): Int = position

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): PageViewHolder {
        // 이미 inflate된 뷰를 그대로 반환 (중복 inflate 없음)
        return PageViewHolder(pages[viewType])
    }

    override fun onBindViewHolder(holder: PageViewHolder, position: Int) { /* no-op */ }

    class PageViewHolder(view: View) : RecyclerView.ViewHolder(view)
}
