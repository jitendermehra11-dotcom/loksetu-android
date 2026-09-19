package com.example.ui.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.DirectionsBike
import androidx.compose.material.icons.filled.Key
import androidx.compose.material.icons.filled.LocalShipping
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Payments
import androidx.compose.material.icons.filled.Phone
import androidx.compose.material.icons.filled.Shield
import androidx.compose.material.icons.filled.Storefront
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.ExtendedFloatingActionButton
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.data.manager.DeliveryPaymentMode
import com.example.data.manager.ParcelDeliveryOrder
import com.example.data.manager.ParcelOrderStatus
import com.example.location.LocationHelper
import com.example.ui.components.ChainedDeliveryCard
import com.example.ui.components.CreateParcelOrderDialog
import com.example.ui.components.FuelIndexingSummaryCard
import com.example.ui.components.MerchantOnboardingDialog
import com.example.ui.components.OndcNetworkExplorerDialog
import com.example.ui.components.OsmCommercialPlacesDialog
import com.example.ui.components.OtpVerificationDialog
import com.example.ui.components.ReturnCorridorHeaderCard
import com.example.ui.components.SmartReturnMatchCard
import com.example.ui.viewmodel.LokSetuViewModel

@Composable
fun ParcelDeliverySection(
    viewModel: LokSetuViewModel,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val orders by viewModel.parcelOrders.collectAsStateWithLifecycle()
    val totalEscrow by viewModel.totalDeliveryEscrowSecured.collectAsStateWithLifecycle()
    val totalRiderPayouts by viewModel.totalRiderPayoutsSettled.collectAsStateWithLifecycle()
    val totalMerchantPayouts by viewModel.totalMerchantPayoutsSettled.collectAsStateWithLifecycle()

    val fuelState by viewModel.fuelIndexState.collectAsStateWithLifecycle()
    val fuelPrices by viewModel.fuelPrices.collectAsStateWithLifecycle()
    val activeRider by viewModel.activeRider.collectAsStateWithLifecycle()
    val returnMatches by viewModel.returnMatches.collectAsStateWithLifecycle()
    val chainedRuns by viewModel.chainedRuns.collectAsStateWithLifecycle()
    val totalEmptyMilesSaved by viewModel.totalEmptyMilesSavedKm.collectAsStateWithLifecycle()

    val isCreateOpen by viewModel.isCreateParcelOrderOpen.collectAsStateWithLifecycle()
    val selectedOrderForOtp by viewModel.selectedOrderForOtpVerification.collectAsStateWithLifecycle()

    // Automated API & Data Connectors States
    val ondcMerchants by viewModel.ondcMerchants.collectAsStateWithLifecycle()
    val ondcBroadcastOrders by viewModel.ondcBroadcastOrders.collectAsStateWithLifecycle()
    val ondcCatalogs by viewModel.ondcMerchantCatalogs.collectAsStateWithLifecycle()
    val isOndcSyncing by viewModel.isOndcSyncing.collectAsStateWithLifecycle()
    val fuelSyncStatus by viewModel.fuelSyncStatus.collectAsStateWithLifecycle()
    val osmPlaces by viewModel.osmCommercialPlaces.collectAsStateWithLifecycle()
    val isOsmLoading by viewModel.isOsmLoading.collectAsStateWithLifecycle()
    val isOndcOpen by viewModel.isOndcExplorerOpen.collectAsStateWithLifecycle()
    val selectedOndcMerchant by viewModel.selectedOndcMerchantForCatalog.collectAsStateWithLifecycle()
    val isOsmOpen by viewModel.isOsmPlacesOpen.collectAsStateWithLifecycle()
    val isOnboardingOpen by viewModel.isMerchantOnboardingOpen.collectAsStateWithLifecycle()
    val userLoc by viewModel.userLocation.collectAsStateWithLifecycle()

    var filterTab by remember { mutableStateOf("ALL") } // ALL, ACTIVE, DELIVERED

    val filteredOrders = when (filterTab) {
        "ACTIVE" -> orders.filter { it.status != ParcelOrderStatus.DELIVERED_AND_SPLIT_PAID }
        "DELIVERED" -> orders.filter { it.status == ParcelOrderStatus.DELIVERED_AND_SPLIT_PAID }
        else -> orders
    }

    Scaffold(
        modifier = modifier,
        containerColor = Color.Transparent,
        floatingActionButton = {
            ExtendedFloatingActionButton(
                onClick = { viewModel.openCreateParcelOrder() },
                containerColor = MaterialTheme.colorScheme.primary,
                contentColor = MaterialTheme.colorScheme.onPrimary,
                shape = RoundedCornerShape(24.dp),
                icon = { Icon(Icons.Default.Add, contentDescription = null) },
                text = { Text("New Store Delivery", fontWeight = FontWeight.Bold) },
                modifier = Modifier.testTag("new_store_delivery_fab")
            )
        }
    ) { innerPadding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding),
            contentPadding = androidx.compose.foundation.layout.PaddingValues(bottom = 90.dp)
        ) {
            // 1. Live Escrow & Settlement Metrics
            item {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 6.dp),
                    shape = RoundedCornerShape(20.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.45f)),
                    border = BorderStroke(1.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.3f))
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "Store Delivery & Payment Safety",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.ExtraBold,
                                color = MaterialTheme.colorScheme.primary
                            )
                            Surface(
                                shape = RoundedCornerShape(12.dp),
                                color = Color(0xFF2E7D32)
                            ) {
                                Text(
                                    text = "0% Rider Out-of-Pocket",
                                    color = Color.White,
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.Bold,
                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(12.dp))

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Column {
                                Text(
                                    text = "Total Escrow Secured",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                                Text(
                                    text = "₹${totalEscrow.toInt()}",
                                    style = MaterialTheme.typography.titleLarge,
                                    fontWeight = FontWeight.Black,
                                    color = MaterialTheme.colorScheme.primary
                                )
                            }

                            Column {
                                Text(
                                    text = "Merchant Payouts",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                                Text(
                                    text = "₹${totalMerchantPayouts.toInt()}",
                                    style = MaterialTheme.typography.titleLarge,
                                    fontWeight = FontWeight.Black,
                                    color = Color(0xFF1B5E20)
                                )
                            }

                            Column {
                                Text(
                                    text = "Rider Payouts",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                                Text(
                                    text = "₹${totalRiderPayouts.toInt()}",
                                    style = MaterialTheme.typography.titleLarge,
                                    fontWeight = FontWeight.Black,
                                    color = Color(0xFF0D47A1)
                                )
                            }
                        }
                    }
                }
            }

            // 2. Automated API & Data Connectors Hub
            item {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 6.dp),
                    shape = RoundedCornerShape(18.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant)
                ) {
                    Column(modifier = Modifier.padding(14.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Surface(
                                    shape = CircleShape,
                                    color = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.6f),
                                    modifier = Modifier.size(32.dp)
                                ) {
                                    Box(contentAlignment = Alignment.Center) {
                                        Text("⚡", fontSize = 16.sp)
                                    }
                                }
                                Spacer(modifier = Modifier.width(8.dp))
                                Column {
                                    Text(
                                        text = "Automated API & Data Connectors",
                                        fontWeight = FontWeight.ExtraBold,
                                        style = MaterialTheme.typography.titleSmall
                                    )
                                    Text(
                                        text = "ONDC Beckn Gateway • Live Fuel API • OSM Mandi Layer",
                                        fontSize = 10.sp,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(10.dp))

                        // Connectors Quick Actions
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            // ONDC Protocol Explorer
                            Surface(
                                shape = RoundedCornerShape(12.dp),
                                color = Color(0xFFE8F5E9),
                                border = BorderStroke(1.dp, Color(0xFFA5D6A7)),
                                modifier = Modifier
                                    .weight(1f)
                                    .clickable { viewModel.openOndcExplorer() }
                            ) {
                                Column(modifier = Modifier.padding(10.dp)) {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Text("🌐", fontSize = 14.sp)
                                        Spacer(modifier = Modifier.width(4.dp))
                                        Text(
                                            text = "ONDC Protocol",
                                            fontWeight = FontWeight.Bold,
                                            fontSize = 11.sp,
                                            color = Color(0xFF1B5E20)
                                        )
                                    }
                                    Spacer(modifier = Modifier.height(4.dp))
                                    Text(
                                        text = "${ondcMerchants.size} Merchants • ${ondcBroadcastOrders.size} Orders",
                                        fontSize = 9.sp,
                                        color = Color(0xFF2E7D32)
                                    )
                                }
                            }

                            // OSM Mandi Layer
                            Surface(
                                shape = RoundedCornerShape(12.dp),
                                color = Color(0xFFE1F5FE),
                                border = BorderStroke(1.dp, Color(0xFF81D4FA)),
                                modifier = Modifier
                                    .weight(1f)
                                    .clickable { viewModel.openOsmPlaces() }
                            ) {
                                Column(modifier = Modifier.padding(10.dp)) {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Text("🗺️", fontSize = 14.sp)
                                        Spacer(modifier = Modifier.width(4.dp))
                                        Text(
                                            text = "OSM Mandis",
                                            fontWeight = FontWeight.Bold,
                                            fontSize = 11.sp,
                                            color = Color(0xFF01579B)
                                        )
                                    }
                                    Spacer(modifier = Modifier.height(4.dp))
                                    Text(
                                        text = "${osmPlaces.size} Wholesale Hubs",
                                        fontSize = 9.sp,
                                        color = Color(0xFF0277BD)
                                    )
                                }
                            }

                            // 60-second Merchant Auto-Onboarding
                            Surface(
                                shape = RoundedCornerShape(12.dp),
                                color = Color(0xFFFFF3E0),
                                border = BorderStroke(1.dp, Color(0xFFFFCC80)),
                                modifier = Modifier
                                    .weight(1f)
                                    .clickable { viewModel.openMerchantOnboarding() }
                            ) {
                                Column(modifier = Modifier.padding(10.dp)) {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Text("🏪", fontSize = 14.sp)
                                        Spacer(modifier = Modifier.width(4.dp))
                                        Text(
                                            text = "+ Onboard Shop",
                                            fontWeight = FontWeight.Bold,
                                            fontSize = 11.sp,
                                            color = Color(0xFFE65100)
                                        )
                                    }
                                    Spacer(modifier = Modifier.height(4.dp))
                                    Text(
                                        text = "₹0 Fee • 60s Sync",
                                        fontSize = 9.sp,
                                        color = Color(0xFFBF360C)
                                    )
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(8.dp))

                        // Automated Daily Fuel Rate Status & 1-Tap Sync Bar
                        Surface(
                            shape = RoundedCornerShape(10.dp),
                            color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(horizontal = 10.dp, vertical = 8.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column(modifier = Modifier.weight(1f)) {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Text(
                                            text = "⛽ Fuel Index API:",
                                            fontWeight = FontWeight.Bold,
                                            fontSize = 11.sp,
                                            color = MaterialTheme.colorScheme.onSurface
                                        )
                                        Spacer(modifier = Modifier.width(4.dp))
                                        Text(
                                            text = if (fuelSyncStatus.isSyncing) "Syncing..." else "Daily Synced (${fuelSyncStatus.formattedLastSync})",
                                            fontSize = 10.sp,
                                            color = if (fuelSyncStatus.isSyncing) MaterialTheme.colorScheme.primary else Color(0xFF2E7D32),
                                            fontWeight = FontWeight.SemiBold
                                        )
                                    }
                                    Text(
                                        text = "Feeds IOCL/HPCL rates directly into Rider Per-Km Payouts",
                                        fontSize = 9.sp,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }

                                androidx.compose.material3.OutlinedButton(
                                    onClick = { viewModel.syncDailyFuelRates(forceRefresh = true) },
                                    shape = RoundedCornerShape(8.dp),
                                    contentPadding = androidx.compose.foundation.layout.PaddingValues(horizontal = 8.dp, vertical = 4.dp),
                                    enabled = !fuelSyncStatus.isSyncing
                                ) {
                                    if (fuelSyncStatus.isSyncing) {
                                        androidx.compose.material3.CircularProgressIndicator(
                                            modifier = Modifier.size(12.dp),
                                            strokeWidth = 2.dp
                                        )
                                    } else {
                                        Text("Sync Now", fontSize = 10.sp, fontWeight = FontWeight.Bold)
                                    }
                                }
                            }
                        }
                    }
                }
            }

            // 3. Live Fuel Price Indexing Card
            item {
                FuelIndexingSummaryCard(
                    fuelState = fuelState,
                    fuelPrices = fuelPrices,
                    onSelectFuelType = { viewModel.selectFuelType(it) },
                    onUpdatePrice = { type, price -> viewModel.updateFuelPrice(type, price) },
                    onResetPrices = { viewModel.resetFuelPrices() }
                )
            }

            // 3. Smart Return Trip Corridor
            item {
                ReturnCorridorHeaderCard(
                    activeRider = activeRider,
                    totalEmptyMilesSaved = totalEmptyMilesSaved,
                    onRefreshMatches = { viewModel.refreshSmartReturnMatches() }
                )
            }

            // 4. Smart Return Backhaul Matches
            if (returnMatches.isNotEmpty()) {
                item {
                    Text(
                        text = "⚡ Return Corridor Backhauls (${returnMatches.size} Matches)",
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.secondary,
                        modifier = Modifier.padding(horizontal = 20.dp, vertical = 6.dp)
                    )
                }
                items(returnMatches, key = { "return_${it.order.id}" }) { match ->
                    SmartReturnMatchCard(
                        match = match,
                        onChainOrder = { returnOrder ->
                            val primaryOrder = orders.firstOrNull { it.id != returnOrder.id } ?: returnOrder
                            viewModel.createMultiStopChain(primaryOrder, returnOrder)
                        },
                        onAcceptReturnOrder = { returnOrder ->
                            viewModel.assignRiderToOrder(
                                orderId = returnOrder.id,
                                riderName = activeRider.name,
                                riderPhone = activeRider.phone,
                                riderUpiId = activeRider.upiId,
                                vehicleType = activeRider.vehicleType
                            )
                        }
                    )
                }
            }

            // 5. Multi-Stop Chained Runs
            if (chainedRuns.isNotEmpty()) {
                item {
                    Text(
                        text = "🔗 Multi-Stop Chained Runs (${chainedRuns.size})",
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.padding(horizontal = 20.dp, vertical = 6.dp)
                    )
                }
                items(chainedRuns, key = { "chain_${it.chainId}" }) { run ->
                    ChainedDeliveryCard(
                        run = run,
                        onAdvanceStop = { chainId, stopNo ->
                            viewModel.advanceChainStop(chainId, stopNo)
                        }
                    )
                }
            }

            // 6. Three Pillars of Safety Card
            item {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 6.dp),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant)
                ) {
                    Column(modifier = Modifier.padding(14.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.Shield,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.size(20.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "How LokSetu Protects Store Deliveries:",
                                style = MaterialTheme.typography.labelLarge,
                                fontWeight = FontWeight.Bold
                            )
                        }
                        Spacer(modifier = Modifier.height(8.dp))

                        Text(
                            text = "1. 🛡️ Pre-paid Escrow: 100% item cost + delivery fee locked upfront. Riders spend ZERO out-of-pocket cash at local shops.",
                            fontSize = 11.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            lineHeight = 16.sp
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "2. 🔢 4-Digit Customer OTP: Verified upon parcel drop-off to trigger instant automated UPI split to merchant & rider.",
                            fontSize = 11.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            lineHeight = 16.sp
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "3. 💵 COD Protection: Requires customer upfront confirmation before rider dispatch. Rider retains fee with automated settlement.",
                            fontSize = 11.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            lineHeight = 16.sp
                        )
                    }
                }
            }

            // 3. Filter Chips Row
            item {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 6.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    FilterChip(
                        selected = filterTab == "ALL",
                        onClick = { filterTab = "ALL" },
                        label = { Text("All Orders (${orders.size})", fontSize = 12.sp) }
                    )
                    FilterChip(
                        selected = filterTab == "ACTIVE",
                        onClick = { filterTab = "ACTIVE" },
                        label = {
                            Text(
                                "Active (${orders.count { it.status != ParcelOrderStatus.DELIVERED_AND_SPLIT_PAID }})",
                                fontSize = 12.sp
                            )
                        }
                    )
                    FilterChip(
                        selected = filterTab == "DELIVERED",
                        onClick = { filterTab = "DELIVERED" },
                        label = {
                            Text(
                                "Settled (${orders.count { it.status == ParcelOrderStatus.DELIVERED_AND_SPLIT_PAID }})",
                                fontSize = 12.sp
                            )
                        }
                    )
                }
            }

            // 4. Orders List
            items(filteredOrders, key = { it.id }) { order ->
                ParcelOrderCard(
                    order = order,
                    onVerifyOtpClick = { viewModel.openOtpVerification(order) },
                    onAssignRiderClick = {
                        viewModel.assignRiderToOrder(
                            orderId = order.id,
                            riderName = "Kishan Lal",
                            riderPhone = "+91 98122 33445",
                            riderUpiId = "kishan.bike@upi",
                            vehicleType = "🏍️ Hero Splendor (Bike Parcel)"
                        )
                    },
                    onConfirmPickupClick = {
                        viewModel.confirmPickupFromStore(order.id)
                    },
                    onConfirmCodClick = {
                        viewModel.confirmCodUpfront(order.id)
                    },
                    onCallCustomer = {
                        LocationHelper.makeDirectPhoneCall(context, order.customerPhone)
                    },
                    onCallStore = {
                        LocationHelper.makeDirectPhoneCall(context, order.storePhone)
                    }
                )
            }
        }
    }

    // Modal Sheet for New Store Parcel Order
    if (isCreateOpen) {
        CreateParcelOrderDialog(
            viewModel = viewModel,
            onDismiss = { viewModel.closeCreateParcelOrder() }
        )
    }

    // Modal Dialog for OTP Verification
    selectedOrderForOtp?.let { order ->
        OtpVerificationDialog(
            order = order,
            viewModel = viewModel,
            onDismiss = { viewModel.closeOtpVerification() }
        )
    }

    // Modal Sheet for ONDC Protocol Network Explorer
    if (isOndcOpen) {
        OndcNetworkExplorerDialog(
            merchants = ondcMerchants,
            broadcastOrders = ondcBroadcastOrders,
            catalogs = ondcCatalogs,
            selectedMerchant = selectedOndcMerchant,
            onSelectMerchant = { viewModel.selectOndcMerchantForCatalog(it) },
            onAcceptBroadcastOrder = { viewModel.acceptOndcBroadcastOrder(it) },
            onOpenOnboarding = {
                viewModel.closeOndcExplorer()
                viewModel.openMerchantOnboarding()
            },
            onDismiss = { viewModel.closeOndcExplorer() },
            userLatitude = userLoc.latitude,
            userLongitude = userLoc.longitude
        )
    }

    // Modal Sheet for OpenStreetMap Commercial Places Layer
    if (isOsmOpen) {
        OsmCommercialPlacesDialog(
            places = osmPlaces,
            isLoading = isOsmLoading,
            onSearch = { viewModel.searchOsmPlaces(it) },
            onSelectPlace = { viewModel.selectOsmPlaceAsDeliveryHub(it) },
            onDismiss = { viewModel.closeOsmPlaces() },
            userLatitude = userLoc.latitude,
            userLongitude = userLoc.longitude
        )
    }

    // Modal Sheet for Merchant Auto-Onboarding Workflow
    if (isOnboardingOpen) {
        MerchantOnboardingDialog(
            initialStoreAddress = userLoc.address,
            initialDistrict = "Delhi-NCR Mandi Corridor",
            userLatitude = userLoc.latitude,
            userLongitude = userLoc.longitude,
            onboardManager = viewModel.merchantOnboardingManager,
            onOnboardingComplete = { storeName, ownerName, category, phone, upiId, address, district, presets ->
                viewModel.onboardMerchant(
                    storeName = storeName,
                    ownerName = ownerName,
                    category = category,
                    phone = phone,
                    upiId = upiId,
                    address = address,
                    district = district,
                    selectedPresets = presets
                )
            },
            onDismiss = { viewModel.closeMerchantOnboarding() }
        )
    }
}

@Composable
fun ParcelOrderCard(
    order: ParcelDeliveryOrder,
    onVerifyOtpClick: () -> Unit,
    onAssignRiderClick: () -> Unit,
    onConfirmPickupClick: () -> Unit,
    onConfirmCodClick: () -> Unit,
    onCallCustomer: () -> Unit,
    onCallStore: () -> Unit
) {
    ElevatedCard(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 6.dp)
            .testTag("parcel_order_${order.id}"),
        shape = RoundedCornerShape(18.dp),
        colors = CardDefaults.elevatedCardColors(containerColor = MaterialTheme.colorScheme.surface)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            // Header: ID + Badge
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = order.id,
                        fontWeight = FontWeight.ExtraBold,
                        style = MaterialTheme.typography.titleMedium,
                        color = MaterialTheme.colorScheme.primary
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = order.formattedDate,
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                Surface(
                    shape = RoundedCornerShape(10.dp),
                    color = if (order.paymentMode == DeliveryPaymentMode.PREPAID_ESCROW) Color(0xFFE8F5E9) else Color(0xFFFFF3E0),
                    border = BorderStroke(1.dp, if (order.paymentMode == DeliveryPaymentMode.PREPAID_ESCROW) Color(0xFFA5D6A7) else Color(0xFFFFCC80))
                ) {
                    Text(
                        text = order.paymentMode.badge,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = if (order.paymentMode == DeliveryPaymentMode.PREPAID_ESCROW) Color(0xFF1B5E20) else Color(0xFFE65100),
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Zero Rider Spending Guarantee Tag
            if (order.riderZeroSpendingGuaranteed) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.CheckCircle,
                        contentDescription = null,
                        tint = Color(0xFF2E7D32),
                        modifier = Modifier.size(15.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = "Zero Rider Out-of-Pocket: Item cost funded by LokSetu Escrow",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = Color(0xFF2E7D32)
                    )
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Store Details
            Row(modifier = Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = Icons.Default.Storefront,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(18.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = order.storeName,
                        fontWeight = FontWeight.Bold,
                        fontSize = 13.sp
                    )
                    Text(
                        text = order.storeAddress,
                        fontSize = 11.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                IconButton(onClick = onCallStore, modifier = Modifier.size(32.dp)) {
                    Icon(
                        imageVector = Icons.Default.Phone,
                        contentDescription = "Call Store",
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(16.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Customer Details
            Row(modifier = Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = Icons.Default.LocalShipping,
                    contentDescription = null,
                    tint = Color(0xFFE65100),
                    modifier = Modifier.size(18.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = "Deliver to: ${order.customerName}",
                        fontWeight = FontWeight.Bold,
                        fontSize = 13.sp
                    )
                    Text(
                        text = order.customerAddress,
                        fontSize = 11.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                IconButton(onClick = onCallCustomer, modifier = Modifier.size(32.dp)) {
                    Icon(
                        imageVector = Icons.Default.Phone,
                        contentDescription = "Call Customer",
                        tint = Color(0xFFE65100),
                        modifier = Modifier.size(16.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Items Description
            Surface(
                shape = RoundedCornerShape(10.dp),
                color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(
                    text = "📦 Items: ${order.parcelItemsDescription}",
                    style = MaterialTheme.typography.bodySmall,
                    modifier = Modifier.padding(8.dp),
                    fontWeight = FontWeight.Medium
                )
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Financial Breakdown & Status
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = "Total Order Value",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Text(
                        text = "₹${order.totalAmount}",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Black,
                        color = MaterialTheme.colorScheme.primary
                    )
                    Text(
                        text = "Item: ₹${order.itemCost} | Rider: ₹${order.deliveryFee}",
                        fontSize = 10.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                Surface(
                    shape = RoundedCornerShape(12.dp),
                    color = when (order.status) {
                        ParcelOrderStatus.DELIVERED_AND_SPLIT_PAID -> Color(0xFFE8F5E9)
                        ParcelOrderStatus.OUT_FOR_DELIVERY -> Color(0xFFFFF8E1)
                        else -> MaterialTheme.colorScheme.secondaryContainer
                    }
                ) {
                    Text(
                        text = order.status.title,
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.Bold,
                        color = when (order.status) {
                            ParcelOrderStatus.DELIVERED_AND_SPLIT_PAID -> Color(0xFF2E7D32)
                            ParcelOrderStatus.OUT_FOR_DELIVERY -> Color(0xFFF57F17)
                            else -> MaterialTheme.colorScheme.onSecondaryContainer
                        },
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp)
                    )
                }
            }

            // Assigned Rider info
            order.assignedRiderName?.let { rider ->
                Spacer(modifier = Modifier.height(8.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Default.DirectionsBike,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = "Assigned Rider: $rider (${order.assignedVehicleType ?: "Bike"})",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = MaterialTheme.colorScheme.primary
                    )
                }
            }

            Spacer(modifier = Modifier.height(12.dp))
            HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant)
            Spacer(modifier = Modifier.height(10.dp))

            // Action Buttons based on Status
            when (order.status) {
                ParcelOrderStatus.ESCROW_LOCKED_PENDING_RIDER -> {
                    Button(
                        onClick = onAssignRiderClick,
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Icon(Icons.Default.DirectionsBike, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Assign Nearby Rider (0% Rider Out-of-Pocket)")
                    }
                }

                ParcelOrderStatus.COD_CONFIRMED_PENDING_RIDER -> {
                    Button(
                        onClick = onAssignRiderClick,
                        shape = RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFE65100)),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Icon(Icons.Default.DirectionsBike, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Assign Rider (COD Upfront Confirmed)")
                    }
                }

                ParcelOrderStatus.RIDER_ASSIGNED -> {
                    Button(
                        onClick = onConfirmPickupClick,
                        shape = RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF0288D1)),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Icon(Icons.Default.LocalShipping, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Confirm Store Pickup (₹0 Paid by Rider)")
                    }
                }

                ParcelOrderStatus.PICKED_UP_FROM_STORE,
                ParcelOrderStatus.OUT_FOR_DELIVERY -> {
                    Button(
                        onClick = onVerifyOtpClick,
                        shape = RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF2E7D32)),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Icon(Icons.Default.Key, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Verify 4-Digit Customer OTP & Disburse UPI Split", fontWeight = FontWeight.Bold)
                    }
                }

                ParcelOrderStatus.DELIVERED_AND_SPLIT_PAID -> {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(containerColor = Color(0xFFF1F8E9)),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Column(modifier = Modifier.padding(10.dp)) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    imageVector = Icons.Default.CheckCircle,
                                    contentDescription = null,
                                    tint = Color(0xFF2E7D32),
                                    modifier = Modifier.size(16.dp)
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    text = "Automated UPI Split Payout Completed:",
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 11.sp,
                                    color = Color(0xFF1B5E20)
                                )
                            }
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = "• Merchant Payout: ₹${order.itemCost} (${order.merchantPayoutTxnId ?: "UPI/MERCH/DONE"})",
                                fontSize = 10.sp,
                                color = Color(0xFF33691E)
                            )
                            Text(
                                text = "• Rider Delivery Fee: ₹${order.deliveryFee} (${order.riderPayoutTxnId ?: "UPI/RIDER/DONE"})",
                                fontSize = 10.sp,
                                color = Color(0xFF01579B)
                            )
                        }
                    }
                }

                else -> {}
            }
        }
    }
}
