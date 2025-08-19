package com.lcw.library.stickerview

import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.util.AttributeSet
import android.view.MotionEvent
import android.view.View
import android.widget.Toast

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
                mPaint = Paint(Paint.ANTI_ALIAS_FLAG)
                mPaint!!.setColor(Color.BLACK)
                mPaint!!.setStrokeWidth(2f)
            }
            return mPaint
        }
        set(mPaint) {
            this.mPaint = mPaint
        }


    /**
     * Add New Sticker
     *
     * @param sticker
     */
    fun addSticker(sticker: Sticker?) {
        val size = StickerManager.getInstance().stickerList.size
        if (size < 9) {
            StickerManager.getInstance().addSticker(sticker)
            StickerManager.getInstance().setFocusSticker(sticker)
            invalidate()
        } else {
            Toast.makeText(
                mContext,
                "// The maximum number of stickers cannot exceed 9",
                Toast.LENGTH_SHORT
            ).show()
        }
    }

    /**
     * Remove Selected Sticker
     *
     * @param sticker
     */
    fun removeSticker(sticker: Sticker) {
        if (sticker.isFocus) {
            StickerManager.getInstance().removeSticker(sticker)
            invalidate()
        }
    }

    /**
     * Remove All Stickers
     */
    fun removeAllSticker() {
        StickerManager.getInstance().removeAllSticker()
        invalidate()
    }


    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)
        val stickerList = StickerManager.getInstance().stickerList
        var focusSticker: Sticker? = null
        for (i in stickerList.indices) {
            val sticker = stickerList[i]
            if (sticker.isFocus) {
                focusSticker = sticker
            } else {
                sticker.onDraw(canvas, this.paint)
            }
        }
        if (focusSticker != null) {
            focusSticker.onDraw(canvas, this.paint)
        }
    }

    override fun onTouch(v: View?, event: MotionEvent): Boolean {
        when (event.getAction() and MotionEvent.ACTION_MASK) {
            MotionEvent.ACTION_DOWN, MotionEvent.ACTION_POINTER_DOWN -> {
                // Determines whether the delete button is pressed
                mStick = StickerManager.getInstance().getDelButton(event.getX(), event.getY())
                if (mStick != null) {
                    removeSticker(mStick!!)
                    mStick = null
                }
                // Indicates whether a single finger is touching the sticker
                mStick = StickerManager.getInstance().getSticker(event.x, event.y)
                if (mStick == null) {
                    if (event.pointerCount == 2) {
                        // Handles two-finger touch: the first finger does not touch the sticker, but the second finger does touch the sticker
                        mStick =
                            StickerManager.getInstance().getSticker(event.getX(1), event.getY(1))
                    }
                }
                if (mStick != null) {
                    StickerManager.getInstance().setFocusSticker(mStick)
                }
            }

            else -> {}
        }
        if (mStick != null) {
            mStick!!.onTouch(event)
        } else {
            StickerManager.getInstance().clearAllFocus()
        }
        invalidate()
        return true
    }
}
