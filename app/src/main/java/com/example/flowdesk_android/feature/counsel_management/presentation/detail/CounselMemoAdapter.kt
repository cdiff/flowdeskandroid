package com.example.flowdesk_android.feature.counsel_management.presentation.detail

import android.graphics.Color
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.flowdesk_android.R
import com.example.flowdesk_android.feature.counsel_management.domain.model.CounselMemo

class CounselMemoAdapter : RecyclerView.Adapter<CounselMemoAdapter.MemoViewHolder>() {

    private val items = mutableListOf<CounselMemo>()

    fun submitList(newItems: List<CounselMemo>) {
        items.clear()
        items.addAll(newItems)
        notifyDataSetChanged()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MemoViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_counsel_memo, parent, false)
        return MemoViewHolder(view)
    }

    override fun onBindViewHolder(holder: MemoViewHolder, position: Int) {
        holder.bind(items[position])
    }

    override fun getItemCount(): Int = items.size

    class MemoViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val tvWriter: TextView = itemView.findViewById(R.id.tv_memo_writer)
        private val tvDate: TextView = itemView.findViewById(R.id.tv_memo_date)
        private val tvContent: TextView = itemView.findViewById(R.id.tv_memo_content)

        fun bind(memo: CounselMemo) {
            tvWriter.text = memo.creatorName
            tvDate.text = formatDateTime(memo.createdAt)
            tvContent.text = memo.memoText
        }

        private fun formatDateTime(iso: String): String {
            return try { "${iso.substring(0, 10)} ${iso.substring(11, 19)}" } catch (e: Exception) { iso }
        }
    }
}
