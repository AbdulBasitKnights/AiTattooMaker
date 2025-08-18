package com.basit.aitattoomaker.domain

import android.os.Parcelable
import androidx.annotation.Keep
import kotlinx.parcelize.Parcelize

@Keep
@Parcelize
data class Tattoo(
    val id: String,
    val name: String,
    val imageUrl: String,
    val thumbnailUrl: String,
    val category: String,
    val isPremium: Boolean = false
): Parcelable