package com.basit.aitattoomaker.presentation.ai_tools

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel

class AiToolsViewModel : ViewModel() {

    private val _text = MutableLiveData<String>().apply {
        value = "This is Ai Tools Fragment"
    }
    val text: LiveData<String> = _text
}