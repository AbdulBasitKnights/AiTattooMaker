package com.basit.aitattoomaker.presentation.utils

import android.app.Dialog
import android.content.Context
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.view.Window
import android.widget.TextView
import com.basit.aitattoomaker.R

object DialogUtils {

    var dialog: Dialog? = null

    fun show(context: Context, title: String = "Saving...") {
        if (dialog?.isShowing == true) return  // already showing
        dialog = Dialog(context).apply {
            requestWindowFeature(Window.FEATURE_NO_TITLE)
            setCancelable(false)
            setContentView(R.layout.dialog_loader)
            // set title
            findViewById<TextView>(R.id.tvTitle)?.text = title
            // set transparent background
            window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
        }
    }

    fun dismiss() {
        try {
            dialog?.dismiss()
            dialog = null
        } catch (e: Exception) {
           e.printStackTrace()
        }
    }
}