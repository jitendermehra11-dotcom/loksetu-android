package com.example.data.manager

import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.UUID

enum class EscrowBookingStatus(val label: String, val hindiLabel: String) {
    PAYMENT_LOCKED("Escrow Locked (100%)", "100% एस्क्रो में सुरक्षित"),
    PROVIDER_EN_ROUTE("Provider En Route", "प्रोफेशनल रास्ते में हैं"),
    WORK_IN_PROGRESS("Work In Progress", "कार्य प्रगति पर है"),
    COMPLETED("Service Completed", "सेवा पूर्ण - भुगतान जारी"),
    DISPUTED("Dispute Raised", "विवाद दर्ज - समीक्षा जारी")
}

data class ServiceEscrowBooking(
    val id: String = UUID.randomUUID().toString().take(8).uppercase(),
    val providerId: Long,
    val providerName: String,
    val providerPhone: String,
    val providerCategory: String,
    val customerName: String,
    val customerPhone: String,
    val customerAddress: String,
    val serviceFee: Double,
    val travelAllowance: Double,
    val totalLockedAmount: Double = serviceFee + travelAllowance,
    val safetyDeposit: Double = 0.0,
    val status: EscrowBookingStatus = EscrowBookingStatus.PAYMENT_LOCKED,
    val createdAtFormatted: String = SimpleDateFormat("dd MMM, hh:mm a", Locale.getDefault()).format(Date()),
    val completionOtp: String = ((1000..9999).random()).toString(),
    val isOtpVerified: Boolean = false,
    val providerUpiId: String = "provider@upi",
    val transactionNote: String = "100% Upfront Escrow Locked before provider travel"
)

class ServiceEscrowManager private constructor() {

    private val _bookings = MutableStateFlow<List<ServiceEscrowBooking>>(emptyList())
    val bookings: StateFlow<List<ServiceEscrowBooking>> = _bookings.asStateFlow()

    private val _totalEscrowLocked = MutableStateFlow(0.0)
    val totalEscrowLocked: StateFlow<Double> = _totalEscrowLocked.asStateFlow()

    private val _totalDisbursedToProviders = MutableStateFlow(0.0)
    val totalDisbursedToProviders: StateFlow<Double> = _totalDisbursedToProviders.asStateFlow()

    init {
        seedInitialBookings()
    }

    private fun seedInitialBookings() {
        val seed = listOf(
            ServiceEscrowBooking(
                id = "SE-8821",
                providerId = 201L,
                providerName = "Sunita Sharma",
                providerPhone = "+91 98110 54321",
                providerCategory = "HOME_BEAUTY",
                customerName = "Priya Malhotra",
                customerPhone = "+91 98765 11223",
                customerAddress = "Flat 402, Royal Palms Society, Sector 62",
                serviceFee = 1200.0,
                travelAllowance = 150.0,
                status = EscrowBookingStatus.PROVIDER_EN_ROUTE,
                completionOtp = "4812",
                providerUpiId = "sunita.beauty@okhdfcbank"
            ),
            ServiceEscrowBooking(
                id = "SE-8822",
                providerId = 202L,
                providerName = "Sister Anjali Thomas",
                providerPhone = "+91 98230 67890",
                providerCategory = "HOME_HEALTHCARE",
                customerName = "Col. R.K. Varma",
                customerPhone = "+91 99112 33445",
                customerAddress = "B-12, Officers Enclave, Cantonment Road",
                serviceFee = 1800.0,
                travelAllowance = 200.0,
                status = EscrowBookingStatus.WORK_IN_PROGRESS,
                completionOtp = "7391",
                providerUpiId = "anjali.nursing@oksbi"
            )
        )
        _bookings.value = seed
        recalculateTotals()
    }

    fun lockUpfrontEscrowBooking(
        providerId: Long,
        providerName: String,
        providerPhone: String,
        providerCategory: String,
        customerName: String,
        customerPhone: String,
        customerAddress: String,
        serviceFee: Double,
        travelAllowance: Double = 150.0,
        providerUpiId: String = "provider@upi"
    ): ServiceEscrowBooking {
        val newBooking = ServiceEscrowBooking(
            providerId = providerId,
            providerName = providerName,
            providerPhone = providerPhone,
            providerCategory = providerCategory,
            customerName = customerName,
            customerPhone = customerPhone,
            customerAddress = customerAddress,
            serviceFee = serviceFee,
            travelAllowance = travelAllowance,
            status = EscrowBookingStatus.PAYMENT_LOCKED,
            providerUpiId = providerUpiId
        )
        _bookings.value = listOf(newBooking) + _bookings.value
        recalculateTotals()
        return newBooking
    }

    fun updateBookingStatus(bookingId: String, newStatus: EscrowBookingStatus): Boolean {
        var updated = false
        _bookings.value = _bookings.value.map { booking ->
            if (booking.id == bookingId) {
                updated = true
                booking.copy(status = newStatus)
            } else booking
        }
        if (newStatus == EscrowBookingStatus.COMPLETED) {
            val completedBooking = _bookings.value.find { it.id == bookingId }
            if (completedBooking != null) {
                _totalDisbursedToProviders.value += completedBooking.totalLockedAmount
            }
        }
        recalculateTotals()
        return updated
    }

    fun verifyOtpAndDisburse(bookingId: String, enteredOtp: String): Pair<Boolean, String> {
        val booking = _bookings.value.find { it.id == bookingId }
            ?: return Pair(false, "Booking not found")

        if (booking.status == EscrowBookingStatus.COMPLETED) {
            return Pair(true, "Already settled and disbursed to ${booking.providerName}")
        }

        if (booking.completionOtp == enteredOtp.trim()) {
            _bookings.value = _bookings.value.map {
                if (it.id == bookingId) {
                    it.copy(
                        status = EscrowBookingStatus.COMPLETED,
                        isOtpVerified = true,
                        transactionNote = "Instant 100% Escrow disbursed to UPI ${it.providerUpiId}"
                    )
                } else it
            }
            _totalDisbursedToProviders.value += booking.totalLockedAmount
            recalculateTotals()
            return Pair(true, "₹${booking.totalLockedAmount.toInt()} successfully released to ${booking.providerName} via UPI!")
        } else {
            return Pair(false, "Incorrect 4-Digit OTP. Please ask customer to confirm.")
        }
    }

    private fun recalculateTotals() {
        val activeLocked = _bookings.value
            .filter { it.status != EscrowBookingStatus.COMPLETED }
            .sumOf { it.totalLockedAmount }
        _totalEscrowLocked.value = activeLocked
    }

    companion object {
        @Volatile
        private var instance: ServiceEscrowManager? = null

        fun getInstance(): ServiceEscrowManager {
            return instance ?: synchronized(this) {
                instance ?: ServiceEscrowManager().also { instance = it }
            }
        }
    }
}
