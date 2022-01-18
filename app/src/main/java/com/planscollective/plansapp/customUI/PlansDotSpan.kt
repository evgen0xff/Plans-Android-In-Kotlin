package com.planscollective.plansapp.customUI

import android.graphics.Canvas
import android.graphics.Paint
import android.text.style.LineBackgroundSpan

class PlansDotSpan (
    private val radius: Float = DEFAULT_RADIUS,
    private val color: Int = 0,
    private val position: PositionType = PositionType.TOP
): LineBackgroundSpan {

    companion object {
        /**
         * Default radius used
         */
        const val DEFAULT_RADIUS = 3f
    }

    enum class PositionType {
        TOP,
        BOTTOM
    }

    override fun drawBackground(
        canvas: Canvas, paint: Paint,
        left: Int, right: Int, top: Int, baseline: Int, bottom: Int,
        charSequence: CharSequence,
        start: Int, end: Int, lineNum: Int
    ) {
        val oldColor = paint.color
        if (color != 0) {
            paint.color = color
        }
        val cy = when (position) {
            PositionType.TOP -> {
                top - radius
            }
            PositionType.BOTTOM -> {
                bottom + radius
            }
        }
        canvas.drawCircle(((left + right) / 2).toFloat(), cy, radius, paint)
        paint.color = oldColor
    }

}
