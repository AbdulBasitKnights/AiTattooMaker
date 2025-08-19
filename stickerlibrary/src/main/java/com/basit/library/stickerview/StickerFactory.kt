package com.basit.library.stickerview

import android.content.Context
import android.graphics.BitmapFactory
import android.graphics.PorterDuff
import androidx.annotation.ColorInt
import androidx.annotation.IntRange
object StickerFactory {
    var currentSticker: Sticker?=null
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
}
