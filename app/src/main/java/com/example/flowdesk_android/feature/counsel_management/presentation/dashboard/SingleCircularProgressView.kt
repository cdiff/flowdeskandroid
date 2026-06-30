package com.example.flowdesk_android.feature.counsel_management.presentation.dashboard

import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.RectF
import android.util.AttributeSet
import android.view.View
import com.example.flowdesk_android.R

class SingleCircularProgressView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : View(context, attrs, defStyleAttr) {

    private var progress = 0f // 0..100
    private var activeColor = Color.parseColor("#3B82F6") // Blue default
    private var useGapArc = false

    private val activePaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        style = Paint.Style.STROKE
        strokeCap = Paint.Cap.ROUND
    }

    private val bgPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        style = Paint.Style.STROKE
        strokeCap = Paint.Cap.ROUND
        color = Color.parseColor("#E5E7EB") // Light Gray (Gray 200)
    }

    private val rect = RectF()
    private var strokeWidthPx = 16f

    init {
        val density = resources.displayMetrics.density
        strokeWidthPx = 7f * density // 7dp
        activePaint.strokeWidth = strokeWidthPx
        bgPaint.strokeWidth = strokeWidthPx
        activePaint.color = activeColor

        if (attrs != null) {
            val typedArray = context.obtainStyledAttributes(attrs, R.styleable.SingleCircularProgressView)
            useGapArc = typedArray.getBoolean(R.styleable.SingleCircularProgressView_useGapArc, false)
            typedArray.recycle()
        }
    }

    fun setProgress(progress: Float) {
        this.progress = progress.coerceIn(0f, 100f)
        invalidate()
    }

    fun setActiveColor(color: Int) {
        this.activeColor = color
        activePaint.color = color
        invalidate()
    }

    fun setUseGapArc(useGapArc: Boolean) {
        this.useGapArc = useGapArc
        invalidate()
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)

        val padding = strokeWidthPx / 2f + 4f
        val w = width.toFloat()
        val h = height.toFloat()
        val size = Math.min(w, h)

        val cx = w / 2f
        val cy = h / 2f
        val radius = (size / 2f) - padding

        rect.set(cx - radius, cy - radius, cx + radius, cy + radius)

        if (useGapArc) {
            // Draw background track with a gap at the bottom (starts at 135 degrees, sweeps 270 degrees)
            canvas.drawArc(rect, 135f, 270f, false, bgPaint)

            // Draw active progress arc (starts at 135 degrees, sweeps up to 270 degrees)
            val angle = (progress / 100f) * 270f
            canvas.drawArc(rect, 135f, angle, false, activePaint)
        } else {
            // Draw standard 360-degree background track
            canvas.drawArc(rect, 0f, 360f, false, bgPaint)

            // Draw active progress arc (starts from -90 degrees, clockwise)
            val angle = (progress / 100f) * 360f
            canvas.drawArc(rect, -90f, angle, false, activePaint)
        }
    }
}
