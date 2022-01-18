package com.planscollective.plansapp.customUI

import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.RectF
import android.os.CountDownTimer
import android.util.AttributeSet
import android.view.View
import com.planscollective.plansapp.extension.toPx

class RecordProgressView(context : Context, attrs: AttributeSet): View(context, attrs) {
    private val interval: Long = 100 // ms
    private var totalDuration: Long = 30 * 1000 // ms
    private var tintColor: Int = Color.parseColor("#38A29A")
    private var progressDuration: Long = 0
    private var isRunning: Boolean = false
    private var timer: CountDownTimer? = null

    fun setTotalDuration(duration: Long) {
        totalDuration = duration
    }

    fun setTintColor(color: Int) {
        tintColor = color
    }

    fun startAnim() {
        startTimer()
    }

    fun stopAnim() {
        stopTimer()
    }

    private fun startTimer() {
        isRunning = true
        progressDuration = 0

        if (timer != null) {
            timer?.cancel()
            timer = null
        }

        timer = object : CountDownTimer(totalDuration, interval) {
            override fun onTick(millisUntilFinished: Long) {
                progressDuration += interval
                invalidate()
            }

            override fun onFinish() {
                stopTimer()
            }
        }
        timer?.start()
    }

    private fun stopTimer() {
        isRunning = false
        timer?.cancel()
        progressDuration = 0
        invalidate()
    }

    override fun dispatchDraw(canvas: Canvas?) {
        // Draw center circle
        val centerX = this.measuredWidth / 2f
        val centerY = this.measuredHeight / 2f
        val radius = Math.min(this.measuredWidth / 2f, this.measuredHeight / 2f)

        val paintCenterCircle = Paint()
        paintCenterCircle.color = Color.WHITE
        paintCenterCircle.style = Paint.Style.FILL
        paintCenterCircle.isAntiAlias = true;
        canvas?.drawCircle(centerX, centerY, radius / 2f, paintCenterCircle)

        if (isRunning && progressDuration > 0) {
            val strokeWidth: Float = 3.toPx().toFloat()
            val ringRadius = radius - strokeWidth / 2f
            val rcDraw = RectF(centerX - ringRadius, centerY - ringRadius, centerX + ringRadius, centerY + ringRadius)
            val startAngle = 0f
            val endAngle = 360 * progressDuration / totalDuration.toFloat()

            // Draw white ring
            val paintProgress = Paint()
            paintProgress.color = Color.WHITE
            paintProgress.strokeWidth = strokeWidth
            paintProgress.style = Paint.Style.STROKE
            paintProgress.isAntiAlias = true;
            canvas?.drawCircle(centerX, centerY, ringRadius, paintProgress)

            // Draw progress bar
            paintProgress.color = tintColor
            canvas?.drawArc(rcDraw, 270 + startAngle, endAngle - startAngle, false, paintProgress)
        } else {
            val strokeWidth: Float = 2.toPx().toFloat()
            val paintCenterRing = Paint()
            paintCenterRing.color = Color.WHITE
            paintCenterRing.strokeWidth = strokeWidth
            paintCenterRing.style = Paint.Style.STROKE
            paintCenterRing.isAntiAlias = true;
            canvas?.drawCircle(centerX, centerY, radius / 2f + strokeWidth * 3 / 2f, paintCenterRing)
        }

        super.dispatchDraw(canvas)
    }

    override fun onSizeChanged(w: Int, h: Int, oldw: Int, oldh: Int) {
        super.onSizeChanged(w, h, oldw, oldh)
        invalidate()
    }
}