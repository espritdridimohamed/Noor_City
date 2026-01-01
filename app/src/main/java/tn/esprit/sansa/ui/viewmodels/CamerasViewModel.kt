package tn.esprit.sansa.ui.viewmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import tn.esprit.sansa.data.repositories.FirebaseCamerasRepository
import tn.esprit.sansa.ui.screens.models.Camera

class CamerasViewModel : ViewModel() {
    private val repository = FirebaseCamerasRepository()

    private val _cameras = MutableStateFlow<List<Camera>>(emptyList())
    val cameras: StateFlow<List<Camera>> = _cameras.asStateFlow()

    private val _isLoading = MutableStateFlow(true)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    init {
        fetchCameras()
    }

    fun refresh() {
        fetchCameras()
    }

    private fun fetchCameras() {
        viewModelScope.launch {
            _isLoading.value = true
            repository.getCameras().collect { cameraList ->
                _cameras.value = cameraList
                _isLoading.value = false
            }
        }
    }

    fun addCamera(camera: Camera, onComplete: (Boolean) -> Unit) {
        viewModelScope.launch {
            repository.addCamera(camera, onComplete)
        }
    }

    fun deleteCamera(id: String) {
        viewModelScope.launch {
            repository.deleteCamera(id)
        }
    }
}
