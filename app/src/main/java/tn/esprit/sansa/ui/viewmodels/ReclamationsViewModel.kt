package tn.esprit.sansa.ui.viewmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import tn.esprit.sansa.data.repositories.FirebaseReclamationsRepository
import tn.esprit.sansa.ui.screens.models.Reclamation

import tn.esprit.sansa.data.repositories.FirebaseStreetlightsRepository
import tn.esprit.sansa.ui.screens.models.*

class ReclamationsViewModel(
    private val repository: FirebaseReclamationsRepository = FirebaseReclamationsRepository(),
    private val streetlightRepository: FirebaseStreetlightsRepository = FirebaseStreetlightsRepository()
) : ViewModel() {

    private val _reclamations = MutableStateFlow<List<Reclamation>>(emptyList())
    val reclamations: StateFlow<List<Reclamation>> = _reclamations.asStateFlow()

    private val _isLoading = MutableStateFlow(true)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    private val _allStreetlights = MutableStateFlow<List<Streetlight>>(emptyList())
    val allStreetlights: StateFlow<List<Streetlight>> = _allStreetlights.asStateFlow()

    init {
        fetchReclamations()
        fetchStreetlights()
    }

    private fun fetchStreetlights() {
        viewModelScope.launch {
            streetlightRepository.getStreetlights().collect {
                _allStreetlights.value = it
            }
        }
    }

    fun fetchReclamations() {
        viewModelScope.launch {
            _isLoading.value = true
            repository.getReclamations().collect { list ->
                _reclamations.value = list
                _isLoading.value = false
            }
        }
    }

    fun addReclamation(reclamation: Reclamation, onComplete: (Boolean) -> Unit) {
        repository.addReclamation(reclamation, onComplete)
    }

    fun deleteReclamation(id: String, onComplete: (Boolean) -> Unit = {}) {
        repository.deleteReclamation(id, onComplete)
    }

    fun refresh() {
        fetchReclamations()
    }
}
