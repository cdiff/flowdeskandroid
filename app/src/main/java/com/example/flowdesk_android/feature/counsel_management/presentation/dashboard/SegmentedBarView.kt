package com.example.flowdesk_android.feature.counsel_management.presentation.dashboard

import android.content.Context
import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.Path
import android.graphics.RectF
import android.util.AttributeSet
import android.view.View

class SegmentedBarView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : View(context, attrs, defStyleAttr) {

    data class Segment(
        val value: Float,
        val color: Int
    )

    private var segments: List<Segment> = emptyList()
    private val paint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        style = Paint.Style.FILL
    }
    private val clipPath = Path()
    private val rectF = RectF()

    fun setSegments(newSegments: List<Segment>) {
        this.segments = newSegments.filter { it.value > 0 }
        invalidate()
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)
        if (segments.isEmpty()) return

        val w = width.toFloat()
        val h = height.toFloat()
        val radius = 4f * resources.displayMetrics.density // 4dp corner radius (more square/boxy)

        // Clip the drawing region to a rounded rectangle
        clipPath.reset()
        rectF.set(0f, 0f, w, h)
        clipPath.addRoundRect(rectF, radius, radius, Path.Direction.CW)
        canvas.save()
        canvas.clipPath(clipPath)

        val total = segments.sumOf { it.value.toDouble() }.toFloat()
        if (total > 0) {
            var currentX = 0f
            segments.forEach { segment ->
                val segmentWidth = (segment.value / total) * w
                val nextX = currentX + segmentWidth

                paint.color = segment.color
                canvas.drawRect(currentX, 0f, nextX, h, paint)

                currentX = nextX
            }
        }
        canvas.restore()
    }
}
