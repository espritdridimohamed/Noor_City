package tn.esprit.sansa.ui.viewmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import tn.esprit.sansa.data.repositories.FirebaseStreetlightsRepository
import tn.esprit.sansa.ui.screens.models.Streetlight

enum class PaymentStatus { INITIAL, COLLECTING_CARD_INFO, PENDING, SUCCESS, FAILED }

class NoorChargeViewModel : ViewModel() {
    private val repository = FirebaseStreetlightsRepository()

    private val _paymentStatus = MutableStateFlow(PaymentStatus.INITIAL)
    val paymentStatus: StateFlow<PaymentStatus> = _paymentStatus.asStateFlow()

    private val _isCharging = MutableStateFlow(false)
    val isCharging: StateFlow<Boolean> = _isCharging.asStateFlow()

    private val _energyConsumed = MutableStateFlow(0.0)
    val energyConsumed: StateFlow<Double> = _energyConsumed.asStateFlow()

    private val _timeRemaining = MutableStateFlow(0) // Seconds
    val timeRemaining: StateFlow<Int> = _timeRemaining.asStateFlow()

    private var simulationJob: Job? = null

    /**
     * Initie le flux Stripe en demandant les infos de carte
     */
    fun initiatePayment() {
        _paymentStatus.value = PaymentStatus.COLLECTING_CARD_INFO
    }

    /**
     * Simulation du paiement Stripe avec validation fictive
     */
    fun processStripePayment(amount: Double, cardNumber: String, onPaymentSuccess: () -> Unit) {
        viewModelScope.launch {
            _paymentStatus.value = PaymentStatus.PENDING
            delay(3000) // Simulation du temps de transaction Stripe
            
            // Simulation du succès
            if (cardNumber.replace(" ", "").length >= 16) {
                _paymentStatus.value = PaymentStatus.SUCCESS
                onPaymentSuccess()
            } else {
                _paymentStatus.value = PaymentStatus.FAILED
            }
        }
    }

    /**
     * Démarre la session de charge (Physiquement active le Servo sur l'ESP32 via Firebase)
     */
    fun startChargingSession(streetlight: Streetlight, durationMinutes: Int) {
        _isCharging.value = true
        _timeRemaining.value = durationMinutes * 60
        _energyConsumed.value = 0.0

        // Informer Firebase (L'ESP32 écoutera ce changement pour tourner le servo)
        repository.updateChargingStatus(streetlight.id, true)

        // Démarrer la simulation de consommation d'énergie
        simulationJob?.cancel()
        simulationJob = viewModelScope.launch {
            while (_timeRemaining.value > 0 && _isCharging.value) {
                delay(1000)
                _timeRemaining.value -= 1
                
                // Simuler une consommation réaliste (ex: 0.5Wh par seconde pour une trottinette)
                _energyConsumed.value += 0.5
                
                // Mettre à jour Firebase pour que l'Admin voit la charge en direct
                if (_timeRemaining.value % 5 == 0) { // Update Firebase every 5s to save quota
                    repository.updateChargingStatus(streetlight.id, true, _energyConsumed.value)
                }
            }
            stopChargingSession(streetlight)
        }
    }

    fun stopChargingSession(streetlight: Streetlight) {
        _isCharging.value = false
        simulationJob?.cancel()
        repository.updateChargingStatus(streetlight.id, false, _energyConsumed.value, "AVAILABLE")
    }

    fun resetPayment() {
        _paymentStatus.value = PaymentStatus.INITIAL
    }
}
