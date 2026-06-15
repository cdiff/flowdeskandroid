package com.example.flowdesk_android.feature.counsel_management.presentation.list

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.graphics.Color
import android.graphics.drawable.GradientDrawable
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.widget.PopupMenu
import androidx.recyclerview.widget.RecyclerView
import com.example.flowdesk_android.R
import com.example.flowdesk_android.databinding.ItemCounselListBinding
import com.example.flowdesk_android.feature.counsel_management.domain.model.CounselItem
import com.example.flowdesk_android.feature.counsel_management.domain.model.CounselStatusStat

class CounselListAdapter(
    private val onCopyClick: (String) -> Unit,
    private val onOptionsClick: (CounselItem, View) -> Unit,
    private val onItemClick: (CounselItem) -> Unit = {}
) : RecyclerView.Adapter<CounselListAdapter.CounselViewHolder>() {

    private val items = mutableListOf<CounselItem>()
    private val statusColorMap = mutableMapOf<String, Int>()

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

    fun submitList(newList: List<CounselItem>) {
        items.clear()
        items.addAll(newList)
        notifyDataSetChanged()
    }

    fun addList(additionalList: List<CounselItem>) {
        val start = items.size
        items.addAll(additionalList)
        notifyItemRangeInserted(start, additionalList.size)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CounselViewHolder {
        val binding = ItemCounselListBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return CounselViewHolder(binding)
    }

    override fun onBindViewHolder(holder: CounselViewHolder, position: Int) {
        holder.bind(items[position])
    }

    override fun getItemCount(): Int = items.size

    inner class CounselViewHolder(private val binding: ItemCounselListBinding) : RecyclerView.ViewHolder(binding.root) {

        fun bind(item: CounselItem) {
            val context = itemView.context

            // 0. 카드 전체 클릭 → 상세 화면 이동
            itemView.setOnClickListener { onItemClick(item) }

            // 2. Status Tag (Dynamic Background with Soft Tint matching Status Color)
            val statusColor = getStatusColor(item.statusName)
            val softBg = Color.argb(25, Color.red(statusColor), Color.green(statusColor), Color.blue(statusColor))
            val badgeBg = GradientDrawable().apply {
                setColor(softBg)
                cornerRadius = 4.dpToPx(context).toFloat()
            }
            binding.tvStatusTag.background = badgeBg
            binding.tvStatusTag.setTextColor(statusColor)
            binding.tvStatusTag.text = item.statusName

            // 3. Duplicate Tag
            binding.tvDuplicateTag.visibility = if (item.duplicateState == "Y" && item.statusName != "중복") View.VISIBLE else View.GONE

            // 4. Website and Option Menu
            binding.tvWebsite.text = item.webTitle
            binding.ivMore.setOnClickListener { view ->
                onOptionsClick(item, view)
            }

            // 5. Customer Name & Phone Number
            binding.tvCustomerName.text = item.name
            binding.tvPhone.text = item.counselHp
            binding.btnCopyPhone.setOnClickListener {
                onCopyClick(item.counselHp)
            }

            // 6. Assigned Employee Spinner
            binding.tvEmployeeName.text = item.empName ?: "미배정"
            binding.layoutEmployee.setOnClickListener { view ->
                val popup = PopupMenu(context, view)
                popup.menu.add("담당자 변경 (준비 중)")
                popup.show()
            }

            // 7. Dates (Registration & Reservation)
            binding.tvRegDate.text = "등록일: ${formatDateTime(item.regDtm)}"
            val resvDateStr = formatDateTime(item.counselResvDtm)
            if (!resvDateStr.isNullOrBlank()) {
                binding.tvResvDate.text = "예약일: $resvDateStr"
                binding.tvResvDate.visibility = View.VISIBLE
            } else {
                binding.tvResvDate.visibility = View.GONE
            }

            // 8. Custom dynamic fields values
            binding.layoutCustomFields.removeAllViews()
            if (item.fieldValues.isNotEmpty()) {
                item.fieldValues.forEach { fv ->
                    val rawVal = when (fv.fieldType) {
                        "number" -> fv.valueNumber?.let { if (it % 1.0 == 0.0) it.toInt().toString() else it.toString() }
                        "date" -> fv.valueDate
                        "datetime" -> fv.valueDatetime
                        else -> fv.valueText
                    }
                    if (!rawVal.isNullOrBlank()) {
                        val tv = TextView(context).apply {
                            text = "${fv.label}: $rawVal"
                            textSize = 10f
                            setTextColor(Color.parseColor("#4B5563"))
                            setPadding(8.dpToPx(context), 3.dpToPx(context), 8.dpToPx(context), 3.dpToPx(context))
                            background = GradientDrawable().apply {
                                setColor(Color.parseColor("#E5E7EB"))
                                cornerRadius = 4.dpToPx(context).toFloat()
                            }
                            layoutParams = LinearLayout.LayoutParams(
                                LinearLayout.LayoutParams.WRAP_CONTENT,
                                LinearLayout.LayoutParams.WRAP_CONTENT
                            ).apply {
                                rightMargin = 6.dpToPx(context)
                            }
                        }
                        binding.layoutCustomFields.addView(tv)
                    }
                }
                binding.layoutCustomFields.visibility = View.VISIBLE
            } else {
                binding.layoutCustomFields.visibility = View.GONE
            }
        }

        private fun getStatusColor(statusName: String): Int {
            return statusColorMap[statusName] ?: Color.parseColor("#9CA3AF")
        }

        private fun formatDateTime(isoString: String?): String? {
            if (isoString.isNullOrBlank()) return null
            return try {
                val datePart = isoString.substring(0, 10)
                val timePart = isoString.substring(11, 16)
                "$datePart $timePart"
            } catch (e: Exception) {
                isoString
            }
        }

        private fun Int.dpToPx(context: Context): Int {
            return (this * context.resources.displayMetrics.density).toInt()
        }
    }
}
