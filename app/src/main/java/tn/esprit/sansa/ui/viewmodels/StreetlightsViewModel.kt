package tn.esprit.sansa.ui.viewmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import tn.esprit.sansa.data.repositories.FirebaseStreetlightsRepository
import tn.esprit.sansa.data.repositories.FirebaseZonesRepository
import tn.esprit.sansa.ui.screens.models.Streetlight
import tn.esprit.sansa.ui.screens.models.Zone

class StreetlightsViewModel : ViewModel() {
    private val streetlightRepository = FirebaseStreetlightsRepository()
    private val zoneRepository = FirebaseZonesRepository()

    private val _streetlights = MutableStateFlow<List<Streetlight>>(emptyList())
    val streetlights: StateFlow<List<Streetlight>> = _streetlights.asStateFlow()

    private val _zones = MutableStateFlow<List<Zone>>(emptyList())
    val zones: StateFlow<List<Zone>> = _zones.asStateFlow()

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    init {
        loadData()
    }

    private fun loadData() {
        viewModelScope.launch {
            _isLoading.value = true
            // Load streetlights
            launch {
                 streetlightRepository.getStreetlights().collect {
                    _streetlights.value = it
                    _isLoading.value = false
                }
            }
            // Load zones for the addition screen
            launch {
                zoneRepository.getZones().collect {
                    _zones.value = it
                }
            }
        }
    }

    fun addStreetlight(streetlight: Streetlight, onComplete: (Boolean) -> Unit) {
        viewModelScope.launch {
            streetlightRepository.addStreetlight(streetlight, onComplete)
        }
    }

    fun updateStreetlight(streetlight: Streetlight, onComplete: (Boolean) -> Unit) {
        viewModelScope.launch {
            streetlightRepository.addStreetlight(streetlight, onComplete)
        }
    }

    fun deleteStreetlight(id: String) {
        viewModelScope.launch {
            streetlightRepository.deleteStreetlight(id) { }
        }
    }

    fun getStreetlightById(id: String): Streetlight? {
        return _streetlights.value.find { it.id == id }
    }

    /**
     * Generate next sequential streetlight ID (format: L001, L002, etc.)
     */
    suspend fun generateNextStreetlightId(): String {
        // Fetch fresh data from Firebase to ensure we have the latest IDs
        val allStreetlights = streetlightRepository.getAllStreetlightsOnce()
        val existingIds = allStreetlights
            .mapNotNull { it.id.removePrefix("L").toIntOrNull() }
            .maxOrNull() ?: 0
        
        val nextNumber = existingIds + 1
        return "L${nextNumber.toString().padStart(3, '0')}"
    }
}
