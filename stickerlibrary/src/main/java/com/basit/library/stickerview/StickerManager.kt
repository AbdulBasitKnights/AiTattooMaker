package com.basit.library.stickerview

import android.graphics.Matrix
import com.basit.library.stickerview.StickerFactory.currentSticker
import com.basit.library.stickerview.StickerFactory.isStickerFocused
import kotlin.concurrent.Volatile

/**
 * M Abdul Basit
 * Create by: Abdul Basit
 * Date: 2025/8/19
 * Time: 9:44 AM
 */
class StickerManager {

    val stickerList: MutableList<Sticker> = ArrayList<Sticker>()

    fun addSticker(sticker: Sticker?) {
        stickerList.add(sticker!!)
    }

    /**
     * removeSticker
     *
     * @param sticker
     */
    fun removeSticker(sticker: Sticker) {
        val bitmap = sticker.bitmap
        if (bitmap.isRecycled) {
            bitmap.recycle()
        }
        stickerList.remove(sticker)
    }

    /**
     * removeAllSticker
     */
    fun removeAllSticker() {
        for (i in stickerList.indices) {
            val bitmap = stickerList[i].bitmap
            if (bitmap.isRecycled) {
                bitmap.recycle()
            }
        }
        stickerList.clear()
    }

    /**
     * setFocusSticker
     *
     * @param focusSticker
     */
    fun setFocusSticker(focusSticker: Sticker?) {
        for (i in stickerList.indices) {
            val sticker = stickerList[i]
            if (sticker == focusSticker) {
                sticker.isFocus = true
                isStickerFocused.postValue(true)
            } else {
                sticker.isFocus = false
//                isStickerFocused.postValue(false)
            }
        }

    }

    /**
     * clearAllFocus
     */
    fun clearAllFocus() {
        for (i in stickerList.indices) {
            val sticker = stickerList[i]
            sticker.isFocus = false
            isStickerFocused.postValue(false)
        }
    }

    /**
     * getSticker
     *
     * @param x
     * @param y
     * @return
     */
    fun getSticker(x: Float, y: Float): Sticker? {
        val dstPoints = FloatArray(2)
        val srcPoints = floatArrayOf(x, y)

        for (i in stickerList.indices.reversed()) {
            val sticker = stickerList[i]
            val matrix = Matrix()
            sticker.matrix.invert(matrix)
            matrix.mapPoints(dstPoints, srcPoints)
            if (sticker.stickerBitmapBound!!.contains(dstPoints[0], dstPoints[1])) {
                return sticker
            }
        }
        return null
    }

    /**
     * To get Delete Button View
     *
     * @param x
     * @param y
     * @return
     */
    fun getDelButton(x: Float, y: Float): Sticker? {
        val dstPoints = FloatArray(2)
        val srcPoints = floatArrayOf(x, y)

        for (i in stickerList.indices.reversed()) {
            val sticker = stickerList[i]
            val matrix = Matrix()
            sticker.matrix.invert(matrix)
            matrix.mapPoints(dstPoints, srcPoints)
            if (sticker.delBitmapBound!!.contains(dstPoints[0], dstPoints[1])) {
                return sticker
            }
        }
        return null
    }
    /**
     * Get the currently focused sticker
     */
    fun getFocusSticker(): Sticker? {
        return stickerList.firstOrNull { it.isFocus }
    }


    companion object {
        @Volatile
        private var instance: StickerManager? = null

        fun getInstance(): StickerManager {
            return instance ?: synchronized(this) {
                instance ?: StickerManager().also { instance = it }
            }
        }
    }
}
