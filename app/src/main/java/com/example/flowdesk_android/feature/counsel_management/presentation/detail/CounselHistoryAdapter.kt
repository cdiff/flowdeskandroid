package com.example.flowdesk_android.feature.counsel_management.presentation.detail

import android.graphics.Color
import android.graphics.drawable.GradientDrawable
import android.util.TypedValue
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.flowdesk_android.R
import com.example.flowdesk_android.feature.counsel_management.domain.model.CounselLog
import com.example.flowdesk_android.feature.counsel_management.domain.model.CounselStatusStat

class CounselHistoryAdapter : RecyclerView.Adapter<CounselHistoryAdapter.HistoryViewHolder>() {

    private val items = mutableListOf<CounselLog>()
    private val statusColorMap = mutableMapOf<String, Int>()

    fun submitList(newItems: List<CounselLog>) {
        items.clear()
        items.addAll(newItems)
        notifyDataSetChanged()
    }

    fun setStatusColors(stats: List<CounselStatusStat>) {
        statusColorMap.clear()
        stats.forEach { stat ->
            try {
                statusColorMap[stat.statusName] = Color.parseColor(stat.color)
            } catch (e: Exception) {
                // Ignore parsing errors
            }
        }
        notifyDataSetChanged()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): HistoryViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_counsel_history, parent, false)
        return HistoryViewHolder(view)
    }

    override fun onBindViewHolder(holder: HistoryViewHolder, position: Int) {
        val showTopLine = position > 0
        val showBottomLine = position < itemCount - 1
        holder.bind(items[position], showTopLine, showBottomLine, statusColorMap)
    }

    override fun getItemCount(): Int = items.size

    class HistoryViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val tvTitle: TextView = itemView.findViewById(R.id.tv_history_title)
        private val tvDate: TextView = itemView.findViewById(R.id.tv_history_date)
        private val vTopLine: View = itemView.findViewById(R.id.v_timeline_top_line)
        private val vBottomLine: View = itemView.findViewById(R.id.v_timeline_bottom_line)
        private val vDot: View = itemView.findViewById(R.id.cv_timeline_dot)

        fun bind(
            log: CounselLog,
            showTopLine: Boolean,
            showBottomLine: Boolean,
            colorMap: Map<String, Int>
        ) {
            tvTitle.text = log.statusName
            tvDate.text = formatDateTime(log.regDtm)

            // Timeline line visibility
            vTopLine.visibility = if (showTopLine) View.VISIBLE else View.INVISIBLE
            vBottomLine.visibility = if (showBottomLine) View.VISIBLE else View.INVISIBLE

            // Dot color matching status
            val statusColor = colorMap[log.statusName] ?: Color.parseColor("#4285F4") // Default blue

            // Set Dot Stroke Color
            val dotDrawable = vDot.background as? GradientDrawable
            if (dotDrawable != null) {
                val strokeWidthPx = TypedValue.applyDimension(
                    TypedValue.COMPLEX_UNIT_DIP,
                    2.5f,
                    vDot.context.resources.displayMetrics
                ).toInt()
                dotDrawable.setStroke(strokeWidthPx, statusColor)
            }

            // Set Title Chip Text, Text Color and Background (Alpha applied)
            tvTitle.setTextColor(statusColor)
            val chipDrawable = tvTitle.background as? GradientDrawable
            if (chipDrawable != null) {
                val alphaColor = Color.argb(
                    38, // 15% opacity
                    Color.red(statusColor),
                    Color.green(statusColor),
                    Color.blue(statusColor)
                )
                chipDrawable.setColor(alphaColor)
            }
        }

        private fun formatDateTime(iso: String): String {
            return try { "${iso.substring(0, 10)} ${iso.substring(11, 19)}" } catch (e: Exception) { iso }
        }
    }
}
