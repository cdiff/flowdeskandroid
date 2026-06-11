package com.example.flowdesk_android.feature.counsel_management.presentation.detail

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.flowdesk_android.R
import com.example.flowdesk_android.feature.counsel_management.domain.model.CounselLog

class CounselHistoryAdapter : RecyclerView.Adapter<CounselHistoryAdapter.HistoryViewHolder>() {

    private val items = mutableListOf<CounselLog>()

    fun submitList(newItems: List<CounselLog>) {
        items.clear()
        items.addAll(newItems)
        notifyDataSetChanged()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): HistoryViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_counsel_history, parent, false)
        return HistoryViewHolder(view)
    }

    override fun onBindViewHolder(holder: HistoryViewHolder, position: Int) {
        holder.bind(items[position])
    }

    override fun getItemCount(): Int = items.size

    class HistoryViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val tvTitle: TextView = itemView.findViewById(R.id.tv_history_title)
        private val tvAuthor: TextView = itemView.findViewById(R.id.tv_history_author)
        private val tvDate: TextView = itemView.findViewById(R.id.tv_history_date)

        fun bind(log: CounselLog) {
            tvTitle.text = "상태: ${log.statusName}"
            tvAuthor.text = "이력 번호: ${log.logNo}"
            tvDate.text = formatDateTime(log.regDtm)
        }

        private fun formatDateTime(iso: String): String {
            return try { "${iso.substring(0, 10)} ${iso.substring(11, 19)}" } catch (e: Exception) { iso }
        }
    }
}
