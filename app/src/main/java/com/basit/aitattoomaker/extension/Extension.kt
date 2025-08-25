package com.basit.aitattoomaker.extension

import android.animation.Animator
import android.animation.AnimatorListenerAdapter
import android.app.Activity
import android.content.Context
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Rect
import android.view.KeyEvent
import android.view.View
import android.view.animation.AlphaAnimation
import android.view.animation.Animation
import android.view.inputmethod.InputMethodManager
import android.widget.EditText
import android.widget.RadioButton
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.core.graphics.createBitmap
import androidx.core.graphics.drawable.toDrawable
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity
import com.basit.aitattoomaker.R


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
