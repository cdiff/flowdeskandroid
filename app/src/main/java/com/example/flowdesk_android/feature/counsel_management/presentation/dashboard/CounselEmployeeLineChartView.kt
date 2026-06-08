package com.example.flowdesk_android.feature.counsel_management.presentation.dashboard

import android.content.Context
import android.graphics.*
import android.util.AttributeSet
import android.view.MotionEvent
import android.view.View
import com.example.flowdesk_android.feature.counsel_management.domain.model.EmployeeStat

class CounselEmployeeLineChartView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : View(context, attrs, defStyleAttr) {

    private var dataList: List<EmployeeStat> = emptyList()
    private var maxCount = 4
    private var selectedIndex = -1

    // Cached coordinates for touch detection
    private var px = FloatArray(0)
    private var py = FloatArray(0)

    // Paints
    private val gridPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        style = Paint.Style.STROKE
        color = Color.parseColor("#E5E7EB") // Gray 200 (Very light gray horizontal grid lines)
    }

    private val linePaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        style = Paint.Style.STROKE
        color = Color.parseColor("#3B82F6") // Blue 500 (Point color)
    }

    private val dotNormalPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        style = Paint.Style.FILL
        color = Color.parseColor("#94A3B8") // Slate 400 (Grey dots)
    }

    private val dotActiveInnerPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        style = Paint.Style.FILL
        color = Color.parseColor("#3B82F6") // Blue 500
    }

    private val dotActiveOuterPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        style = Paint.Style.FILL
        color = Color.parseColor("#3B82F6")
        alpha = 40 // ~15% opacity outer glow
    }

    private val dotBorderPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        style = Paint.Style.STROKE
        color = Color.WHITE
    }

    private val labelPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = Color.parseColor("#94A3B8") // Slate 400 (Grey text for axis labels)
    }

    private val xLabelPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = Color.parseColor("#475569") // Slate 600 (Darker gray for X axis label)
        typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
        textAlign = Paint.Align.CENTER
    }

    private val tooltipBgPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        style = Paint.Style.FILL
        color = Color.parseColor("#1E293B") // Slate 800 (Dark tooltip)
    }

    private val tooltipTextPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = Color.WHITE
        typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
        textAlign = Paint.Align.CENTER
    }

    init {
        val density = resources.displayMetrics.density
        gridPaint.strokeWidth = 1f * density
        linePaint.strokeWidth = 2f * density
        dotBorderPaint.strokeWidth = 1.5f * density
        labelPaint.textSize = 10f * density
        xLabelPaint.textSize = 11f * density
        tooltipTextPaint.textSize = 11f * density
    }

    fun setData(list: List<EmployeeStat>) {
        this.dataList = list
        val rawMax = list.maxOfOrNull { it.count } ?: 0
        this.maxCount = when {
            rawMax <= 4 -> 4
            rawMax <= 8 -> 8
            rawMax <= 12 -> 12
            rawMax <= 16 -> 16
            else -> (((rawMax - 1) / 4) + 1) * 4
        }

        // Set initial selected index to the one with the maximum count
        var maxIndex = -1
        for (i in list.indices) {
            if (list[i].count == rawMax && maxIndex == -1 && rawMax > 0) {
                maxIndex = i
            }
        }
        selectedIndex = if (maxIndex != -1) maxIndex else 0

        px = FloatArray(list.size)
        py = FloatArray(list.size)
        invalidate()
    }

    override fun onTouchEvent(event: MotionEvent): Boolean {
        if (dataList.isEmpty() || px.isEmpty() || py.isEmpty()) return false

        when (event.action) {
            MotionEvent.ACTION_DOWN, MotionEvent.ACTION_MOVE -> {
                val touchX = event.x
                val touchY = event.y
                val density = resources.displayMetrics.density
                val selectionThreshold = 40f * density // 40dp horizontal threshold

                var closestIndex = -1
                var minDistance = Float.MAX_VALUE

                for (i in 0 until dataList.size) {
                    val dx = Math.abs(px[i] - touchX)
                    if (dx < selectionThreshold && dx < minDistance) {
                        minDistance = dx
                        closestIndex = i
                    }
                }

                if (closestIndex != -1 && closestIndex != selectedIndex) {
                    selectedIndex = closestIndex
                    invalidate()
                }
                return true
            }
            MotionEvent.ACTION_UP -> {
                performClick()
                return true
            }
        }
        return super.onTouchEvent(event)
    }

    override fun performClick(): Boolean {
        super.performClick()
        return true
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)
        if (dataList.isEmpty()) return

        val density = resources.displayMetrics.density
        val paddingLeft = 38f * density
        val paddingRight = 12f * density
        val paddingTop = 28f * density
        val paddingBottom = 40f * density

        val chartWidth = width - paddingLeft - paddingRight
        val chartHeight = height - paddingTop - paddingBottom
        if (chartWidth <= 0 || chartHeight <= 0) return

        val n = dataList.size

        // 1. Draw horizontal grid lines (5 lines) and Y-axis labels
        val fontMetrics = labelPaint.fontMetrics
        val textOffset = -(fontMetrics.ascent + fontMetrics.descent) / 2f

        for (k in 0..4) {
            val yVal = (maxCount * (k / 4f)).toInt()
            val y = paddingTop + chartHeight - (k / 4f) * chartHeight

            // Draw thin line
            canvas.drawLine(paddingLeft, y, width - paddingRight, y, gridPaint)

            // Draw Y label
            val labelText = "${yVal}건"
            labelPaint.textAlign = Paint.Align.RIGHT
            canvas.drawText(labelText, paddingLeft - 8f * density, y + textOffset, labelPaint)
        }

        // Initialize / Update coordinates
        if (px.size != n) px = FloatArray(n)
        if (py.size != n) py = FloatArray(n)

        val sidePadding = 12f * density // 12dp padding from the edges of the grid
        val contentWidth = chartWidth - 2 * sidePadding

        for (i in 0 until n) {
            px[i] = if (n > 1) {
                paddingLeft + sidePadding + i * (contentWidth / (n - 1))
            } else {
                paddingLeft + chartWidth / 2f
            }
            py[i] = paddingTop + chartHeight - (dataList[i].count / maxCount.toFloat()) * chartHeight
        }

        // 2. Draw line path
        val path = Path()
        for (i in 0 until n) {
            if (i == 0) {
                path.moveTo(px[i], py[i])
            } else {
                path.lineTo(px[i], py[i])
            }
        }
        canvas.drawPath(path, linePaint)

        // 3. Draw dots and X-axis labels
        val dotRadiusNormal = 4f * density
        val dotRadiusActiveInner = 5f * density
        val dotRadiusActiveOuter = 10f * density

        for (i in 0 until n) {
            val isSelected = (i == selectedIndex)

            if (isSelected) {
                // Outer glow
                canvas.drawCircle(px[i], py[i], dotRadiusActiveOuter, dotActiveOuterPaint)
                // Inner solid blue dot
                canvas.drawCircle(px[i], py[i], dotRadiusActiveInner, dotActiveInnerPaint)
                // White border
                canvas.drawCircle(px[i], py[i], dotRadiusActiveInner, dotBorderPaint)
            } else {
                // Grey dot
                canvas.drawCircle(px[i], py[i], dotRadiusNormal, dotNormalPaint)
            }

            // X-axis Label (Manager Name)
            val name = dataList[i].empName
            canvas.drawText(name, px[i], height - 12f * density, xLabelPaint)
        }

        // 4. Draw Tooltip for the Selected point
        if (selectedIndex >= 0 && selectedIndex < n) {
            val mx = px[selectedIndex]
            val my = py[selectedIndex]
            val tooltipText = "${dataList[selectedIndex].count}건"

            // Measure text width
            val textWidth = tooltipTextPaint.measureText(tooltipText)
            val rectW = textWidth + 20f * density
            val rectH = 26f * density

            val rectLeft = mx - rectW / 2f
            val rectTop = my - 10f * density - rectH
            val rectRight = mx + rectW / 2f
            val rectBottom = my - 10f * density

            // Draw tooltip rounded rectangle
            val rectF = RectF(rectLeft, rectTop, rectRight, rectBottom)
            canvas.drawRoundRect(rectF, 4f * density, 4f * density, tooltipBgPaint)

            // Draw small triangle pointing down
            val trianglePath = Path().apply {
                moveTo(mx - 4f * density, rectBottom)
                lineTo(mx + 4f * density, rectBottom)
                lineTo(mx, rectBottom + 4f * density)
                close()
            }
            canvas.drawPath(trianglePath, tooltipBgPaint)

            // Draw text
            val tooltipFontMetrics = tooltipTextPaint.fontMetrics
            val tooltipTextOffset = -(tooltipFontMetrics.ascent + tooltipFontMetrics.descent) / 2f
            canvas.drawText(tooltipText, mx, rectTop + rectH / 2f + tooltipTextOffset, tooltipTextPaint)
        }
    }
}
