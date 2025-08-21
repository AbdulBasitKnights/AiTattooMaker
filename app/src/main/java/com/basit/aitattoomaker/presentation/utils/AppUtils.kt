package com.basit.aitattoomaker.presentation.utils

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.ImageDecoder
import android.os.Build
import androidx.annotation.DrawableRes
import androidx.exifinterface.media.ExifInterface
import androidx.fragment.app.FragmentActivity
import com.basit.aitattoomaker.MainActivity
import com.basit.aitattoomaker.R
import java.io.File

object AppUtils {
    @DrawableRes var tattooID:Int= R.drawable.tattoo
    fun getMain(activity: FragmentActivity?): MainActivity? {
        return activity as? MainActivity
    }
    fun decodeAndFixOrientation(file: File): Bitmap? {
        return if (Build.VERSION.SDK_INT >= 28) {
            // ImageDecoder usually respects EXIF automatically
            val src = ImageDecoder.createSource(file)
            ImageDecoder.decodeBitmap(src) { decoder, _, _ ->
                decoder.isMutableRequired = true
            }
        } else {
            // BitmapFactory + manual EXIF rotate
            val bmp = BitmapFactory.decodeFile(file.absolutePath) ?: return null
            val exif = androidx.exifinterface.media.ExifInterface(file.absolutePath)
            val orientation = exif.getAttributeInt(
                androidx.exifinterface.media.ExifInterface.TAG_ORIENTATION,
                androidx.exifinterface.media.ExifInterface.ORIENTATION_NORMAL
            )
            val matrix = android.graphics.Matrix().apply {
                when (orientation) {
                    ExifInterface.ORIENTATION_ROTATE_90 -> postRotate(90f)
                    ExifInterface.ORIENTATION_ROTATE_180 -> postRotate(180f)
                    ExifInterface.ORIENTATION_ROTATE_270 -> postRotate(270f)
                    ExifInterface.ORIENTATION_FLIP_HORIZONTAL -> postScale(-1f, 1f)
                    ExifInterface.ORIENTATION_FLIP_VERTICAL -> postScale(1f, -1f)
                    ExifInterface.ORIENTATION_TRANSPOSE -> { postRotate(90f); postScale(-1f, 1f) }
                    ExifInterface.ORIENTATION_TRANSVERSE -> { postRotate(270f); postScale(-1f, 1f) }
                    else -> { /* no-op */ }
                }
            }
            if (!matrix.isIdentity) Bitmap.createBitmap(bmp, 0, 0, bmp.width, bmp.height, matrix, true) else bmp
        }
    }

}