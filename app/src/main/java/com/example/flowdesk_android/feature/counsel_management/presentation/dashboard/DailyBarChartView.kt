package com.example.flowdesk_android.feature.counsel_management.presentation.dashboard

import android.content.Context
import android.graphics.*
import android.util.AttributeSet
import android.view.View
import com.example.flowdesk_android.feature.counsel_management.domain.model.DailyTrend

class DailyBarChartView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : View(context, attrs, defStyleAttr) {

    private var dataList: List<DailyTrend> = emptyList()

    private val barPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        style = Paint.Style.FILL
        color = Color.parseColor("#E2E8F0") // Light gray for regular days
    }

    private val todayBarPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        style = Paint.Style.FILL
        color = Color.parseColor("#3B82F6") // Blue 500 for today
    }

    private val labelPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = Color.parseColor("#94A3B8") // Gray/Slate 400
        textAlign = Paint.Align.CENTER
    }

    private val todayLabelPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = Color.parseColor("#3B82F6") // Blue 500
        textAlign = Paint.Align.CENTER
        typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
    }

    private val valuePaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = Color.parseColor("#64748B") // Slate 500
        textAlign = Paint.Align.CENTER
    }

    private val todayValuePaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = Color.parseColor("#3B82F6") // Blue 500
        textAlign = Paint.Align.CENTER
        typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
    }

    init {
        val density = resources.displayMetrics.density
        labelPaint.textSize = 11f * density
        todayLabelPaint.textSize = 11f * density
        valuePaint.textSize = 11f * density
        todayValuePaint.textSize = 11f * density
    }

    fun setData(trends: List<DailyTrend>) {
        // Limit to 6 items as requested: "그래프 개숫는 6개만하고"
        this.dataList = trends.sortedBy { it.date }.takeLast(6)
        invalidate()
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)
        if (dataList.isEmpty()) return

        val density = resources.displayMetrics.density
        val w = width.toFloat()
        val h = height.toFloat()

        val paddingLeft = 16f * density
        val paddingRight = 16f * density
        val paddingTop = 28f * density
        val paddingBottom = 24f * density

        val contentW = w - paddingLeft - paddingRight
        val contentH = h - paddingTop - paddingBottom
        if (contentW <= 0 || contentH <= 0) return

        val n = dataList.size
        val colW = contentW / n
        val barW = colW * 0.62f // Bar takes 62% of the column width (thicker layout)
        val rx = 6f * density // 6dp corner radius for rounded top

        val maxVal = dataList.maxOfOrNull { it.count }?.coerceAtLeast(1) ?: 1

        for (i in 0 until n) {
            val item = dataList[i]
            val isToday = (i == n - 1) // The last element is "today"

            // Calculate coordinates
            val colCenter = paddingLeft + (i * colW) + (colW / 2)
            val left = colCenter - (barW / 2)
            val right = colCenter + (barW / 2)
            val bottom = h - paddingBottom

            val barHeight = contentH * (item.count.toFloat() / maxVal)
            val top = bottom - barHeight

            // Draw Bar with rounded top corners
            val paint = if (isToday) todayBarPaint else barPaint
            val path = Path()
            path.moveTo(left, bottom)

            // Adjust rx if bar height is too small
            val actualRx = Math.min(rx, barHeight)

            path.lineTo(left, top + actualRx)
            path.quadTo(left, top, left + actualRx, top)
            path.lineTo(right - actualRx, top)
            path.quadTo(right, top, right, top + actualRx)
            path.lineTo(right, bottom)
            path.close()

            canvas.drawPath(path, paint)

            // Draw X-axis Label
            val labelStr = if (isToday) {
                "오늘"
            } else {
                // Parse date like "2026-06-08" to "6.8" or "06-08"
                val parts = item.date.split("-")
                if (parts.size >= 3) {
                    "${parts[1].toInt()}.${parts[2].toInt()}"
                } else {
                    item.date
                }
            }
            val lPaint = if (isToday) todayLabelPaint else labelPaint
            val labelY = h - 6f * density
            canvas.drawText(labelStr, colCenter, labelY, lPaint)

            // Draw Value above the bar
            val valueStr = "${item.count}건"
            val vPaint = if (isToday) todayValuePaint else valuePaint
            val valueY = top - 6f * density
            canvas.drawText(valueStr, colCenter, valueY, vPaint)
        }
    }
}
