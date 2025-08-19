package com.basit.library.stickerview

import android.content.Context
import android.graphics.*
import android.view.MotionEvent
import kotlin.math.atan2
import kotlin.math.sqrt

class Sticker(context: Context, bitmap: Bitmap) : BaseSticker(context, bitmap) {
    private val mLastSinglePoint = PointF()
    private val mLastDistanceVector = PointF()
    private val mDistanceVector = PointF()
    private var mLastDistance = 0f

    // Initial Points
    private val mFirstPoint = PointF()
    private val mSecondPoint = PointF()
    private var filterColor: Int? = null
    private var filterMode: PorterDuff.Mode? = null
    // Color and alpha properties
    private var mColorFilter: PorterDuffColorFilter? = null
    var alpha: Int = 255
        set(value) {
            field = value.coerceIn(0, 255)
        }

    /**
     * Set color filter for the sticker
     */
//    fun setColorFilter(color: Int, mode: PorterDuff.Mode = PorterDuff.Mode.SRC_ATOP) {
//        mColorFilter = PorterDuffColorFilter(color, mode)
//    }

    /**
     * Set color filter for the sticker
     */
    fun setColorFilter(color: Int, mode: PorterDuff.Mode = PorterDuff.Mode.SRC_ATOP) {
        filterColor = color
        filterMode = mode
        mColorFilter = PorterDuffColorFilter(color, mode)
    }

    /**
     * Clear color filter
     */
    fun clearColorFilter() {
        filterColor = null
        filterMode = null
        mColorFilter = null
    }
    /**
     * Clear color filter
     */

    fun getColorFilterData(): Pair<Int, PorterDuff.Mode>? {
        return if (filterColor != null && filterMode != null) {
            filterColor!! to filterMode!!
        } else null
    }
    /**
     * Reset touch state
     */
    fun reset() {
        mLastSinglePoint.set(0f, 0f)
        mLastDistanceVector.set(0f, 0f)
        mDistanceVector.set(0f, 0f)
        mLastDistance = 0f
        mMode = MODE_NONE
    }

    /**
     * Calculate distance between two points
     */
    private fun calculateDistance(first: PointF, second: PointF): Float {
        val dx = first.x - second.x
        val dy = first.y - second.y
        return sqrt(dx * dx + dy * dy)
    }

    /**
     * Calculate rotation angle between two vectors
     */
    private fun calculateDegrees(last: PointF, current: PointF): Float {
        val lastAngle = atan2(last.y, last.x)
        val currentAngle = atan2(current.y, current.x)
        return Math.toDegrees((currentAngle - lastAngle).toDouble()).toFloat()
    }

    /**
     * Handle touch events
     */
    override fun onTouch(event: MotionEvent?) {
        event?.let {
            when (it.action and MotionEvent.ACTION_MASK) {
                MotionEvent.ACTION_DOWN -> {
                    mMode = MODE_SINGLE
                    mLastSinglePoint.set(it.x, it.y)
                }

                MotionEvent.ACTION_POINTER_DOWN -> if (it.pointerCount == 2) {
                    mMode = MODE_MULTIPLE
                    mFirstPoint.set(it.getX(0), it.getY(0))
                    mSecondPoint.set(it.getX(1), it.getY(1))
                    mLastDistanceVector.set(
                        mFirstPoint.x - mSecondPoint.x,
                        mFirstPoint.y - mSecondPoint.y
                    )
                    mLastDistance = calculateDistance(mFirstPoint, mSecondPoint)
                }

                MotionEvent.ACTION_MOVE -> {
                    if (mMode == MODE_SINGLE) {
                        translate(it.x - mLastSinglePoint.x, it.y - mLastSinglePoint.y)
                        mLastSinglePoint.set(it.x, it.y)
                    }
                    if (mMode == MODE_MULTIPLE && it.pointerCount == 2) {
                        mFirstPoint.set(it.getX(0), it.getY(0))
                        mSecondPoint.set(it.getX(1), it.getY(1))

                        val distance = calculateDistance(mFirstPoint, mSecondPoint)
                        val scale = distance / mLastDistance
                        this.scale(scale, scale)
                        mLastDistance = distance

                        mDistanceVector.set(
                            mFirstPoint.x - mSecondPoint.x,
                            mFirstPoint.y - mSecondPoint.y
                        )
                        rotate(calculateDegrees(mLastDistanceVector, mDistanceVector))
                        mLastDistanceVector.set(mDistanceVector.x, mDistanceVector.y)
                    }
                }

                MotionEvent.ACTION_UP -> reset()
            }
        }
    }

    /**
     * Override draw to apply color filter and alpha
     */
    override fun onDraw(canvas: Canvas?, paint: Paint?) {
        // Create a paint just for the tattoo
        val tattooPaint = Paint().apply {
            alpha = this@Sticker.alpha
            mColorFilter?.let { colorFilter = it }
        }

        // Draw tattoo bitmap with custom paint
        drawStickerBitmap(canvas, tattooPaint)

        // Draw border + delete icon without affecting alpha/color
        drawControls(canvas, paint)
    }
    companion object {
        // Touch modes
        const val MODE_NONE = 0
        const val MODE_SINGLE = 1
        const val MODE_MULTIPLE = 2
    }
}