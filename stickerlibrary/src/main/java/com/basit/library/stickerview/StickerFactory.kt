package com.basit.library.stickerview

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.PorterDuff
import androidx.annotation.ColorInt
import androidx.annotation.IntRange
import androidx.lifecycle.MutableLiveData

object StickerFactory {
    var currentSticker: Sticker?=null
    var isStickerFocused = MutableLiveData<Boolean?>()
    fun createSticker(
        context: Context,
        drawableId: Int,
        @IntRange(from = 0, to = 255) alpha: Int? = null,
        @ColorInt color: Int? = null,
        mode: PorterDuff.Mode = PorterDuff.Mode.SRC_ATOP
    ): Sticker {
        val bitmap = BitmapFactory.decodeResource(context.resources, drawableId)
        val sticker = Sticker(context, bitmap)
        // Apply alpha if provided, otherwise default 128
        sticker.alpha = alpha ?: 128
        // Apply color filter if provided
        color?.let {
            sticker.setColorFilter(it, mode )
        }

        return sticker
    }
    fun createStickerFromAsset(
        context: Context,
        assetPath: String,
        @IntRange(from = 0, to = 255) alpha: Int? = null,
        @ColorInt color: Int? = null,
        mode: PorterDuff.Mode = PorterDuff.Mode.SRC_ATOP
    ): Sticker? {
        return try {
            // 🔹 Normalize the path (remove prefix if present)
            val path = assetPath.removePrefix("file:///android_asset/")

            // 🔹 Decode bitmap safely
            val rawBitmap = context.assets.open(path).use { input ->
                BitmapFactory.decodeStream(input)
            } ?: return null

            val bitmap = rawBitmap.copy(Bitmap.Config.ARGB_8888, true)

            // 🔹 Build sticker
            Sticker(context, bitmap).apply {
                this.alpha = alpha ?: 128
                color?.let { setColorFilter(it, mode) }
            }
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }


}
