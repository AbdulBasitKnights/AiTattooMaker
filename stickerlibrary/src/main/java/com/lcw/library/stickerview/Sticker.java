package com.lcw.library.stickerview;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.PointF;
import android.view.MotionEvent;

/**
 * M Abdul Basit
 * Create by: Abdul Basit
 * Date: 2025/8/19
 * Time: 9:44 AM
 */
public class Sticker extends BaseSticker {

    private PointF mLastSinglePoint = new PointF();
    private PointF mLastDistanceVector = new PointF();
    private PointF mDistanceVector = new PointF();
    private float mLastDistance;//last distance

    //Initial Point
    private PointF mFirstPoint = new PointF();
    private PointF mSecondPoint = new PointF();

    public Sticker(Context context, Bitmap bitmap) {
        super(context, bitmap);
    }

    /**
     * Reset State
     */
    public void reset() {
        mLastSinglePoint.set(0f, 0f);
        mLastDistanceVector.set(0f, 0f);
        mDistanceVector.set(0f, 0f);
        mLastDistance = 0f;
        mMode = MODE_NONE;
    }

    /**
     * Distance
     */
    public float calculateDistance(PointF firstPointF, PointF secondPointF) {
        float x = firstPointF.x - secondPointF.x;
        float y = firstPointF.y - secondPointF.y;
        return (float) Math.sqrt(x * x + y * y);
    }


    /**
     * Degree Calculation
     *
     * @param lastVector
     * @param currentVector
     * @return
     */
    public float calculateDegrees(PointF lastVector, PointF currentVector) {
        float lastDegrees = (float) Math.atan2(lastVector.y, lastVector.x);
        float currentDegrees = (float) Math.atan2(currentVector.y, currentVector.x);
        return (float) Math.toDegrees(currentDegrees - lastDegrees);
    }


    /**
     * Touch Event
     *
     * @param event
     */
    public void onTouch(MotionEvent event) {
        switch (event.getAction() & MotionEvent.ACTION_MASK) {
            case MotionEvent.ACTION_DOWN:

                mMode = Sticker.MODE_SINGLE;

                mLastSinglePoint.set(event.getX(), event.getY());
                break;
            case MotionEvent.ACTION_POINTER_DOWN:
                if (event.getPointerCount() == 2) {
                    mMode = Sticker.MODE_MULTIPLE;

                    mFirstPoint.set(event.getX(0), event.getY(0));
                    mSecondPoint.set(event.getX(1), event.getY(1));

                    mLastDistanceVector.set(mFirstPoint.x - mSecondPoint.x, mFirstPoint.y - mSecondPoint.y);

                    mLastDistance = calculateDistance(mFirstPoint, mSecondPoint);
                }
                break;
            case MotionEvent.ACTION_MOVE:
                if (mMode == MODE_SINGLE) {
                    translate(event.getX() - mLastSinglePoint.x, event.getY() - mLastSinglePoint.y);
                    mLastSinglePoint.set(event.getX(), event.getY());
                }
                if (mMode == MODE_MULTIPLE && event.getPointerCount() == 2) {

                    mFirstPoint.set(event.getX(0), event.getY(0));
                    mSecondPoint.set(event.getX(1), event.getY(1));

                    float distance = calculateDistance(mFirstPoint, mSecondPoint);

                    float scale = distance / mLastDistance;
                    scale(scale, scale);
                    mLastDistance = distance;

                    mDistanceVector.set(mFirstPoint.x - mSecondPoint.x, mFirstPoint.y - mSecondPoint.y);
                    rotate(calculateDegrees(mLastDistanceVector, mDistanceVector));
                    mLastDistanceVector.set(mDistanceVector.x, mDistanceVector.y);
                }
                break;
            case MotionEvent.ACTION_UP:
                reset();
                break;
        }
    }

}
