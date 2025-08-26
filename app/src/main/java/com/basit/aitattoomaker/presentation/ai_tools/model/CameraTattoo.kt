package com.basit.aitattoomaker.presentation.ai_tools.model

import android.os.Parcelable
import androidx.annotation.DrawableRes
import androidx.annotation.Keep
import kotlinx.parcelize.Parcelize

@Keep
@Parcelize
data class CameraTattoo(
    val name: String,
    @DrawableRes val id:Int,
    val imageUrl: String="",
    val thumbnailUrl: String="",
    val category: String="",
    val isPremium: Boolean = false
): Parcelable
