package com.basit.aitattoomaker.presentation.setting.adapter

import android.graphics.drawable.Drawable
import androidx.annotation.DrawableRes

data class ModelSettings(
    val title : String,
    val isHeader : Boolean=false,
    @DrawableRes val icon : Int?=null,
    val background : Drawable?=null,
    val showView:Boolean=true
)