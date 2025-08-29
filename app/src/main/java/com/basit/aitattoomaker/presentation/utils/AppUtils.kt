package com.basit.aitattoomaker.presentation.utils

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.ImageDecoder
import android.os.Build
import androidx.annotation.DrawableRes
import androidx.exifinterface.media.ExifInterface
import androidx.fragment.app.FragmentActivity
import com.basit.aitattoomaker.MainActivity
import com.basit.aitattoomaker.R
import com.basit.aitattoomaker.domain.TattooResponse
import com.google.gson.Gson
import java.io.File

object AppUtils {
    val tattooPrompts = listOf(
        "A majestic dragon wrapped around a samurai sword",
        "Realistic lion roaring with fiery mane",
        "Floral mandala with intricate patterns",
        "Watercolor phoenix rising from ashes",
        "Minimalist geometric wolf design",
        "Japanese koi fish swimming upstream",
        "Gothic skull with roses and thorns",
        "Abstract cosmic galaxy with stars and planets",
        "Steampunk mechanical heart with gears",
        "Angel wings spreading across the back",
        "Tribal tribal wolf howling at moon",
        "Surreal clock melting over a tree branch",
        "Lion head combined with crown",
        "Tiger emerging from jungle leaves",
        "Cute tiny tattoo of a cat silhouette",
        "Mystical owl sitting on crescent moon",
        "Biomechanical arm tattoo with robotic parts"
    )
    @DrawableRes var tattooID:Int= R.drawable.dragon
    var tattooPath:String= "file:///android_asset/library/dragon.png"
    fun getMain(activity: FragmentActivity?): MainActivity? {
        return activity as? MainActivity
    }
    fun loadTattoos(context: Context): TattooResponse? {
        return try {
            val json = context.assets.open("tattoos.json")
                .bufferedReader().use { it.readText() }
            Gson().fromJson(json, TattooResponse::class.java)
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
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