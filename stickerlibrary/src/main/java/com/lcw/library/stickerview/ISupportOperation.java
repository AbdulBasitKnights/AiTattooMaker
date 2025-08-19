package com.lcw.library.stickerview;

import android.graphics.Canvas;
import android.graphics.Paint;
import android.view.MotionEvent;

/**
 * M Abdul Basit
 * Create by: Abdul Basit
 * Date: 2025/8/19
 * Time: 9:44 AM
 */
public interface ISupportOperation {

    /**
     *
     *
     * @param dx
     * @param dy
     */
    void translate(float dx, float dy);

    /**
     *
     *
     * @param sx
     * @param sy
     */
    void scale(float sx, float sy);

    /**
     *
     *
     * @param degrees
     */
    void rotate(float degrees);


    /**
     *
     *
     * @param canvas
     * @param paint
     */
    void onDraw(Canvas canvas, Paint paint);

    /**
     *
     *
     * @param event
     */
    void onTouch(MotionEvent event);

}
