package com.basit.library.stickerview

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Canvas
import android.graphics.Matrix
import android.graphics.Paint
import android.graphics.PointF
import android.graphics.RectF
import android.util.DisplayMetrics
import android.view.WindowManager

/**
 * M Abdul Basit
 * Create by: Abdul Basit
 * Date: 2025/8/19
 * Time: 9:44 AM
 */
abstract class BaseSticker(
    context: Context, // Sticker image
    val bitmap: Bitmap
) : ISupportOperation {
    private val mDelBitmap: Bitmap =
        BitmapFactory.decodeResource(context.resources, R.mipmap.cross) //Delete Sticker
    val matrix: Matrix = Matrix() // Matrix that manages image transformations
    @JvmField
    var isFocus: Boolean = false // Indicates whether it is currently focused
    protected var mMode: Int = 0 // Current mode

    private val mSrcPoints = floatArrayOf(
        0f, 0f,
        bitmap.width.toFloat(), 0f,
        bitmap.width.toFloat(), bitmap.height.toFloat(),
        0f, bitmap.height.toFloat(),
        (bitmap.width / 2).toFloat(), (bitmap.height / 2).toFloat()
    ) // Coordinates of the points before matrix transformation
    private val mDstPoints: FloatArray =
        mSrcPoints.clone() // Coordinates of points after matrix transformation
    val stickerBitmapBound: RectF? =
        RectF(0f, 0f, bitmap.width.toFloat(), bitmap.height.toFloat()) // Sticker bounds
    val delBitmapBound: RectF? = RectF(
        (0 - mDelBitmap.width / 2 - PADDING).toFloat(),
        (0 - mDelBitmap.height / 2 - PADDING).toFloat(),
        (mDelBitmap.width / 2 + PADDING).toFloat(),
        (mDelBitmap.height / 2 + PADDING).toFloat()
    ) // Delete button area
    private val mMidPointF: PointF = PointF() // Coordinates of the sticker's center point

    init {

        // Move the sticker to the center of the screen by default
        val windowManager = context.getSystemService(Context.WINDOW_SERVICE) as WindowManager
        val displayMetrics = DisplayMetrics()
        windowManager.getDefaultDisplay().getMetrics(displayMetrics)
        val dx = (displayMetrics.widthPixels / 2 - bitmap.width / 2).toFloat()
        val dy = (displayMetrics.heightPixels / 2 - bitmap.height / 2).toFloat()
        translate(dx, dy)
        // By default, scale the sticker to half its original size
        scale(0.5f, 0.5f)
    }

    /**
     * Translate
     *
     * @param dx
     * @param dy
     */
    override fun translate(dx: Float, dy: Float) {
        matrix.postTranslate(dx, dy)
        updatePoints()
    }

    /**
     * Scaling
     *
     * @param sx
     * @param sy
     */
    override fun scale(sx: Float, sy: Float) {
        matrix.postScale(sx, sy, mMidPointF.x, mMidPointF.y)
        updatePoints()
    }

    /**
     * Rotation
     *
     * @param degrees
     */
    override fun rotate(degrees: Float) {
        matrix.postRotate(degrees, mMidPointF.x, mMidPointF.y)
        updatePoints()
    }

    /**
     * When the matrix changes, update the coordinates (the src points are mapped into dst points by the matrix)
     */
    private fun updatePoints() {
        // Update sticker point coordinates
        matrix.mapPoints(mDstPoints, mSrcPoints)
        // Update the coordinates of the sticker's center point
        mMidPointF.set(mDstPoints[8], mDstPoints[9])
    }

    /**
     * Draw Sticker by Canvas
     *
     * @param canvas
     * @param paint
     */
    override fun onDraw(canvas: Canvas?, paint: Paint?) {
        // Draw the sticker
        canvas?.drawBitmap(this.bitmap, this.matrix, paint)
        if (isFocus) {
            // Draw the sticker border
            paint?.let {
                canvas?.drawLine(
                    mDstPoints[0] - PADDING,
                    mDstPoints[1] - PADDING,
                    mDstPoints[2] + PADDING,
                    mDstPoints[3] - PADDING,
                    paint
                )
                canvas?.drawLine(
                    mDstPoints[2] + PADDING,
                    mDstPoints[3] - PADDING,
                    mDstPoints[4] + PADDING,
                    mDstPoints[5] + PADDING,
                    paint
                )
                canvas?.drawLine(
                    mDstPoints[4] + PADDING,
                    mDstPoints[5] + PADDING,
                    mDstPoints[6] - PADDING,
                    mDstPoints[7] + PADDING,
                    paint
                )
                canvas?.drawLine(
                    mDstPoints[6] - PADDING,
                    mDstPoints[7] + PADDING,
                    mDstPoints[0] - PADDING,
                    mDstPoints[1] - PADDING,
                    paint
                )
            }
            // Draw the delete button
            canvas?.drawBitmap(
                mDelBitmap,
                mDstPoints[0] - mDelBitmap.getWidth() / 2 - PADDING,
                mDstPoints[1] - mDelBitmap.getHeight() / 2 - PADDING,
                paint
            )
        }
    }

    companion object {
        const val MODE_NONE: Int = 0 // Initial state
        const val MODE_SINGLE: Int = 1 // Indicates whether moving is enabled
        const val MODE_MULTIPLE: Int = 2 // Indicates whether scaling and rotation are enabled

        private const val PADDING =
            30 // To prevent the image from being too close to the border, a margin is set here
    }
}
