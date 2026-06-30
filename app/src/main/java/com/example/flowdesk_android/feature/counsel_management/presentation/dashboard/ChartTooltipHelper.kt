package com.example.flowdesk_android.feature.counsel_management.presentation.dashboard

import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.Path
import android.graphics.RectF
import android.graphics.Typeface

class ChartTooltipHelper(context: Context) {
    val density = context.resources.displayMetrics.density

    private val bgPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        style = Paint.Style.FILL
    }

    private val strokePaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        style = Paint.Style.STROKE
    }

    private val textPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
        textAlign = Paint.Align.CENTER
    }

    private val rectF = RectF()

    fun getTooltipWidth(text: String, textSizeSp: Float = 11f): Float {
        textPaint.textSize = textSizeSp * density
        return textPaint.measureText(text) + 18f * density
    }

    fun getTooltipHeight(): Float {
        return 24f * density
    }

    /**
     * Draws a beautiful white-themed tooltip with orange outline and text.
     */
    fun drawTooltip(
        canvas: Canvas,
        text: String,
        posX: Float,
        posY: Float,
        bgColor: Int = Color.WHITE,
        textColor: Int = Color.parseColor("#3B82F6"), // Premium Blue 500
        strokeColor: Int = Color.parseColor("#93C5FD"), // Light Blue outline 300
        textSizeSp: Float = 11f
    ) {
        if (text.isEmpty()) return

        // 1. Prepare paint configurations
        bgPaint.color = bgColor
        textPaint.color = textColor
        textPaint.textSize = textSizeSp * density
        strokePaint.color = strokeColor
        strokePaint.strokeWidth = 1f * density

        val textWidth = textPaint.measureText(text)
        val w = textWidth + 18f * density
        val h = 24f * density

        // Increased margin: 16dp spacing from data point to bubble bottom
        // Placing the triangle pointer at posX, and shifting the bubble to the right
        // Left boundary is posX - 12dp, making the pointer stand 12dp from the left edge of the bubble
        val left = posX - 12f * density
        val right = left + w
        val top = posY - h - 16f * density
        val bottom = posY - 16f * density

        // 2. Generate a clean unified path to draw the tooltip bubble with stroke
        val tooltipPath = Path()
        rectF.set(left, top, right, bottom)
        tooltipPath.addRoundRect(rectF, 4f * density, 4f * density, Path.Direction.CW)

        val trianglePath = Path().apply {
            moveTo(posX - 4f * density, bottom)
            lineTo(posX + 4f * density, bottom)
            lineTo(posX, bottom + 4f * density)
            close()
        }
        tooltipPath.op(trianglePath, Path.Op.UNION)

        // 3. Draw fill and then stroke to cleanly overlay the outline
        canvas.drawPath(tooltipPath, bgPaint)
        canvas.drawPath(tooltipPath, strokePaint)

        // 4. Draw text inside the bubble (aligned to the center of the bubble, not posX)
        val fontMetrics = textPaint.fontMetrics
        val textOffset = -(fontMetrics.ascent + fontMetrics.descent) / 2f
        canvas.drawText(text, left + w / 2f, top + h / 2f + textOffset, textPaint)
    }
}
