package com.example

import android.content.Context
import android.view.KeyEvent
import androidx.test.core.app.ApplicationProvider
import com.example.data.manager.AdWelfareFundManager
import com.example.data.manager.DealStage
import com.example.data.manager.FarmerDealManager
import com.example.data.manager.JobListingManager
import com.example.data.manager.PaymentBreakdown
import com.example.data.manager.PaymentSettlementManager
import com.example.data.manager.SosEmergencyManager
import com.example.data.model.JobCategory
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner

@RunWith(RobolectricTestRunner::class)
class LokSetuCoreModulesTest {

    private lateinit var context: Context

    @Before
    fun setup() {
        context = ApplicationProvider.getApplicationContext()
    }

    @Test
    fun testPaymentSettlementBreakdown_threePercentFeeAndOneRupeeInsurance() {
        // Test ₹1000 gross job
        val breakdown = PaymentBreakdown.compute(1000.0)
        assertEquals(1000.0, breakdown.grossAmount, 0.001)
        assertEquals(30.0, breakdown.platformFeeAmount, 0.001) // 3% fee
        assertEquals(1.0, breakdown.microInsuranceAmount, 0.001) // ₹1 micro-insurance
        assertEquals(969.0, breakdown.netPayoutAmount, 0.001) // Instant 97% - ₹1 payout

        val manager = PaymentSettlementManager()
        val record = manager.processJobSettlement(
            providerId = 101L,
            providerName = "Vikram Singh Driver",
            providerCategory = "LOADING_DRIVER",
            providerUpiId = "vikram.tempo@upi",
            jobDescription = "Azadpur Mandi Agro Haul",
            grossAmount = 1000.0
        )

        assertEquals("vikram.tempo@upi", record.providerUpiId)
        assertEquals(969.0, record.breakdown.netPayoutAmount, 0.001)
        assertTrue(record.transactionReference.startsWith("LOK"))

        val upiUri = manager.buildUpiPaymentUri(
            payeeUpi = "vikram.tempo@upi",
            payeeName = "Vikram Singh",
            amount = 969.0,
            transactionNote = "Job #101 Settlement"
        )
        assertTrue(upiUri.toString().contains("vikram.tempo"))
    }

    @Test
    fun testSosEmergencyVoiceAndHardwareTrigger() {
        val sosManager = SosEmergencyManager(context)

        // Test Voice triggers ('मदद करो', 'इमरजेंसी', 'emergency')
        val matchHindi = sosManager.checkVoiceTrigger(
            spokenText = "अरे भैया मदद करो जल्दी",
            currentLat = 28.6139,
            currentLng = 77.2090,
            accuracyMeters = 5.0f,
            address = "Delhi"
        )
        assertTrue(matchHindi)

        val matchEmergency = sosManager.checkVoiceTrigger(
            spokenText = "इमरजेंसी हो गई है हाइवे पर",
            currentLat = 28.6139,
            currentLng = 77.2090,
            accuracyMeters = 5.0f,
            address = "Delhi"
        )
        assertTrue(matchEmergency)

        val normalSpeech = sosManager.checkVoiceTrigger(
            spokenText = "गाड़ी सही चल रही है",
            currentLat = 28.6139,
            currentLng = 77.2090,
            accuracyMeters = 5.0f,
            address = "Delhi"
        )
        assertFalse(normalSpeech)

        // Test Hardware volume key 3-press detection
        val downEvent = KeyEvent(KeyEvent.ACTION_DOWN, KeyEvent.KEYCODE_VOLUME_DOWN)
        sosManager.onHardwareKeyEvent(KeyEvent.KEYCODE_VOLUME_DOWN, downEvent)
        sosManager.onHardwareKeyEvent(KeyEvent.KEYCODE_VOLUME_DOWN, downEvent)
        val triggered = sosManager.onHardwareKeyEvent(KeyEvent.KEYCODE_VOLUME_DOWN, downEvent)
        assertTrue(triggered)
    }

    @Test
    fun testAdWelfareFundCollection_zeroCostToDrivers() {
        val welfareManager = AdWelfareFundManager()
        val initialPool = welfareManager.totalPoolFundAccumulated.value

        welfareManager.recordAdContribution(
            sponsorName = "Mahindra Commercial Trucks",
            campaignTitle = "Bolero Maxi Truck Fleet",
            adCategory = "Commercial Auto",
            amount = 25.0
        )

        assertEquals(initialPool + 25.0, welfareManager.totalPoolFundAccumulated.value, 0.001)
        assertEquals(2500000L, welfareManager.standardCoverageTier.sumInsuredAmount) // ₹25 Lakh
        assertEquals(5000000L, welfareManager.superCoverageTier.sumInsuredAmount) // ₹50 Lakh
        assertEquals(0.0, welfareManager.standardCoverageTier.driverCost, 0.001) // ₹0 cost to driver
    }

    @Test
    fun testFarmerDealEscrowAndCancellationPenalty() {
        val dealManager = FarmerDealManager()

        // 100 Quintals at ₹2400/Qtl = ₹2,40,000 Total Value
        val deal = dealManager.createFarmDeal(
            cropName = "Wheat",
            variety = "Sharbati Gold",
            quantityQuintals = 100.0,
            ratePerQuintal = 2400.0,
            farmerName = "Sukhvinder Farmer",
            farmerPhone = "+91 9876543210",
            farmerLocation = "Khanna Mandi Gate 2",
            buyerName = "Royal Agro Millers",
            buyerPhone = "+91 9123456780"
        )

        assertEquals(240000.0, deal.totalDealValue, 0.001)
        assertEquals(36000.0, deal.advanceEscrowAmount, 0.001) // 15% escrow advance token
        assertEquals(204000.0, deal.remainingBalanceAmount, 0.001) // 85% balance due
        assertEquals(DealStage.ESCROW_LOCKED, deal.stage)

        // Buyer cancellation test: 15% advance in escrow transferred to farmer as penalty
        val cancelledDeal = dealManager.cancelDealByBuyer(deal.id, "Buyer failed to send haulage truck")
        assertNotNull(cancelledDeal)
        assertEquals(DealStage.CANCELLED_BY_BUYER, cancelledDeal?.stage)
        assertEquals(36000.0, cancelledDeal?.penaltyAmount ?: 0.0, 0.001)
    }

    @Test
    fun testJobsAndEmploymentSection() {
        val jobManager = JobListingManager()
        assertTrue(jobManager.jobs.value.isNotEmpty())

        val drivers = jobManager.jobs.value.filter { it.category == JobCategory.DRIVER }
        assertTrue(drivers.isNotEmpty())

        val posted = jobManager.postJob(
            title = "Night Security Watchman",
            category = JobCategory.SECURITY_GUARD,
            employerName = "Khanna Mandi Warehousing",
            salaryDisplay = "₹18,000 / month",
            jobType = "Full-Time",
            vacancies = 3,
            location = "Khanna Mandi Gate 1",
            latitude = 28.7,
            longitude = 77.1,
            shiftTimings = "Night (8 PM - 6 AM)",
            contactPhone = "+91 9988776655",
            whatsAppNumber = "919988776655",
            requirements = listOf("Aadhaar Card", "Clean Police Record"),
            description = "Night guard duty for Mandi grain depot."
        )

        assertNotNull(posted)
        assertEquals(3, posted.vacancies)
        assertTrue(jobManager.jobs.value.any { it.id == posted.id })
    }

    @Test
    fun testOndcNetworkConnector_merchantsCatalogsAndBroadcastOrders() {
        val ondcConnector = com.example.data.network.ONDCNetworkConnector.getInstance()
        assertNotNull(ondcConnector)

        val merchants = ondcConnector.registeredMerchants.value
        assertTrue("ONDC should have registered merchants", merchants.isNotEmpty())

        val firstMerchant = merchants.first()
        val catalogs = ondcConnector.merchantCatalogs.value
        val items = catalogs[firstMerchant.id]
        assertNotNull("Merchant should have catalog items", items)
        assertTrue("Merchant catalog should not be empty", items!!.isNotEmpty())

        // Test live broadcast orders
        val broadcastOrders = ondcConnector.liveBroadcastOrders.value
        assertTrue("Live broadcast orders should be available", broadcastOrders.isNotEmpty())

        val firstOrder = broadcastOrders.first()
        val accepted = ondcConnector.acceptBroadcastOrder(
            orderId = firstOrder.id,
            riderName = "Kishan Lal",
            riderPhone = "9812233445",
            riderUpi = "kishan@upi"
        )
        assertNotNull(accepted)
        assertEquals(com.example.data.network.OndcBroadcastOrderStatus.ASSIGNED, accepted?.status)
        assertEquals("Kishan Lal", accepted?.assignedRiderName)
    }

    @Test
    fun testFuelPriceSyncManager_syncRatesAndFeedsPaymentSettlement() = kotlinx.coroutines.runBlocking {
        val paymentManager = PaymentSettlementManager.getInstance()
        val syncManager = com.example.data.manager.FuelPriceSyncManager.getInstance(paymentManager)

        val result = syncManager.syncDailyFuelRates(forceRefresh = true)
        assertTrue(result.isSuccess)

        val rates = result.getOrNull()
        assertNotNull(rates)
        assertTrue(rates!!.containsKey(com.example.data.manager.FuelType.PETROL))
        assertTrue(rates.containsKey(com.example.data.manager.FuelType.DIESEL))
        assertTrue(rates.containsKey(com.example.data.manager.FuelType.CNG))

        // Verify updated price in PaymentSettlementManager
        val petrolPrice = paymentManager.fuelPrices.value[com.example.data.manager.FuelType.PETROL] ?: 0.0
        assertTrue(petrolPrice > 80.0)
    }

    @Test
    fun testOsmCommercialPlacesManager_mandiMappingAndZones() {
        val osmManager = com.example.data.manager.OsmCommercialPlacesManager.getInstance()
        val places = osmManager.commercialPlaces.value
        assertTrue("OSM should contain commercial mandi hubs", places.isNotEmpty())

        val wholesaleMandi = places.firstOrNull { it.type == com.example.data.manager.OsmPlaceType.WHOLESALE_MANDI }
        assertNotNull(wholesaleMandi)
        assertTrue(wholesaleMandi!!.deliveryZoneRadiusKm >= 10.0)
        assertTrue(wholesaleMandi.popularGoods.isNotEmpty())

        // Test distance computation
        val dist = wholesaleMandi.distanceKmFrom(28.6139, 77.2090)
        assertTrue(dist >= 0.0)
    }

    @Test
    fun testMerchantOnboardingWorkflow_zeroHiddenListingFee() {
        val onboardManager = com.example.data.manager.MerchantOnboardingManager.getInstance()
        val presets = onboardManager.getPresetTemplatesForCategory(com.example.data.network.OndcCategory.KIRANA)
        assertTrue("Should provide preset templates", presets.isNotEmpty())

        val newMerchant = onboardManager.onboardMerchantWithCatalog(
            storeName = "Kisan Seva Kirana Kendra",
            ownerName = "Mohanlal Sharma",
            category = com.example.data.network.OndcCategory.KIRANA,
            phone = "9811122233",
            upiId = "mohanlal@upi",
            address = "Shop 12, Grain Mandi, Rohtak",
            district = "Rohtak",
            latitude = 28.8955,
            longitude = 76.6066,
            selectedPresets = presets.take(3),
            customItems = emptyList()
        )

        assertNotNull(newMerchant)
        assertTrue(newMerchant.zeroCommissionVerified)
        assertEquals("Mohanlal Sharma", newMerchant.ownerName)

        val ondcConnector = com.example.data.network.ONDCNetworkConnector.getInstance()
        val registered = ondcConnector.registeredMerchants.value.find { it.id == newMerchant.id }
        assertNotNull("Merchant must be registered in ONDC connector", registered)

        val merchantCatalog = ondcConnector.merchantCatalogs.value[newMerchant.id]
        assertNotNull(merchantCatalog)
        assertEquals(3, merchantCatalog?.size)
        assertTrue(merchantCatalog!!.all { it.zeroListingFee })
    }

    @Test
    fun testPaymentSettlementManager_liveFuelIndexedPerKmPayoutAdjustments() {
        val paymentManager = PaymentSettlementManager.getInstance()

        // 1. Petrol test for Hyperlocal Bike Delivery (10 km)
        val petrolPayout = paymentManager.calculateFuelLinkedPayout(
            distanceKm = 10.0,
            fuelType = com.example.data.manager.FuelType.PETROL,
            baseFare = 30.0
        )
        assertNotNull(petrolPayout)
        assertEquals(com.example.data.manager.FuelType.PETROL, petrolPayout.fuelType)
        assertEquals(10.0, petrolPayout.distanceKm, 0.001)
        assertEquals(30.0, petrolPayout.baseFare, 0.001)
        assertEquals(12.0, petrolPayout.basePerKmRate, 0.001)
        assertEquals(120.0, petrolPayout.baseDistanceFare, 0.001)
        assertTrue("Petrol surcharge should be computed based on current price", petrolPayout.totalFuelSurcharge >= 0.0)
        assertTrue("Gross payout must include base, distance, and fuel surcharge", petrolPayout.grossPayout >= 150.0)
        // Platform 3% fee deduction & instant UPI payout
        assertEquals(petrolPayout.grossPayout, petrolPayout.breakdown.grossAmount, 0.001)
        assertTrue(petrolPayout.breakdown.netPayoutAmount > 0.0)

        // 2. Diesel test for Cargo Loader / Tata Ace (25 km)
        val dieselPayout = paymentManager.calculateFuelLinkedPayout(
            distanceKm = 25.0,
            fuelType = com.example.data.manager.FuelType.DIESEL,
            baseFare = 50.0
        )
        assertNotNull(dieselPayout)
        assertEquals(com.example.data.manager.FuelType.DIESEL, dieselPayout.fuelType)
        assertEquals(25.0, dieselPayout.basePerKmRate, 0.001)
        assertEquals(625.0, dieselPayout.baseDistanceFare, 0.001)
        assertTrue(dieselPayout.grossPayout >= 675.0)

        // 3. CNG test for Auto / Tempo
        val cngPayout = paymentManager.calculateFuelLinkedPayout(
            distanceKm = 15.0,
            fuelType = com.example.data.manager.FuelType.CNG,
            baseFare = 25.0
        )
        assertNotNull(cngPayout)
        assertEquals(16.0, cngPayout.basePerKmRate, 0.001)
        assertEquals(240.0, cngPayout.baseDistanceFare, 0.001)
    }

    @Test
    fun testOrderDispatchManager_smartReturnRouteMatchingAndChainedDeliveries() {
        val dispatchManager = com.example.data.manager.OrderDispatchManager.getInstance()
        val parcelManager = com.example.data.manager.ParcelDeliveryManager.getInstance()
        assertNotNull(dispatchManager)
        assertNotNull(parcelManager)

        // Compute smart return corridor matches from active parcel orders
        val matches = dispatchManager.findSmartReturnMatches(parcelManager.orders.value)
        assertTrue("Smart return matches must be computed for active orders", matches.isNotEmpty())

        // Verify active rider profile
        val rider = dispatchManager.activeRider.value
        assertNotNull(rider)
        assertTrue(rider.name.isNotBlank())
        assertTrue(rider.directEmptyReturnDistanceKm > 0.0)

        val returnMatches = dispatchManager.returnMatches.value
        assertTrue("Return order matches must be identified in state flow", returnMatches.isNotEmpty())

        val topMatch = returnMatches.first()
        assertTrue("Empty km saved must be non-negative", topMatch.emptyKmSaved >= 0.0)
        assertTrue("Synergy percentage must be > 0%", topMatch.synergyPercent > 0)
        assertTrue("Estimated rider earnings must be > ₹0", topMatch.estimatedRiderEarnings > 0.0)

        // Verify chained delivery runs
        val chainedRuns = dispatchManager.chainedRuns.value
        assertTrue("Chained delivery runs must be available", chainedRuns.isNotEmpty())

        val firstChain = chainedRuns.first()
        assertTrue("Chain must have at least 3 stops", firstChain.stops.size >= 3)
        assertTrue("Chain must save distance compared to disjoint runs", firstChain.kmSaved > 0.0)
        assertTrue("Chain must calculate fuel saved in liters", firstChain.fuelSavedLiters > 0.0)
        assertTrue("Chain must boost efficiency percent", firstChain.efficiencyBoostPercent > 0)

        // Test step completion in chain
        val initialIndex = firstChain.currentStopIndex
        val advanced = dispatchManager.advanceChainStop(firstChain.chainId, initialIndex)
        assertTrue(advanced)
        val updatedRun = dispatchManager.chainedRuns.value.find { it.chainId == firstChain.chainId }
        assertNotNull(updatedRun)
        assertTrue(updatedRun!!.currentStopIndex > initialIndex || updatedRun.status == "COMPLETED")
    }

    @Test
    fun testSafetyProtocolManager_panicSosAlertAndHomeVisitTracking() {
        val safetyManager = com.example.data.manager.SafetyProtocolManager.getInstance(context)
        assertNotNull(safetyManager)

        // Start active home visit tracking
        safetyManager.startHomeVisitTracking(
            providerName = "Pooja Verma",
            providerPhone = "+91 98112 34567",
            customerName = "Mrs. Sunita Kapoor",
            customerAddress = "Flat 102, Blossom Heights, Sector 45",
            startLat = 28.6200,
            startLng = 77.2100
        )

        val activeSession = safetyManager.activeSession.value
        assertNotNull("Active session must be set", activeSession)
        assertEquals("Pooja Verma", activeSession?.providerName)
        assertEquals("Mrs. Sunita Kapoor", activeSession?.customerName)
        assertTrue("Tracking must be active", activeSession?.isTrackingActive == true)
        assertTrue("Emergency contacts must include 112 and 1091", activeSession!!.emergencyContacts.any { it.contains("112") })
        assertTrue("Emergency contacts must include 1091", activeSession.emergencyContacts.any { it.contains("1091") })

        // Trigger 1-Tap Discreet Panic Alert
        val alertMessage = safetyManager.triggerDiscretePanicAlert("Discreet Panic SOS Pressed")
        assertTrue(safetyManager.isPanicAlertActive.value)
        assertTrue(alertMessage.contains("LOKSETU SAFETY ALERT"))
        assertTrue(alertMessage.contains("Pooja Verma"))
        assertTrue(alertMessage.contains("maps.google.com"))

        // Verify safety log
        val logs = safetyManager.safetyLog.value
        assertTrue("Safety log must record critical panic dispatch", logs.any { it.contains("CRITICAL: Discreet Panic SOS Pressed") || it.contains("Auto-broadcasted") })

        // Resolve panic alert
        safetyManager.resolvePanicAlert()
        assertFalse("Panic alert must be resolved", safetyManager.isPanicAlertActive.value)
    }

    @Test
    fun testServiceEscrowManager_homeBeautyNursingUpfrontTravelLock() {
        val escrowManager = com.example.data.manager.ServiceEscrowManager.getInstance()
        assertNotNull(escrowManager)

        val initialLocked = escrowManager.totalEscrowLocked.value

        // Lock 100% upfront booking: Service Fee ₹1200 + Travel Allowance ₹150
        val booking = escrowManager.lockUpfrontEscrowBooking(
            providerId = 301L,
            providerName = "Sister Anjali Joseph",
            providerPhone = "+91 98711 65432",
            providerCategory = "HOME_HEALTHCARE",
            customerName = "Mr. Verma",
            customerPhone = "+91 98111 00223",
            customerAddress = "House 14, Defense Enclave",
            serviceFee = 1200.0,
            travelAllowance = 150.0
        )

        assertNotNull(booking)
        assertEquals(1350.0, booking.totalLockedAmount, 0.001)
        assertEquals(com.example.data.manager.EscrowBookingStatus.PAYMENT_LOCKED, booking.status)
        assertFalse(booking.isOtpVerified)
        assertEquals(4, booking.completionOtp.length)

        // Verify updated locked pool
        assertTrue(escrowManager.totalEscrowLocked.value >= initialLocked + 1350.0)

        // Advance to En Route
        val enRouteSuccess = escrowManager.updateBookingStatus(booking.id, com.example.data.manager.EscrowBookingStatus.PROVIDER_EN_ROUTE)
        assertTrue(enRouteSuccess)
        val enRouteBooking = escrowManager.bookings.value.find { it.id == booking.id }
        assertEquals(com.example.data.manager.EscrowBookingStatus.PROVIDER_EN_ROUTE, enRouteBooking?.status)

        // Advance to Work in Progress
        val inProgressSuccess = escrowManager.updateBookingStatus(booking.id, com.example.data.manager.EscrowBookingStatus.WORK_IN_PROGRESS)
        assertTrue(inProgressSuccess)
        val inProgressBooking = escrowManager.bookings.value.find { it.id == booking.id }
        assertEquals(com.example.data.manager.EscrowBookingStatus.WORK_IN_PROGRESS, inProgressBooking?.status)

        // Incorrect OTP verification test
        val failedResult = escrowManager.verifyOtpAndDisburse(booking.id, "0000")
        assertFalse(failedResult.first)

        // Correct OTP verification & Instant Disbursement test
        val successResult = escrowManager.verifyOtpAndDisburse(booking.id, booking.completionOtp)
        assertTrue(successResult.first)
        assertTrue(successResult.second.contains("successfully released"))

        val finalized = escrowManager.bookings.value.find { it.id == booking.id }
        assertNotNull(finalized)
        assertTrue(finalized!!.isOtpVerified)
        assertEquals(com.example.data.manager.EscrowBookingStatus.COMPLETED, finalized.status)
    }

    @Test
    fun testCustomerRatingManager_disputeWorkflowsAndRepeatOffenderAutoBlocking() {
        val ratingManager = com.example.data.manager.CustomerRatingManager.getInstance()
        assertNotNull(ratingManager)

        // 1. Submit good review for courteous customer
        val goodProfile = ratingManager.submitProviderRatingForCustomer(
            customerPhone = "+91 98123 44556",
            customerName = "Aarav Sharma",
            providerName = "Pooja Verma",
            providerRole = "Home Beautician",
            ratingScore = 5,
            flagType = com.example.data.manager.DisputeFlagType.EXCELLENT_CLIENT,
            feedbackNote = "Very polite customer and verified OTP immediately."
        )
        assertNotNull(goodProfile)
        assertFalse("Polite customer must not be blocked", goodProfile.isPlatformBlocked)
        assertEquals(5.0f, goodProfile.averageSafetyScore, 0.01f)

        // 2. Submit critical harassment dispute for bad customer
        val badCustomerPhone = "+91 99999 88888"
        val blockedProfile = ratingManager.submitProviderRatingForCustomer(
            customerPhone = badCustomerPhone,
            customerName = "Hostile Client",
            providerName = "Sister Anjali",
            providerRole = "Home Nurse",
            ratingScore = 1,
            flagType = com.example.data.manager.DisputeFlagType.UNSAFE_BEHAVIOR,
            feedbackNote = "Aggressive threats made towards home nurse."
        )

        assertNotNull(blockedProfile)
        assertTrue("Harassment / Unsafe Behavior flag must immediately trigger platform block", blockedProfile.isPlatformBlocked)
        val fetchedProfile = ratingManager.getCustomerSafetyProfile(badCustomerPhone)
        assertTrue(fetchedProfile.isPlatformBlocked)
        assertTrue(blockedProfile.harassmentFlagsCount >= 1)

        // 3. Repeat non-payment offender test
        val nonPayerPhone = "+91 88888 77777"
        ratingManager.submitProviderRatingForCustomer(
            customerPhone = nonPayerPhone,
            customerName = "Defaulter One",
            providerName = "Sunita Devi",
            providerRole = "Domestic Helper",
            ratingScore = 1,
            flagType = com.example.data.manager.DisputeFlagType.NON_PAYMENT,
            feedbackNote = "Refused agreed ₹350 payment."
        )
        val nonPayerProfile2 = ratingManager.submitProviderRatingForCustomer(
            customerPhone = nonPayerPhone,
            customerName = "Defaulter One",
            providerName = "Rajesh Plumber",
            providerRole = "Plumber",
            ratingScore = 1,
            flagType = com.example.data.manager.DisputeFlagType.NON_PAYMENT,
            feedbackNote = "Second refusal to pay service visit fee."
        )

        assertTrue("Repeated non-payment flags must trigger platform auto-block", nonPayerProfile2.isPlatformBlocked)
        val nonPayerFetched = ratingManager.getCustomerSafetyProfile(nonPayerPhone)
        assertTrue(nonPayerFetched.isPlatformBlocked)
    }
}
