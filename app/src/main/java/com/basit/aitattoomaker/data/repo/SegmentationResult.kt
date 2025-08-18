package com.basit.aitattoomaker.data.repo

import android.graphics.Bitmap
import android.graphics.Color
import java.nio.ByteBuffer

data class SegmentationResult(
    val mask: ByteBuffer,  // Raw mask data from ML Kit
    val width: Int,       // Width of the mask
    val height: Int,       // Height of the mask
    val scaleX: Float = 1f, // Scale factor for X-axis (if needed)
    val scaleY: Float = 1f  // Scale factor for Y-axis (if needed)
) {
    // Helper function to create a bitmap from the mask
    fun toBitmap(): Bitmap {
        val bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)
        mask.rewind()

        val pixels = IntArray(width * height)
        for (i in 0 until width * height) {
            val confidence = mask.float // ML Kit returns confidence values [0,1]
            pixels[i] = if (confidence > 0.5f) {
                Color.argb((confidence * 255).toInt(), 255, 255, 255)
            } else {
                Color.TRANSPARENT
            }
        }

        bitmap.setPixels(pixels, 0, width, 0, 0, width, height)
        return bitmap
    }
}