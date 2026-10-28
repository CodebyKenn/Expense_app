package com.group.pockit.ui.dashboard


import android.content.Context
import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.RectF
import android.graphics.Typeface
import android.util.AttributeSet
import android.util.TypedValue
import android.view.View
import kotlin.math.max
import kotlin.math.min

object ChartColors {
    private val list = intArrayOf(
        0xFF7B4B94.toInt(), 0xFFF4A340.toInt(), 0xFF2E8B84.toInt(),
        0xFF2A5C84.toInt(), 0xFFC0392B.toInt(), 0xFF8A8397.toInt()
    )
    fun at(i: Int) = list[i % list.size]
}

class DonutChartView @JvmOverloads constructor(
    context: Context, attrs: AttributeSet? = null, defStyleAttr: Int = 0
) : View(context, attrs, defStyleAttr) {

    private var slices: List<Pair<Float, Int>> = emptyList()
    private var topText = ""
    private var bottomText = ""
    private val rect = RectF()

    private val arcPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply { style = Paint.Style.STROKE }
    private val topPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = 0xFF8A8397.toInt(); textAlign = Paint.Align.CENTER; textSize = sp(11f)
    }
    private val bottomPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = 0xFF46044B.toInt(); textAlign = Paint.Align.CENTER; textSize = sp(17f)
        typeface = Typeface.DEFAULT_BOLD
    }

    private fun sp(v: Float) = TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_SP, v, resources.displayMetrics)

    fun setData(data: List<Pair<Float, Int>>, top: String, bottom: String) {
        slices = data; topText = top; bottomText = bottom
        invalidate()
    }

    override fun onDraw(canvas: Canvas) {
        val size = min(width, height).toFloat()
        val stroke = size * 0.2f
        arcPaint.strokeWidth = stroke
        val cx = width / 2f
        val cy = height / 2f
        val r = size / 2f - stroke / 2f
        rect.set(cx - r, cy - r, cx + r, cy + r)

        if (slices.isEmpty()) {
            arcPaint.color = 0xFFE3D3F2.toInt()
            canvas.drawArc(rect, 0f, 360f, false, arcPaint)
        } else {
            var start = -90f
            val gap = if (slices.size > 1) 2f else 0f
            for ((fraction, color) in slices) {
                val sweep = fraction * 360f
                arcPaint.color = color
                canvas.drawArc(rect, start + gap / 2, max(sweep - gap, 0.5f), false, arcPaint)
                start += sweep
            }
        }
        canvas.drawText(topText, cx, cy - topPaint.textSize * 0.2f, topPaint)
        canvas.drawText(bottomText, cx, cy + bottomPaint.textSize * 0.9f, bottomPaint)
    }
}