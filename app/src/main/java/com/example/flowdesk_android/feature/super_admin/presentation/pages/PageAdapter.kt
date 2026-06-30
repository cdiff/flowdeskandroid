package com.example.flowdesk_android.feature.super_admin.presentation.pages

import android.transition.AutoTransition
import android.transition.TransitionManager
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.example.flowdesk_android.R
import com.example.flowdesk_android.databinding.ItemSuperAdminPageBinding
import com.example.flowdesk_android.feature.super_admin.domain.model.Page

class PageAdapter(
    private val onToggleStatusClick: (Page) -> Unit,
    private val onDeleteClick: (Page) -> Unit,
    private val onToggleParentClick: (Page) -> Unit,
    private var expandedParents: Set<Int> // 현재 열려있는 부모 ID들
) : ListAdapter<Page, PageAdapter.PageViewHolder>(PageDiffCallback()) {

    private val expandedHiddenMenu = mutableSetOf<Int>()

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): PageViewHolder {
        val binding = ItemSuperAdminPageBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )
        return PageViewHolder(binding)
    }

    override fun onBindViewHolder(holder: PageViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    fun updateExpandedParents(expanded: Set<Int>) {
        expandedParents = expanded
        notifyDataSetChanged()
    }

    inner class PageViewHolder(
        private val binding: ItemSuperAdminPageBinding
    ) : RecyclerView.ViewHolder(binding.root) {

        fun bind(page: Page) {
            val context = itemView.context

            binding.tvPageName.text     = page.pageName
            binding.tvPageDisplayName.text  = page.displayName
            binding.tvPagePath.text         = page.path
            binding.tvPageOrder.text        = page.sortOrder.toString()
            binding.tvPageChildCount.text   = page.childCount.toString()
            binding.tvPagePermissionCount.text    = page.permissionCount.toString()

            // 들여쓰기 (margin) 및 펼침 아이콘 로직
            val density = context.resources.displayMetrics.density
            val lp = binding.ivExpand.layoutParams as ViewGroup.MarginLayoutParams

            if (page.parentId != null) {
                // 자식 페이지: 들여쓰기 (예: 40dp), 펼침 아이콘 숨김
                lp.marginStart = (40 * density).toInt()
                binding.ivExpand.visibility = View.INVISIBLE
                binding.llChildCount.visibility = View.GONE
            } else {
                // 부모 페이지: 들여쓰기 없음, 자식이 있으면 아이콘 표시
                lp.marginStart = 0
                binding.llChildCount.visibility = View.VISIBLE
                if (page.childCount > 0) {
                    binding.ivExpand.visibility = View.VISIBLE
                    // 열려있으면 아래를 보게 회전 (90도)
                    binding.ivExpand.rotation = if (expandedParents.contains(page.pageId)) 90f else 0f
                } else {
                    binding.ivExpand.visibility = View.INVISIBLE
                }
            }
            binding.ivExpand.layoutParams = lp

            // 부모 클릭 시 자식 펼치기 토글
            val toggleAction = {
                if (page.parentId == null && page.childCount > 0) {
                    onToggleParentClick(page)
                }
            }
            binding.ivExpand.setOnClickListener { toggleAction() }
            binding.llChildCount.setOnClickListener { toggleAction() }

            // 상태 뱃지
            if (page.isActive) {
                binding.tvPageStatus.text = context.getString(R.string.label_status_active)
                binding.tvPageStatus.setTextColor(ContextCompat.getColor(context, R.color.color_success))
                binding.tvPageStatus.setBackgroundResource(R.drawable.bg_tag_light_green)
            } else {
                binding.tvPageStatus.text = context.getString(R.string.label_status_inactive)
                binding.tvPageStatus.setTextColor(ContextCompat.getColor(context, R.color.text_secondary))
                binding.tvPageStatus.setBackgroundResource(R.drawable.bg_rect_rounded_light_gray)
            }

            // 전체 영역 클릭 시 자식 펼치기 토글
            itemView.setOnClickListener { toggleAction() }

            // 더보기 expand/collapse
            val isMenuExpanded = expandedHiddenMenu.contains(page.pageId)
            binding.llHiddenMenu.visibility = if (isMenuExpanded) View.VISIBLE else View.GONE
            binding.btnToggleStatus.text    = if (page.isActive) {
                context.getString(R.string.label_status_inactive) + "화"
            } else {
                context.getString(R.string.label_status_active) + "화"
            }

            binding.ivMore.setOnClickListener {
                val nowExpanded = expandedHiddenMenu.contains(page.pageId)
                if (nowExpanded) expandedHiddenMenu.remove(page.pageId)
                else expandedHiddenMenu.add(page.pageId)

                (itemView.parent as? ViewGroup)?.let {
                    TransitionManager.beginDelayedTransition(it, AutoTransition().apply { duration = 180 })
                }
                binding.llHiddenMenu.visibility =
                    if (expandedHiddenMenu.contains(page.pageId)) View.VISIBLE else View.GONE
            }

            binding.btnToggleStatus.setOnClickListener {
                onToggleStatusClick(page)
                expandedHiddenMenu.remove(page.pageId)
                binding.llHiddenMenu.visibility = View.GONE
            }

            binding.btnDelete.setOnClickListener {
                onDeleteClick(page)
                expandedHiddenMenu.remove(page.pageId)
                binding.llHiddenMenu.visibility = View.GONE
            }
        }
    }
}

class PageDiffCallback : DiffUtil.ItemCallback<Page>() {
    override fun areItemsTheSame(oldItem: Page, newItem: Page) = oldItem.pageId == newItem.pageId
    override fun areContentsTheSame(oldItem: Page, newItem: Page) = oldItem == newItem
}
