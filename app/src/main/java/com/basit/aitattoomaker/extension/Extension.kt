package com.basit.aitattoomaker.extension

import android.app.Activity
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Rect
import android.view.View
import androidx.core.graphics.createBitmap
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity


fun View.toBitmap(): Bitmap {
        val bitmap = createBitmap(width, height)
        val canvas = Canvas(bitmap)
        draw(canvas)
        return bitmap
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
