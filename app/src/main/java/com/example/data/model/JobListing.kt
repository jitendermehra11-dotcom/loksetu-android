package com.example.data.model

enum class JobCategory(val displayName: String, val iconName: String) {
    ALL("All Roles", "Work"),
    DRIVER("Drivers", "LocalShipping"),
    HELPER("Helpers & Loaders", "Handyman"),
    SECURITY_GUARD("Security Guards", "Security"),
    SKILLED_WORKER("Skilled Trades", "Construction")
}

data class JobListing(
    val id: String,
    val title: String,
    val category: JobCategory,
    val employerName: String,
    val salaryDisplay: String, // e.g. "₹650 - ₹800 / day" or "₹18,000 - ₹24,000 / month"
    val jobType: String, // "Full-Time", "Daily Wage", "Shift Work", "Contract"
    val vacancies: Int,
    val location: String,
    val latitude: Double,
    val longitude: Double,
    val shiftTimings: String, // e.g. "Morning Mandi Shift (5 AM - 1 PM)"
    val contactPhone: String,
    val whatsAppNumber: String,
    val isVerifiedEmployer: Boolean = true,
    val requirements: List<String>,
    val description: String,
    val postedAt: Long = System.currentTimeMillis()
)
