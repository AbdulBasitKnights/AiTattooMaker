package com.basit.aitattoomaker.presentation.result.model

import android.os.Parcelable
import androidx.annotation.DrawableRes
import androidx.annotation.Keep
import kotlinx.parcelize.Parcelize

@Keep
@Parcelize
data class ResultItem(
    var id: String="",
    var title: String="",
    var isPro:Boolean=false,
    var isSelected: Boolean = false,
    var imageUrl: String? = "",
    var prompt:String=""
): Parcelable
