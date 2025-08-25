package com.basit.aitattoomaker.presentation.ai_create.dialog

import android.app.Dialog
import android.content.Context
import android.graphics.Color
import android.graphics.drawable.Animatable
import android.graphics.drawable.ColorDrawable
import android.view.Window
import android.widget.ImageView
import com.basit.aitattoomaker.R
import com.bumptech.glide.Glide
import com.bumptech.glide.load.engine.DiskCacheStrategy
import androidx.core.graphics.drawable.toDrawable

class AiCreationDialog(
    private val context: Context
) {
    private var dialog: Dialog? = null

    fun show() {
        dialog = Dialog(context, android.R.style.Theme_Black_NoTitleBar_Fullscreen).apply {
            requestWindowFeature(Window.FEATURE_NO_TITLE)
            setCancelable(false)
            setContentView(R.layout.ai_creation_dialog)
            window?.setBackgroundDrawable(Color.BLACK.toDrawable())
        }

        val imageView = dialog?.findViewById<ImageView>(R.id.fullscreenWebPView)
        imageView?.let { iv ->
            val drawable = iv.drawable
            if (drawable is Animatable) {
                drawable.start() // starts animation
            }
            Glide.with(context)
                .load(R.raw.loader_anim) // can be animated WebP in drawable
                .diskCacheStrategy(DiskCacheStrategy.AUTOMATIC)
                .into(iv) // don't use .asGif() for drawable WebP
        }

        dialog?.show()
    }

    fun dismiss() {
        dialog?.dismiss()
    }
}
