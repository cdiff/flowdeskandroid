package com.example.flowdesk_android.feature.counsel_management.presentation.dashboard

import android.content.Context
import android.graphics.*
import android.util.AttributeSet
import android.view.MotionEvent
import android.view.View
import com.example.flowdesk_android.feature.counsel_management.domain.model.HourlyDistribution

class CounselHourlyLineChartView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : View(context, attrs, defStyleAttr) {

    private var dataList: List<HourlyDistribution> = emptyList()
    private var maxCount = 4
    private var selectedIndex = -1

    private var px = FloatArray(0)
    private var py = FloatArray(0)

    private val density = resources.displayMetrics.density
    private val tooltipHelper = ChartTooltipHelper(context)

    // Paints
    private val gridPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        style = Paint.Style.STROKE
        color = Color.parseColor("#E5E7EB") // Gray 200
        strokeWidth = 1f * density
    }

    private val linePaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        style = Paint.Style.STROKE
        color = Color.parseColor("#3B82F6") // Blue 500
        strokeWidth = 2f * density
    }

    private val fillPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        style = Paint.Style.FILL
    }

    private val highlightLinePaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        style = Paint.Style.STROKE
        color = Color.parseColor("#CBD5E1") // Slate 300
        strokeWidth = 1f * density
    }

    private val dotActiveInnerPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        style = Paint.Style.FILL
        color = Color.WHITE
    }

    private val dotActiveBorderPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        style = Paint.Style.STROKE
        color = Color.parseColor("#3B82F6") // Blue 500 border
        strokeWidth = 1.5f * density
    }

    private val xLabelPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = Color.parseColor("#94A3B8") // Slate 400
        textSize = 9f * density
        textAlign = Paint.Align.CENTER
    }

    fun setData(list: List<HourlyDistribution>) {
        val countMap = list.associate { it.hour to it.count }
        val fullList = (0..23).map { h ->
            HourlyDistribution(hour = h, count = countMap[h] ?: 0)
        }
        this.dataList = fullList

        val rawMax = fullList.maxOfOrNull { it.count } ?: 0
        this.maxCount = when {
            rawMax <= 4 -> 4
            rawMax <= 8 -> 8
            rawMax <= 12 -> 12
            rawMax <= 16 -> 16
            else -> (((rawMax - 1) / 4) + 1) * 4
        }.coerceAtLeast(4)

        // 평상시에는 툴팁 및 포인트가 보이지 않도록 -1로 셋팅
        selectedIndex = -1

        px = FloatArray(fullList.size)
        py = FloatArray(fullList.size)
        invalidate()
    }

    override fun onTouchEvent(event: MotionEvent): Boolean {
        if (dataList.isEmpty() || px.isEmpty() || py.isEmpty()) return false

        when (event.action) {
            MotionEvent.ACTION_DOWN, MotionEvent.ACTION_MOVE -> {
                parent.requestDisallowInterceptTouchEvent(true)
                val touchX = event.x
                val selectionThreshold = 24f * density // 24dp horizontal threshold for 24 points

                var closestIndex = -1
                var minDistance = Float.MAX_VALUE

                for (i in px.indices) {
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
            MotionEvent.ACTION_UP, MotionEvent.ACTION_CANCEL -> {
                parent.requestDisallowInterceptTouchEvent(false)
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

        val paddingLeft = 16f * density
        val paddingRight = 16f * density
        val paddingTop = 32f * density
        val paddingBottom = 24f * density

        val chartWidth = width - paddingLeft - paddingRight
        val chartHeight = height - paddingTop - paddingBottom
        if (chartWidth <= 0 || chartHeight <= 0) return

        val n = dataList.size
        val bottomY = paddingTop + chartHeight

        // 1. Draw horizontal grid lines (5 grid lines), Y labels are hidden as per requirements
        for (k in 0..4) {
            val y = paddingTop + chartHeight - (k / 4f) * chartHeight
            canvas.drawLine(paddingLeft, y, width - paddingRight, y, gridPaint)
        }

        // Initialize / Update coordinates
        if (px.size != n) px = FloatArray(n)
        if (py.size != n) py = FloatArray(n)

        val cellWidth = chartWidth / (n - 1).toFloat()

        for (i in 0 until n) {
            px[i] = paddingLeft + i * cellWidth
            py[i] = paddingTop + chartHeight - (dataList[i].count / maxCount.toFloat()) * chartHeight
        }

        // 2. Draw line path using smooth cubic curves (Bezier)
        val path = Path()
        path.moveTo(px[0], py[0])
        for (i in 1 until n) {
            val prevX = px[i - 1]
            val prevY = py[i - 1]
            val currX = px[i]
            val currY = py[i]

            // Control points for smooth bez curves
            val conX1 = prevX + (currX - prevX) / 2f
            val conY1 = prevY
            val conX2 = prevX + (currX - prevX) / 2f
            val conY2 = currY

            path.cubicTo(conX1, conY1, conX2, conY2, currX, currY)
        }
        canvas.drawPath(path, linePaint)

        // 3. Draw gradient fill underneath the curve
        val fillPath = Path(path)
        fillPath.lineTo(px[n - 1], bottomY)
        fillPath.lineTo(px[0], bottomY)
        fillPath.close()

        val fillGradient = LinearGradient(
            0f, paddingTop, 0f, bottomY,
            Color.parseColor("#E0F2FE"), // Light Sky Blue 100
            Color.parseColor("#FFFFFF"), // White
            Shader.TileMode.CLAMP
        )
        fillPaint.shader = fillGradient
        canvas.drawPath(fillPath, fillPaint)

        // 4. Draw X-axis labels (0시, 4시, 8시, 12시, 16시, 20시, 24시 대응)
        // 4시간 단위로 인쇄
        for (i in 0 until n) {
            if (i % 4 == 0) {
                val labelText = "${i}시"
                canvas.drawText(labelText, px[i], height - 6f * density, xLabelPaint)
            }
        }
        // 24시 라벨은 마지막 칸 끝쪽에 맞게 그려줌 (n-1은 23이므로, X축 편의상 마지막 부분에 수동 추가 가능)
        if ((n - 1) % 4 != 0) {
            canvas.drawText("24시", px[n - 1] + 2f * density, height - 6f * density, xLabelPaint)
        }

        // 5. Draw highlighted guides, dots and tooltips (Only when touched)
        if (selectedIndex in 0 until n) {
            val mx = px[selectedIndex]
            val my = py[selectedIndex]

            // A. Draw vertical guide line: from bottom up to data point
            canvas.drawLine(mx, bottomY, mx, my, highlightLinePaint)

            // B. Draw active point (Circle: inner white, outer blue)
            val dotRadius = 3f * density
            canvas.drawCircle(mx, my, dotRadius, dotActiveInnerPaint)
            canvas.drawCircle(mx, my, dotRadius, dotActiveBorderPaint)

            // C. Draw premium white-orange tooltip: "15시 | 3건" 포맷 적용
            val tooltipText = "${dataList[selectedIndex].hour}시 | ${dataList[selectedIndex].count}건"
            tooltipHelper.drawTooltip(canvas, tooltipText, mx, my)
        }
    }
}
