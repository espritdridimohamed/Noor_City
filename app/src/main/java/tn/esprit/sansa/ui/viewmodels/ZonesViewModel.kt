package tn.esprit.sansa.ui.viewmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import tn.esprit.sansa.data.repositories.FirebaseZonesRepository
import tn.esprit.sansa.data.repositories.GeocodingResult
import tn.esprit.sansa.ui.screens.models.Zone
import tn.esprit.sansa.ui.screens.models.ZoneStatus
import tn.esprit.sansa.ui.screens.models.ZoneType

class ZonesViewModel : ViewModel() {
    private val repository = FirebaseZonesRepository()

    private val _zones = MutableStateFlow<List<Zone>>(emptyList())
    val zones: StateFlow<List<Zone>> = _zones

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading

    private val _geocodingResults = MutableStateFlow<List<GeocodingResult>>(emptyList())
    val geocodingResults: StateFlow<List<GeocodingResult>> = _geocodingResults

    init {
        loadZones()
    }

    fun loadZones() {
        viewModelScope.launch {
            _isLoading.value = true
            repository.getZones().collect { 
                _zones.value = it
                _isLoading.value = false
            }
        }
    }

    fun addZone(zone: Zone, onComplete: (Boolean) -> Unit) {
        repository.addZone(zone, onComplete)
    }

    fun deleteZone(id: String, onComplete: (Boolean) -> Unit) {
        repository.deleteZone(id, onComplete)
    }

    fun searchLocation(query: String) {
        if (query.length < 3) {
            _geocodingResults.value = emptyList()
            return
        }
        viewModelScope.launch {
            val results = repository.searchLocation(query)
            _geocodingResults.value = results
        }
    }

    fun clearGeocodingResults() {
        _geocodingResults.value = emptyList()
    }

    fun refresh() {
        loadZones()
    }
}
