package com.loksetu.data.model

import java.util.UUID

enum class UserRole {
    MANDI_VENDOR,
    SKILLED_WORKER,
    TRANSPORT_DRIVER,
    CUSTOMER
}

data class PrecisionLocation(
    val latitude: Double = 0.0,
    val longitude: Double = 0.0,
    val fullAddress: String = "",
    val landmark: String = "",
    val city: String = "",
    val pincode: String = ""
) {
    fun toGoogleMapsUrl(): String {
        return "https://www.google.com/maps/search/?api=1&query=$latitude,$longitude"
    }
}

data class DirectContactInfo(
    val phoneNumber: String,
    val whatsappNumber: String,
    val name: String,
    val profileImageUrl: String? = null
)

data class LokSetuListing(
    val id: String = UUID.randomUUID().toString(),
    val title: String,
    val category: UserRole,
    val subCategory: String,
    val priceOrRate: String,
    val description: String = "",
    val providerInfo: DirectContactInfo,
    val location: PrecisionLocation,
    val isAvailable: Boolean = true,
    val createdAtTimestamp: Long = System.currentTimeMillis()
)
