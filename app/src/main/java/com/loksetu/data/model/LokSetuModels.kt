package com.loksetu.data.model

import java.util.UUID

// यूजर रोल
enum class UserRole {
    MANDI_VENDOR,
    SKILLED_WORKER,
    TRANSPORT_DRIVER,
    CUSTOMER
}

// वाहन की श्रेणी (Vehicle Category)
enum class VehicleType(val displayName: String, val maxWeightKg: Int) {
    BIKE("बाइक / टू-व्हीलर", 50),
    AUTO_3WHEELER("3-व्हीलर / ऑटो रिक्शा", 300),
    TEMPO_SMALL("छोटा हाथी / पिकअप", 1000),
    TRUCK_LARGE("बड़ा ट्रक / कैंटर", 5000)
}

// लोकेशन
data class PrecisionLocation(
    val latitude: Double = 0.0,
    val longitude: Double = 0.0,
    val fullAddress: String = "",
    val landmark: String = "",
    val city: String = "",
    val pincode: String = ""
)

// संपर्क जानकारी
data class DirectContactInfo(
    val phoneNumber: String,
    val whatsappNumber: String,
    val name: String,
    val profileImageUrl: String? = null
)

// ट्रांसपोर्ट/राइड बुकिंग की पूरी पारदर्शी जानकारी (Fair & Accurate Pricing)
data class TransportPricingDetails(
    val vehicleType: VehicleType,
    val exactWeightKg: Double,          // सामान का सटीक वज़न (kg में)
    val parcelPhotoUrl: String? = null, // सामान की फोटो
    val baseFare: Double,               // शुरुआती बेस किराया (₹)
    val perKmRate: Double,              // प्रति किलोमीटर रेट (₹/km)
    val loadingCharges: Double = 0.0    // लोडिंग/अनलोडिंग का साफ़ चार्ज (₹)
)

// मुख्य लिस्टिंग मॉडल
data class LokSetuListing(
    val id: String = UUID.randomUUID().toString(),
    val title: String,
    val category: UserRole,
    val subCategory: String,
    val priceOrRate: String,
    val description: String = "",
    val providerInfo: DirectContactInfo,
    val location: PrecisionLocation,
    val transportDetails: TransportPricingDetails? = null, // ड्राइवर/पार्सल के लिए विशेष फ़ील्ड
    val isAvailable: Boolean = true,
    val createdAtTimestamp: Long = System.currentTimeMillis()
)
