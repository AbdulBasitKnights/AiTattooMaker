package com.basit.aitattoomaker.presentation.utils

import android.graphics.*
import android.graphics.drawable.Drawable
import androidx.annotation.ColorInt
import kotlin.math.cos
import kotlin.math.sin

class GradientStrokeDrawable(
    private val radiusPx: Float,
    private val strokePx: Float,
    @ColorInt private val startColor: Int,
    @ColorInt private val endColor: Int,
    private val angleDeg: Float = 0f,            // 0/360 = left→right
    @ColorInt private val fillColor: Int = Color.WHITE // 🔥 solid fill inside
) : Drawable() {

    private val strokePaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        style = Paint.Style.STROKE
        strokeWidth = strokePx
    }
    private val fillPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        style = Paint.Style.FILL
        color = fillColor
    }

    private val rect = RectF()

    override fun onBoundsChange(bounds: Rect) {
        super.onBoundsChange(bounds)
        rect.set(bounds)

        // Keep stroke fully inside bounds
        val half = strokePx / 2f
        rect.inset(half, half)

        // Linear gradient along angle
        val rad = Math.toRadians(angleDeg.toDouble())
        val dx = (cos(rad) * rect.width() / 2f).toFloat()
        val dy = (sin(rad) * rect.height() / 2f).toFloat()
        val cx = rect.centerX()
        val cy = rect.centerY()

        strokePaint.shader = LinearGradient(
            cx - dx, cy - dy,
            cx + dx, cy + dy,
            startColor, endColor,
            Shader.TileMode.CLAMP
        )
    }

    override fun draw(canvas: Canvas) {
        // 1) Fill white (full rect, accounting for the stroke inset done above)
        canvas.drawRoundRect(rect, radiusPx, radiusPx, fillPaint)
        // 2) Stroke on top with gradient
        canvas.drawRoundRect(rect, radiusPx, radiusPx, strokePaint)
    }

    override fun setAlpha(alpha: Int) {
        strokePaint.alpha = alpha
        fillPaint.alpha = alpha
        invalidateSelf()
    }

    override fun setColorFilter(colorFilter: ColorFilter?) {
        strokePaint.colorFilter = colorFilter
        fillPaint.colorFilter = colorFilter
        invalidateSelf()
    }

    override fun getOpacity(): Int = PixelFormat.TRANSLUCENT
}
