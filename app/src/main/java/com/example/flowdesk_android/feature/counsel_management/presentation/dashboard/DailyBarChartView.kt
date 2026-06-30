package com.example.flowdesk_android.feature.counsel_management.presentation.dashboard

import android.animation.ValueAnimator
import android.content.Context
import android.graphics.*
import android.util.AttributeSet
import android.view.View
import android.view.animation.AccelerateDecelerateInterpolator
import com.example.flowdesk_android.feature.counsel_management.domain.model.DailyTrend

class DailyBarChartView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : View(context, attrs, defStyleAttr) {

    private var dataList: List<DailyTrend> = emptyList()
    private var maxCount = 4

    // Animation progress (0f to 1f)
    private var animationProgress = 0f
    private var animator: ValueAnimator? = null

    // Month comparison anchor (e.g., "06" for June)
    private var currentMonthStr = ""

    private val rectF = RectF()

    // Paints
    private val activeBarPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        style = Paint.Style.FILL
        color = Color.parseColor("#3B82F6") // Blue 500
    }

    private val inactiveBarPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        style = Paint.Style.FILL
        color = Color.parseColor("#E2E8F0") // Light gray (Slate/Gray 200)
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
        labelPaint.textSize = 10f * density
        todayLabelPaint.textSize = 10f * density
        valuePaint.textSize = 10f * density
        todayValuePaint.textSize = 10f * density
    }

    fun setData(trends: List<DailyTrend>) {
        this.dataList = trends.sortedBy { it.date }

        val rawMax = dataList.maxOfOrNull { it.count } ?: 0
        this.maxCount = rawMax.coerceAtLeast(1)

        // Extract the month from the last/most recent item (e.g., "06" from "2026-06-16")
        val lastItem = dataList.lastOrNull()
        if (lastItem != null) {
            val parts = lastItem.date.split("-")
            if (parts.size >= 2) {
                currentMonthStr = parts[1]
            }
        }

        // Run sequential bar rise animation
        animator?.cancel()
        animator = ValueAnimator.ofFloat(0f, 1f).apply {
            duration = 450
            interpolator = AccelerateDecelerateInterpolator()
            addUpdateListener { animation ->
                animationProgress = animation.animatedValue as Float
                invalidate()
            }
            start()
        }
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)
        if (dataList.isEmpty()) return

        val density = resources.displayMetrics.density
        val w = width.toFloat()
        val h = height.toFloat()

        val paddingLeft = 12f * density
        val paddingRight = 12f * density
        val paddingTop = 24f * density
        val paddingBottom = 24f * density

        val contentW = w - paddingLeft - paddingRight
        val contentH = h - paddingTop - paddingBottom
        if (contentW <= 0 || contentH <= 0) return

        val n = dataList.size
        val colW = contentW / n

        // Adjust bar fill ratio based on data size so bars aren't too thin
        val barRatio = when {
            n <= 6 -> 0.62f // Make 1w mode bars thick as per mockups
            n <= 14 -> 0.55f
            else -> 0.70f
        }
        val barW = (colW * barRatio).coerceAtLeast(2f * density)

        // Corner radius (maximum 6dp, and restricted to barW / 3.5f to preserve a flat top for narrow bars)
        val rx = (6f * density).coerceAtMost(barW / 3.5f)

        for (i in 0 until n) {
            val item = dataList[i]
            val isToday = (i == n - 1)

            // Determine if the bar should be highlighted in Blue
            val isHighlighted = if (n <= 6) {
                isToday // 1w mode: highlight only the last bar (Today)
            } else {
                // 2w / 1m mode: highlight all bars from the current month
                var isCurrentMonth = false
                val parts = item.date.split("-")
                if (parts.size >= 2) {
                    isCurrentMonth = (parts[1] == currentMonthStr)
                }
                isCurrentMonth
            }

            val parts = item.date.split("-")

            // Calculate layout coordinates
            val colCenter = paddingLeft + (i * colW) + (colW / 2)
            val left = colCenter - (barW / 2)
            val right = colCenter + (barW / 2)
            val bottom = h - paddingBottom

            // Calculate height with sequential rise animation progress
            // Delay rises from left to right: delay scales from 0.0 to 0.4 based on index
            val delayFraction = if (n > 1) (i.toFloat() / (n - 1) * 0.4f) else 0f
            val barProgress = ((animationProgress - delayFraction) / (1f - delayFraction)).coerceIn(0f, 1f)

            val minHeight = 4f * density // 4dp minimum height to show a subtle bar silhouette for 0 count
            val baseHeight = contentH * (item.count.toFloat() / maxCount)
            val fullHeight = if (item.count == 0) minHeight else baseHeight.coerceAtLeast(minHeight)
            val barHeight = fullHeight * barProgress
            val top = bottom - barHeight

            // Draw Bar (only if height is greater than 0)
            if (barHeight > 0f) {
                val paint = if (isHighlighted) activeBarPaint else inactiveBarPaint
                val path = Path()
                val actualRx = Math.min(rx, barHeight)
                val bottomRx = actualRx * 0.5f // Round bottom corners slightly (half of the top radius)

                rectF.set(left, top, right, bottom)
                // Top-left and top-right rounded with actualRx, bottom-right and bottom-left slightly rounded
                val radii = floatArrayOf(
                    actualRx, actualRx, // Top-left
                    actualRx, actualRx, // Top-right
                    bottomRx, bottomRx, // Bottom-right
                    bottomRx, bottomRx  // Bottom-left
                )
                path.addRoundRect(rectF, radii, Path.Direction.CW)

                canvas.drawPath(path, paint)
            }

            // Draw X-axis label using Smart Label Skipping
            val shouldDrawLabel = when {
                n <= 6 -> true // 1. Always draw all labels if <= 6 items (1w)
                isToday -> true // 2. Always draw the last label (anchor date)
                item.date.endsWith("-01") -> true // 3. Always highlight the start of a month (e.g. "6.1")
                n <= 14 -> (n - 1 - i) % 3 == 0 // 4. Draw labels every 3 days working backwards for 2w
                else -> (n - 1 - i) % 7 == 0 // 5. Draw labels every 7 days working backwards for 1m
            }

            if (shouldDrawLabel) {
                val labelStr = if (parts.size >= 3) {
                    "${parts[1].toInt()}.${parts[2].toInt()}"
                } else {
                    item.date
                }
                val lPaint = if (isToday) todayLabelPaint else labelPaint
                val labelY = h - 6f * density
                canvas.drawText(labelStr, colCenter, labelY, lPaint)
            }

            // Draw Value count above the bar (only when screen is not cluttered, e.g. <= 6 bars)
            if (n <= 6 && barHeight > 0f) {
                val valueStr = "${item.count}건"
                val vPaint = if (isToday) todayValuePaint else valuePaint
                val valueY = top - 6f * density
                canvas.drawText(valueStr, colCenter, valueY, vPaint)
            }
        }
    }
}
