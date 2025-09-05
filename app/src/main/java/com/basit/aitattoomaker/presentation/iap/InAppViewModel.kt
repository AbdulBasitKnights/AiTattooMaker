package com.basit.aitattoomaker.presentation.iap

import android.content.Context
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.basit.aitattoomaker.data.repo.TattooApiService
import com.basit.aitattoomaker.presentation.iap.model.SubscriptionModel
import com.bumptech.glide.load.engine.Resource
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
@HiltViewModel
class InAppViewModel @Inject constructor(
    val aspireApiService: TattooApiService,
    @ApplicationContext private val context: Context,
) : ViewModel(){
    var _subscription: MutableLiveData<Resource<SubscriptionModel?>> =
        MutableLiveData<Resource<SubscriptionModel?>>()
    val subscription: LiveData<Resource<SubscriptionModel?>> = _subscription



}