package com.basit.aitattoomaker.presentation.utils

import android.graphics.Bitmap
import android.net.Uri
import androidx.annotation.DrawableRes
import androidx.lifecycle.MutableLiveData
import com.basit.aitattoomaker.R
import com.basit.aitattoomaker.presentation.ai_create.model.StyleItem

var tattooCreation = MutableLiveData<Boolean?>()
var capturedBitmap: Bitmap?=null
@DrawableRes var selectedTattoo: Int?=null
val style_list: ArrayList<StyleItem> = arrayListOf(
    StyleItem("1", "Fire", R.drawable.tattoo,false,true,"file:///android_asset/tattoos/tattoo.png"),
    StyleItem("2", "Dragon", R.drawable.dragon,false,false,"file:///android_asset/tattoos/dragon.png"),
    StyleItem("3", "Flower", R.drawable.flower,true,false,"file:///android_asset/tattoos/flower.png"),
    StyleItem("4", "Heart", R.drawable.heart,false,false,"file:///android_asset/tattoos/heart.png"),
    StyleItem("5", "Sparrow", R.drawable.heart,false,false,"file:///android_asset/tattoos/sparrow.png")
)

val styleLiveData: MutableLiveData<ArrayList<StyleItem>> = MutableLiveData(style_list)