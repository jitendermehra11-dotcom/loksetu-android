package com.example.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey

enum class ProviderCategory(val displayName: String, val tagline: String, val iconEmoji: String = "") {
    BIKE_DELIVERY("Bike Parcel & Delivery", "Quick courier, food parcels, documents & hyperlocal bike express", "🏍️"),
    CAB_TAXI("Cab/Taxi Ride", "Affordable local & outstation auto, cab, sedans & SUV rides", "🚗"),
    CARGO_LOADER("Cargo Loader", "Tata Ace Chhota Hathi, pickup trucks, cargo tempos & logistics", "🚚"),
    FARMER_VENDOR("Farmers & Mandi", "Direct farm produce, grains, organic vegetables & fruits", "🌾"),
    SKILLED_WORKER("Skilled Workers", "Certified plumbers, electricians, carpenters & masons", "🛠️"),
    HOME_BEAUTY("Home Beauty & Care", "Beauticians, bridal mehendi, skin/hair grooming & makeover", "💅"),
    HOME_HEALTHCARE("Home Healthcare & Nursing", "Certified home nurses, patient attendants & elderly care", "🩺"),
    DOMESTIC_SERVICES("Domestic & Society", "House help, maids, deep cleaning, society & facility staff", "🧹"),
    LOADING_DRIVER("Cargo Loader", "Tata Ace Chhota Hathi, pickup trucks, cargo tempos & logistics", "🚚"); // Backwards-compatible alias

    companion object {
        val registrationCategories = listOf(
            BIKE_DELIVERY,
            CAB_TAXI,
            CARGO_LOADER,
            FARMER_VENDOR,
            SKILLED_WORKER,
            HOME_BEAUTY,
            HOME_HEALTHCARE,
            DOMESTIC_SERVICES
        )
    }
}

@Entity(tableName = "providers")
data class ProviderEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0L,
    val name: String,
    val category: String, // From ProviderCategory.name
    val subCategory: String, // e.g. "Electrician", "Vegetable Farmer", "1-Ton Mini Truck", "Bike Express", "AC Sedan Cab"
    val speciality: String, // Short highlighted skills or produce
    val phone: String, // e.g. "+91 98123 45678"
    val whatsAppNumber: String, // Digits only or with +91
    val latitude: Double,
    val longitude: Double,
    val locationName: String, // e.g. "Kisan Sabzi Mandi, Gate 2"
    val landmark: String = "",
    val pricing: String, // e.g. "₹250 service visit" or "₹35/kg fresh bulk" or "₹550 base + ₹25/km"
    val rating: Float = 4.8f,
    val reviewCount: Int = 24,
    val isAvailableNow: Boolean = true,
    val isVerified: Boolean = true,
    val experienceYears: Int = 5,
    val description: String = "",
    val isFavorite: Boolean = false,
    val createdAt: Long = System.currentTimeMillis(),
    // KYC Verification fields
    val aadhaarNumber: String = "",
    val drivingLicenseNumber: String = "",
    val vehicleRcNumber: String = "",
    val aadhaarDocUri: String = "",
    val dlDocUri: String = "",
    val rcDocUri: String = "",
    val kycStatus: String = "VERIFIED"
)

@Entity(tableName = "contact_history")
data class ContactHistoryEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0L,
    val providerId: Long,
    val providerName: String,
    val providerCategory: String,
    val providerPhone: String,
    val actionType: String, // "PHONE_CALL" or "WHATSAPP_MESSAGE"
    val notes: String = "",
    val timestamp: Long = System.currentTimeMillis()
)

data class UserLocation(
    val latitude: Double,
    val longitude: Double,
    val address: String = "Detecting current location...",
    val accuracyMeters: Float = 0f,
    val isManualPin: Boolean = false
)
