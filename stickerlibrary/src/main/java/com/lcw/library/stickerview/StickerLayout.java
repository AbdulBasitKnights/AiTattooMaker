package com.lcw.library.stickerview;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Toast;

import java.util.List;

/**
 * M Abdul Basit
 * Create by: Abdul Basit
 * Date: 2025/8/19
 * Time: 9:44 AM
 */
public class StickerLayout extends View implements View.OnTouchListener {

    private Context mContext;
    private Paint mPaint;

    // Records the currently operated sticker object
    private Sticker mStick;

    public StickerLayout(Context context) {
        super(context);
        init(context);
    }

    public StickerLayout(Context context, AttributeSet attrs) {
        super(context, attrs);
        init(context);
    }

    public StickerLayout(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init(context);
    }

    /**
     * Initial operation / initialization operation
     */
    private void init(Context context) {
        this.mContext = context;
        // Set touch listener
        setOnTouchListener(this);
    }

    public Paint getPaint() {
        if (mPaint == null) {
            mPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
            mPaint.setColor(Color.BLACK);
            mPaint.setStrokeWidth(2);
        }
        return mPaint;
    }

    public void setPaint(Paint mPaint) {
        this.mPaint = mPaint;
    }


    /**
     * Add New Sticker
     *
     * @param sticker
     */
    public void addSticker(Sticker sticker) {
        int size = StickerManager.getInstance().getStickerList().size();
        if (size < 9) {
            StickerManager.getInstance().addSticker(sticker);
            StickerManager.getInstance().setFocusSticker(sticker);
            invalidate();
        } else {
            Toast.makeText(mContext, "// The maximum number of stickers cannot exceed 9", Toast.LENGTH_SHORT).show();
        }
    }

    /**
     * Remove Selected Sticker
     *
     * @param sticker
     */
    public void removeSticker(Sticker sticker) {
        if (sticker.isFocus()) {
            StickerManager.getInstance().removeSticker(sticker);
            invalidate();
        }
    }

    /**
     * Remove All Stickers
     */
    public void removeAllSticker() {
        StickerManager.getInstance().removeAllSticker();
        invalidate();
    }


    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        List<Sticker> stickerList = StickerManager.getInstance().getStickerList();
        Sticker focusSticker = null;
        for (int i = 0; i < stickerList.size(); i++) {
            Sticker sticker = stickerList.get(i);
            if (sticker.isFocus()) {
                focusSticker = sticker;
            } else {
                sticker.onDraw(canvas, getPaint());
            }
        }
        if (focusSticker != null) {
            focusSticker.onDraw(canvas, getPaint());
        }
    }

    @Override
    public boolean onTouch(View v, MotionEvent event) {
        switch (event.getAction() & MotionEvent.ACTION_MASK) {
            case MotionEvent.ACTION_DOWN:
            case MotionEvent.ACTION_POINTER_DOWN:
                // Determines whether the delete button is pressed
                mStick = StickerManager.getInstance().getDelButton(event.getX(), event.getY());
                if (mStick != null) {
                    removeSticker(mStick);
                    mStick = null;
                }
                // Indicates whether a single finger is touching the sticker
                mStick = StickerManager.getInstance().getSticker(event.getX(), event.getY());
                if (mStick == null) {
                    if (event.getPointerCount() == 2) {
                        // Handles two-finger touch: the first finger does not touch the sticker, but the second finger does touch the sticker
                        mStick = StickerManager.getInstance().getSticker(event.getX(1), event.getY(1));
                    }
                }
                if (mStick != null) {
                    StickerManager.getInstance().setFocusSticker(mStick);
                }
                break;
            default:
                break;
        }
        if (mStick != null) {
            mStick.onTouch(event);
        } else {
            StickerManager.getInstance().clearAllFocus();
        }
        invalidate();
        return true;
    }
}
