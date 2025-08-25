package com.basit.aitattoomaker.presentation.utils

import androidx.lifecycle.MutableLiveData
import com.basit.aitattoomaker.R
import com.basit.aitattoomaker.presentation.ai_create.model.StyleItem

var tattooCreation = MutableLiveData<Boolean?>()
val style_list: ArrayList<StyleItem> = arrayListOf(
    StyleItem("1", "Fire", R.drawable.tattoo,false,true),
    StyleItem("2", "Dragon", R.drawable.dragon,false,false),
    StyleItem("3", "Flower", R.drawable.flower,true,false),
    StyleItem("4", "Heart", R.drawable.heart,false,false)
)

val styleLiveData: MutableLiveData<ArrayList<StyleItem>> = MutableLiveData(style_list)