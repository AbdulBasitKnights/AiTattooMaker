package com.basit.aitattoomaker.presentation.camera.overlay

import android.content.Context
import android.graphics.*
import android.util.AttributeSet
import android.view.MotionEvent
import android.view.ScaleGestureDetector
import android.view.View
import kotlin.math.atan2

class TattooOverlayView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : View(context, attrs, defStyleAttr) {

    private var tattooBitmap: Bitmap? = null
    private var tattooMatrix = Matrix()
    private var currentScale = 1f
    private var currentRotation = 0f

    // For dragging
    private var lastTouchX = 0f
    private var lastTouchY = 0f

    // For gestures
    private val scaleDetector = ScaleGestureDetector(context, ScaleListener())
    private var isTwoFinger = false
    private var initialRotation = 0f

    private var segmentationMask: Bitmap? = null
    private val maskPaint = Paint().apply {
        colorFilter = PorterDuffColorFilter(Color.WHITE, PorterDuff.Mode.SRC_IN)
    }

    fun updateSegmentation(mask: Bitmap) {
        segmentationMask = mask
        invalidate()
    }

    fun setTattoo(bitmap: Bitmap) {
        tattooBitmap = bitmap
        tattooMatrix.reset()

        // Center it
        val cx = width / 2f
        val cy = height / 2f
        tattooMatrix.postTranslate(cx - bitmap.width / 2f, cy - bitmap.height / 2f)

        invalidate()
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)

        // Draw segmentation mask
        segmentationMask?.let { mask ->
            canvas.drawBitmap(mask, null, Rect(0, 0, width, height), maskPaint)
        }

        // Draw tattoo
        tattooBitmap?.let { tattoo ->
            canvas.drawBitmap(tattoo, tattooMatrix, null)
        }
    }

    override fun onTouchEvent(event: MotionEvent): Boolean {
        scaleDetector.onTouchEvent(event)

        when (event.actionMasked) {
            MotionEvent.ACTION_DOWN -> {
                lastTouchX = event.x
                lastTouchY = event.y
                isTwoFinger = false
            }

            MotionEvent.ACTION_POINTER_DOWN -> {
                if (event.pointerCount == 2) {
                    isTwoFinger = true
                    initialRotation = getRotation(event)
                }
            }

            MotionEvent.ACTION_MOVE -> {
                if (!isTwoFinger) {
                    val dx = event.x - lastTouchX
                    val dy = event.y - lastTouchY
                    tattooMatrix.postTranslate(dx, dy)
                    invalidate()

                    lastTouchX = event.x
                    lastTouchY = event.y
                } else if (event.pointerCount == 2) {
                    val newRotation = getRotation(event)
                    val deltaRotation = newRotation - initialRotation
                    val centerX = (event.getX(0) + event.getX(1)) / 2
                    val centerY = (event.getY(0) + event.getY(1)) / 2

                    tattooMatrix.postRotate(deltaRotation, centerX, centerY)
                    invalidate()

                    initialRotation = newRotation
                }
            }
        }
        return true
    }

    private fun getRotation(event: MotionEvent): Float {
        return if (event.pointerCount >= 2) {
            val dx = event.getX(1) - event.getX(0)
            val dy = event.getY(1) - event.getY(0)
            Math.toDegrees(atan2(dy.toDouble(), dx.toDouble())).toFloat()
        } else 0f
    }

    private inner class ScaleListener : ScaleGestureDetector.SimpleOnScaleGestureListener() {
        override fun onScale(detector: ScaleGestureDetector): Boolean {
            tattooBitmap?.let {
                val scaleFactor = detector.scaleFactor
                currentScale *= scaleFactor
                tattooMatrix.postScale(scaleFactor, scaleFactor, detector.focusX, detector.focusY)
                invalidate()
            }
            return true
        }
    }

    /** Merge tattoo overlay with a base image (camera frame) */
    fun getFinalBitmap(base: Bitmap): Bitmap {
        val result = Bitmap.createBitmap(base.width, base.height, base.config?: Bitmap.Config.ARGB_8888)
        val canvas = Canvas(result)
        canvas.drawBitmap(base, 0f, 0f, null)
        tattooBitmap?.let {
            canvas.drawBitmap(it, tattooMatrix, null)
        }
        return result
    }
}