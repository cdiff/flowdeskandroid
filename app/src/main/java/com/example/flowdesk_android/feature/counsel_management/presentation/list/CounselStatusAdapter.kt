package com.example.flowdesk_android.feature.counsel_management.presentation.list

import android.animation.ValueAnimator
import android.content.Context
import android.graphics.Color
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.flowdesk_android.databinding.ItemCounselStatusTabBinding
import com.example.flowdesk_android.feature.counsel_management.domain.model.CounselStatusStat

class CounselStatusAdapter(
    private val onStatusSelected: (CounselStatusStat?) -> Unit
) : RecyclerView.Adapter<CounselStatusAdapter.StatusViewHolder>() {

    private val items = mutableListOf<CounselStatusStat>()
    private var selectedPosition = -1 // -1 means no status filter selected ("Total" is active)

    fun submitList(newList: List<CounselStatusStat>) {
        items.clear()
        items.addAll(newList)
        notifyDataSetChanged()
    }

    fun clearSelection() {
        val prev = selectedPosition
        selectedPosition = -1
        if (prev != -1) notifyItemChanged(prev)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): StatusViewHolder {
        val binding = ItemCounselStatusTabBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return StatusViewHolder(binding)
    }

    override fun onBindViewHolder(holder: StatusViewHolder, position: Int) {
        holder.bind(items[position], position == selectedPosition)
    }

    override fun getItemCount(): Int = items.size

    inner class StatusViewHolder(val binding: ItemCounselStatusTabBinding) : RecyclerView.ViewHolder(binding.root) {

        fun bind(item: CounselStatusStat, isSelected: Boolean) {
            val context = itemView.context
            binding.tvStatusLabel.text = item.statusName
            binding.tvStatusCount.text = item.count.toString()

            val statusColor = try {
                Color.parseColor(item.color)
            } catch (e: Exception) {
                Color.parseColor("#4285F4")
            }

            binding.viewStatusColorFill.setBackgroundColor(statusColor)

            if (isSelected) {
                binding.tvStatusLabel.setTextColor(Color.parseColor("#111827"))
                binding.tvStatusCount.setTextColor(Color.WHITE)
                binding.cardCircleContainer.strokeColor = statusColor
                binding.cardCircleContainer.strokeWidth = 0
                binding.ivStatusIndicator.visibility = View.VISIBLE

                // Animate expansion from 6dp to 56dp
                animateHeight(6.dpToPx(context), 56.dpToPx(context))
            } else {
                binding.tvStatusLabel.setTextColor(Color.parseColor("#6B7280"))
                binding.tvStatusCount.setTextColor(Color.parseColor("#374151"))
                binding.cardCircleContainer.strokeColor = Color.parseColor("#E2E8F0")
                binding.cardCircleContainer.strokeWidth = 1.5.dpToPx(context)
                binding.ivStatusIndicator.visibility = View.INVISIBLE

                // Reset height to 6dp
                val params = binding.viewStatusColorFill.layoutParams
                params.height = 6.dpToPx(context)
                binding.viewStatusColorFill.layoutParams = params
            }

            itemView.setOnClickListener {
                val pos = bindingAdapterPosition
                if (pos != RecyclerView.NO_POSITION && selectedPosition != pos) {
                    val prev = selectedPosition
                    selectedPosition = pos
                    if (prev != -1) notifyItemChanged(prev)
                    notifyItemChanged(selectedPosition)
                    onStatusSelected(items[pos])
                }
            }
        }

        private fun animateHeight(from: Int, to: Int) {
            val animator = ValueAnimator.ofInt(from, to)
            animator.duration = 200
            animator.addUpdateListener { anim ->
                val h = anim.animatedValue as Int
                val params = binding.viewStatusColorFill.layoutParams
                params.height = h
                binding.viewStatusColorFill.layoutParams = params
            }
            animator.start()
        }

        private fun Int.dpToPx(context: Context): Int {
            return (this * context.resources.displayMetrics.density).toInt()
        }

        private fun Double.dpToPx(context: Context): Int {
            return (this * context.resources.displayMetrics.density).toInt()
        }
    }
}
