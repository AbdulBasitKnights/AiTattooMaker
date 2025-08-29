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
    StyleItem("1", "Anime", R.drawable.dragon,false,true,"file:///android_asset/styles/anime.webp"),
    StyleItem("2", "Chicano", R.drawable.dragon,false,false,"file:///android_asset/styles/chicano.webp"),
    StyleItem("3", "Engraving", R.drawable.dragon,true,false,"file:///android_asset/styles/engraving.webp"),
    StyleItem("4", "Flames", R.drawable.dragon,false,false,"file:///android_asset/styles/flames.webp"),
    StyleItem("5", "Geometric", R.drawable.dragon,false,false,"file:///android_asset/styles/geometric.webp"),
    StyleItem("6", "Gothic", R.drawable.dragon,false,false,"file:///android_asset/styles/gothic.webp"),
    StyleItem("7", "Graffiti", R.drawable.dragon,false,false,"file:///android_asset/styles/graffiti.webp"),
    StyleItem("8", "Horror", R.drawable.dragon,false,false,"file:///android_asset/styles/horror.webp"),
    StyleItem("9", "Japanese", R.drawable.dragon,false,false,"file:///android_asset/styles/japanese.webp"),
    StyleItem("10", "Minimalist", R.drawable.dragon,false,false,"file:///android_asset/styles/minimalist.webp"),
    StyleItem("11", "Motives", R.drawable.dragon,false,false,"file:///android_asset/styles/motives.webp"),
    StyleItem("12", "No Style", R.drawable.dragon,false,false,"file:///android_asset/styles/nostyle.webp"),
    StyleItem("13", "Old School", R.drawable.dragon,false,false,"file:///android_asset/styles/oldschool.webp"),
    StyleItem("14", "Realistic", R.drawable.dragon,false,false,"file:///android_asset/styles/realistic.webp"),
    StyleItem("15", "Sketch", R.drawable.dragon,false,false,"file:///android_asset/styles/sketch.webp"),
    StyleItem("16", "Surrealism", R.drawable.dragon,false,false,"file:///android_asset/styles/surrealism.webp"),
    StyleItem("17", "Trash Polka", R.drawable.dragon,false,false,"file:///android_asset/styles/trashpolka.webp"),
    StyleItem("18", "Tribal", R.drawable.dragon,false,false,"file:///android_asset/styles/tribal.webp"),
    StyleItem("19", "Victorian", R.drawable.dragon,false,false,"file:///android_asset/styles/victorian.webp"),
    StyleItem("20", "Water Color", R.drawable.dragon,false,false,"file:///android_asset/styles/watercolor.webp"),
)

val styleLiveData: MutableLiveData<ArrayList<StyleItem>> = MutableLiveData(style_list)