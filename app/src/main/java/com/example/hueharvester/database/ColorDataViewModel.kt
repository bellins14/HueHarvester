package com.example.hueharvester.database

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.launch
import androidx.lifecycle.LiveData
import androidx.lifecycle.asLiveData

class ColorDataViewModel(private val repository: ColorDataRepository) : ViewModel() {
    val allColorData: LiveData<List<ColorData>> = repository.allColorData.asLiveData()
    var lastColorData: ColorData? = null
    /**
    * Launching a new coroutine to insert the data in a non-blocking way
    */
    fun insert(colorData: ColorData) = viewModelScope.launch {
        repository.insert(colorData)
    }

    /*fun getDataAfter(startId: Int) = viewModelScope.launch {
        repository.getDataAfter(startId)
    }

    fun getLastInsertedData() = viewModelScope.launch {
        lastColorData = repository.getLastInsertedData()
    }
*/
    fun deleteOldData(startId: Int) = viewModelScope.launch {
        repository.deleteOldData(startId)
    }
}

class ColorDataViewModelFactory(private val repository: ColorDataRepository) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(ColorDataViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return ColorDataViewModel(repository) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}
