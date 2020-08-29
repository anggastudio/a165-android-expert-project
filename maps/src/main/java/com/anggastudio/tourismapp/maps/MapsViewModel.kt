package com.anggastudio.tourismapp.maps

import androidx.lifecycle.ViewModel
import androidx.lifecycle.asLiveData
import com.anggastudio.tourismapp.core.domain.usecase.TourismUseCase

class MapsViewModel(tourismUseCase: TourismUseCase) : ViewModel() {
    val tourism = tourismUseCase.getAllTourism().asLiveData()
}