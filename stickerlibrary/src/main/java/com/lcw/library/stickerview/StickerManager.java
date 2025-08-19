package com.lcw.library.stickerview;

import android.graphics.Bitmap;
import android.graphics.Matrix;

import java.util.ArrayList;
import java.util.List;

/**
 * M Abdul Basit
 * Create by: Abdul Basit
 * Date: 2025/8/19
 * Time: 9:44 AM
 */
public class StickerManager {

    private static volatile StickerManager mInstance;

    private List<Sticker> mStickerList = new ArrayList<>();

    public static StickerManager getInstance() {
        if (mInstance == null) {
            synchronized (StickerManager.class) {
                if (mInstance == null) {
                    mInstance = new StickerManager();
                }
            }
        }
        return mInstance;
    }

    public void addSticker(Sticker sticker) {
        mStickerList.add(sticker);
    }

    public List<Sticker> getStickerList() {
        return mStickerList;
    }

    /**
     * removeSticker
     *
     * @param sticker
     */
    public void removeSticker(Sticker sticker) {
        Bitmap bitmap = sticker.getBitmap();
        if (bitmap != null && bitmap.isRecycled()) {
            bitmap.recycle();
        }
        mStickerList.remove(sticker);

    }

    /**
     * removeAllSticker
     */
    public void removeAllSticker() {
        for (int i = 0; i < mStickerList.size(); i++) {
            Bitmap bitmap = mStickerList.get(i).getBitmap();
            if (bitmap != null && bitmap.isRecycled()) {
                bitmap.recycle();
            }
        }
        mStickerList.clear();
    }

    /**
     * setFocusSticker
     *
     * @param focusSticker
     */
    public void setFocusSticker(Sticker focusSticker) {
        for (int i = 0; i < mStickerList.size(); i++) {
            Sticker sticker = mStickerList.get(i);
            if (sticker == focusSticker) {
                sticker.setFocus(true);
            } else {
                sticker.setFocus(false);
            }
        }
    }

    /**
     * clearAllFocus
     */
    public void clearAllFocus() {
        for (int i = 0; i < mStickerList.size(); i++) {
            Sticker sticker = mStickerList.get(i);
            sticker.setFocus(false);
        }
    }

    /**
     * getSticker
     *
     * @param x
     * @param y
     * @return
     */
    public Sticker getSticker(float x, float y) {

        float[] dstPoints = new float[2];
        float[] srcPoints = new float[]{x, y};

        for (int i = mStickerList.size() - 1; i >= 0; i--) {
            Sticker sticker = mStickerList.get(i);
            Matrix matrix = new Matrix();
            sticker.getMatrix().invert(matrix);
            matrix.mapPoints(dstPoints, srcPoints);
            if (sticker.getStickerBitmapBound().contains(dstPoints[0], dstPoints[1])) {
                return sticker;
            }
        }
        return null;
    }

    /**
     * To get Delete Button View
     *
     * @param x
     * @param y
     * @return
     */
    public Sticker getDelButton(float x, float y) {

        float[] dstPoints = new float[2];
        float[] srcPoints = new float[]{x, y};

        for (int i = mStickerList.size() - 1; i >= 0; i--) {
            Sticker sticker = mStickerList.get(i);
            Matrix matrix = new Matrix();
            sticker.getMatrix().invert(matrix);
            matrix.mapPoints(dstPoints, srcPoints);
            if (sticker.getDelBitmapBound().contains(dstPoints[0], dstPoints[1])) {
                return sticker;
            }
        }
        return null;

    }


}
