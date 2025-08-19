package com.basit.library.stickerview

import android.graphics.Canvas
import android.graphics.Paint
import android.view.MotionEvent

/**
 * M Abdul Basit
 * Create by: Abdul Basit
 * Date: 2025/8/19
 * Time: 9:44 AM
 */
interface ISupportOperation {
    /**
     *
     *
     * @param dx
     * @param dy
     */
    fun translate(dx: Float, dy: Float)

    /**
     *
     *
     * @param sx
     * @param sy
     */
    fun scale(sx: Float, sy: Float)

    /**
     *
     *
     * @param degrees
     */
    fun rotate(degrees: Float)


    /**
     *
     *
     * @param canvas
     * @param paint
     */
    fun onDraw(canvas: Canvas?, paint: Paint?)

    /**
     *
     *
     * @param event
     */
    fun onTouch(event: MotionEvent?)
}
