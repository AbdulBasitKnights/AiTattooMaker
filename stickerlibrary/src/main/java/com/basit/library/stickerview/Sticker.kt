package com.basit.library.stickerview

import android.content.Context
import android.graphics.Bitmap
import android.graphics.PointF
import android.view.MotionEvent
import kotlin.math.atan2
import kotlin.math.sqrt

/**
 * M Abdul Basit
 * Create by: Abdul Basit
 * Date: 2025/8/19
 * Time: 9:44 AM
 */
class Sticker(context: Context, bitmap: Bitmap) : BaseSticker(context, bitmap) {
    private val mLastSinglePoint = PointF()
    private val mLastDistanceVector = PointF()
    private val mDistanceVector = PointF()
    private var mLastDistance = 0f //last distance

    //Initial Point
    private val mFirstPoint = PointF()
    private val mSecondPoint = PointF()

    /**
     * Reset State
     */
    fun reset() {
        mLastSinglePoint.set(0f, 0f)
        mLastDistanceVector.set(0f, 0f)
        mDistanceVector.set(0f, 0f)
        mLastDistance = 0f
        mMode = MODE_NONE
    }

    /**
     * Distance
     */
    fun calculateDistance(firstPointF: PointF, secondPointF: PointF): Float {
        val x = firstPointF.x - secondPointF.x
        val y = firstPointF.y - secondPointF.y
        return sqrt((x * x + y * y).toDouble()).toFloat()
    }


    /**
     * Degree Calculation
     *
     * @param lastVector
     * @param currentVector
     * @return
     */
    fun calculateDegrees(lastVector: PointF, currentVector: PointF): Float {
        val lastDegrees = atan2(lastVector.y.toDouble(), lastVector.x.toDouble()).toFloat()
        val currentDegrees = atan2(currentVector.y.toDouble(), currentVector.x.toDouble()).toFloat()
        return Math.toDegrees((currentDegrees - lastDegrees).toDouble()).toFloat()
    }


    /**
     * Touch Event
     *
     * @param event
     */
    override fun onTouch(event: MotionEvent?) {
        when (event?.action?.and(MotionEvent.ACTION_MASK)) {
            MotionEvent.ACTION_DOWN -> {
                mMode = MODE_SINGLE

                mLastSinglePoint.set(event.x, event.y)
            }

            MotionEvent.ACTION_POINTER_DOWN -> if (event.pointerCount == 2) {
                mMode = MODE_MULTIPLE

                mFirstPoint.set(event.getX(0), event.getY(0))
                mSecondPoint.set(event.getX(1), event.getY(1))

                mLastDistanceVector.set(
                    mFirstPoint.x - mSecondPoint.x,
                    mFirstPoint.y - mSecondPoint.y
                )

                mLastDistance = calculateDistance(mFirstPoint, mSecondPoint)
            }

            MotionEvent.ACTION_MOVE -> {
                if (mMode == MODE_SINGLE) {
                    translate(event.x - mLastSinglePoint.x, event.y - mLastSinglePoint.y)
                    mLastSinglePoint.set(event.x, event.y)
                }
                if (mMode == MODE_MULTIPLE && event.pointerCount == 2) {
                    mFirstPoint.set(event.getX(0), event.getY(0))
                    mSecondPoint.set(event.getX(1), event.getY(1))

                    val distance = calculateDistance(mFirstPoint, mSecondPoint)

                    val scale = distance / mLastDistance
                    scale(scale, scale)
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
