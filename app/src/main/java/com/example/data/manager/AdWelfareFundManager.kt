package com.example.data.manager

import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

/**
 * 3. Ad Welfare Fund Manager (AdWelfareFundManager)
 * Implements:
 * - Ad revenue collection logic to fund drivers' ₹25-50 Lakh group accidental insurance pool
 * - 100% ₹0 cost to drivers
 * - Transparent ledger of sponsor ad impressions and live welfare pool accumulation
 */
data class DriverInsuranceCoverage(
    val tierTitle: String,
    val sumInsuredAmount: Long, // 25,00,000 or 50,00,000
    val formattedSumInsured: String,
    val monthlyPremiumCovered: Double,
    val driverCost: Double = 0.0, // Always ₹0
    val accidentalDeathCoverage: String,
    val permanentDisabilityCoverage: String,
    val childEducationBenefit: String,
    val activeDriverCount: Int
)

data class AdImpressionRecord(
    val id: String,
    val sponsorName: String,
    val campaignTitle: String,
    val adCategory: String,
    val revenueAddedToPool: Double,
    val timestamp: Long = System.currentTimeMillis()
) {
    val formattedTime: String
        get() = SimpleDateFormat("dd MMM, hh:mm a", Locale.getDefault()).format(Date(timestamp))
}

class AdWelfareFundManager {

    private val _totalPoolFundAccumulated = MutableStateFlow(4285400.0)
    val totalPoolFundAccumulated: StateFlow<Double> = _totalPoolFundAccumulated.asStateFlow()

    private val _monthlyTargetFund = MutableStateFlow(500000.0)
    val monthlyTargetFund: StateFlow<Double> = _monthlyTargetFund.asStateFlow()

    private val _monthlyCollectedFund = MutableStateFlow(482650.0)
    val monthlyCollectedFund: StateFlow<Double> = _monthlyCollectedFund.asStateFlow()

    private val _activeEnrolledDrivers = MutableStateFlow(1840)
    val activeEnrolledDrivers: StateFlow<Int> = _activeEnrolledDrivers.asStateFlow()

    private val _totalAdImpressions = MutableStateFlow(142890)
    val totalAdImpressions: StateFlow<Int> = _totalAdImpressions.asStateFlow()

    private val _adLedger = MutableStateFlow<List<AdImpressionRecord>>(createInitialAdLedger())
    val adLedger: StateFlow<List<AdImpressionRecord>> = _adLedger.asStateFlow()

    // 2 Insurance Coverage Tiers funded at ₹0 cost
    val standardCoverageTier = DriverInsuranceCoverage(
        tierTitle = "Standard Driver Accidental Cover",
        sumInsuredAmount = 2500000L,
        formattedSumInsured = "₹25 Lakh",
        monthlyPremiumCovered = 185.0,
        driverCost = 0.0,
        accidentalDeathCoverage = "₹25,00,000 lump-sum to nominee",
        permanentDisabilityCoverage = "₹25,00,000 financial security",
        childEducationBenefit = "₹1,00,000 per child (up to 2 children)",
        activeDriverCount = 1290
    )

    val superCoverageTier = DriverInsuranceCoverage(
        tierTitle = "Highway & Night Haul Super Cover",
        sumInsuredAmount = 5000000L,
        formattedSumInsured = "₹50 Lakh",
        monthlyPremiumCovered = 320.0,
        driverCost = 0.0,
        accidentalDeathCoverage = "₹50,00,000 complete family safety",
        permanentDisabilityCoverage = "₹50,00,000 critical disability security",
        childEducationBenefit = "₹2,50,000 higher education corpus",
        activeDriverCount = 550
    )

    /**
     * Records ad view/click revenue contribution to the driver insurance pool.
     */
    fun recordAdContribution(
        sponsorName: String,
        campaignTitle: String,
        adCategory: String,
        amount: Double = 15.0
    ): AdImpressionRecord {
        val record = AdImpressionRecord(
            id = "AD${(1000..9999).random()}",
            sponsorName = sponsorName,
            campaignTitle = campaignTitle,
            adCategory = adCategory,
            revenueAddedToPool = amount,
            timestamp = System.currentTimeMillis()
        )

        _adLedger.value = listOf(record) + _adLedger.value.take(25)
        _totalPoolFundAccumulated.value += amount
        _monthlyCollectedFund.value += amount
        _totalAdImpressions.value += 1

        return record
    }

    private fun createInitialAdLedger(): List<AdImpressionRecord> {
        val now = System.currentTimeMillis()
        return listOf(
            AdImpressionRecord(
                id = "AD9012",
                sponsorName = "Mahindra Tractors & Commercial",
                campaignTitle = "Bolero Maxi Truck Fleet Upgrade",
                adCategory = "Automotive / Loading",
                revenueAddedToPool = 25.0,
                timestamp = now - (12 * 60 * 1000)
            ),
            AdImpressionRecord(
                id = "AD8741",
                sponsorName = "Apollo Tyres India",
                campaignTitle = "Heavy Load Radial Tyres 5-Year Warranty",
                adCategory = "Logistics Equipment",
                revenueAddedToPool = 20.0,
                timestamp = now - (45 * 60 * 1000)
            ),
            AdImpressionRecord(
                id = "AD7230",
                sponsorName = "Castrol Agri & Heavy Duty Lubricants",
                campaignTitle = "High Mileage Engine Oil for Tempos",
                adCategory = "Maintenance & Spares",
                revenueAddedToPool = 18.0,
                timestamp = now - (90 * 60 * 1000)
            ),
            AdImpressionRecord(
                id = "AD6129",
                sponsorName = "IFFCO Kisan Agri Implements",
                campaignTitle = "Direct Mandi Transport Support 2026",
                adCategory = "Farming & Mandi",
                revenueAddedToPool = 30.0,
                timestamp = now - (140 * 60 * 1000)
            )
        )
    }

    companion object {
        @Volatile
        private var instance: AdWelfareFundManager? = null

        fun getInstance(): AdWelfareFundManager {
            return instance ?: synchronized(this) {
                instance ?: AdWelfareFundManager().also { instance = it }
            }
        }
    }
}
