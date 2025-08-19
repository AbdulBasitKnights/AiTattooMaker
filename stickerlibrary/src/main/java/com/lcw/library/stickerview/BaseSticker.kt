package com.lcw.library.stickerview;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.PointF;
import android.graphics.RectF;
import android.util.DisplayMetrics;
import android.view.WindowManager;


/**
 * M Abdul Basit
 * Create by: Abdul Basit
 * Date: 2025/8/19
 * Time: 9:44 AM
 */
public abstract class BaseSticker implements ISupportOperation {

    private Bitmap mStickerBitmap;// Sticker image
    private Bitmap mDelBitmap;//Delete Sticker
    private Matrix mMatrix;// Matrix that manages image transformations
    private boolean isFocus;// Indicates whether it is currently focused
    protected int mMode;// Current mode

    private float[] mSrcPoints;// Coordinates of the points before matrix transformation
    private float[] mDstPoints;// Coordinates of points after matrix transformation
    private RectF mStickerBound;// Sticker bounds
    private RectF mDelBound;// Delete button area
    private PointF mMidPointF;// Coordinates of the sticker's center point

    public static final int MODE_NONE = 0;// Initial state
    public static final int MODE_SINGLE = 1;// Indicates whether moving is enabled
    public static final int MODE_MULTIPLE = 2;// Indicates whether scaling and rotation are enabled

    private static final int PADDING = 30;// To prevent the image from being too close to the border, a margin is set here


    public BaseSticker(Context context, Bitmap bitmap) {
        this.mStickerBitmap = bitmap;
        mMatrix = new Matrix();
        mMidPointF = new PointF();

        mSrcPoints = new float[]{
                0, 0,
                bitmap.getWidth(), 0,
                bitmap.getWidth(), bitmap.getHeight(),
                0, bitmap.getHeight(),
                bitmap.getWidth() / 2, bitmap.getHeight() / 2
        };
        mDstPoints = mSrcPoints.clone();
        mStickerBound = new RectF(0, 0, bitmap.getWidth(), bitmap.getHeight());

        mDelBitmap = BitmapFactory.decodeResource(context.getResources(), R.mipmap.icon_delete);
        mDelBound = new RectF(0 - mDelBitmap.getWidth() / 2 - PADDING, 0 - mDelBitmap.getHeight() / 2 - PADDING, mDelBitmap.getWidth() / 2 + PADDING, mDelBitmap.getHeight() / 2 + PADDING);

        // Move the sticker to the center of the screen by default
        WindowManager windowManager = (WindowManager) context.getSystemService(Context.WINDOW_SERVICE);
        DisplayMetrics displayMetrics = new DisplayMetrics();
        windowManager.getDefaultDisplay().getMetrics(displayMetrics);
        float dx = displayMetrics.widthPixels / 2 - mStickerBitmap.getWidth() / 2;
        float dy = displayMetrics.heightPixels / 2 - mStickerBitmap.getHeight() / 2;
        translate(dx, dy);
        // By default, scale the sticker to half its original size
        scale(0.5f, 0.5f);
    }

    public Bitmap getBitmap() {
        return mStickerBitmap;
    }

    public RectF getStickerBitmapBound() {
        return mStickerBound;
    }

    public RectF getDelBitmapBound() {
        return mDelBound;
    }

    public Matrix getMatrix() {
        return mMatrix;
    }

    public boolean isFocus() {
        return isFocus;
    }

    public void setFocus(boolean focus) {
        isFocus = focus;
    }

    /**
     * Translate
     *
     * @param dx
     * @param dy
     */
    @Override
    public void translate(float dx, float dy) {
        mMatrix.postTranslate(dx, dy);
        updatePoints();
    }

    /**
     * Scaling
     *
     * @param sx
     * @param sy
     */
    @Override
    public void scale(float sx, float sy) {
        mMatrix.postScale(sx, sy, mMidPointF.x, mMidPointF.y);
        updatePoints();
    }

    /**
     * Rotation
     *
     * @param degrees
     */
    @Override
    public void rotate(float degrees) {
        mMatrix.postRotate(degrees, mMidPointF.x, mMidPointF.y);
        updatePoints();
    }

    /**
     * When the matrix changes, update the coordinates (the src points are mapped into dst points by the matrix)
     */

    private void updatePoints() {
        // Update sticker point coordinates
        mMatrix.mapPoints(mDstPoints, mSrcPoints);
        // Update the coordinates of the sticker's center point
        mMidPointF.set(mDstPoints[8], mDstPoints[9]);
    }

    /**
     * Draw Sticker by Canvas
     *
     * @param canvas
     * @param paint
     */
    @Override
    public void onDraw(Canvas canvas, Paint paint) {
        // Draw the sticker
        canvas.drawBitmap(mStickerBitmap, mMatrix, paint);
        if (isFocus) {
            // Draw the sticker border
            canvas.drawLine(mDstPoints[0] - PADDING, mDstPoints[1] - PADDING, mDstPoints[2] + PADDING, mDstPoints[3] - PADDING, paint);
            canvas.drawLine(mDstPoints[2] + PADDING, mDstPoints[3] - PADDING, mDstPoints[4] + PADDING, mDstPoints[5] + PADDING, paint);
            canvas.drawLine(mDstPoints[4] + PADDING, mDstPoints[5] + PADDING, mDstPoints[6] - PADDING, mDstPoints[7] + PADDING, paint);
            canvas.drawLine(mDstPoints[6] - PADDING, mDstPoints[7] + PADDING, mDstPoints[0] - PADDING, mDstPoints[1] - PADDING, paint);
            // Draw the delete button
            canvas.drawBitmap(mDelBitmap, mDstPoints[0] - mDelBitmap.getWidth() / 2 - PADDING, mDstPoints[1] - mDelBitmap.getHeight() / 2 - PADDING, paint);
        }
    }

}
