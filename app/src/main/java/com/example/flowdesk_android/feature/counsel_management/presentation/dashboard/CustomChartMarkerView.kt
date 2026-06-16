package com.example.flowdesk_android.feature.counsel_management.presentation.dashboard

import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.Path
import android.graphics.RectF
import android.graphics.Typeface
import com.github.mikephil.charting.components.IMarker
import com.github.mikephil.charting.data.Entry
import com.github.mikephil.charting.highlight.Highlight
import com.github.mikephil.charting.utils.MPPointF

class CustomChartMarkerView(context: Context) : IMarker {

    private var text = ""
    private val tooltipHelper = ChartTooltipHelper(context)
    private val offset = MPPointF()

    override fun getOffset(): MPPointF {
        return offset
    }

    override fun getOffsetForDrawingAtPoint(posX: Float, posY: Float): MPPointF {
        val h = tooltipHelper.getTooltipHeight()
        // Shifting X offset by -12dp matching left alignment, and Y offset by -16dp spacing
        offset.x = -12f * tooltipHelper.density
        offset.y = -h - 16f * tooltipHelper.density
        return offset
    }

    override fun refreshContent(e: Entry, highlight: Highlight) {
        text = "${e.x.toInt()}시: ${e.y.toInt()}건"
    }

    override fun draw(canvas: Canvas, posX: Float, posY: Float) {
        tooltipHelper.drawTooltip(canvas, text, posX, posY)
    }
}
