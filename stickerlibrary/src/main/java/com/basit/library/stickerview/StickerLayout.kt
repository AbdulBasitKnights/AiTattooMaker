package com.basit.library.stickerview

import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.DashPathEffect
import android.graphics.Paint
import android.util.AttributeSet
import android.view.MotionEvent
import android.view.View
import android.widget.Toast
import com.basit.library.stickerview.StickerFactory.currentSticker
import com.basit.library.stickerview.StickerFactory.isStickerFocused
import com.basit.library.stickerview.StickerManager.Companion.getInstance

/**
 * M Abdul Basit
 * Create by: Abdul Basit
 * Date: 2025/8/19
 * Time: 9:44 AM
 */
class StickerLayout : View, View.OnTouchListener {
    private var mContext: Context? = null
    private var mPaint: Paint? = null

    // Records the currently operated sticker object
    private var mStick: Sticker? = null

    constructor(context: Context?) : super(context) {
        init(context)
    }

    constructor(context: Context?, attrs: AttributeSet?) : super(context, attrs) {
        init(context)
    }

    constructor(context: Context?, attrs: AttributeSet?, defStyleAttr: Int) : super(
        context,
        attrs,
        defStyleAttr
    ) {
        init(context)
    }

    /**
     * Initial operation / initialization operation
     */
    private fun init(context: Context?) {
        this.mContext = context
        // Set touch listener
        setOnTouchListener(this)
    }

    var paint: Paint?
        get() {
            if (mPaint == null) {
                mPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
                    color = Color.WHITE
                    strokeWidth = 2f
                    style = Paint.Style.STROKE
                    strokeCap = Paint.Cap.ROUND
                    strokeJoin = Paint.Join.ROUND
                    // Dash pattern: [dashLength, spaceLength]
                    pathEffect = DashPathEffect(floatArrayOf(20f, 10f), 0f)
                }
            }
            return mPaint
        }
        set(value) {
            mPaint = value
        }



    /**
     * Add New Sticker
     *
     * @param sticker
     */
    fun addSticker(sticker: Sticker?) {
        val size = getInstance().stickerList.size
        if (size < 9) {
            getInstance().addSticker(sticker)
            getInstance().setFocusSticker(sticker)
            sticker?.reset()
            sticker?.scale(2.5f,2.5f)
            invalidate()
        } else {
            Toast.makeText(
                mContext,
                "// The maximum number of stickers cannot exceed 9",
                Toast.LENGTH_SHORT
            ).show()
        }
    }
    fun updateStickerZoom(alpha:Int=128) {
        currentSticker?.let {sticker->
            val manager = getInstance()
            val focused = manager.getFocusSticker()
            if (focused != null && sticker != null) {
                focused.bitmap = sticker.bitmap
                focused.alpha = alpha
                sticker.reset()
                sticker?.scale(1.5f,1.5f)
                focused.clearColorFilter()
                sticker?.getColorFilterData()?.let { (color, mode) ->
                    focused.setColorFilter(color, mode)
                }
                invalidate()

            }
        }
    }
    fun addOrUpdateSticker(sticker: Sticker?,alpha:Int=128) {
        val manager = getInstance()
        val focused = manager.getFocusSticker()
        if (focused != null && sticker != null) {
            focused.bitmap = sticker.bitmap
            focused.alpha = alpha

            focused.clearColorFilter()
            sticker.getColorFilterData()?.let { (color, mode) ->
                focused.setColorFilter(color, mode)
            }
            invalidate()

        } else {
            // No focused sticker → add new one
            val size = manager.stickerList.size
            if (size < 9) {
                manager.addSticker(sticker)
                manager.setFocusSticker(sticker)
                invalidate()
            } else {
                Toast.makeText(
                    mContext,
                    "// The maximum number of stickers cannot exceed 9",
                    Toast.LENGTH_SHORT
                ).show()
            }
        }
    }
    fun updateSticker(alpha:Int=128) {
        currentSticker?.let {sticker->
            val manager = getInstance()
            val focused = manager.getFocusSticker()
            if (focused != null && sticker != null) {
                focused.bitmap = sticker.bitmap
                focused.alpha = alpha

                focused.clearColorFilter()
                sticker?.getColorFilterData()?.let { (color, mode) ->
                    focused.setColorFilter(color, mode)
                }
                invalidate()

            }
        }

    }
    fun clearFocusAll() {
        getInstance().clearAllFocus()
        isStickerFocused.postValue(false)
        invalidate()
    }

    /**
     * Remove Selected Sticker
     *
     * @param sticker
     */
    fun removeSticker(sticker: Sticker) {
        if (sticker.isFocus) {
            getInstance().removeSticker(sticker)
            currentSticker=null
            invalidate()
        }
    }

    /**
     * Remove All Stickers
     */
    fun removeAllSticker() {
        getInstance().removeAllSticker()
        currentSticker=null
        invalidate()
    }


    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)
        val stickerList = getInstance().stickerList
        var focused: Sticker? = null

        for (sticker in stickerList) {
            if (sticker.isFocus) {
                focused = sticker
            } else {
                sticker.onDraw(canvas, paint) // normal paint
            }
        }

        // For focused sticker → dashed + rounded paint
        focused?.let {
            currentSticker?.onDraw(canvas, paint)
        }
    }


    override fun onTouch(v: View?, event: MotionEvent): Boolean {
        when (event.action and MotionEvent.ACTION_MASK) {
            MotionEvent.ACTION_DOWN, MotionEvent.ACTION_POINTER_DOWN -> {
                // Determines whether the delete button is pressed
                isStickerFocused.postValue(true)
                currentSticker = getInstance().getDelButton(event.x, event.y)
//                if (currentSticker != null) {
//                    removeSticker(currentSticker!!)
//                    currentSticker = null
//                }
                // Indicates whether a single finger is touching the sticker
                currentSticker = getInstance().getSticker(event.x, event.y)
                if (currentSticker == null) {
                    if (event.pointerCount == 2) {
                        // Handles two-finger touch: the first finger does not touch the sticker, but the second finger does touch the sticker
                        currentSticker =
                            getInstance().getSticker(event.getX(1), event.getY(1))
                    }
                }

                if (currentSticker != null) {
                    getInstance().setFocusSticker(currentSticker)
                }
                else{
                    currentSticker?.let {
                        getInstance().setFocusSticker(it)
                    }
                }
            }

            else -> {
            }
        }
        if (currentSticker != null) {
            currentSticker?.onTouch(event)
        } else {
            getInstance().clearAllFocus()
        }
        invalidate()
        return true
    }
}
