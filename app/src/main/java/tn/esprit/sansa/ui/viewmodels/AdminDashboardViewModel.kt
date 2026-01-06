package tn.esprit.sansa.ui.viewmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.*
import tn.esprit.sansa.data.repositories.FirebaseReclamationsRepository
import tn.esprit.sansa.data.repositories.FirebaseStreetlightsRepository
import tn.esprit.sansa.ui.screens.models.*

data class DashboardStats(
    val totalStreetlights: Int = 0,
    val malfunctioningLights: Int = 0,
    val activeReclamations: Int = 0,
    val isLoading: Boolean = true
)

class AdminDashboardViewModel(
    streetlightRepository: FirebaseStreetlightsRepository = FirebaseStreetlightsRepository(),
    reclamationRepository: FirebaseReclamationsRepository = FirebaseReclamationsRepository()
) : ViewModel() {

    private val streetlightsFlow = streetlightRepository.getStreetlights()
        .onStart { emit(emptyList()) }
        .catch { emit(emptyList()) }

    private val reclamationsFlow = reclamationRepository.getReclamations()
        .onStart { emit(emptyList()) }
        .catch { emit(emptyList()) }

    val stats: StateFlow<DashboardStats> = combine(
        streetlightsFlow,
        reclamationsFlow
    ) { lights, recs ->
        // Filtrage des entrées potentiellement corrompues ou vides
        val validLights = lights.filter { it.id.isNotBlank() }
        val validRecs = recs.filter { it.id.isNotBlank() }

        DashboardStats(
            totalStreetlights = validLights.size,
            malfunctioningLights = validLights.count { it.status == StreetlightStatus.ERROR },
            activeReclamations = validRecs.count { 
                it.status == ReclamationStatus.PENDING || it.status == ReclamationStatus.IN_PROGRESS 
            },
            isLoading = false
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = DashboardStats()
    )
}
