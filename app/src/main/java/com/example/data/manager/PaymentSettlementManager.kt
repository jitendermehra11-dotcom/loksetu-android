package com.example.data.manager

import android.net.Uri
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.UUID
import kotlin.math.roundToInt

/**
 * 1. Payment & Settlement Manager
 * Implements:
 * - 3% platform fee deduction
 * - ₹1 micro-insurance contribution (group accidental & healthcare buffer)
 * - Instant 97% provider UPI payout breakdown logic upon job completion
 * - Dynamic Fuel-Linked Payout Index (Live Petrol, Diesel, CNG rates dynamically adjusting base per-km earnings)
 */
enum class SettlementStatus(val label: String) {
    PENDING("Pending Confirmation"),
    PROCESSING("Processing UPI Transfer"),
    SUCCESSFUL("Settled via Instant UPI"),
    FAILED("Payment Failed")
}

/**
 * Live Fuel Price Index configuration
 * Petrol: standard 2-wheelers & hyperlocal bike deliveries
 * Diesel: Tata Ace Chhota Hathi, pickup trucks & heavy loaders
 * CNG: auto rickshaws, eco vans & delivery tempos
 */
enum class FuelType(
    val displayName: String,
    val vehicleCategory: String,
    val unit: String,
    val baselinePrice: Double, // Benchmark rate (₹)
    val defaultCurrentPrice: Double, // Live rate today in NCR/local mandis (₹)
    val averageMileageKm: Double, // km per L or kg
    val basePerKmRate: Double, // Base driver rate per km (₹)
    val iconEmoji: String
) {
    PETROL("Petrol", "2-Wheelers & Bike Delivery", "₹/L", 90.00, 96.72, 35.0, 12.0, "🏍️"),
    DIESEL("Diesel", "Tata Ace, Pickup & Cargo Trucks", "₹/L", 80.00, 89.62, 14.0, 25.0, "🚚"),
    CNG("CNG", "Auto, Tempo & Eco Delivery", "₹/kg", 70.00, 76.59, 24.0, 16.0, "🛺");

    companion object {
        fun fromVehicleString(vehicle: String?): FuelType {
            if (vehicle == null) return PETROL
            val lower = vehicle.lowercase()
            return when {
                lower.contains("diesel") || lower.contains("ace") || lower.contains("truck") || lower.contains("pickup") -> DIESEL
                lower.contains("cng") || lower.contains("auto") || lower.contains("tempo") || lower.contains("rickshaw") -> CNG
                else -> PETROL
            }
        }
    }
}

data class FuelIndexState(
    val fuelType: FuelType = FuelType.PETROL,
    val currentPrice: Double = fuelType.defaultCurrentPrice,
    val baselinePrice: Double = fuelType.baselinePrice,
    val lastUpdated: String = "Live Mandi Fuel Index"
) {
    val priceDiff: Double
        get() = (currentPrice - baselinePrice).coerceAtLeast(0.0)

    val indexMultiplier: Double
        get() = if (baselinePrice > 0) ((currentPrice / baselinePrice) * 1000.0).roundToInt() / 1000.0 else 1.0

    val percentageChange: Double
        get() = if (baselinePrice > 0) (((currentPrice - baselinePrice) / baselinePrice) * 1000.0).roundToInt() / 10.0 else 0.0

    // Fuel surcharge per km = (Current Price - Baseline) / Average Mileage
    val fuelSurchargePerKm: Double
        get() = if (fuelType.averageMileageKm > 0) {
            val raw = priceDiff / fuelType.averageMileageKm
            (raw * 100.0).roundToInt() / 100.0
        } else 0.0

    val dynamicPerKmRate: Double
        get() = ((fuelType.basePerKmRate + fuelSurchargePerKm) * 100.0).roundToInt() / 100.0
}

data class FuelLinkedPayoutResult(
    val fuelType: FuelType,
    val distanceKm: Double,
    val baseFare: Double, // Fixed pickup / handling base
    val basePerKmRate: Double,
    val baseDistanceFare: Double,
    val fuelPrice: Double,
    val baselinePrice: Double,
    val fuelSurchargePerKm: Double,
    val totalFuelSurcharge: Double,
    val grossPayout: Double,
    val breakdown: PaymentBreakdown
)

data class PaymentBreakdown(
    val grossAmount: Double,
    val platformFeePercent: Double = 3.0,
    val platformFeeAmount: Double,
    val microInsuranceAmount: Double = 1.0,
    val netPayoutAmount: Double,
    val effectivePayoutPercent: Double
) {
    companion object {
        fun compute(grossAmount: Double): PaymentBreakdown {
            val safeGross = if (grossAmount <= 0.0) 0.0 else grossAmount
            val fee = (safeGross * 0.03 * 100.0).roundToInt() / 100.0
            val insurance = if (safeGross > 1.0) 1.0 else 0.0
            val net = (safeGross - fee - insurance).coerceAtLeast(0.0)
            val netRounded = (net * 100.0).roundToInt() / 100.0
            val effPercent = if (safeGross > 0.0) ((netRounded / safeGross) * 1000.0).roundToInt() / 10.0 else 97.0
            return PaymentBreakdown(
                grossAmount = safeGross,
                platformFeePercent = 3.0,
                platformFeeAmount = fee,
                microInsuranceAmount = insurance,
                netPayoutAmount = netRounded,
                effectivePayoutPercent = effPercent
            )
        }
    }
}

data class SettlementRecord(
    val id: String = UUID.randomUUID().toString().take(8).uppercase(),
    val providerId: Long,
    val providerName: String,
    val providerCategory: String,
    val providerUpiId: String,
    val jobDescription: String,
    val breakdown: PaymentBreakdown,
    val transactionReference: String,
    val status: SettlementStatus = SettlementStatus.SUCCESSFUL,
    val timestamp: Long = System.currentTimeMillis()
) {
    val formattedDate: String
        get() = SimpleDateFormat("dd MMM yyyy, hh:mm a", Locale.getDefault()).format(Date(timestamp))
}

class PaymentSettlementManager {

    private val _settlementHistory = MutableStateFlow<List<SettlementRecord>>(createInitialSettlementHistory())
    val settlementHistory: StateFlow<List<SettlementRecord>> = _settlementHistory.asStateFlow()

    private val _totalPlatformFeeCollected = MutableStateFlow(348.0)
    val totalPlatformFeeCollected: StateFlow<Double> = _totalPlatformFeeCollected.asStateFlow()

    private val _totalMicroInsurancePool = MutableStateFlow(116.0)
    val totalMicroInsurancePool: StateFlow<Double> = _totalMicroInsurancePool.asStateFlow()

    private val _totalPayoutsDisbursed = MutableStateFlow(11246.0)
    val totalPayoutsDisbursed: StateFlow<Double> = _totalPayoutsDisbursed.asStateFlow()

    // Dynamic Fuel-Linked Index state
    private val _fuelPrices = MutableStateFlow(
        mapOf(
            FuelType.PETROL to FuelType.PETROL.defaultCurrentPrice,
            FuelType.DIESEL to FuelType.DIESEL.defaultCurrentPrice,
            FuelType.CNG to FuelType.CNG.defaultCurrentPrice
        )
    )
    val fuelPrices: StateFlow<Map<FuelType, Double>> = _fuelPrices.asStateFlow()

    private val _fuelIndexState = MutableStateFlow(
        FuelIndexState(
            fuelType = FuelType.PETROL,
            currentPrice = FuelType.PETROL.defaultCurrentPrice,
            baselinePrice = FuelType.PETROL.baselinePrice,
            lastUpdated = SimpleDateFormat("dd MMM yyyy", Locale.getDefault()).format(Date()) + " • Daily Mandi Index"
        )
    )
    val fuelIndexState: StateFlow<FuelIndexState> = _fuelIndexState.asStateFlow()

    fun selectFuelType(fuelType: FuelType) {
        val currentPrice = _fuelPrices.value[fuelType] ?: fuelType.defaultCurrentPrice
        _fuelIndexState.value = FuelIndexState(
            fuelType = fuelType,
            currentPrice = currentPrice,
            baselinePrice = fuelType.baselinePrice,
            lastUpdated = SimpleDateFormat("dd MMM yyyy", Locale.getDefault()).format(Date()) + " • Daily Mandi Index"
        )
    }

    fun updateFuelPrice(fuelType: FuelType, newPrice: Double) {
        val safePrice = (newPrice * 100.0).roundToInt() / 100.0
        val updatedMap = _fuelPrices.value.toMutableMap()
        updatedMap[fuelType] = safePrice
        _fuelPrices.value = updatedMap

        if (_fuelIndexState.value.fuelType == fuelType) {
            _fuelIndexState.value = _fuelIndexState.value.copy(
                currentPrice = safePrice,
                lastUpdated = SimpleDateFormat("dd MMM, hh:mm a", Locale.getDefault()).format(Date()) + " • User Adjusted"
            )
        }
    }

    fun resetToDefaultFuelPrices() {
        val defaultMap = mapOf(
            FuelType.PETROL to FuelType.PETROL.defaultCurrentPrice,
            FuelType.DIESEL to FuelType.DIESEL.defaultCurrentPrice,
            FuelType.CNG to FuelType.CNG.defaultCurrentPrice
        )
        _fuelPrices.value = defaultMap
        selectFuelType(_fuelIndexState.value.fuelType)
    }

    /**
     * Calculates dynamic fuel-linked payout:
     * - Base pickup/handling fare (e.g. ₹30)
     * - Distance fare = distanceKm * basePerKmRate
     * - Fuel surcharge = distanceKm * fuelSurchargePerKm
     * - Gross payout = Base + Distance + Fuel surcharge
     * - Disbursed via 97% instant UPI breakdown
     */
    fun calculateFuelLinkedPayout(
        distanceKm: Double,
        fuelType: FuelType = _fuelIndexState.value.fuelType,
        baseFare: Double = 30.0
    ): FuelLinkedPayoutResult {
        val currentPrice = _fuelPrices.value[fuelType] ?: fuelType.defaultCurrentPrice
        val state = FuelIndexState(
            fuelType = fuelType,
            currentPrice = currentPrice,
            baselinePrice = fuelType.baselinePrice
        )
        val safeDistance = distanceKm.coerceAtLeast(0.5)
        val baseDistanceFare = ((safeDistance * fuelType.basePerKmRate) * 100.0).roundToInt() / 100.0
        val fuelSurchargePerKm = state.fuelSurchargePerKm
        val totalFuelSurcharge = ((safeDistance * fuelSurchargePerKm) * 100.0).roundToInt() / 100.0
        val gross = ((baseFare + baseDistanceFare + totalFuelSurcharge) * 100.0).roundToInt() / 100.0
        val breakdown = PaymentBreakdown.compute(gross)

        return FuelLinkedPayoutResult(
            fuelType = fuelType,
            distanceKm = safeDistance,
            baseFare = baseFare,
            basePerKmRate = fuelType.basePerKmRate,
            baseDistanceFare = baseDistanceFare,
            fuelPrice = currentPrice,
            baselinePrice = fuelType.baselinePrice,
            fuelSurchargePerKm = fuelSurchargePerKm,
            totalFuelSurcharge = totalFuelSurcharge,
            grossPayout = gross,
            breakdown = breakdown
        )
    }

    fun calculateBreakdown(grossAmount: Double): PaymentBreakdown {
        return PaymentBreakdown.compute(grossAmount)
    }

    fun processJobSettlement(
        providerId: Long,
        providerName: String,
        providerCategory: String,
        providerUpiId: String,
        jobDescription: String,
        grossAmount: Double
    ): SettlementRecord {
        val breakdown = PaymentBreakdown.compute(grossAmount)
        val utr = "LOK${System.currentTimeMillis().toString().takeLast(8)}${(100..999).random()}"
        
        val record = SettlementRecord(
            providerId = providerId,
            providerName = providerName,
            providerCategory = providerCategory,
            providerUpiId = providerUpiId.ifBlank { "${providerName.lowercase().replace(" ", "")}@upi" },
            jobDescription = jobDescription,
            breakdown = breakdown,
            transactionReference = utr,
            status = SettlementStatus.SUCCESSFUL,
            timestamp = System.currentTimeMillis()
        )

        _settlementHistory.value = listOf(record) + _settlementHistory.value
        _totalPlatformFeeCollected.value += breakdown.platformFeeAmount
        _totalMicroInsurancePool.value += breakdown.microInsuranceAmount
        _totalPayoutsDisbursed.value += breakdown.netPayoutAmount

        return record
    }

    fun buildUpiPaymentUri(
        payeeUpi: String,
        payeeName: String,
        amount: Double,
        transactionNote: String
    ): Uri {
        return Uri.parse(
            "upi://pay"
        ).buildUpon()
            .appendQueryParameter("pa", payeeUpi)
            .appendQueryParameter("pn", payeeName)
            .appendQueryParameter("am", String.format(Locale.US, "%.2f", amount))
            .appendQueryParameter("cu", "INR")
            .appendQueryParameter("tn", transactionNote.take(40))
            .build()
    }

    private fun createInitialSettlementHistory(): List<SettlementRecord> {
        val now = System.currentTimeMillis()
        return listOf(
            SettlementRecord(
                id = "SET9102",
                providerId = 1L,
                providerName = "Rameshwar Sharma",
                providerCategory = "FARMER_VENDOR",
                providerUpiId = "rameshwar.kisan@okhdfcbank",
                jobDescription = "50kg Fresh Mandi Farm Vegetables",
                breakdown = PaymentBreakdown.compute(2000.0),
                transactionReference = "LOK98234121",
                status = SettlementStatus.SUCCESSFUL,
                timestamp = now - (2 * 3600 * 1000)
            ),
            SettlementRecord(
                id = "SET8841",
                providerId = 4L,
                providerName = "Mohammad Aslam",
                providerCategory = "SKILLED_WORKER",
                providerUpiId = "aslam.electrician@paytm",
                jobDescription = "Factory MCB panel rewiring & earthing check",
                breakdown = PaymentBreakdown.compute(1200.0),
                transactionReference = "LOK97118230",
                status = SettlementStatus.SUCCESSFUL,
                timestamp = now - (14 * 3600 * 1000)
            ),
            SettlementRecord(
                id = "SET7429",
                providerId = 7L,
                providerName = "Gurpreet Singh",
                providerCategory = "LOADING_DRIVER",
                providerUpiId = "gurpreet.tempo@sbi",
                jobDescription = "1.5-Ton Grain Shifting from Mandi to Godown",
                breakdown = PaymentBreakdown.compute(2800.0),
                transactionReference = "LOK95340912",
                status = SettlementStatus.SUCCESSFUL,
                timestamp = now - (26 * 3600 * 1000)
            )
        )
    }

    companion object {
        @Volatile
        private var instance: PaymentSettlementManager? = null

        fun getInstance(): PaymentSettlementManager {
            return instance ?: synchronized(this) {
                instance ?: PaymentSettlementManager().also { instance = it }
            }
        }
    }
}
