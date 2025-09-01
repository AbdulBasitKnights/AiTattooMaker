package com.basit.aitattoomaker.presentation.ai_tools

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.basit.aitattoomaker.data.repo.TattooRepository
import com.basit.aitattoomaker.presentation.ai_tools.model.CameraTattoo
import com.basit.aitattoomaker.presentation.application.AppController
import com.basit.aitattoomaker.presentation.utils.AppUtils
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import javax.inject.Inject

@HiltViewModel
class AiToolsViewModel@Inject constructor(
    private val getApplication: Application,
    private val repo: TattooRepository

) : ViewModel() {
    private val _library = MutableLiveData<List<CameraTattoo>>()
    val library: LiveData<List<CameraTattoo>> get() = _library

    private val _history = MutableLiveData<List<CameraTattoo>>()
    val history: LiveData<List<CameraTattoo>> get() = _history

    init {
        loadTattoos()
    }

     fun loadTattoos() {
        viewModelScope.launch(Dispatchers.IO) {
            val data = AppUtils.loadTattoos(getApplication)
            withContext(Dispatchers.Main) {
                _library.value = data?.library ?: emptyList()
                _history.value = data?.history ?: emptyList()
            }
        }
    }
}