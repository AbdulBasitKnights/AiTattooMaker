package com.basit.aitattoomaker.presentation.history.model

import android.os.Parcelable
import kotlinx.parcelize.Parcelize
import java.util.UUID

@Parcelize
data class Creation(
    val id: String = UUID.randomUUID().toString(), // auto-generated unique ID
    val imageUrl: String
) : Parcelable
