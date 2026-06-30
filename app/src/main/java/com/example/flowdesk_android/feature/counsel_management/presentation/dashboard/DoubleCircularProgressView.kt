package com.example.flowdesk_android.feature.counsel_management.presentation.dashboard

import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.RectF
import android.util.AttributeSet
import android.view.View
import androidx.core.content.ContextCompat
import com.example.flowdesk_android.R

class DoubleCircularProgressView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : View(context, attrs, defStyleAttr) {

    private var outerProgress = 0f // 0..100
    private var innerProgress = 0f // 0..100

    private val outerPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        style = Paint.Style.STROKE
        strokeCap = Paint.Cap.ROUND
    }

    private val outerBgPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        style = Paint.Style.STROKE
        color = Color.parseColor("#E8F0FE") // light blue background
    }

    private val innerPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        style = Paint.Style.STROKE
        strokeCap = Paint.Cap.ROUND
    }

    private val innerBgPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        style = Paint.Style.STROKE
        color = Color.parseColor("#F3F4F6") // light gray background
    }

    private val outerRect = RectF()
    private val innerRect = RectF()

    private var strokeWidthPx = 24f
    private var spacingPx = 12f

    init {
        val density = resources.displayMetrics.density
        strokeWidthPx = 10f * density // 10dp
        spacingPx = 6f * density // 6dp

        outerPaint.strokeWidth = strokeWidthPx
        outerBgPaint.strokeWidth = strokeWidthPx
        innerPaint.strokeWidth = strokeWidthPx
        innerBgPaint.strokeWidth = strokeWidthPx

        outerPaint.color = ContextCompat.getColor(context, R.color.login_blue)
        innerPaint.color = Color.parseColor("#9CA3AF") // Gray 400
    }

    fun setProgress(outer: Float, inner: Float) {
        this.outerProgress = outer.coerceIn(0f, 100f)
        this.innerProgress = inner.coerceIn(0f, 100f)
        invalidate()
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)

        val padding = strokeWidthPx / 2f + 8f
        val w = width.toFloat()
        val h = height.toFloat()
        val size = Math.min(w, h)

        val cx = w / 2f
        val cy = h / 2f
        val radiusOuter = (size / 2f) - padding

        // Outer ring bounds
        outerRect.set(cx - radiusOuter, cy - radiusOuter, cx + radiusOuter, cy + radiusOuter)

        // Inner ring bounds
        val radiusInner = radiusOuter - strokeWidthPx - spacingPx
        innerRect.set(cx - radiusInner, cy - radiusInner, cx + radiusInner, cy + radiusInner)

        // Draw Outer Bg
        canvas.drawArc(outerRect, 0f, 360f, false, outerBgPaint)

        // Draw Outer Progress (start from -90 degrees, clockwise)
        val outerAngle = (outerProgress / 100f) * 360f
        canvas.drawArc(outerRect, -90f, outerAngle, false, outerPaint)

        // Draw Inner Bg
        canvas.drawArc(innerRect, 0f, 360f, false, innerBgPaint)

        // Draw Inner Progress
        val innerAngle = (innerProgress / 100f) * 360f
        canvas.drawArc(innerRect, -90f, innerAngle, false, innerPaint)
    }
}
