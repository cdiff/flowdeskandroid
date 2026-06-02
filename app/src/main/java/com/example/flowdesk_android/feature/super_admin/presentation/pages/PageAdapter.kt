package com.example.flowdesk_android.feature.super_admin.presentation.pages

import android.transition.AutoTransition
import android.transition.TransitionManager
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.example.flowdesk_android.R
import com.example.flowdesk_android.feature.super_admin.domain.model.Page

class PageAdapter(
    private val onToggleStatusClick: (Page) -> Unit,
    private val onDeleteClick: (Page) -> Unit,
    private val onToggleParentClick: (Page) -> Unit,
    private val expandedParents: Set<Int> // 현재 열려있는 부모 ID들
) : ListAdapter<Page, PageAdapter.PageViewHolder>(PageDiffCallback()) {

    private val expandedHiddenMenu = mutableSetOf<Int>()

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): PageViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_super_admin_page, parent, false)
        return PageViewHolder(view)
    }

    override fun onBindViewHolder(holder: PageViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    inner class PageViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val ivExpand: ImageView         = itemView.findViewById(R.id.iv_expand)
        private val tvPageName: TextView        = itemView.findViewById(R.id.tv_page_name)
        private val tvDisplayName: TextView     = itemView.findViewById(R.id.tv_page_display_name)
        private val tvPath: TextView            = itemView.findViewById(R.id.tv_page_path)
        private val tvStatus: TextView          = itemView.findViewById(R.id.tv_page_status)
        private val tvOrder: TextView           = itemView.findViewById(R.id.tv_page_order)
        private val llChildCount: View          = itemView.findViewById(R.id.ll_child_count)
        private val tvChildCount: TextView      = itemView.findViewById(R.id.tv_page_child_count)
        private val tvPermCount: TextView       = itemView.findViewById(R.id.tv_page_permission_count)
        private val ivMore: View                = itemView.findViewById(R.id.iv_more)
        private val llHiddenMenu: View?         = itemView.findViewById(R.id.ll_hidden_menu)
        private val btnToggleStatus: TextView?  = itemView.findViewById(R.id.btn_toggle_status)
        private val btnDelete: View?            = itemView.findViewById(R.id.btn_delete)

        fun bind(page: Page) {
            tvPageName.text     = page.pageName
            tvDisplayName.text  = page.displayName
            tvPath.text         = page.path
            tvOrder.text        = page.sortOrder.toString()
            tvChildCount.text   = page.childCount.toString()
            tvPermCount.text    = page.permissionCount.toString()

            // 들여쓰기 (margin) 및 펼침 아이콘 로직
            val density = itemView.context.resources.displayMetrics.density
            val lp = ivExpand.layoutParams as ViewGroup.MarginLayoutParams

            if (page.parentId != null) {
                // 자식 페이지: 들여쓰기 (예: 40dp), 펼침 아이콘 숨김
                lp.marginStart = (40 * density).toInt()
                ivExpand.visibility = View.INVISIBLE
                llChildCount.visibility = View.GONE
            } else {
                // 부모 페이지: 들여쓰기 없음, 자식이 있으면 아이콘 표시
                lp.marginStart = 0
                llChildCount.visibility = View.VISIBLE
                if (page.childCount > 0) {
                    ivExpand.visibility = View.VISIBLE
                    // 열려있으면 아래를 보게 회전 (90도)
                    ivExpand.rotation = if (expandedParents.contains(page.pageId)) 90f else 0f
                } else {
                    ivExpand.visibility = View.INVISIBLE
                }
            }
            ivExpand.layoutParams = lp

            // 부모 클릭 시 자식 펼치기 토글
            val toggleAction = {
                if (page.parentId == null && page.childCount > 0) {
                    onToggleParentClick(page)
                }
            }
            ivExpand.setOnClickListener { toggleAction() }
            llChildCount.setOnClickListener { toggleAction() }

            // 상태 뱃지
            if (page.isActive) {
                tvStatus.text = "활성"
                tvStatus.setTextColor(ContextCompat.getColor(itemView.context, R.color.green_accent))
                tvStatus.setBackgroundResource(R.drawable.bg_tag_light_green)
            } else {
                tvStatus.text = "비활성"
                tvStatus.setTextColor(ContextCompat.getColor(itemView.context, R.color.gray_text))
                tvStatus.setBackgroundResource(R.drawable.bg_rect_rounded_light_gray)
            }

            // 전체 영역 클릭 시 자식 펼치기 토글
            itemView.setOnClickListener { toggleAction() }

            // 더보기 expand/collapse
            val isMenuExpanded = expandedHiddenMenu.contains(page.pageId)
            llHiddenMenu?.visibility = if (isMenuExpanded) View.VISIBLE else View.GONE
            btnToggleStatus?.text    = if (page.isActive) "비활성화" else "활성화"

            ivMore.setOnClickListener {
                val nowExpanded = expandedHiddenMenu.contains(page.pageId)
                if (nowExpanded) expandedHiddenMenu.remove(page.pageId)
                else expandedHiddenMenu.add(page.pageId)

                (itemView.parent as? ViewGroup)?.let {
                    TransitionManager.beginDelayedTransition(it, AutoTransition().apply { duration = 180 })
                }
                llHiddenMenu?.visibility =
                    if (expandedHiddenMenu.contains(page.pageId)) View.VISIBLE else View.GONE
            }

            btnToggleStatus?.setOnClickListener {
                onToggleStatusClick(page)
                expandedHiddenMenu.remove(page.pageId)
                llHiddenMenu?.visibility = View.GONE
            }

            btnDelete?.setOnClickListener {
                onDeleteClick(page)
                expandedHiddenMenu.remove(page.pageId)
                llHiddenMenu?.visibility = View.GONE
            }
        }
    }
}

class PageDiffCallback : DiffUtil.ItemCallback<Page>() {
    override fun areItemsTheSame(oldItem: Page, newItem: Page) = oldItem.pageId == newItem.pageId
    override fun areContentsTheSame(oldItem: Page, newItem: Page) = oldItem == newItem
}
