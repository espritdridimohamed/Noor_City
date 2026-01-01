package tn.esprit.sansa.ui.viewmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import tn.esprit.sansa.data.repositories.FirebaseLightingRepository
import tn.esprit.sansa.data.repositories.FirebaseStreetlightsRepository
import tn.esprit.sansa.ui.screens.models.LightingProgram
import tn.esprit.sansa.ui.screens.models.Streetlight

class LightingProgramsViewModel : ViewModel() {
    private val lightingRepository = FirebaseLightingRepository()
    private val streetlightRepository = FirebaseStreetlightsRepository()

    private val _programs = MutableStateFlow<List<LightingProgram>>(emptyList())
    val programs: StateFlow<List<LightingProgram>> = _programs.asStateFlow()

    private val _streetlights = MutableStateFlow<List<Streetlight>>(emptyList())
    val streetlights: StateFlow<List<Streetlight>> = _streetlights.asStateFlow()

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    init {
        loadData()
    }

    private fun loadData() {
        viewModelScope.launch {
            _isLoading.value = true
            launch {
                lightingRepository.getPrograms().collect {
                    _programs.value = it
                    _isLoading.value = false
                }
            }
            launch {
                streetlightRepository.getStreetlights().collect {
                    _streetlights.value = it
                }
            }
        }
    }

    fun addProgram(program: LightingProgram, onComplete: (Boolean) -> Unit) {
        viewModelScope.launch {
            lightingRepository.addProgram(program, onComplete)
        }
    }

    fun deleteProgram(id: String) {
        viewModelScope.launch {
            lightingRepository.deleteProgram(id) { }
        }
    }
}
