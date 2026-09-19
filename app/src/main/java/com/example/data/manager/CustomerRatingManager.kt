package com.example.data.manager

import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.UUID

enum class DisputeFlagType(val displayName: String, val severity: String) {
    NON_PAYMENT("Payment Refusal / Bad UPI", "HIGH"),
    UNSAFE_BEHAVIOR("Harassment / Misbehavior / Threat", "CRITICAL"),
    EXTRA_UNPAID_WORK("Demanding Unpaid Heavy Labor", "MEDIUM"),
    WRONG_ADDRESS_DELAY("Fake Address / Hostile Environment", "MEDIUM"),
    EXCELLENT_CLIENT("Respectful & Immediate Payout", "POSITIVE")
}

data class CustomerReviewReport(
    val id: String = UUID.randomUUID().toString().take(8),
    val customerPhone: String,
    val customerName: String,
    val providerName: String,
    val providerRole: String,
    val ratingScore: Int, // 1 to 5
    val flagType: DisputeFlagType,
    val feedbackNote: String,
    val reportedDateFormatted: String = SimpleDateFormat("dd MMM yyyy", Locale.getDefault()).format(Date()),
    val isBlacklisted: Boolean = false
)

data class CustomerSafetyProfile(
    val customerPhone: String,
    val customerName: String,
    val totalRatingsReceived: Int,
    val averageSafetyScore: Float,
    val nonPaymentFlagsCount: Int,
    val harassmentFlagsCount: Int,
    val isPlatformBlocked: Boolean,
    val blockReason: String? = null
)

class CustomerRatingManager private constructor() {

    private val _reviews = MutableStateFlow<List<CustomerReviewReport>>(emptyList())
    val reviews: StateFlow<List<CustomerReviewReport>> = _reviews.asStateFlow()

    private val _customerProfiles = MutableStateFlow<Map<String, CustomerSafetyProfile>>(emptyMap())
    val customerProfiles: StateFlow<Map<String, CustomerSafetyProfile>> = _customerProfiles.asStateFlow()

    private val _blockedCustomersCount = MutableStateFlow(0)
    val blockedCustomersCount: StateFlow<Int> = _blockedCustomersCount.asStateFlow()

    init {
        seedInitialReviewsAndProfiles()
    }

    private fun seedInitialReviewsAndProfiles() {
        val initialReviews = listOf(
            CustomerReviewReport(
                customerPhone = "+91 99119 00112",
                customerName = "Rakesh Verma (Repeat Offender)",
                providerName = "Sunita Sharma",
                providerRole = "Home Beautician",
                ratingScore = 1,
                flagType = DisputeFlagType.UNSAFE_BEHAVIOR,
                feedbackNote = "Aggressive tone and refused to let provider exit until police helpline was mentioned.",
                isBlacklisted = true
            ),
            CustomerReviewReport(
                customerPhone = "+91 99119 00112",
                customerName = "Rakesh Verma (Repeat Offender)",
                providerName = "Radha Devi",
                providerRole = "Domestic Helper",
                ratingScore = 1,
                flagType = DisputeFlagType.NON_PAYMENT,
                feedbackNote = "Refused ₹500 deep cleaning payment and falsely claimed items damaged.",
                isBlacklisted = true
            ),
            CustomerReviewReport(
                customerPhone = "+91 98765 11223",
                customerName = "Priya Malhotra",
                providerName = "Sunita Sharma",
                providerRole = "Home Beautician",
                ratingScore = 5,
                flagType = DisputeFlagType.EXCELLENT_CLIENT,
                feedbackNote = "Courteous client, offered tea and verified escrow OTP instantly.",
                isBlacklisted = false
            )
        )

        _reviews.value = initialReviews
        recalculateProfiles()
    }

    fun submitProviderRatingForCustomer(
        customerPhone: String,
        customerName: String,
        providerName: String,
        providerRole: String,
        ratingScore: Int,
        flagType: DisputeFlagType,
        feedbackNote: String
    ): CustomerSafetyProfile {
        val newReport = CustomerReviewReport(
            customerPhone = customerPhone,
            customerName = customerName,
            providerName = providerName,
            providerRole = providerRole,
            ratingScore = ratingScore,
            flagType = flagType,
            feedbackNote = feedbackNote,
            isBlacklisted = flagType == DisputeFlagType.UNSAFE_BEHAVIOR || flagType == DisputeFlagType.NON_PAYMENT
        )

        _reviews.value = listOf(newReport) + _reviews.value
        recalculateProfiles()
        return _customerProfiles.value[customerPhone] ?: getCustomerSafetyProfile(customerPhone, customerName)
    }

    fun getCustomerSafetyProfile(customerPhone: String, defaultName: String = "Client"): CustomerSafetyProfile {
        val existing = _customerProfiles.value[customerPhone]
        if (existing != null) return existing

        return CustomerSafetyProfile(
            customerPhone = customerPhone,
            customerName = defaultName,
            totalRatingsReceived = 0,
            averageSafetyScore = 5.0f,
            nonPaymentFlagsCount = 0,
            harassmentFlagsCount = 0,
            isPlatformBlocked = false
        )
    }

    private fun recalculateProfiles() {
        val grouped = _reviews.value.groupBy { it.customerPhone }
        val newProfiles = mutableMapOf<String, CustomerSafetyProfile>()
        var blockedTotal = 0

        for ((phone, reportList) in grouped) {
            val avgScore = reportList.map { it.ratingScore }.average().toFloat()
            val nonPayCount = reportList.count { it.flagType == DisputeFlagType.NON_PAYMENT }
            val harassCount = reportList.count { it.flagType == DisputeFlagType.UNSAFE_BEHAVIOR }
            val clientName = reportList.firstOrNull()?.customerName ?: "Customer"

            // Automatic rule: 2 or more serious flags or severe harassment flags triggers immediate platform block
            val isBlocked = harassCount >= 1 || nonPayCount >= 2 || avgScore < 2.0f
            val reason = when {
                harassCount >= 1 -> "BLOCKED: Harassment / Safety violation reported by home service professional."
                nonPayCount >= 2 -> "BLOCKED: Repeated payment defaults and escrow breach."
                avgScore < 2.0f -> "BLOCKED: Safety trust score fell below platform threshold."
                else -> null
            }

            if (isBlocked) blockedTotal++

            newProfiles[phone] = CustomerSafetyProfile(
                customerPhone = phone,
                customerName = clientName,
                totalRatingsReceived = reportList.size,
                averageSafetyScore = avgScore,
                nonPaymentFlagsCount = nonPayCount,
                harassmentFlagsCount = harassCount,
                isPlatformBlocked = isBlocked,
                blockReason = reason
            )
        }

        _customerProfiles.value = newProfiles
        _blockedCustomersCount.value = blockedTotal
    }

    companion object {
        @Volatile
        private var instance: CustomerRatingManager? = null

        fun getInstance(): CustomerRatingManager {
            return instance ?: synchronized(this) {
                instance ?: CustomerRatingManager().also { instance = it }
            }
        }
    }
}
