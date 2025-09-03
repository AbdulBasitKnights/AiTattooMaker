package com.basit.aitattoomaker.presentation.ai_create.model

import android.os.Parcelable
import androidx.annotation.DrawableRes
import androidx.annotation.Keep
import kotlinx.parcelize.Parcelize

@Keep
@Parcelize
data class StyleItem(
    var id: String="",
    var title: String="",
    var isPro:Boolean=false,
    var isSelected: Boolean = false,
    var imageUrl: String? = "",
    var prompt:String=""
): Parcelable
