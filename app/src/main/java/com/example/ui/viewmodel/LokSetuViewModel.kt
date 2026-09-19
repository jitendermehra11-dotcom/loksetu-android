package com.example.ui.viewmodel

import android.app.Application
import android.content.Context
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.local.LokSetuDatabase
import com.example.data.manager.AdWelfareFundManager
import com.example.data.manager.ChainedDeliveryRun
import com.example.data.manager.DeliveryPaymentMode
import com.example.data.manager.FarmerDealManager
import com.example.data.manager.FuelIndexState
import com.example.data.manager.FuelLinkedPayoutResult
import com.example.data.manager.FuelType
import com.example.data.manager.JobListingManager
import com.example.data.manager.OrderDispatchManager
import com.example.data.manager.ParcelDeliveryManager
import com.example.data.manager.ParcelDeliveryOrder
import com.example.data.manager.ParcelOrderStatus
import com.example.data.manager.CatalogPresetTemplate
import com.example.data.manager.FuelPriceSyncManager
import com.example.data.manager.FuelSyncStatus
import com.example.data.manager.MerchantOnboardingManager
import com.example.data.manager.OsmCommercialPlace
import com.example.data.manager.OsmCommercialPlacesManager
import com.example.data.manager.OsmPlaceType
import com.example.data.network.ONDCNetworkConnector
import com.example.data.network.OndcBroadcastOrder
import com.example.data.network.OndcBroadcastOrderStatus
import com.example.data.network.OndcCategory
import com.example.data.network.OndcMerchant
import com.example.data.network.OndcProductItem
import com.example.data.manager.PaymentBreakdown
import com.example.data.manager.PaymentSettlementManager
import com.example.data.manager.RiderProfile
import com.example.data.manager.SettlementRecord
import com.example.data.manager.SmartReturnOrderMatch
import com.example.data.manager.SosEmergencyManager
import com.example.data.manager.ServiceEscrowManager
import com.example.data.manager.SafetyProtocolManager
import com.example.data.manager.CustomerRatingManager
import com.example.data.manager.DisputeFlagType
import com.example.data.model.ContactHistoryEntity
import com.example.data.model.JobCategory
import com.example.data.model.JobListing
import com.example.data.model.ProviderCategory
import com.example.data.model.ProviderEntity
import com.example.data.model.UserLocation
import com.example.data.repository.LokSetuRepository
import com.example.location.LocationHelper
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

enum class AppSection(val label: String, val subtitle: String) {
    PROVIDERS("Directory", "Farmers, Workers & Drivers"),
    STORE_DELIVERY("Store Delivery", "Pre-paid Escrow & OTP Delivery"),
    JOBS("Jobs & Hiring", "Verified Driver, Helper & Guard Openings"),
    DIRECT_DEALS("Farm Deals", "15% Advance Escrow & Penalty Protection"),
    WELFARE_FUND("Driver Welfare", "₹25-50L Group Accident Pool (₹0 Cost)"),
    SETTLEMENT("Settlement", "3% Fee, ₹1 Insurance, 97% Payout")
}

enum class AppLanguage(val code: String, val displayName: String, val nativeName: String) {
    HINDI("hi", "Hindi", "हिंदी"),
    ENGLISH("en", "English", "English")
}

class LokSetuViewModel(application: Application) : AndroidViewModel(application) {

    private val repository: LokSetuRepository

    // Multi-Language State (Default to Hindi for accessibility, 1-tap English toggle)
    private val _currentLanguage = MutableStateFlow(AppLanguage.HINDI)
    val currentLanguage: StateFlow<AppLanguage> = _currentLanguage.asStateFlow()

    // Terms of Use & Legal Guidelines Sheet State
    private val _isTermsSheetOpen = MutableStateFlow(false)
    val isTermsSheetOpen: StateFlow<Boolean> = _isTermsSheetOpen.asStateFlow()

    // Core Managers for Modules
    val paymentManager = PaymentSettlementManager.getInstance()
    val sosManager = SosEmergencyManager(application)
    val welfareManager = AdWelfareFundManager.getInstance()
    val dealManager = FarmerDealManager.getInstance()
    val jobManager = JobListingManager.getInstance()
    val parcelManager = ParcelDeliveryManager.getInstance()
    val dispatchManager = OrderDispatchManager.getInstance()
    val ondcConnector = ONDCNetworkConnector.getInstance()
    val fuelSyncManager = FuelPriceSyncManager.getInstance(paymentManager)
    val osmPlacesManager = OsmCommercialPlacesManager.getInstance()
    val merchantOnboardingManager = MerchantOnboardingManager.getInstance()
    val serviceEscrowManager = ServiceEscrowManager.getInstance()
    val homeVisitSafetyManager = SafetyProtocolManager.getInstance(application)
    val customerRatingManager = CustomerRatingManager.getInstance()

    // Home Service Escrow Dialog State
    private val _isServiceEscrowOpen = MutableStateFlow(false)
    val isServiceEscrowOpen: StateFlow<Boolean> = _isServiceEscrowOpen.asStateFlow()

    private val _selectedProviderForEscrow = MutableStateFlow<ProviderEntity?>(null)
    val selectedProviderForEscrow: StateFlow<ProviderEntity?> = _selectedProviderForEscrow.asStateFlow()

    // Home Visit & Women Safety Sheet State
    private val _isHomeVisitSafetyOpen = MutableStateFlow(false)
    val isHomeVisitSafetyOpen: StateFlow<Boolean> = _isHomeVisitSafetyOpen.asStateFlow()

    // Customer Rating & Dispute Sheet State
    private val _isCustomerRatingOpen = MutableStateFlow(false)
    val isCustomerRatingOpen: StateFlow<Boolean> = _isCustomerRatingOpen.asStateFlow()

    // ONDC Open Commerce Network Flows
    val ondcMerchants: StateFlow<List<OndcMerchant>> = ondcConnector.registeredMerchants
    val ondcBroadcastOrders: StateFlow<List<OndcBroadcastOrder>> = ondcConnector.liveBroadcastOrders
    val ondcMerchantCatalogs: StateFlow<Map<String, List<OndcProductItem>>> = ondcConnector.merchantCatalogs
    val ondcStatusMessage: StateFlow<String?> = ondcConnector.networkStatusMessage
    val isOndcSyncing: StateFlow<Boolean> = ondcConnector.isSyncing

    // Automated Daily Fuel Price Sync Flow
    val fuelSyncStatus: StateFlow<FuelSyncStatus> = fuelSyncManager.syncStatus

    // OpenStreetMap (OSM) Commercial Places Layer Flows
    val osmCommercialPlaces: StateFlow<List<OsmCommercialPlace>> = osmPlacesManager.commercialPlaces
    val isOsmLoading: StateFlow<Boolean> = osmPlacesManager.isLoading

    // Dialog and Sheet states for Automated API Connectors
    private val _isOndcExplorerOpen = MutableStateFlow(false)
    val isOndcExplorerOpen: StateFlow<Boolean> = _isOndcExplorerOpen.asStateFlow()

    private val _selectedOndcMerchantForCatalog = MutableStateFlow<OndcMerchant?>(null)
    val selectedOndcMerchantForCatalog: StateFlow<OndcMerchant?> = _selectedOndcMerchantForCatalog.asStateFlow()

    private val _isOsmPlacesOpen = MutableStateFlow(false)
    val isOsmPlacesOpen: StateFlow<Boolean> = _isOsmPlacesOpen.asStateFlow()

    private val _isMerchantOnboardingOpen = MutableStateFlow(false)
    val isMerchantOnboardingOpen: StateFlow<Boolean> = _isMerchantOnboardingOpen.asStateFlow()

    // Store Delivery & Pre-paid Escrow Flows
    val parcelOrders: StateFlow<List<ParcelDeliveryOrder>> = parcelManager.orders
    val totalDeliveryEscrowSecured: StateFlow<Double> = parcelManager.totalEscrowSecured
    val totalRiderPayoutsSettled: StateFlow<Double> = parcelManager.totalRiderPayoutsSettled
    val totalMerchantPayoutsSettled: StateFlow<Double> = parcelManager.totalMerchantPayoutsSettled

    // Dynamic Fuel-Linked Index Flows
    val fuelIndexState: StateFlow<FuelIndexState> = paymentManager.fuelIndexState
    val fuelPrices: StateFlow<Map<FuelType, Double>> = paymentManager.fuelPrices

    // Smart Return Trip Routing & Multi-Stop Chain Delivery Flows
    val activeRider: StateFlow<RiderProfile> = dispatchManager.activeRider
    val returnMatches: StateFlow<List<SmartReturnOrderMatch>> = dispatchManager.returnMatches
    val chainedRuns: StateFlow<List<ChainedDeliveryRun>> = dispatchManager.chainedRuns
    val totalEmptyMilesSavedKm: StateFlow<Double> = dispatchManager.totalEmptyMilesSavedKm
    val totalChainFuelSavedLiters: StateFlow<Double> = dispatchManager.totalChainFuelSavedLiters

    private val _isCreateParcelOrderOpen = MutableStateFlow(false)
    val isCreateParcelOrderOpen: StateFlow<Boolean> = _isCreateParcelOrderOpen.asStateFlow()

    private val _selectedOrderForOtpVerification = MutableStateFlow<ParcelDeliveryOrder?>(null)
    val selectedOrderForOtpVerification: StateFlow<ParcelDeliveryOrder?> = _selectedOrderForOtpVerification.asStateFlow()

    private val _otpVerificationMessage = MutableStateFlow<String?>(null)
    val otpVerificationMessage: StateFlow<String?> = _otpVerificationMessage.asStateFlow()

    // Active Navigation Section
    private val _currentSection = MutableStateFlow(AppSection.PROVIDERS)
    val currentSection: StateFlow<AppSection> = _currentSection.asStateFlow()

    // User location state (default to Delhi Mandi area, updated with high-accuracy GPS)
    private val _userLocation = MutableStateFlow(
        UserLocation(
            latitude = 28.6139,
            longitude = 77.2090,
            address = "Connaught Place / Central Mandi, Delhi",
            accuracyMeters = 5.0f,
            isManualPin = false
        )
    )
    val userLocation: StateFlow<UserLocation> = _userLocation.asStateFlow()

    private val _isDetectingLocation = MutableStateFlow(false)
    val isDetectingLocation: StateFlow<Boolean> = _isDetectingLocation.asStateFlow()

    // Filters
    private val _selectedCategory = MutableStateFlow<String?>(null)
    val selectedCategory: StateFlow<String?> = _selectedCategory.asStateFlow()

    private val _searchQuery = MutableStateFlow("")
    val searchQuery: StateFlow<String> = _searchQuery.asStateFlow()

    private val _filterAvailableOnly = MutableStateFlow(false)
    val filterAvailableOnly: StateFlow<Boolean> = _filterAvailableOnly.asStateFlow()

    private val _filterVerifiedOnly = MutableStateFlow(false)
    val filterVerifiedOnly: StateFlow<Boolean> = _filterVerifiedOnly.asStateFlow()

    private val _sortByDistance = MutableStateFlow(true)
    val sortByDistance: StateFlow<Boolean> = _sortByDistance.asStateFlow()

    private val _radiusFilterKm = MutableStateFlow<Double?>(null) // null = all
    val radiusFilterKm: StateFlow<Double?> = _radiusFilterKm.asStateFlow()

    // Dialog & sheet states
    private val _selectedProviderForDetail = MutableStateFlow<ProviderEntity?>(null)
    val selectedProviderForDetail: StateFlow<ProviderEntity?> = _selectedProviderForDetail.asStateFlow()

    private val _selectedProviderForWhatsApp = MutableStateFlow<ProviderEntity?>(null)
    val selectedProviderForWhatsApp: StateFlow<ProviderEntity?> = _selectedProviderForWhatsApp.asStateFlow()

    private val _isLocationPickerOpen = MutableStateFlow(false)
    val isLocationPickerOpen: StateFlow<Boolean> = _isLocationPickerOpen.asStateFlow()

    private val _isAddProviderOpen = MutableStateFlow(false)
    val isAddProviderOpen: StateFlow<Boolean> = _isAddProviderOpen.asStateFlow()

    private val _isHistoryOpen = MutableStateFlow(false)
    val isHistoryOpen: StateFlow<Boolean> = _isHistoryOpen.asStateFlow()

    // 1. Payment & Settlement Dialog State
    private val _selectedProviderForSettlement = MutableStateFlow<ProviderEntity?>(null)
    val selectedProviderForSettlement: StateFlow<ProviderEntity?> = _selectedProviderForSettlement.asStateFlow()

    private val _isCustomSettlementOpen = MutableStateFlow(false)
    val isCustomSettlementOpen: StateFlow<Boolean> = _isCustomSettlementOpen.asStateFlow()

    // 2. SOS Emergency Dialog State
    private val _isSosDialogOpen = MutableStateFlow(false)
    val isSosDialogOpen: StateFlow<Boolean> = _isSosDialogOpen.asStateFlow()

    // 3. Ad Welfare Fund State
    val welfareLedger = welfareManager.adLedger
    val totalWelfarePool = welfareManager.totalPoolFundAccumulated
    val monthlyWelfareCollected = welfareManager.monthlyCollectedFund
    val monthlyWelfareTarget = welfareManager.monthlyTargetFund
    val activeCoveredDrivers = welfareManager.activeEnrolledDrivers

    // 4. Farmer Deals State
    private val _isCreateDealOpen = MutableStateFlow(false)
    val isCreateDealOpen: StateFlow<Boolean> = _isCreateDealOpen.asStateFlow()
    val activeFarmDeals = dealManager.activeDeals

    // 5. Jobs & Employment State
    private val _selectedJobCategory = MutableStateFlow(JobCategory.ALL)
    val selectedJobCategory: StateFlow<JobCategory> = _selectedJobCategory.asStateFlow()

    private val _jobSearchQuery = MutableStateFlow("")
    val jobSearchQuery: StateFlow<String> = _jobSearchQuery.asStateFlow()

    private val _isPostJobOpen = MutableStateFlow(false)
    val isPostJobOpen: StateFlow<Boolean> = _isPostJobOpen.asStateFlow()

    val filteredJobListings: StateFlow<List<JobListing>> = combine(
        jobManager.jobs,
        _selectedJobCategory,
        _jobSearchQuery
    ) { allJobs, category, query ->
        allJobs.filter { job ->
            val matchCategory = (category == JobCategory.ALL || job.category == category)
            val matchQuery = query.isBlank() ||
                    job.title.contains(query, ignoreCase = true) ||
                    job.employerName.contains(query, ignoreCase = true) ||
                    job.location.contains(query, ignoreCase = true) ||
                    job.requirements.any { it.contains(query, ignoreCase = true) }
            matchCategory && matchQuery
        }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    private val _userNoticeEvent = MutableSharedFlow<String>()
    val userNoticeEvent: SharedFlow<String> = _userNoticeEvent.asSharedFlow()

    init {
        val db = LokSetuDatabase.getInstance(application)
        repository = LokSetuRepository(db.providerDao())

        viewModelScope.launch {
            repository.checkAndSeedInitialData(
                _userLocation.value.latitude,
                _userLocation.value.longitude
            )
            // Initialize smart return routing matches
            dispatchManager.findSmartReturnMatches(parcelManager.orders.value)
        }
    }

    val contactHistory: StateFlow<List<ContactHistoryEntity>> = repository.contactHistory
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // Combined filtered providers
    val filteredProviders: StateFlow<List<ProviderWithDistance>> = combine(
        repository.allProviders,
        _selectedCategory,
        _searchQuery,
        _filterAvailableOnly,
        _filterVerifiedOnly,
        _sortByDistance,
        _radiusFilterKm,
        _userLocation
    ) { args: Array<Any?> ->
        @Suppress("UNCHECKED_CAST")
        val rawList = args[0] as List<ProviderEntity>
        val cat = args[1] as String?
        val query = (args[2] as String).trim().lowercase()
        val availableOnly = args[3] as Boolean
        val verifiedOnly = args[4] as Boolean
        val sortByDist = args[5] as Boolean
        val radius = args[6] as Double?
        val uLoc = args[7] as UserLocation

        val withDistances = rawList.map { provider ->
            val dist = LocationHelper.calculateDistanceKm(
                uLoc.latitude,
                uLoc.longitude,
                provider.latitude,
                provider.longitude
            )
            ProviderWithDistance(provider, dist)
        }

        var filtered = withDistances.filter { item ->
            val p = item.provider
            val matchCategory = when (cat) {
                null -> true
                ProviderCategory.CARGO_LOADER.name, ProviderCategory.LOADING_DRIVER.name ->
                    p.category == ProviderCategory.CARGO_LOADER.name || p.category == ProviderCategory.LOADING_DRIVER.name
                else -> p.category == cat
            }
            val matchQuery = query.isEmpty() ||
                    p.name.lowercase().contains(query) ||
                    p.speciality.lowercase().contains(query) ||
                    p.subCategory.lowercase().contains(query) ||
                    p.locationName.lowercase().contains(query)
            val matchAvailable = !availableOnly || p.isAvailableNow
            val matchVerified = !verifiedOnly || p.isVerified
            val matchRadius = radius == null || item.distanceKm <= radius

            matchCategory && matchQuery && matchAvailable && matchVerified && matchRadius
        }

        if (sortByDist) {
            filtered = filtered.sortedBy { it.distanceKm }
        } else {
            filtered = filtered.sortedByDescending { it.provider.rating }
        }

        filtered
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    fun detectGpsLocation(context: Context) {
        viewModelScope.launch {
            _isDetectingLocation.value = true
            try {
                val loc = LocationHelper.fetchCurrentGpsLocation(context)
                if (loc != null) {
                    _userLocation.value = loc
                    _userNoticeEvent.emit("GPS Location updated: ${loc.address} (±${loc.accuracyMeters.toInt()}m)")
                } else {
                    _userNoticeEvent.emit("Could not fetch GPS. Please ensure Location is enabled.")
                }
            } catch (e: Exception) {
                _userNoticeEvent.emit("Location error: ${e.localizedMessage}")
            } finally {
                _isDetectingLocation.value = false
            }
        }
    }

    fun updateManualLocation(lat: Double, lng: Double, address: String) {
        _userLocation.value = UserLocation(
            latitude = lat,
            longitude = lng,
            address = address,
            accuracyMeters = 2.5f,
            isManualPin = true
        )
        viewModelScope.launch {
            _userNoticeEvent.emit("Location set to: $address")
        }
    }

    fun toggleLanguage() {
        _currentLanguage.value = if (_currentLanguage.value == AppLanguage.HINDI) {
            AppLanguage.ENGLISH
        } else {
            AppLanguage.HINDI
        }
    }

    fun setLanguage(lang: AppLanguage) {
        _currentLanguage.value = lang
    }

    fun openTermsSheet() {
        _isTermsSheetOpen.value = true
    }

    fun closeTermsSheet() {
        _isTermsSheetOpen.value = false
    }

    fun setCategory(category: String?) {
        _selectedCategory.value = category
    }

    fun setSearchQuery(q: String) {
        _searchQuery.value = q
    }

    fun toggleAvailableOnly() {
        _filterAvailableOnly.value = !_filterAvailableOnly.value
    }

    fun toggleVerifiedOnly() {
        _filterVerifiedOnly.value = !_filterVerifiedOnly.value
    }

    fun toggleSortByDistance() {
        _sortByDistance.value = !_sortByDistance.value
    }

    fun setRadiusFilter(km: Double?) {
        _radiusFilterKm.value = km
    }

    fun openDetail(provider: ProviderEntity) {
        _selectedProviderForDetail.value = provider
    }

    fun closeDetail() {
        _selectedProviderForDetail.value = null
    }

    fun openWhatsAppDialog(provider: ProviderEntity) {
        _selectedProviderForWhatsApp.value = provider
    }

    fun closeWhatsAppDialog() {
        _selectedProviderForWhatsApp.value = null
    }

    fun openLocationPicker() {
        _isLocationPickerOpen.value = true
    }

    fun closeLocationPicker() {
        _isLocationPickerOpen.value = false
    }

    fun openAddProvider() {
        _isAddProviderOpen.value = true
    }

    fun closeAddProvider() {
        _isAddProviderOpen.value = false
    }

    fun openHistory() {
        _isHistoryOpen.value = true
    }

    fun closeHistory() {
        _isHistoryOpen.value = false
    }

    fun toggleFavorite(provider: ProviderEntity) {
        viewModelScope.launch {
            repository.toggleFavorite(provider.id, !provider.isFavorite)
            // Also update the selected detail if it's currently open
            if (_selectedProviderForDetail.value?.id == provider.id) {
                _selectedProviderForDetail.value = provider.copy(isFavorite = !provider.isFavorite)
            }
        }
    }

    fun executePhoneCall(context: Context, provider: ProviderEntity) {
        LocationHelper.makeDirectPhoneCall(context, provider.phone)
        viewModelScope.launch {
            repository.logContactAction(provider, "PHONE_CALL", "Dialed ${provider.phone}")
            _userNoticeEvent.emit("Calling ${provider.name}...")
        }
    }

    fun executeDirectEmergencyCall(context: Context, phoneNumber: String) {
        LocationHelper.makeDirectPhoneCall(context, phoneNumber)
        viewModelScope.launch {
            _userNoticeEvent.emit("Calling emergency responder $phoneNumber...")
        }
    }

    fun executeWhatsAppMessage(context: Context, provider: ProviderEntity, message: String) {
        LocationHelper.openWhatsAppMessage(context, provider.whatsAppNumber, message)
        viewModelScope.launch {
            repository.logContactAction(provider, "WHATSAPP_MESSAGE", message.take(60))
            _selectedProviderForWhatsApp.value = null
            _userNoticeEvent.emit("Opening WhatsApp for ${provider.name}...")
        }
    }

    fun addNewProvider(provider: ProviderEntity) {
        viewModelScope.launch {
            repository.addProvider(provider)
            _isAddProviderOpen.value = false
            _userNoticeEvent.emit("Successfully registered ${provider.name} on LokSetu!")
        }
    }

    fun clearHistory() {
        viewModelScope.launch {
            repository.clearHistory()
            _userNoticeEvent.emit("Contact history cleared")
        }
    }

    // --- 1. Payment & Settlement Actions ---
    fun openSettlementForProvider(provider: ProviderEntity) {
        _selectedProviderForSettlement.value = provider
    }

    fun closeSettlementForProvider() {
        _selectedProviderForSettlement.value = null
    }

    fun openCustomSettlement() {
        _isCustomSettlementOpen.value = true
    }

    fun closeCustomSettlement() {
        _isCustomSettlementOpen.value = false
    }

    fun processJobPayment(
        providerId: Long,
        providerName: String,
        providerCategory: String,
        providerUpiId: String,
        jobDescription: String,
        grossAmount: Double
    ): SettlementRecord {
        val record = paymentManager.processJobSettlement(
            providerId = providerId,
            providerName = providerName,
            providerCategory = providerCategory,
            providerUpiId = providerUpiId,
            jobDescription = jobDescription,
            grossAmount = grossAmount
        )
        viewModelScope.launch {
            _userNoticeEvent.emit("Settlement ₹${record.breakdown.netPayoutAmount} disbursed to ${record.providerUpiId} (3% Fee: ₹${record.breakdown.platformFeeAmount}, Ins: ₹${record.breakdown.microInsuranceAmount})")
        }
        return record
    }

    // --- 2. Emergency SOS Actions ---
    fun openSosDialog() {
        _isSosDialogOpen.value = true
    }

    fun closeSosDialog() {
        _isSosDialogOpen.value = false
        sosManager.cancelSos()
    }

    fun triggerSos(reason: String) {
        val loc = _userLocation.value
        sosManager.triggerSos(
            reason = reason,
            currentLat = loc.latitude,
            currentLng = loc.longitude,
            accuracyMeters = loc.accuracyMeters,
            address = loc.address
        )
    }

    fun cancelSos() {
        sosManager.cancelSos()
    }

    fun handleVoiceTrigger(spokenText: String): Boolean {
        val loc = _userLocation.value
        return sosManager.checkVoiceTrigger(
            spokenText = spokenText,
            currentLat = loc.latitude,
            currentLng = loc.longitude,
            accuracyMeters = loc.accuracyMeters,
            address = loc.address
        )
    }

    // --- 3. Ad Welfare Fund Actions ---
    fun contributeToWelfareFund(
        sponsorName: String,
        campaignTitle: String,
        adCategory: String,
        amount: Double = 15.0
    ) {
        welfareManager.recordAdContribution(
            sponsorName = sponsorName,
            campaignTitle = campaignTitle,
            adCategory = adCategory,
            amount = amount
        )
        viewModelScope.launch {
            _userNoticeEvent.emit("Sponsor Ad revenue +₹$amount credited to Drivers' Accidental Insurance Fund (₹0 Driver Cost)!")
        }
    }

    // --- 4. Farmer Direct Deal Actions ---
    fun openCreateDeal() {
        _isCreateDealOpen.value = true
    }

    fun closeCreateDeal() {
        _isCreateDealOpen.value = false
    }

    fun createFarmerDeal(
        cropName: String,
        variety: String,
        quantityQuintals: Double,
        ratePerQuintal: Double,
        farmerName: String,
        farmerPhone: String,
        farmerLocation: String,
        buyerName: String,
        buyerPhone: String
    ) {
        val deal = dealManager.createFarmDeal(
            cropName = cropName,
            variety = variety,
            quantityQuintals = quantityQuintals,
            ratePerQuintal = ratePerQuintal,
            farmerName = farmerName,
            farmerPhone = farmerPhone,
            farmerLocation = farmerLocation,
            buyerName = buyerName,
            buyerPhone = buyerPhone
        )
        _isCreateDealOpen.value = false
        viewModelScope.launch {
            _userNoticeEvent.emit("Deal #${deal.id} created! 15% Escrow Advance (₹${deal.advanceEscrowAmount}) successfully locked.")
        }
    }

    fun cancelDealByBuyer(dealId: String, cancellationReason: String) {
        val updated = dealManager.cancelDealByBuyer(dealId, cancellationReason)
        viewModelScope.launch {
            if (updated != null) {
                _userNoticeEvent.emit("Buyer Cancelled: 15% Escrow (₹${updated.advanceEscrowAmount}) transferred directly to farmer ${updated.farmerName}!")
            }
        }
    }

    fun cancelDealByFarmer(dealId: String, defaultReason: String) {
        val updated = dealManager.cancelDealByFarmer(dealId, defaultReason)
        viewModelScope.launch {
            if (updated != null) {
                _userNoticeEvent.emit("Farmer Defaulted: 15% Advance refunded + ₹${updated.penaltyAmount} (5% penalty) compensated to buyer.")
            }
        }
    }

    fun completeDeal(dealId: String) {
        val updated = dealManager.completeDeal(dealId)
        viewModelScope.launch {
            if (updated != null) {
                _userNoticeEvent.emit("Deal #${updated.id} Completed! 15% Escrow + 85% Balance (Total ₹${updated.totalDealValue}) settled to farmer.")
            }
        }
    }

    // --- 5. Jobs & Employment Actions ---
    fun selectSection(section: AppSection) {
        _currentSection.value = section
    }

    fun setSection(section: AppSection) {
        selectSection(section)
    }

    fun setJobCategory(category: JobCategory) {
        _selectedJobCategory.value = category
    }

    fun setJobSearchQuery(query: String) {
        _jobSearchQuery.value = query
    }

    fun openPostJob() {
        _isPostJobOpen.value = true
    }

    fun closePostJob() {
        _isPostJobOpen.value = false
    }

    fun postNewJob(
        title: String,
        category: JobCategory,
        employerName: String,
        salaryDisplay: String,
        jobType: String,
        vacancies: Int,
        location: String,
        shiftTimings: String,
        contactPhone: String,
        whatsAppNumber: String,
        requirements: List<String>,
        description: String
    ) {
        val loc = _userLocation.value
        val job = jobManager.postJob(
            title = title,
            category = category,
            employerName = employerName,
            salaryDisplay = salaryDisplay,
            jobType = jobType,
            vacancies = vacancies,
            location = location,
            latitude = loc.latitude,
            longitude = loc.longitude,
            shiftTimings = shiftTimings,
            contactPhone = contactPhone,
            whatsAppNumber = whatsAppNumber,
            requirements = requirements,
            description = description
        )
        _isPostJobOpen.value = false
        viewModelScope.launch {
            _userNoticeEvent.emit("Verified Job Opening posted: ${job.title} (${job.vacancies} vacancies)")
        }
    }

    fun applyJobViaPhone(context: Context, job: JobListing) {
        LocationHelper.makeDirectPhoneCall(context, job.contactPhone)
        viewModelScope.launch {
            _userNoticeEvent.emit("Calling ${job.employerName} for ${job.title}...")
        }
    }

    fun applyJobViaWhatsApp(context: Context, job: JobListing) {
        val appMsg = "Namaste, I found your verified opening for '${job.title}' on LokSetu. I am interested and available to join. Please let me know the joining formalities."
        LocationHelper.openWhatsAppMessage(context, job.whatsAppNumber, appMsg)
        viewModelScope.launch {
            _userNoticeEvent.emit("Opening WhatsApp to apply for ${job.title}...")
        }
    }

    // ------------------------------------------------------------------------
    // Store-to-Customer Parcel Delivery & Payment Safety Methods
    // ------------------------------------------------------------------------

    fun openCreateParcelOrder() {
        _isCreateParcelOrderOpen.value = true
    }

    fun closeCreateParcelOrder() {
        _isCreateParcelOrderOpen.value = false
    }

    fun openOtpVerification(order: ParcelDeliveryOrder) {
        _selectedOrderForOtpVerification.value = order
        _otpVerificationMessage.value = null
    }

    fun closeOtpVerification() {
        _selectedOrderForOtpVerification.value = null
        _otpVerificationMessage.value = null
    }

    fun createPrepaidParcelOrder(
        storeName: String,
        storeAddress: String,
        storePhone: String,
        storeUpiId: String,
        customerName: String,
        customerPhone: String,
        customerAddress: String,
        itemsDescription: String,
        itemCost: Double,
        deliveryFee: Double,
        distanceKm: Double = 5.2,
        fuelType: FuelType = FuelType.PETROL,
        fuelSurchargeBonus: Double = 0.0
    ) {
        val order = parcelManager.createPrepaidEscrowOrder(
            storeName = storeName,
            storeAddress = storeAddress,
            storePhone = storePhone,
            storeUpiId = storeUpiId,
            customerName = customerName,
            customerPhone = customerPhone,
            customerAddress = customerAddress,
            itemsDescription = itemsDescription,
            itemCost = itemCost,
            deliveryFee = deliveryFee,
            distanceKm = distanceKm,
            fuelType = fuelType,
            fuelSurchargeBonus = fuelSurchargeBonus
        )
        _isCreateParcelOrderOpen.value = false
        dispatchManager.findSmartReturnMatches(parcelOrders.value)
        viewModelScope.launch {
            _userNoticeEvent.emit("🛡️ Order ${order.id} locked in 100% Escrow (₹${order.totalAmount}). Rider spends ₹0 at store.")
        }
    }

    fun createCodParcelOrder(
        storeName: String,
        storeAddress: String,
        storePhone: String,
        storeUpiId: String,
        customerName: String,
        customerPhone: String,
        customerAddress: String,
        itemsDescription: String,
        itemCost: Double,
        deliveryFee: Double,
        customerConfirmed: Boolean,
        distanceKm: Double = 4.8,
        fuelType: FuelType = FuelType.PETROL,
        fuelSurchargeBonus: Double = 0.0
    ) {
        val order = parcelManager.createCodOrder(
            storeName = storeName,
            storeAddress = storeAddress,
            storePhone = storePhone,
            storeUpiId = storeUpiId,
            customerName = customerName,
            customerPhone = customerPhone,
            customerAddress = customerAddress,
            itemsDescription = itemsDescription,
            itemCost = itemCost,
            deliveryFee = deliveryFee,
            customerConfirmed = customerConfirmed,
            distanceKm = distanceKm,
            fuelType = fuelType,
            fuelSurchargeBonus = fuelSurchargeBonus
        )
        _isCreateParcelOrderOpen.value = false
        dispatchManager.findSmartReturnMatches(parcelOrders.value)
        viewModelScope.launch {
            _userNoticeEvent.emit("💵 COD Order ${order.id} confirmed upfront by customer. Ready for rider assignment.")
        }
    }

    fun assignRiderToOrder(
        orderId: String,
        riderName: String,
        riderPhone: String,
        riderUpiId: String,
        vehicleType: String
    ) {
        val ok = parcelManager.assignRider(orderId, riderName, riderPhone, riderUpiId, vehicleType)
        if (ok) {
            viewModelScope.launch {
                _userNoticeEvent.emit("🏍️ Rider $riderName assigned to Order $orderId (heading to store)")
            }
        }
    }

    fun confirmPickupFromStore(orderId: String) {
        val ok = parcelManager.confirmPickupFromStore(orderId)
        if (ok) {
            viewModelScope.launch {
                _userNoticeEvent.emit("📦 Parcel picked up from merchant (₹0 paid by rider). Out for delivery!")
            }
        }
    }

    fun verifyOrderDeliveryOtp(orderId: String, enteredOtp: String): Boolean {
        when (val res = parcelManager.verifyOtpAndCompleteDelivery(orderId, enteredOtp)) {
            is ParcelDeliveryManager.OtpVerificationResult.Success -> {
                _otpVerificationMessage.value = "SUCCESS: Instant UPI Split Settled! Merchant: ₹${res.merchantPayout} (${res.merchantTxn}), Rider: ₹${res.riderPayout} (${res.riderTxn})"
                viewModelScope.launch {
                    _userNoticeEvent.emit("✅ 4-Digit OTP Verified! Instant UPI Payout Split Settled to Merchant & Rider.")
                }
                return true
            }
            is ParcelDeliveryManager.OtpVerificationResult.Error -> {
                _otpVerificationMessage.value = res.message
                return false
            }
        }
    }

    fun confirmCodUpfront(orderId: String) {
        val ok = parcelManager.confirmCodUpfront(orderId)
        if (ok) {
            viewModelScope.launch {
                _userNoticeEvent.emit("💵 COD Order confirmed upfront by customer! Protection activated.")
            }
        }
    }

    // 1. Dynamic Fuel Price Index Operations
    fun selectFuelType(fuelType: FuelType) {
        paymentManager.selectFuelType(fuelType)
        dispatchManager.findSmartReturnMatches(parcelOrders.value)
    }

    fun updateFuelPrice(fuelType: FuelType, newPrice: Double) {
        paymentManager.updateFuelPrice(fuelType, newPrice)
        viewModelScope.launch {
            _userNoticeEvent.emit("⛽ ${fuelType.displayName} price updated to ₹$newPrice! Per-km payouts adjusted.")
        }
    }

    fun resetFuelPrices() {
        paymentManager.resetToDefaultFuelPrices()
        viewModelScope.launch {
            _userNoticeEvent.emit("🔄 Fuel prices reset to daily benchmark mandi rates.")
        }
    }

    // 2. Smart Return Trip Routing Operations
    fun refreshSmartReturnMatches() {
        dispatchManager.findSmartReturnMatches(parcelOrders.value)
    }

    fun updateRiderLocation(lat: Double, lng: Double, locationName: String) {
        dispatchManager.updateRiderLocation(lat, lng, locationName)
        dispatchManager.findSmartReturnMatches(parcelOrders.value)
        viewModelScope.launch {
            _userNoticeEvent.emit("📍 Rider location updated to $locationName. Recalculated return backhauls.")
        }
    }

    fun updateRiderHomeHub(lat: Double, lng: Double, hubName: String) {
        dispatchManager.updateRiderHomeHub(lat, lng, hubName)
        dispatchManager.findSmartReturnMatches(parcelOrders.value)
        viewModelScope.launch {
            _userNoticeEvent.emit("🏠 Rider home hub set to $hubName. Corridor routing optimized.")
        }
    }

    // 3. Multi-Stop Chain Delivery Operations
    fun createMultiStopChain(primaryOrder: ParcelDeliveryOrder, returnOrder: ParcelDeliveryOrder) {
        val run = dispatchManager.createMultiStopChain(primaryOrder, returnOrder)
        viewModelScope.launch {
            _userNoticeEvent.emit("🔗 Multi-Stop Chained Run ${run.chainId} Created! Saves ${run.kmSaved} km empty miles (+${run.efficiencyBoostPercent}% efficiency).")
        }
    }

    fun advanceChainStop(chainId: String, stopNumber: Int) {
        val ok = dispatchManager.advanceChainStop(chainId, stopNumber)
        if (ok) {
            viewModelScope.launch {
                _userNoticeEvent.emit("✅ Stop $stopNumber confirmed! Multi-stop route progress updated.")
            }
        }
    }

    // ==========================================
    // Automated API & Data Connector Operations
    // ==========================================

    // 1. ONDC Protocol Integration Operations
    fun openOndcExplorer() {
        _isOndcExplorerOpen.value = true
    }

    fun closeOndcExplorer() {
        _isOndcExplorerOpen.value = false
        _selectedOndcMerchantForCatalog.value = null
    }

    fun selectOndcMerchantForCatalog(merchant: OndcMerchant?) {
        _selectedOndcMerchantForCatalog.value = merchant
    }

    fun queryOndcGateway(query: String) {
        viewModelScope.launch {
            ondcConnector.queryOndcGateway(
                queryKeyword = query,
                latitude = _userLocation.value.latitude,
                longitude = _userLocation.value.longitude
            )
        }
    }

    fun acceptOndcBroadcastOrder(broadcastOrder: OndcBroadcastOrder) {
        val activeRiderProfile = activeRider.value
        val updated = ondcConnector.acceptBroadcastOrder(
            orderId = broadcastOrder.id,
            riderName = activeRiderProfile.name,
            riderPhone = activeRiderProfile.phone,
            riderUpi = activeRiderProfile.upiId
        )
        if (updated != null) {
            // Also bridge into ParcelDeliveryManager as a verified pre-paid escrow order
            val parcelOrder = parcelManager.createPrepaidEscrowOrder(
                storeName = broadcastOrder.merchantName,
                storeAddress = broadcastOrder.pickupAddress,
                storePhone = broadcastOrder.merchantPhone,
                storeUpiId = broadcastOrder.merchantUpiId,
                customerName = broadcastOrder.customerName,
                customerPhone = broadcastOrder.customerPhone,
                customerAddress = broadcastOrder.customerAddress,
                itemsDescription = broadcastOrder.itemsSummary,
                itemCost = broadcastOrder.totalItemValue,
                deliveryFee = broadcastOrder.deliveryPayout,
                distanceKm = broadcastOrder.distanceKm,
                fuelType = paymentManager.fuelIndexState.value.fuelType
            )

            // Auto-assign to current rider
            parcelManager.assignRider(
                orderId = parcelOrder.id,
                riderName = activeRiderProfile.name,
                riderPhone = activeRiderProfile.phone,
                riderUpiId = activeRiderProfile.upiId,
                vehicleType = activeRiderProfile.vehicleType
            )

            // Recalculate return corridor backhauls
            dispatchManager.findSmartReturnMatches(parcelManager.orders.value)

            viewModelScope.launch {
                _userNoticeEvent.emit("📦 ONDC Order ${broadcastOrder.id} Accepted! Added to active deliveries with ₹${broadcastOrder.deliveryPayout.toInt()} dynamic payout.")
            }
        }
    }

    // 2. Automated Daily Fuel Price Sync Operations
    fun syncDailyFuelRates(forceRefresh: Boolean = true) {
        viewModelScope.launch {
            val result = fuelSyncManager.syncDailyFuelRates(forceRefresh = forceRefresh)
            if (result.isSuccess) {
                val rates = result.getOrNull()
                val petrol = rates?.get(FuelType.PETROL) ?: FuelType.PETROL.defaultCurrentPrice
                val diesel = rates?.get(FuelType.DIESEL) ?: FuelType.DIESEL.defaultCurrentPrice
                val cng = rates?.get(FuelType.CNG) ?: FuelType.CNG.defaultCurrentPrice
                _userNoticeEvent.emit("⛽ Daily fuel index synced! Petrol ₹$petrol, Diesel ₹$diesel, CNG ₹$cng. Dynamic rider per-km earnings updated.")
            } else {
                _userNoticeEvent.emit("⚠️ Fuel sync: Using verified local mandi benchmark rates.")
            }
        }
    }

    // 3. OpenStreetMap (OSM) Commercial Places Layer Operations
    fun openOsmPlaces() {
        _isOsmPlacesOpen.value = true
        // Pre-fetch nearby hubs based on current user GPS
        viewModelScope.launch {
            osmPlacesManager.searchPlacesNearby(
                userLat = _userLocation.value.latitude,
                userLon = _userLocation.value.longitude
            )
        }
    }

    fun closeOsmPlaces() {
        _isOsmPlacesOpen.value = false
    }

    fun searchOsmPlaces(query: String?) {
        viewModelScope.launch {
            osmPlacesManager.searchPlacesNearby(
                userLat = _userLocation.value.latitude,
                userLon = _userLocation.value.longitude,
                query = query
            )
        }
    }

    fun selectOsmPlaceAsDeliveryHub(place: OsmCommercialPlace) {
        // Update user location address / hub
        _userLocation.value = _userLocation.value.copy(
            latitude = place.latitude,
            longitude = place.longitude,
            address = place.name
        )
        // Also update rider home hub for corridor routing
        dispatchManager.updateRiderHomeHub(place.latitude, place.longitude, place.name)
        dispatchManager.findSmartReturnMatches(parcelManager.orders.value)

        viewModelScope.launch {
            _userNoticeEvent.emit("📍 Selected '${place.name}' as primary trade & delivery hub (${place.deliveryZoneRadiusKm.toInt()} km zone active).")
        }
    }

    // 4. Merchant Auto-Onboarding Operations
    fun openMerchantOnboarding() {
        _isMerchantOnboardingOpen.value = true
    }

    fun closeMerchantOnboarding() {
        _isMerchantOnboardingOpen.value = false
    }

    fun onboardMerchant(
        storeName: String,
        ownerName: String,
        category: OndcCategory,
        phone: String,
        upiId: String,
        address: String,
        district: String,
        selectedPresets: List<CatalogPresetTemplate>
    ) {
        val newMerchant = merchantOnboardingManager.onboardMerchantWithCatalog(
            storeName = storeName,
            ownerName = ownerName,
            category = category,
            phone = phone,
            upiId = upiId,
            address = address,
            district = district,
            latitude = _userLocation.value.latitude,
            longitude = _userLocation.value.longitude,
            selectedPresets = selectedPresets,
            customItems = emptyList()
        )

        // Automatically create a sample broadcast delivery request from this new store
        val order = OndcBroadcastOrder(
            merchantId = newMerchant.id,
            merchantName = newMerchant.name,
            merchantPhone = newMerchant.phone,
            merchantUpiId = newMerchant.upiId,
            pickupAddress = newMerchant.address,
            pickupLat = newMerchant.latitude,
            pickupLon = newMerchant.longitude,
            customerName = "Local Mandi Customer",
            customerPhone = "9876500000",
            customerAddress = "Near Panchayat Bhavan, ${newMerchant.district}",
            customerLat = newMerchant.latitude + 0.02,
            customerLon = newMerchant.longitude + 0.02,
            itemsSummary = selectedPresets.take(2).joinToString(", ") { it.title },
            totalItemValue = selectedPresets.take(2).sumOf { it.defaultPrice },
            deliveryPayout = 85.0,
            distanceKm = 4.2,
            status = OndcBroadcastOrderStatus.BROADCASTING
        )
        ondcConnector.broadcastNewOrder(order)

        viewModelScope.launch {
            _userNoticeEvent.emit("🎉 Store '${newMerchant.name}' Onboarded to ONDC! ${selectedPresets.size} catalog items broadcasted with 0% commission.")
        }
    }

    // --- 10. Home Service Escrow & Travel Lock Actions ---
    fun openServiceEscrow(provider: ProviderEntity? = null) {
        _selectedProviderForEscrow.value = provider
        _isServiceEscrowOpen.value = true
    }

    fun closeServiceEscrow() {
        _isServiceEscrowOpen.value = false
        _selectedProviderForEscrow.value = null
    }

    fun lockServiceEscrow(
        providerId: Long,
        providerName: String,
        providerPhone: String,
        providerCategory: String,
        customerName: String,
        customerPhone: String,
        customerAddress: String,
        serviceFee: Double,
        travelAllowance: Double
    ) {
        val booking = serviceEscrowManager.lockUpfrontEscrowBooking(
            providerId = providerId,
            providerName = providerName,
            providerPhone = providerPhone,
            providerCategory = providerCategory,
            customerName = customerName,
            customerPhone = customerPhone,
            customerAddress = customerAddress,
            serviceFee = serviceFee,
            travelAllowance = travelAllowance
        )
        // Also start active location tracking for this appointment
        homeVisitSafetyManager.startHomeVisitTracking(
            providerName = providerName,
            providerPhone = providerPhone,
            customerName = customerName,
            customerAddress = customerAddress,
            startLat = _userLocation.value.latitude,
            startLng = _userLocation.value.longitude
        )
        viewModelScope.launch {
            _userNoticeEvent.emit("🔒 100% Escrow of ₹${booking.totalLockedAmount.toInt()} (Fee ₹${serviceFee.toInt()} + Travel ₹${travelAllowance.toInt()}) LOCKED upfront for $providerName!")
        }
    }

    fun verifyEscrowOtpAndRelease(bookingId: String, enteredOtp: String): Pair<Boolean, String> {
        val result = serviceEscrowManager.verifyOtpAndDisburse(bookingId, enteredOtp)
        viewModelScope.launch {
            _userNoticeEvent.emit(result.second)
        }
        return result
    }

    // --- 11. Home-Visit & Women Safety Actions ---
    fun openHomeVisitSafety() {
        _isHomeVisitSafetyOpen.value = true
    }

    fun closeHomeVisitSafety() {
        _isHomeVisitSafetyOpen.value = false
    }

    fun triggerDiscretePanicAlert() {
        val alertMsg = homeVisitSafetyManager.triggerDiscretePanicAlert("1-Tap Discreet Panic Alert Pressed")
        viewModelScope.launch {
            _userNoticeEvent.emit("🚨 Discreet Panic Alert Dispatched to 112, 1091 & Emergency Contacts!")
        }
    }

    fun resolveDiscretePanicAlert() {
        homeVisitSafetyManager.resolvePanicAlert()
        viewModelScope.launch {
            _userNoticeEvent.emit("✅ Safety Alert Resolved. Status updated to safe.")
        }
    }

    // --- 12. Customer Safety & Dispute Rating Actions ---
    fun openCustomerRating() {
        _isCustomerRatingOpen.value = true
    }

    fun closeCustomerRating() {
        _isCustomerRatingOpen.value = false
    }

    fun submitCustomerRating(
        customerPhone: String,
        customerName: String,
        providerName: String,
        providerRole: String,
        ratingScore: Int,
        flagType: DisputeFlagType,
        feedbackNote: String
    ) {
        val profile = customerRatingManager.submitProviderRatingForCustomer(
            customerPhone = customerPhone,
            customerName = customerName,
            providerName = providerName,
            providerRole = providerRole,
            ratingScore = ratingScore,
            flagType = flagType,
            feedbackNote = feedbackNote
        )
        viewModelScope.launch {
            if (profile.isPlatformBlocked) {
                _userNoticeEvent.emit("⚠️ Customer ${profile.customerName} AUTO-BLOCKED across platform due to safety/payment violations!")
            } else {
                _userNoticeEvent.emit("✅ Verified Review recorded for ${profile.customerName} (Avg Score: ${String.format("%.1f", profile.averageSafetyScore)}★)")
            }
        }
    }
}

data class ProviderWithDistance(
    val provider: ProviderEntity,
    val distanceKm: Double
)
