package com.example.flowdesk_android.core.widget

import android.animation.ArgbEvaluator
import android.animation.ValueAnimator
import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.RectF
import android.util.AttributeSet
import android.view.View
import android.view.animation.DecelerateInterpolator

class IosSwitch @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : View(context, attrs, defStyleAttr) {

    private var _isChecked = false
    var isChecked: Boolean
        get() = _isChecked
        set(value) {
            setChecked(value, animate = false)
        }

    private var onCheckedChangeListener: ((IosSwitch, Boolean) -> Unit)? = null

    // Colors
    private val colorOn = Color.parseColor("#34C759") // iOS Green
    private val colorOff = Color.parseColor("#E9E9EB") // iOS Light Gray
    private val colorThumb = Color.WHITE
    private val colorThumbStroke = Color.parseColor("#1A000000") // Very light transparent black for stroke/shadow

    // Paints
    private val paintTrack = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        style = Paint.Style.FILL
    }
    private val paintThumb = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        style = Paint.Style.FILL
    }
    private val paintThumbStroke = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        style = Paint.Style.STROKE
        strokeWidth = dpToPx(0.5f)
        color = colorThumbStroke
    }

    private val trackRect = RectF()

    // Animation variables
    private var thumbProgress = 0f // 0f (OFF) to 1f (ON)
    private var animator: ValueAnimator? = null
    private val argbEvaluator = ArgbEvaluator()

    init {
        // Handle custom attributes if any, e.g., android:checked
        attrs?.let {
            val typedArray = context.obtainStyledAttributes(it, intArrayOf(android.R.attr.checked))
            _isChecked = typedArray.getBoolean(0, false)
            thumbProgress = if (_isChecked) 1f else 0f
            typedArray.recycle()
        }

        setOnClickListener {
            setChecked(!_isChecked, animate = true)
        }
    }

    fun setOnCheckedChangeListener(listener: (IosSwitch, Boolean) -> Unit) {
        this.onCheckedChangeListener = listener
    }

    fun setChecked(checked: Boolean, animate: Boolean) {
        if (_isChecked == checked) return
        _isChecked = checked
        
        onCheckedChangeListener?.invoke(this, checked)

        animator?.cancel()

        val targetProgress = if (checked) 1f else 0f
        if (animate) {
            animator = ValueAnimator.ofFloat(thumbProgress, targetProgress).apply {
                duration = 200
                interpolator = DecelerateInterpolator()
                addUpdateListener {
                    thumbProgress = it.animatedValue as Float
                    postInvalidate()
                }
                start()
            }
        } else {
            thumbProgress = targetProgress
            postInvalidate()
        }
    }

    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        // iOS switch is typically 48dp x 28dp.
        val defaultWidth = dpToPx(48f).toInt()
        val defaultHeight = dpToPx(28f).toInt()

        val measureWidth = measureDimension(defaultWidth, widthMeasureSpec)
        val measureHeight = measureDimension(defaultHeight, heightMeasureSpec)

        setMeasuredDimension(measureWidth, measureHeight)
    }

    private fun measureDimension(defaultSize: Int, measureSpec: Int): Int {
        val specMode = MeasureSpec.getMode(measureSpec)
        val specSize = MeasureSpec.getSize(measureSpec)

        return when (specMode) {
            MeasureSpec.EXACTLY -> specSize
            MeasureSpec.AT_MOST -> Math.min(defaultSize, specSize)
            else -> defaultSize
        }
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)

        val w = width.toFloat()
        val h = height.toFloat()

        // 1. Draw track background
        trackRect.set(0f, 0f, w, h)
        val radius = h / 2f

        val currentTrackColor = argbEvaluator.evaluate(thumbProgress, colorOff, colorOn) as Int
        paintTrack.color = currentTrackColor
        canvas.drawRoundRect(trackRect, radius, radius, paintTrack)

        // 2. Draw thumb
        // The thumb matches the height of the track exactly.
        val thumbRadius = h / 2f
        
        val minStartX = thumbRadius
        val maxEndX = w - thumbRadius
        val thumbCenterX = minStartX + (maxEndX - minStartX) * thumbProgress

        paintThumb.color = colorThumb
        canvas.drawCircle(thumbCenterX, h / 2f, thumbRadius, paintThumb)
        
        // Draw thin stroke/shadow around thumb for depth (iOS style)
        canvas.drawCircle(thumbCenterX, h / 2f, thumbRadius - paintThumbStroke.strokeWidth / 2f, paintThumbStroke)
    }

    private fun dpToPx(dp: Float): Float {
        return dp * context.resources.displayMetrics.density
    }
}
