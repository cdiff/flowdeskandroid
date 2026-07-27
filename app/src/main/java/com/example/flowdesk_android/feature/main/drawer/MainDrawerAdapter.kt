package com.example.flowdesk_android.feature.main.drawer

import android.content.res.ColorStateList
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.example.flowdesk_android.R
import com.example.flowdesk_android.databinding.ItemDrawerMenuBinding
import com.example.flowdesk_android.databinding.ItemDrawerSubMenuBinding

class MainDrawerAdapter(
    private val onHeaderClick: (header: DrawerRow.Header) -> Unit,
    private val onChevronClick: (header: DrawerRow.Header) -> Unit,
    private val onSubItemClick: (subItem: DrawerRow.SubItem) -> Unit
) : ListAdapter<DrawerRow, RecyclerView.ViewHolder>(DiffCallback) {

    companion object {
        private const val VIEW_TYPE_HEADER = 0
        private const val VIEW_TYPE_SUB_ITEM = 1

        const val PAYLOAD_EXPAND = "PAYLOAD_EXPAND"
        const val PAYLOAD_SELECTION = "PAYLOAD_SELECTION"

        private object DiffCallback : DiffUtil.ItemCallback<DrawerRow>() {
            override fun areItemsTheSame(oldItem: DrawerRow, newItem: DrawerRow): Boolean {
                return oldItem.id == newItem.id
            }

            override fun areContentsTheSame(oldItem: DrawerRow, newItem: DrawerRow): Boolean {
                return oldItem == newItem
            }

            override fun getChangePayload(oldItem: DrawerRow, newItem: DrawerRow): Any? {
                if (oldItem is DrawerRow.Header && newItem is DrawerRow.Header) {
                    if (oldItem.isExpanded != newItem.isExpanded) return PAYLOAD_EXPAND
                    if (oldItem.isSelected != newItem.isSelected) return PAYLOAD_SELECTION
                }
                if (oldItem is DrawerRow.SubItem && newItem is DrawerRow.SubItem) {
                    if (oldItem.isSelected != newItem.isSelected) return PAYLOAD_SELECTION
                }
                return super.getChangePayload(oldItem, newItem)
            }
        }
    }

    override fun getItemViewType(position: Int): Int {
        return when (getItem(position)) {
            is DrawerRow.Header -> VIEW_TYPE_HEADER
            is DrawerRow.SubItem -> VIEW_TYPE_SUB_ITEM
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        val inflater = LayoutInflater.from(parent.context)
        return when (viewType) {
            VIEW_TYPE_HEADER -> {
                val binding = ItemDrawerMenuBinding.inflate(inflater, parent, false)
                HeaderViewHolder(binding)
            }
            VIEW_TYPE_SUB_ITEM -> {
                val binding = ItemDrawerSubMenuBinding.inflate(inflater, parent, false)
                SubItemViewHolder(binding)
            }
            else -> throw IllegalArgumentException("Unknown view type: $viewType")
        }
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        when (val item = getItem(position)) {
            is DrawerRow.Header -> (holder as HeaderViewHolder).bind(item)
            is DrawerRow.SubItem -> (holder as SubItemViewHolder).bind(item)
        }
    }

    override fun onBindViewHolder(
        holder: RecyclerView.ViewHolder,
        position: Int,
        payloads: MutableList<Any>
    ) {
        if (payloads.isEmpty()) {
            super.onBindViewHolder(holder, position, payloads)
            return
        }
        val item = getItem(position)
        for (payload in payloads) {
            when (payload) {
                PAYLOAD_EXPAND -> {
                    if (holder is HeaderViewHolder && item is DrawerRow.Header) {
                        holder.updateExpandState(item.isExpanded)
                    }
                }
                PAYLOAD_SELECTION -> {
                    if (holder is HeaderViewHolder && item is DrawerRow.Header) {
                        holder.updateSelectionState(item.isSelected)
                    } else if (holder is SubItemViewHolder && item is DrawerRow.SubItem) {
                        holder.updateSelectionState(item.isSelected)
                    }
                }
                else -> super.onBindViewHolder(holder, position, payloads)
            }
        }
    }

    inner class HeaderViewHolder(private val binding: ItemDrawerMenuBinding) :
        RecyclerView.ViewHolder(binding.root) {

        fun bind(item: DrawerRow.Header) {
            val context = binding.root.context
            binding.tvMenuName.text = item.cleanDisplayName
            binding.ivMenuIcon.setImageResource(item.iconRes)

            updateSelectionState(item.isSelected)

            if (item.hasSubItems) {
                binding.ivChevron.visibility = View.VISIBLE
                updateExpandState(item.isExpanded)
            } else {
                binding.ivChevron.visibility = View.GONE
            }

            binding.llDrawerMenuItem.setOnClickListener { onHeaderClick(item) }
            binding.ivChevron.setOnClickListener { onChevronClick(item) }
        }

        fun updateExpandState(isExpanded: Boolean) {
            binding.ivChevron.animate()
                .rotation(if (isExpanded) 90f else 0f)
                .setDuration(150)
                .start()
        }

        fun updateSelectionState(isSelected: Boolean) {
            val context = binding.root.context
            val primaryBlue = ContextCompat.getColor(context, R.color.drawer_item_selected)
            val defaultSlate = ContextCompat.getColor(context, R.color.slate_600)
            val textDark = ContextCompat.getColor(context, R.color.text_primary)

            binding.llDrawerMenuItem.isSelected = isSelected
            if (isSelected) {
                binding.ivMenuIcon.imageTintList = ColorStateList.valueOf(primaryBlue)
                binding.tvMenuName.setTextColor(primaryBlue)
            } else {
                binding.ivMenuIcon.imageTintList = ColorStateList.valueOf(defaultSlate)
                binding.tvMenuName.setTextColor(textDark)
            }
        }
    }

    inner class SubItemViewHolder(private val binding: ItemDrawerSubMenuBinding) :
        RecyclerView.ViewHolder(binding.root) {

        fun bind(item: DrawerRow.SubItem) {
            binding.tvSubMenuName.text = item.displayName
            updateSelectionState(item.isSelected)
            binding.llDrawerSubItem.setOnClickListener { onSubItemClick(item) }
        }

        fun updateSelectionState(isSelected: Boolean) {
            val context = binding.root.context
            val primaryBlue = ContextCompat.getColor(context, R.color.drawer_item_selected)
            val slate400 = ContextCompat.getColor(context, R.color.slate_400)
            val slate700 = ContextCompat.getColor(context, R.color.slate_700)

            binding.llDrawerSubItem.isSelected = isSelected
            if (isSelected) {
                binding.vBullet.backgroundTintList = ColorStateList.valueOf(primaryBlue)
                binding.tvSubMenuName.setTextColor(primaryBlue)
            } else {
                binding.vBullet.backgroundTintList = ColorStateList.valueOf(slate400)
                binding.tvSubMenuName.setTextColor(slate700)
            }
        }
    }
}
