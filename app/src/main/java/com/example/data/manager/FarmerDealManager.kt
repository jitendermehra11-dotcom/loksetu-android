package com.example.data.manager

import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.UUID
import kotlin.math.roundToInt

/**
 * 4. Farmer Direct Deal Lock & Penalty Manager (FarmerDealManager)
 * Implements:
 * - 15% escrow token advance lock for direct farm-gate deals
 * - Buyer cancellation penalty: 15% advance forfeited and transferred directly to farmer's account
 * - Farmer cancellation penalty: 15% advance refunded to buyer + 5% default penalty compensated to buyer
 * - Completion logic: 15% escrow released + remaining 85% settled upon delivery
 */
enum class DealStage(val label: String) {
    PENDING_ADVANCE("Pending 15% Advance"),
    ESCROW_LOCKED("15% Advance Locked in Escrow"),
    HARVEST_DISPATCH_READY("Harvest Ready & Weighed"),
    COMPLETED("Deal Completed & 100% Settled"),
    CANCELLED_BY_BUYER("Cancelled by Buyer (15% Penalty Transferred to Farmer)"),
    CANCELLED_BY_FARMER("Defaulted by Farmer (15% Refunded + 5% Penalty Paid to Buyer)")
}

data class FarmDeal(
    val id: String = UUID.randomUUID().toString().take(8).uppercase(),
    val cropName: String,
    val variety: String,
    val quantityQuintals: Double,
    val ratePerQuintal: Double,
    val totalDealValue: Double,
    val advanceEscrowAmount: Double, // Exactly 15% of total
    val remainingBalanceAmount: Double, // Exactly 85% of total
    val farmerName: String,
    val farmerPhone: String,
    val farmerLocation: String,
    val buyerName: String,
    val buyerPhone: String,
    val stage: DealStage = DealStage.ESCROW_LOCKED,
    val penaltyAmount: Double = 0.0,
    val penaltyRecipient: String = "",
    val auditNote: String = "15% Token Advance locked securely in LokSetu Farm Escrow.",
    val createdAt: Long = System.currentTimeMillis()
) {
    val formattedDate: String
        get() = SimpleDateFormat("dd MMM yyyy", Locale.getDefault()).format(Date(createdAt))
}

class FarmerDealManager {

    private val _activeDeals = MutableStateFlow<List<FarmDeal>>(createInitialDeals())
    val activeDeals: StateFlow<List<FarmDeal>> = _activeDeals.asStateFlow()

    private val _totalEscrowSecured = MutableStateFlow(186450.0)
    val totalEscrowSecured: StateFlow<Double> = _totalEscrowSecured.asStateFlow()

    private val _totalPenaltiesTransferredToFarmers = MutableStateFlow(32400.0)
    val totalPenaltiesTransferredToFarmers: StateFlow<Double> = _totalPenaltiesTransferredToFarmers.asStateFlow()

    fun createFarmDeal(
        cropName: String,
        variety: String,
        quantityQuintals: Double,
        ratePerQuintal: Double,
        farmerName: String,
        farmerPhone: String,
        farmerLocation: String,
        buyerName: String,
        buyerPhone: String
    ): FarmDeal {
        val total = ((quantityQuintals * ratePerQuintal) * 100.0).roundToInt() / 100.0
        val advance15 = ((total * 0.15) * 100.0).roundToInt() / 100.0
        val balance85 = ((total - advance15) * 100.0).roundToInt() / 100.0

        val deal = FarmDeal(
            cropName = cropName,
            variety = variety,
            quantityQuintals = quantityQuintals,
            ratePerQuintal = ratePerQuintal,
            totalDealValue = total,
            advanceEscrowAmount = advance15,
            remainingBalanceAmount = balance85,
            farmerName = farmerName,
            farmerPhone = farmerPhone,
            farmerLocation = farmerLocation,
            buyerName = buyerName,
            buyerPhone = buyerPhone,
            stage = DealStage.ESCROW_LOCKED,
            auditNote = "15% token advance (₹$advance15) locked in escrow. Deal binding on both parties."
        )

        _activeDeals.value = listOf(deal) + _activeDeals.value
        _totalEscrowSecured.value += advance15
        return deal
    }

    /**
     * Buyer cancels deal:
     * 15% advance in escrow is forfeited by buyer and transferred 100% to the farmer
     * to compensate for crop harvesting, labor and mandi holding losses.
     */
    fun cancelDealByBuyer(dealId: String, cancellationReason: String): FarmDeal? {
        val deal = _activeDeals.value.find { it.id == dealId } ?: return null
        if (deal.stage == DealStage.COMPLETED || deal.stage == DealStage.CANCELLED_BY_BUYER) return deal

        val penaltyAmt = deal.advanceEscrowAmount
        val updated = deal.copy(
            stage = DealStage.CANCELLED_BY_BUYER,
            penaltyAmount = penaltyAmt,
            penaltyRecipient = "Farmer: ${deal.farmerName}",
            auditNote = "Buyer cancelled: \"$cancellationReason\". 15% escrow advance (₹$penaltyAmt) transferred directly to farmer ${deal.farmerName} as harvest loss compensation."
        )

        _activeDeals.value = _activeDeals.value.map { if (it.id == dealId) updated else it }
        _totalPenaltiesTransferredToFarmers.value += penaltyAmt
        return updated
    }

    /**
     * Farmer defaults/cancels deal:
     * 15% advance in escrow is refunded to the buyer,
     * plus a 5% default penalty (of total deal value) is charged to the farmer and credited to buyer.
     */
    fun cancelDealByFarmer(dealId: String, defaultReason: String): FarmDeal? {
        val deal = _activeDeals.value.find { it.id == dealId } ?: return null
        if (deal.stage == DealStage.COMPLETED || deal.stage == DealStage.CANCELLED_BY_FARMER) return deal

        val penalty5Percent = ((deal.totalDealValue * 0.05) * 100.0).roundToInt() / 100.0
        val updated = deal.copy(
            stage = DealStage.CANCELLED_BY_FARMER,
            penaltyAmount = penalty5Percent,
            penaltyRecipient = "Buyer: ${deal.buyerName}",
            auditNote = "Farmer defaulted: \"$defaultReason\". 15% advance (₹${deal.advanceEscrowAmount}) refunded to buyer + ₹$penalty5Percent (5% default penalty) compensated to buyer."
        )

        _activeDeals.value = _activeDeals.value.map { if (it.id == dealId) updated else it }
        return updated
    }

    /**
     * Successfully complete deal at farm gate:
     * 15% escrow released to farmer + remaining 85% settled.
     */
    fun completeDeal(dealId: String): FarmDeal? {
        val deal = _activeDeals.value.find { it.id == dealId } ?: return null

        val updated = deal.copy(
            stage = DealStage.COMPLETED,
            auditNote = "Farm-gate weighment verified. 15% Escrow (₹${deal.advanceEscrowAmount}) released + 85% balance (₹${deal.remainingBalanceAmount}) settled. Total ₹${deal.totalDealValue} paid to farmer."
        )

        _activeDeals.value = _activeDeals.value.map { if (it.id == dealId) updated else it }
        return updated
    }

    private fun createInitialDeals(): List<FarmDeal> {
        return listOf(
            FarmDeal(
                id = "DEAL7104",
                cropName = "Sharbati Wheat (गेहूं)",
                variety = "Grade-A MP Sharbati Gold",
                quantityQuintals = 80.0,
                ratePerQuintal = 2450.0,
                totalDealValue = 196000.0,
                advanceEscrowAmount = 29400.0,
                remainingBalanceAmount = 166600.0,
                farmerName = "Balbir Singh Dhillon",
                farmerPhone = "+91 98721 54321",
                farmerLocation = "Farm Gate, Khanna Mandi Outskirts",
                buyerName = "Aggarwal Roller Flour Mills",
                buyerPhone = "+91 98140 11223",
                stage = DealStage.ESCROW_LOCKED,
                auditNote = "15% Advance (₹29,400) locked in escrow. Vehicle dispatch scheduled tomorrow."
            ),
            FarmDeal(
                id = "DEAL6819",
                cropName = "Fresh Farm Tomatoes (टमाटर)",
                variety = "Himsona Hybrid Red",
                quantityQuintals = 50.0,
                ratePerQuintal = 1800.0,
                totalDealValue = 90000.0,
                advanceEscrowAmount = 13500.0,
                remainingBalanceAmount = 76500.0,
                farmerName = "Hariram Meena",
                farmerPhone = "+91 94142 88761",
                farmerLocation = "Jaipur Rural Farm Belt",
                buyerName = "FreshKart Quick Commerce",
                buyerPhone = "+91 99280 44556",
                stage = DealStage.HARVEST_DISPATCH_READY,
                auditNote = "Crates packed & weighed. 15% escrow locked. Driver loaded."
            ),
            FarmDeal(
                id = "DEAL5401",
                cropName = "Basmati Paddy 1121 (धान)",
                variety = "Pusa 1121 Raw Paddy",
                quantityQuintals = 120.0,
                ratePerQuintal = 3850.0,
                totalDealValue = 462000.0,
                advanceEscrowAmount = 69300.0,
                remainingBalanceAmount = 392700.0,
                farmerName = "Satnam Singh Sandhu",
                farmerPhone = "+91 98888 12345",
                farmerLocation = "Karnal Farm Gate",
                buyerName = "Doaba Rice Exporters",
                buyerPhone = "+91 98765 99887",
                stage = DealStage.COMPLETED,
                auditNote = "Successfully delivered. 100% funds released directly to farmer bank account."
            )
        )
    }

    companion object {
        @Volatile
        private var instance: FarmerDealManager? = null

        fun getInstance(): FarmerDealManager {
            return instance ?: synchronized(this) {
                instance ?: FarmerDealManager().also { instance = it }
            }
        }
    }
}
