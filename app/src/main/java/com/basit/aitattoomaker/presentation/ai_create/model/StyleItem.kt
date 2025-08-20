package com.basit.aitattoomaker.presentation.ai_create.model

import androidx.annotation.DrawableRes

data class StyleItem(
    val id: String,
    val title: String,
    @DrawableRes val url: Int   // drawable resource, OR you could make this a URL String if loading with Glide
)
