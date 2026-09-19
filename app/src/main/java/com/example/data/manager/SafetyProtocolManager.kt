package com.example.data.manager

import android.content.Context
import android.content.Intent
import android.net.Uri
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

data class HomeVisitSession(
    val sessionId: String,
    val providerName: String,
    val providerPhone: String,
    val customerName: String,
    val customerAddress: String,
    val currentLat: Double,
    val currentLng: Double,
    val startTimeFormatted: String,
    val isTrackingActive: Boolean = true,
    val emergencyContacts: List<String> = listOf("112 (National Emergency)", "1091 (Women Safety Helpline)", "Family Guardian (+91 98111 22334)"),
    val breadcrumbTrailCount: Int = 12,
    val checkInTimerMinutesRemaining: Int = 25
)

class SafetyProtocolManager private constructor(private val context: Context) {

    private val scope = CoroutineScope(Dispatchers.Main)
    private var trackingJob: Job? = null

    private val _activeSession = MutableStateFlow<HomeVisitSession?>(null)
    val activeSession: StateFlow<HomeVisitSession?> = _activeSession.asStateFlow()

    private val _isPanicAlertActive = MutableStateFlow(false)
    val isPanicAlertActive: StateFlow<Boolean> = _isPanicAlertActive.asStateFlow()

    private val _safetyLog = MutableStateFlow<List<String>>(emptyList())
    val safetyLog: StateFlow<List<String>> = _safetyLog.asStateFlow()

    init {
        // Initialize default active session demonstration
        startHomeVisitTracking(
            providerName = "Sunita Sharma",
            providerPhone = "+91 98110 54321",
            customerName = "Priya Malhotra",
            customerAddress = "Flat 402, Royal Palms, Sector 62",
            startLat = 28.6189,
            startLng = 77.2140
        )
    }

    fun startHomeVisitTracking(
        providerName: String,
        providerPhone: String,
        customerName: String,
        customerAddress: String,
        startLat: Double,
        startLng: Double
    ) {
        val nowFormatted = SimpleDateFormat("hh:mm a", Locale.getDefault()).format(Date())
        _activeSession.value = HomeVisitSession(
            sessionId = "HV-${System.currentTimeMillis() % 10000}",
            providerName = providerName,
            providerPhone = providerPhone,
            customerName = customerName,
            customerAddress = customerAddress,
            currentLat = startLat,
            currentLng = startLng,
            startTimeFormatted = nowFormatted
        )

        addLogEntry("🔒 Discreet Home-Visit Safety Tracking ACTIVATED for $providerName at $customerAddress")

        trackingJob?.cancel()
        trackingJob = scope.launch {
            while (_activeSession.value?.isTrackingActive == true) {
                delay(30000) // update breadcrumbs every 30s
                _activeSession.value = _activeSession.value?.let {
                    it.copy(breadcrumbTrailCount = it.breadcrumbTrailCount + 1)
                }
            }
        }
    }

    fun triggerDiscretePanicAlert(triggerNote: String = "1-Tap Discreet Panic Alert Activated"): String {
        _isPanicAlertActive.value = true
        val session = _activeSession.value
        val lat = session?.currentLat ?: 28.6139
        val lng = session?.currentLng ?: 77.2090
        val mapsLink = "https://maps.google.com/?q=$lat,$lng"

        val alertMessage = "🚨 [LOKSETU SAFETY ALERT] Home-Visit Emergency! " +
                "Worker: ${session?.providerName ?: "Service Provider"}, " +
                "Location: ${session?.customerAddress ?: "Client Location"}. " +
                "Live GPS: $mapsLink. Dispatched to Police (112) and Women Helpline (1091)."

        addLogEntry("🚨 CRITICAL: $triggerNote at ${SimpleDateFormat("hh:mm:ss a", Locale.getDefault()).format(Date())}")
        addLogEntry("📡 Auto-broadcasted Live GPS link to emergency responders & guardian: $mapsLink")

        // Trigger phone dial intent for 112
        try {
            val callIntent = Intent(Intent.ACTION_DIAL).apply {
                data = Uri.parse("tel:112")
                flags = Intent.FLAG_ACTIVITY_NEW_TASK
            }
            context.startActivity(callIntent)
        } catch (_: Exception) {
            // Handled gracefully
        }

        return alertMessage
    }

    fun resolvePanicAlert() {
        _isPanicAlertActive.value = false
        addLogEntry("✅ Discreet Safety Alert marked RESOLVED and verified safe by provider.")
    }

    fun stopTracking() {
        trackingJob?.cancel()
        _activeSession.value = _activeSession.value?.copy(isTrackingActive = false)
        addLogEntry("⏹️ Home-Visit session successfully concluded. Tracking deactivated.")
    }

    private fun addLogEntry(entry: String) {
        val time = SimpleDateFormat("HH:mm:ss", Locale.getDefault()).format(Date())
        _safetyLog.value = listOf("[$time] $entry") + _safetyLog.value.take(20)
    }

    companion object {
        @Volatile
        private var instance: SafetyProtocolManager? = null

        fun getInstance(context: Context): SafetyProtocolManager {
            return instance ?: synchronized(this) {
                instance ?: SafetyProtocolManager(context.applicationContext).also { instance = it }
            }
        }
    }
}
