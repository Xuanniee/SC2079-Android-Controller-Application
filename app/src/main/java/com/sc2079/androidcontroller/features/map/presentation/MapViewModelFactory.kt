package com.sc2079.androidcontroller.features.map.presentation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.sc2079.androidcontroller.features.map.domain.repository.MapRepository

class MapViewModelFactory(
    private val repo: MapRepository
) : ViewModelProvider.Factory {
    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        require(modelClass.isAssignableFrom(MapViewModel::class.java))
        return MapViewModel(repo) as T
    }
}
