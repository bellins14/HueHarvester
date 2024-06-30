package com.example.hueharvester.database

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.launch
import androidx.lifecycle.LiveData
import androidx.lifecycle.asLiveData

/**
 * View Model to keep a reference to the color data repository and
 * an up-to-date list of all color data.
 * @param repository The color data repository
 * @property allColorData The list of all color data
 * @constructor Creates a new view model with the given repository
 * @method insert Inserts a new color data into the repository
 * @method deleteOldData Deletes all color data with an ID less than the given start ID
 * @see ColorDataRepository
 */
class ColorDataViewModel(private val repository: ColorDataRepository) : ViewModel() {

    val allColorData: LiveData<List<ColorData>> = repository.allColorData.asLiveData()
    /**
    * Launching a new coroutine to insert the data in a non-blocking way
    */
    fun insert(colorData: ColorData) = viewModelScope.launch {
        repository.insert(colorData)
    }

    fun deleteOldData(startId: Int) = viewModelScope.launch {
        repository.deleteOldData(startId)
    }
}

/**
 * Factory for creating a ColorDataViewModel with a repository
 * @param repository The color data repository
 * @constructor Creates a new factory with the given repository
 * @method create Creates a new ColorDataViewModel with the given repository
 * @throws IllegalArgumentException If the model class is not ColorDataViewModel
 * @return The created ColorDataViewModel
 * @see ColorDataViewModel
 */
class ColorDataViewModelFactory(private val repository: ColorDataRepository) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(ColorDataViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return ColorDataViewModel(repository) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}
