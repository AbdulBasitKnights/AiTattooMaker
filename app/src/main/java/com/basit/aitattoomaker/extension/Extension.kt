package com.basit.aitattoomaker.extension

import android.graphics.Bitmap
import android.graphics.Canvas
import android.view.View
import androidx.core.graphics.createBitmap


fun View.toBitmap(): Bitmap {
        val bitmap = createBitmap(width, height)
        val canvas = Canvas(bitmap)
        draw(canvas)
        return bitmap
    }

fun View.dp(value: Int): Float = value * resources.displayMetrics.density
