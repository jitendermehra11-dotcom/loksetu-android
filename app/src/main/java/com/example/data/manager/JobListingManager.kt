package com.example.data.manager

import com.example.data.model.JobCategory
import com.example.data.model.JobListing
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import java.util.UUID

/**
 * 5. Jobs & Employment Manager
 * Manages verified job listings for:
 * - Drivers (Tempo, Truck, Cargo Auto)
 * - Helpers (Mandi loaders, warehouse packing)
 * - Security Guards (Gate, night warehouse)
 * - Skilled Workers (Plumbers, electricians, mechanics)
 */
class JobListingManager {

    private val _jobs = MutableStateFlow<List<JobListing>>(createInitialJobListings())
    val jobs: StateFlow<List<JobListing>> = _jobs.asStateFlow()

    fun postJob(
        title: String,
        category: JobCategory,
        employerName: String,
        salaryDisplay: String,
        jobType: String,
        vacancies: Int,
        location: String,
        latitude: Double,
        longitude: Double,
        shiftTimings: String,
        contactPhone: String,
        whatsAppNumber: String,
        requirements: List<String>,
        description: String
    ): JobListing {
        val cleanPhone = contactPhone.trim()
        val cleanDigits = whatsAppNumber.replace(Regex("[^0-9]"), "")
        val formattedWa = if (cleanDigits.length == 10) "91$cleanDigits" else cleanDigits

        val newJob = JobListing(
            id = "JOB-${UUID.randomUUID().toString().take(6).uppercase()}",
            title = title.trim(),
            category = category,
            employerName = employerName.trim(),
            salaryDisplay = salaryDisplay.trim(),
            jobType = jobType,
            vacancies = vacancies.coerceAtLeast(1),
            location = location.trim(),
            latitude = latitude,
            longitude = longitude,
            shiftTimings = shiftTimings.trim(),
            contactPhone = cleanPhone,
            whatsAppNumber = formattedWa,
            isVerifiedEmployer = true,
            requirements = requirements,
            description = description.trim(),
            postedAt = System.currentTimeMillis()
        )

        _jobs.value = listOf(newJob) + _jobs.value
        return newJob
    }

    private fun createInitialJobListings(): List<JobListing> {
        val now = System.currentTimeMillis()
        return listOf(
            // DRIVER
            JobListing(
                id = "JOB-DRV101",
                title = "Tata Ace Chhota Hathi Driver",
                category = JobCategory.DRIVER,
                employerName = "Balaji Mandi Logistics",
                salaryDisplay = "₹22,000 - ₹26,000 / month + Trip Incentive",
                jobType = "Full-Time",
                vacancies = 3,
                location = "Azadpur Mandi Shed 4, Delhi",
                latitude = 28.7150,
                longitude = 77.1780,
                shiftTimings = "Early Morning Shift (4:00 AM - 1:00 PM)",
                contactPhone = "+91 98101 23456",
                whatsAppNumber = "919810123456",
                isVerifiedEmployer = true,
                requirements = listOf("Valid Commercial LMV Driving License", "Clean traffic record", "Familiarity with NCR mandi routes"),
                description = "Daily morning delivery of fresh vegetables from main mandi to 12 retail hub points. Vehicle & diesel provided by employer.",
                postedAt = now - (3 * 3600 * 1000)
            ),
            JobListing(
                id = "JOB-DRV102",
                title = "Eicher 14-Feet Loading Truck Driver",
                category = JobCategory.DRIVER,
                employerName = "Kisan Express Transport",
                salaryDisplay = "₹28,000 / month + Daily Food Allowance",
                jobType = "Full-Time",
                vacancies = 2,
                location = "Okhla Industrial Transport Hub",
                latitude = 28.5355,
                longitude = 77.2732,
                shiftTimings = "Intercity Night Haul (8:00 PM - 5:00 AM)",
                contactPhone = "+91 98712 34567",
                whatsAppNumber = "919871234567",
                isVerifiedEmployer = true,
                requirements = listOf("Valid HMV/Transport License", "Minimum 3 years highway experience", "Aadhaar verified"),
                description = "Transporting packaged agro goods and seed sacks to regional distribution centers. Covered under LokSetu ₹25L Accident Policy.",
                postedAt = now - (18 * 3600 * 1000)
            ),

            // HELPER
            JobListing(
                id = "JOB-HLP201",
                title = "Mandi Grain Loading & Stacking Helper",
                category = JobCategory.HELPER,
                employerName = "Shree Ram Mandi Warehouse",
                salaryDisplay = "₹700 / day (₹21,000 / month)",
                jobType = "Daily Wage / Regular",
                vacancies = 5,
                location = "Najafgarh Mandi Platform B",
                latitude = 28.6092,
                longitude = 76.9855,
                shiftTimings = "Day Shift (6:00 AM - 3:00 PM)",
                contactPhone = "+91 98112 99881",
                whatsAppNumber = "919811299881",
                isVerifiedEmployer = true,
                requirements = listOf("Physical fitness for 50kg grain sacks", "Punctual & dependable", "Daily cash or weekly UPI pay"),
                description = "Unloading farmer trollies, weighing sacks, and stacking inside ventilated godown. Free chai and breakfast provided.",
                postedAt = now - (6 * 3600 * 1000)
            ),
            JobListing(
                id = "JOB-HLP202",
                title = "Cold Storage Fruit Sorting & Packing Assistant",
                category = JobCategory.HELPER,
                employerName = "Himalayan Fresh Agro Cold Chain",
                salaryDisplay = "₹17,500 / month + Overtime",
                jobType = "Full-Time",
                vacancies = 4,
                location = "Kundli Agro Processing Zone",
                latitude = 28.8680,
                longitude = 77.1260,
                shiftTimings = "Rotational 8-Hour Shift",
                contactPhone = "+91 99912 33445",
                whatsAppNumber = "919991233445",
                isVerifiedEmployer = true,
                requirements = listOf("Basic quality sorting skills", "Careful handling of apples & tomatoes", "Aadhaar card"),
                description = "Grading, sorting, and barcoding fresh seasonal produce in temperature-controlled warehouse prior to dispatch.",
                postedAt = now - (22 * 3600 * 1000)
            ),

            // SECURITY GUARD
            JobListing(
                id = "JOB-SEC301",
                title = "Mandi Gate & Weighbridge Security Guard",
                category = JobCategory.SECURITY_GUARD,
                employerName = "Suraksha Industrial Security Services",
                salaryDisplay = "₹19,500 / month + ESI / PF Benefits",
                jobType = "Shift Work (12 Hours)",
                vacancies = 3,
                location = "Ghazipur Wholesale Market",
                latitude = 28.6258,
                longitude = 77.3290,
                shiftTimings = "Night Shift (7:00 PM - 7:00 AM)",
                contactPhone = "+91 98188 44220",
                whatsAppNumber = "919818844220",
                isVerifiedEmployer = true,
                requirements = listOf("Age 21-45 years", "Height min 5'6\"", "Ex-servicemen or trained guards preferred", "Uniform provided"),
                description = "Monitoring incoming vehicles at main gate, vehicle pass inspection, weighbridge queue safety, and logbook entries.",
                postedAt = now - (10 * 3600 * 1000)
            ),
            JobListing(
                id = "JOB-SEC302",
                title = "Warehouse Perimeter & Yard Security",
                category = JobCategory.SECURITY_GUARD,
                employerName = "Kisan Godown Logistics Park",
                salaryDisplay = "₹18,000 / month + Room Accomodation",
                jobType = "Full-Time",
                vacancies = 2,
                location = "Narela Food Park Hub",
                latitude = 28.8520,
                longitude = 77.0940,
                shiftTimings = "Day & Night 12-Hour Rotational",
                contactPhone = "+91 98109 87654",
                whatsAppNumber = "919810987654",
                isVerifiedEmployer = true,
                requirements = listOf("Clean police verification", "Alert and vigilant", "Free barracks lodging on premises"),
                description = "Checking boundary wall perimeter, CCTV verification assistance, and security of parked tractors and loading trucks.",
                postedAt = now - (36 * 3600 * 1000)
            ),

            // SKILLED WORKER
            JobListing(
                id = "JOB-SKL401",
                title = "Cold Storage Commercial Refrigeration Technician",
                category = JobCategory.SKILLED_WORKER,
                employerName = "Apex Climate Systems & Services",
                salaryDisplay = "₹28,000 - ₹35,000 / month",
                jobType = "Full-Time",
                vacancies = 2,
                location = "Sonipat Agro Cold Cluster",
                latitude = 28.9931,
                longitude = 77.0151,
                shiftTimings = "General Shift (9:00 AM - 6:00 PM) + Emergency On-Call",
                contactPhone = "+91 98120 77665",
                whatsAppNumber = "919812077665",
                isVerifiedEmployer = true,
                requirements = listOf("ITI / Diploma in Refrigeration & AC (RAC)", "3+ years chiller plant experience", "Freon & ammonia cycle troubleshooting"),
                description = "Preventive maintenance and emergency breakdown handling of 2000-ton capacity potato and seed cold rooms.",
                postedAt = now - (14 * 3600 * 1000)
            ),
            JobListing(
                id = "JOB-SKL402",
                title = "Tractor & Farm Machinery Mechanic",
                category = JobCategory.SKILLED_WORKER,
                employerName = "Kisan Tractor Workshop & Spares",
                salaryDisplay = "₹25,000 / month + 10% Service Commission",
                jobType = "Full-Time",
                vacancies = 1,
                location = "Rohtak Road Agri Machinery Hub",
                latitude = 28.6940,
                longitude = 76.9320,
                shiftTimings = "Day Shift (8:30 AM - 6:30 PM)",
                contactPhone = "+91 94160 55443",
                whatsAppNumber = "919416055443",
                isVerifiedEmployer = true,
                requirements = listOf("Mahindra / Swaraj / Sonalika diesel engine overhaul expertise", "Hydraulics troubleshooting", "Tool kit provided"),
                description = "Diagnostic testing and servicing of agricultural tractors, rotavators, seed drills, and threshers.",
                postedAt = now - (28 * 3600 * 1000)
            )
        )
    }

    companion object {
        @Volatile
        private var instance: JobListingManager? = null

        fun getInstance(): JobListingManager {
            return instance ?: synchronized(this) {
                instance ?: JobListingManager().also { instance = it }
            }
        }
    }
}
