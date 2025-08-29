package com.basit.aitattoomaker.presentation.ai_tools

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.basit.aitattoomaker.data.repo.TattooRepository
import com.basit.aitattoomaker.presentation.ai_tools.model.CameraTattoo
import com.basit.aitattoomaker.presentation.utils.AppUtils
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext


class AiToolsViewModel(app: Application) : AndroidViewModel(app) {
    private val _library = MutableLiveData<List<CameraTattoo>>()
    val library: LiveData<List<CameraTattoo>> get() = _library

    private val _history = MutableLiveData<List<CameraTattoo>>()
    val history: LiveData<List<CameraTattoo>> get() = _history

    init {
        loadTattoos()
    }

    private fun loadTattoos() {
        viewModelScope.launch(Dispatchers.IO) {
            val data = AppUtils.loadTattoos(getApplication())
            withContext(Dispatchers.Main) {
                _library.value = data?.library ?: emptyList()
                _history.value = data?.history ?: emptyList()
            }
        }
    }
}