package com.example.myapplication.ui.viewmodel

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.example.myapplication.data.PlacesRepository
import com.example.myapplication.domain.model.HyperMarket
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

@HiltViewModel
class MainActivityViewModel @Inject constructor(private val placesRepository: PlacesRepository) :
    ViewModel() {

    private val _placesLiveData by lazy { MutableLiveData<List<HyperMarket>>() }
    val placesLiveData = _placesLiveData

    fun getPlaces() {
        _placesLiveData.value = placesRepository.getPlaces()
    }

}