package com.example.flowdesk_android.feature.counsel_management.presentation.dashboard

import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.view.View
import com.github.mikephil.charting.animation.ChartAnimator
import com.github.mikephil.charting.highlight.Highlight
import com.github.mikephil.charting.interfaces.dataprovider.LineDataProvider
import com.github.mikephil.charting.renderer.LineChartRenderer
import com.github.mikephil.charting.utils.ViewPortHandler

class CustomLineChartRenderer(
    chart: LineDataProvider,
    animator: ChartAnimator,
    viewPortHandler: ViewPortHandler
) : LineChartRenderer(chart, animator, viewPortHandler) {

    private val highlightPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        style = Paint.Style.STROKE
        color = Color.parseColor("#CBD5E1") // Slate 300 (Light grey highlight line)
        strokeWidth = 1f
    }

    private val activeDotInnerPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        style = Paint.Style.FILL
        color = Color.WHITE // Fill inside with white
    }

    private val activeDotBorderPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        style = Paint.Style.STROKE
        color = Color.parseColor("#3B82F6") // Border in chart blue
    }

    override fun drawHighlighted(c: Canvas, indices: Array<out Highlight>) {
        val lineData = mChart.lineData

        val view = mChart as? View
        val density = view?.resources?.displayMetrics?.density ?: 2f
        
        highlightPaint.strokeWidth = 1f * density
        activeDotBorderPaint.strokeWidth = 1f * density

        for (high in indices) {
            val set = lineData.getDataSetByIndex(high.dataSetIndex)

            if (set == null || !set.isHighlightEnabled) continue

            val e = set.getEntryForXValue(high.x, high.y) ?: continue
            if (!isInBoundsX(e, set)) continue

            // Fetch pixel coordinates for the entry
            val pix = mChart.getTransformer(set.axisDependency).getPixelForValues(e.x, e.y * mAnimator.phaseY)
            val px = pix.x.toFloat()
            val py = pix.y.toFloat()

            // 1. Draw dropped vertical line: from chart bottom up to the data point
            val bottomY = mViewPortHandler.contentBottom()
            c.drawLine(px, bottomY, px, py, highlightPaint)

            // 2. Draw active dot only at the selected point (크기 축소)
            val outerRadius = 3f * density
            
            c.drawCircle(px, py, outerRadius, activeDotInnerPaint)
            c.drawCircle(px, py, outerRadius, activeDotBorderPaint)
        }
    }
}
