package com.example.data.manager

import com.example.data.network.ONDCNetworkConnector
import com.example.data.network.OndcCategory
import com.example.data.network.OndcMerchant
import com.example.data.network.OndcProductItem
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import java.util.UUID

/**
 * 4. Merchant Auto-Onboarding & Catalog Sync Manager
 *
 * Streamlines 60-second merchant registration and instant product catalog
 * broadcasting with 0% commission, zero listing fee, and direct UPI settlement.
 */

data class CatalogPresetTemplate(
    val title: String,
    val hindiTitle: String,
    val defaultPrice: Double,
    val unit: String,
    val category: OndcCategory,
    val iconEmoji: String
)

class MerchantOnboardingManager private constructor(
    private val ondcConnector: ONDCNetworkConnector = ONDCNetworkConnector.getInstance()
) {
    private val _onboardedMerchantCount = MutableStateFlow(5) // initial verified merchants
    val onboardedMerchantCount: StateFlow<Int> = _onboardedMerchantCount.asStateFlow()

    private val _lastOnboardedMerchant = MutableStateFlow<OndcMerchant?>(null)
    val lastOnboardedMerchant: StateFlow<OndcMerchant?> = _lastOnboardedMerchant.asStateFlow()

    /**
     * Get instant catalog preset templates based on selected merchant category
     */
    fun getPresetTemplatesForCategory(category: OndcCategory): List<CatalogPresetTemplate> {
        return when (category) {
            OndcCategory.KIRANA -> listOf(
                CatalogPresetTemplate("Sharbati Whole Wheat Aata", "शरबती गेहूं आटा", 340.0, "10 kg Bag", OndcCategory.KIRANA, "🌾"),
                CatalogPresetTemplate("Pure Kachi Ghani Mustard Oil", "कच्ची घानी सरसों तेल", 145.0, "1 L Bottle", OndcCategory.KIRANA, "🛢️"),
                CatalogPresetTemplate("Desi Unpolished Chana Dal", "देसी चना दाल", 92.0, "1 kg Pack", OndcCategory.KIRANA, "🥣"),
                CatalogPresetTemplate("Refined Pure White Sugar", "रिफाइंड चीनी (शक्कर)", 44.0, "1 kg Pack", OndcCategory.KIRANA, "🍚"),
                CatalogPresetTemplate("Tata Iodized Vacuum Salt", "टाटा आयोडाइज्ड नमक", 26.0, "1 kg Pack", OndcCategory.KIRANA, "🧂")
            )
            OndcCategory.AGRI_MANDI -> listOf(
                CatalogPresetTemplate("Cold Storage Desi Potatoes", "देसी नया आलू", 780.0, "50 kg Sack", OndcCategory.AGRI_MANDI, "🥔"),
                CatalogPresetTemplate("Nashik Fresh Red Onions", "नासिक लाल प्याज", 950.0, "40 kg Sack", OndcCategory.AGRI_MANDI, "🧅"),
                CatalogPresetTemplate("Hybrid Ripe Farm Tomatoes", "ताजा टमाटर क्रेट", 480.0, "25 kg Crate", OndcCategory.AGRI_MANDI, "🍅"),
                CatalogPresetTemplate("Green Spicy Farm Chillies", "ताजा हरी मिर्च", 65.0, "1 kg Pack", OndcCategory.AGRI_MANDI, "🌶️")
            )
            OndcCategory.AGRI_INPUTS -> listOf(
                CatalogPresetTemplate("DAP 18:46:0 IFFCO Fertilizer", "डीएपी 18:46:0 खाद", 1350.0, "50 kg Bag", OndcCategory.AGRI_INPUTS, "🌱"),
                CatalogPresetTemplate("Neem Coated Urea (Subsidized)", "नीम लेपित यूरिया", 266.5, "45 kg Bag", OndcCategory.AGRI_INPUTS, "🌾"),
                CatalogPresetTemplate("Certified HD-2967 Wheat Seeds", "प्रमाणित गेहूं बीज", 980.0, "40 kg Bag", OndcCategory.AGRI_INPUTS, "🌿"),
                CatalogPresetTemplate("Zinc Sulfate 33% Micronutrient", "जिंक सल्फेट 33%", 380.0, "5 kg Pack", OndcCategory.AGRI_INPUTS, "🧪")
            )
            OndcCategory.HARDWARE -> listOf(
                CatalogPresetTemplate("Heavy Duty Submersible Cable", "सबमर्सिबल कॉपर केबल", 1850.0, "90 Meter Coil", OndcCategory.HARDWARE, "🔌"),
                CatalogPresetTemplate("Agriculture PVC Pipe 2.5 inch", "कृषि पीवीसी पाइप 2.5 इंच", 420.0, "20 Ft Length", OndcCategory.HARDWARE, "🚰"),
                CatalogPresetTemplate("Manual Knapsack Crop Sprayer 16L", "16L नैपसैक स्प्रेयर पंप", 1250.0, "1 Unit", OndcCategory.HARDWARE, "🚿"),
                CatalogPresetTemplate("Energy Saving 9W LED Bulbs (Pack)", "9W एलईडी बल्ब (पैक)", 320.0, "Pack of 4", OndcCategory.HARDWARE, "💡")
            )
            OndcCategory.PHARMACY -> listOf(
                CatalogPresetTemplate("Paracetamol 650mg Generic", "पैरासिटामोल 650mg", 18.0, "10 Tablets Strip", OndcCategory.PHARMACY, "💊"),
                CatalogPresetTemplate("ORS Rehydration Salts WHO", "ओआरएस घोल पैकेट", 21.0, "Pack of 3", OndcCategory.PHARMACY, "💧"),
                CatalogPresetTemplate("Veterinary Calcium Tonic (5L)", "पशु कैल्शियम टॉनिक 5L", 650.0, "5 L Can", OndcCategory.PHARMACY, "🐄"),
                CatalogPresetTemplate("First Aid Antiseptic Liquid", "एंटीसेप्टिक लिक्विड", 85.0, "200 ml Bottle", OndcCategory.PHARMACY, "🩹")
            )
            OndcCategory.DAIRY -> listOf(
                CatalogPresetTemplate("Pure Desi Gir Cow Milk", "शुद्ध देसी गाय का दूध", 64.0, "1 Litre Pouch", OndcCategory.DAIRY, "🥛"),
                CatalogPresetTemplate("Fresh Malai Buffalo Paneer", "ताजा मलाई पनीर", 340.0, "1 kg Block", OndcCategory.DAIRY, "🧈"),
                CatalogPresetTemplate("Bilona Desi Cow Ghee (A2)", "बिलौना वैदिक देसी घी", 850.0, "1 Litre Jar", OndcCategory.DAIRY, "🫙"),
                CatalogPresetTemplate("Fresh Sweet Mandi Dahi", "ताजा मीठा दही", 70.0, "1 kg Tub", OndcCategory.DAIRY, "🥣")
            )
        }
    }

    /**
     * Complete merchant onboarding with catalog items and broadcast to ONDC
     */
    fun onboardMerchantWithCatalog(
        storeName: String,
        ownerName: String,
        category: OndcCategory,
        phone: String,
        upiId: String,
        address: String,
        district: String,
        latitude: Double,
        longitude: Double,
        selectedPresets: List<CatalogPresetTemplate>,
        customItems: List<OndcProductItem>
    ): OndcMerchant {
        val merchantId = "ONDC-MER-${UUID.randomUUID().toString().take(6).uppercase()}"

        val presetProducts = selectedPresets.map { preset ->
            OndcProductItem(
                merchantId = merchantId,
                merchantName = storeName,
                title = preset.title,
                hindiTitle = preset.hindiTitle,
                category = preset.category,
                price = preset.defaultPrice,
                mrp = preset.defaultPrice * 1.06,
                unit = preset.unit,
                inStock = true,
                zeroListingFee = true,
                iconEmoji = preset.iconEmoji
            )
        }

        val allProducts = presetProducts + customItems

        val newMerchant = OndcMerchant(
            id = merchantId,
            name = storeName,
            ownerName = ownerName,
            category = category,
            rating = 5.0,
            ratingCount = 1,
            latitude = latitude,
            longitude = longitude,
            address = address,
            district = district,
            phone = phone,
            upiId = upiId,
            zeroCommissionVerified = true,
            catalogCount = allProducts.size,
            tags = listOf("ONDC Registered", "0% Commission", "Instant UPI Settlement", category.displayName)
        )

        // Register with ONDC open connector
        ondcConnector.registerMerchant(newMerchant, allProducts)

        _onboardedMerchantCount.value += 1
        _lastOnboardedMerchant.value = newMerchant

        return newMerchant
    }

    companion object {
        @Volatile
        private var INSTANCE: MerchantOnboardingManager? = null

        fun getInstance(): MerchantOnboardingManager {
            return INSTANCE ?: synchronized(this) {
                INSTANCE ?: MerchantOnboardingManager().also { INSTANCE = it }
            }
        }
    }
}
