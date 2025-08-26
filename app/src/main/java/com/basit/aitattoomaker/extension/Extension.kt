package com.basit.aitattoomaker.extension

import android.animation.Animator
import android.animation.AnimatorListenerAdapter
import android.app.Activity
import android.app.Dialog
import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.*
import androidx.camera.core.ImageProxy
import java.io.ByteArrayOutputStream
import android.graphics.Canvas
import android.graphics.ImageFormat
import android.graphics.Rect
import android.graphics.YuvImage
import android.os.Build
import android.view.KeyEvent
import android.view.View
import android.view.WindowInsets
import android.view.WindowInsetsController
import android.view.animation.AlphaAnimation
import android.view.animation.Animation
import android.view.inputmethod.InputMethodManager
import android.widget.EditText
import android.widget.RadioButton
import android.widget.TextView
import androidx.annotation.OptIn
import androidx.camera.core.ExperimentalGetImage
import androidx.core.content.ContextCompat
import androidx.core.graphics.createBitmap
import androidx.core.graphics.drawable.toDrawable
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity
import com.basit.aitattoomaker.R
import com.basit.aitattoomaker.databinding.DeleteDialogBinding
import com.basit.aitattoomaker.databinding.DiscardDialogBinding
import com.basit.aitattoomaker.databinding.FreeLimitDialogBinding
import com.basit.aitattoomaker.databinding.RegenerationDialogBinding

//Dialogs
fun FragmentActivity.showDiscardDialog(
    onDiscard: () -> Unit,
    onNotNow: () -> Unit
) {
    val dialog = Dialog(this)
    val binding = DiscardDialogBinding.inflate(layoutInflater)
    dialog.setContentView(binding.root)
    // Optional: make background transparent
    dialog.window?.setBackgroundDrawableResource(android.R.color.transparent)
    // Handle clicks
    binding.discard.setOnClickListener {
        onDiscard()
        dialog.dismiss()
    }
    binding.notNow.setOnClickListener {
        onNotNow()
        dialog.dismiss()
    }
    // Prevent outside touch dismiss if you want
    dialog.setCancelable(false)
    dialog.show()
}
fun FragmentActivity.showDeleteDialog(
    onDelete: () -> Unit,
    onCancel: () -> Unit
) {
    val dialog = Dialog(this)
    val binding = DeleteDialogBinding.inflate(layoutInflater)
    dialog.setContentView(binding.root)
    // Optional: make background transparent
    dialog.window?.setBackgroundDrawableResource(android.R.color.transparent)
    // Handle clicks
    binding.delete.setOnClickListener {
        onDelete()
        dialog.dismiss()
    }
    binding.cancel.setOnClickListener {
        onCancel()
        dialog.dismiss()
    }
    // Prevent outside touch dismiss if you want
    dialog.setCancelable(false)
    dialog.show()
}

fun FragmentActivity.regGenLoaderDialog() {
    val dialog = Dialog(this)
    val binding = RegenerationDialogBinding.inflate(layoutInflater)
    dialog.setContentView(binding.root)
    // Optional: make background transparent
    dialog.window?.setBackgroundDrawableResource(android.R.color.transparent)
    // Handle clicks

    // Prevent outside touch dismiss if you want
    dialog.setCancelable(false)
    dialog.show()
}

fun FragmentActivity.freeLimitDialog(
    onUpgrade: () -> Unit,
    onCancel: () -> Unit
) {
    val dialog = Dialog(this)
    val binding = FreeLimitDialogBinding.inflate(layoutInflater)
    dialog.setContentView(binding.root)
    // Optional: make background transparent
    dialog.window?.setBackgroundDrawableResource(android.R.color.transparent)
    // Handle clicks
    binding.upgrade.setOnClickListener {
        onUpgrade()
        dialog.dismiss()
    }
    binding.cross.setOnClickListener {
        onCancel()
        dialog.dismiss()
    }
    // Prevent outside touch dismiss if you want
    dialog.setCancelable(false)
    dialog.show()
}
fun View.toBitmap(): Bitmap {
        val bitmap = createBitmap(width, height)
        val canvas = Canvas(bitmap)
        draw(canvas)
        return bitmap
    }
// Extension function to convert dp to pixels
fun Context.dp(value: Int): Int = (value * resources.displayMetrics.density).toInt()
fun TextView.setDrawableTint(color: Int) {
    // Get all compound drawables (start, top, end, bottom)
    val drawables = compoundDrawablesRelative
    for (i in drawables.indices) {
        drawables[i]?.setTint(color)
    }
    // Re-apply them
    setCompoundDrawablesRelative(drawables[0], drawables[1], drawables[2], drawables[3])
}
fun TextView.setDrawableWithTint(drawableRes: Int, tintColor: Int) {
    val drawable = ContextCompat.getDrawable(context, drawableRes)?.mutate()
    val drawableEnd = ContextCompat.getDrawable(context, R.drawable.arrow_down)?.mutate()
    drawable?.setTint(tintColor)
    // Here we assume you want it at start, adjust as needed
    setCompoundDrawablesRelativeWithIntrinsicBounds(drawable, null, drawableEnd, null)
}
fun FragmentActivity.hideSystemBars() {
    try {
        window.decorView.post {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
                window.setDecorFitsSystemWindows(false) // extend layout into system bar areas
                window.statusBarColor = Color.BLACK     // color top bar area
                window.navigationBarColor = Color.BLACK // color bottom bar area
                window.insetsController?.let {
                    it.hide(WindowInsets.Type.statusBars() or WindowInsets.Type.navigationBars())
                    it.systemBarsBehavior =
                        WindowInsetsController.BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE
                }
            } else {
                @Suppress("DEPRECATION")
                window.decorView.systemUiVisibility = (
                        View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY
                                or View.SYSTEM_UI_FLAG_FULLSCREEN
                                or View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
                                or View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                                or View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
                                or View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
                        )
            }
        }
    } catch (e: Exception) {
        e.printStackTrace()
    }
}
@OptIn(ExperimentalGetImage::class)
fun ImageProxy.toBitmapSafe(): Bitmap? {
    val image = this.image ?: return null
    if (image.format != ImageFormat.YUV_420_888) return null

    val yBuffer = image.planes[0].buffer
    val uBuffer = image.planes[1].buffer
    val vBuffer = image.planes[2].buffer

    val ySize = yBuffer.remaining()
    val uSize = uBuffer.remaining()
    val vSize = vBuffer.remaining()

    // NV21 buffer
    val nv21 = ByteArray(ySize + uSize + vSize)

    // Y plane
    yBuffer.get(nv21, 0, ySize)

    // Interleave V and U
    var uvIndex = ySize
    while (uBuffer.hasRemaining() && vBuffer.hasRemaining()) {
        nv21[uvIndex++] = vBuffer.get()
        nv21[uvIndex++] = uBuffer.get()
    }

    // Convert to JPEG
    val yuvImage = YuvImage(nv21, ImageFormat.NV21, width, height, null)
    val out = ByteArrayOutputStream()
    yuvImage.compressToJpeg(Rect(0, 0, width, height), 90, out)
    val imageBytes = out.toByteArray()
    return BitmapFactory.decodeByteArray(imageBytes, 0, imageBytes.size)
}
fun View.dp(value: Int): Float = value * resources.displayMetrics.density
fun Activity.observeKeyboardLegacy(onChanged: (isVisible: Boolean) -> Unit) {
    val rootView = window.decorView.findViewById<View>(android.R.id.content)
    rootView.viewTreeObserver.addOnGlobalLayoutListener {
        val r = Rect()
        rootView.getWindowVisibleDisplayFrame(r)
        val screenHeight = rootView.rootView.height
        val keypadHeight = screenHeight - r.bottom
        val isVisible = keypadHeight > screenHeight * 0.15 // if >15% of screen, keyboard is open
        onChanged(isVisible)
    }
}

fun View.fadeOut(duration: Long = 200, delay: Long = 200, distance: Float = -60f) {
    this.apply {
        animate().alpha(0f) // Fade out
            .translationY(distance) // Move the view up by `distance`
            .setDuration(duration).setStartDelay(delay)
            .setListener(object : AnimatorListenerAdapter() {
                override fun onAnimationEnd(animation: Animator) {
                    visibility = View.GONE
                    translationY = 0f // Reset the position
                }
            })
    }
}

fun View.fadesIn(duration: Long = 250) {
    this.apply {
        // Make sure the view is not interfering with layout changes
        visibility = View.INVISIBLE

        val fadeIn = AlphaAnimation(0f, 1f).apply {
            this.duration = duration
            fillAfter = true // Keeps the view at its final state
        }

        fadeIn.setAnimationListener(object : Animation.AnimationListener {
            override fun onAnimationStart(animation: Animation?) {
                visibility = View.VISIBLE // Ensures it's visible when animation starts
            }

            override fun onAnimationEnd(animation: Animation?) {
                visibility =
                    View.VISIBLE // Makes sure the visibility is set correctly after animation ends
            }

            override fun onAnimationRepeat(animation: Animation?) {}
        })

        startAnimation(fadeIn)
    }
}


// Extension function to fade out (hide) a view
fun View.fadesOut(duration: Long = 150) {
    this.apply {
        val fadeOut = AlphaAnimation(1f, 0f)
        fadeOut.duration = duration
        fadeOut.setAnimationListener(object : Animation.AnimationListener {
            override fun onAnimationStart(animation: Animation?) {}
            override fun onAnimationEnd(animation: Animation?) {
                visibility = View.GONE
            }

            override fun onAnimationRepeat(animation: Animation?) {}
        })
        startAnimation(fadeOut)
    }
}

// Extension function to toggle visibility with fade in/out effect
fun View.toggleFade(duration: Long = 150) {
    if (visibility == View.VISIBLE) {
        fadesOut(duration)
    } else {
        fadesIn(duration)
    }
}


fun View.show() {
    visibility = View.VISIBLE
}

fun View.hide() {
    visibility = View.GONE
}

fun EditText.changeImeActionLable() {
    setImeActionLabel("Done", KeyEvent.KEYCODE_ENTER)

}

fun View.invisible() {
    visibility = View.INVISIBLE
}

fun View.hideKeyboard() {
    val inputMethodManager =
        context.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
    inputMethodManager.hideSoftInputFromWindow(windowToken, 0)
}

fun View.enable() {
    isEnabled = true
    isActivated = true
    isClickable = true
}


fun View.disable() {
    isEnabled = false
    isActivated = false
    isClickable = false
}

fun RadioButton.check() {
    isChecked = true
}

fun RadioButton.unCheck() {
    isChecked = false
}

fun View.activate() {
    isActivated = true
}

fun View.deactivate() {
    isActivated = false
}


fun View.makeClickAble() {
    isClickable = true
    isEnabled = true
}

fun View.makeUnclickable() {
    isClickable = false
    isEnabled = false
}
