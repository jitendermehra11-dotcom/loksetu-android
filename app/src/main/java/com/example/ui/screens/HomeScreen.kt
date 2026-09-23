package com.example.ui.screens

import android.Manifest
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.Image
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
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
import androidx.compose.material.icons.filled.BusinessCenter
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.Emergency
import androidx.compose.material.icons.filled.Gavel
import androidx.compose.material.icons.filled.GpsFixed
import androidx.compose.material.icons.filled.Handshake
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.LocalShipping
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Map
import androidx.compose.material.icons.filled.MyLocation
import androidx.compose.material.icons.filled.Payments
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Shield
import androidx.compose.material.icons.filled.Storefront
import androidx.compose.material.icons.filled.Translate
import androidx.compose.material.icons.filled.Tune
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExtendedFloatingActionButton
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.FilledTonalIconButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.aistudio.loksetu.vxqtmp.R
import com.example.location.LocationHelper
import com.example.ui.components.CategorySelectorRow
import com.example.ui.components.FilterOptionsRow
import com.example.ui.components.GpsMapPickerDialog
import com.example.ui.components.PaymentSettlementDialog
import com.example.ui.components.ProviderCard
import com.example.ui.components.SosEmergencyDialog
import com.example.ui.components.TermsAndLegalSheet
import com.example.ui.components.WhatsAppMessageDialog
import com.example.ui.components.ServiceEscrowDialog
import com.example.ui.components.HomeVisitSafetySheet
import com.example.ui.components.CustomerRatingDialog
import com.example.ui.viewmodel.AppLanguage
import com.example.ui.viewmodel.AppSection
import com.example.ui.viewmodel.LokSetuViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(viewModel: LokSetuViewModel) {
    val context = LocalContext.current
    val snackbarHostState = remember { SnackbarHostState() }

    val currentSection by viewModel.currentSection.collectAsStateWithLifecycle()
    val isSosDialogOpen by viewModel.isSosDialogOpen.collectAsStateWithLifecycle()
    val isCustomSettlementOpen by viewModel.isCustomSettlementOpen.collectAsStateWithLifecycle()
    val selectedProviderForSettlement by viewModel.selectedProviderForSettlement.collectAsStateWithLifecycle()

    val userLocation by viewModel.userLocation.collectAsStateWithLifecycle()
    val isDetectingLocation by viewModel.isDetectingLocation.collectAsStateWithLifecycle()
    val selectedCategory by viewModel.selectedCategory.collectAsStateWithLifecycle()
    val searchQuery by viewModel.searchQuery.collectAsStateWithLifecycle()
    val filterAvailableOnly by viewModel.filterAvailableOnly.collectAsStateWithLifecycle()
    val filterVerifiedOnly by viewModel.filterVerifiedOnly.collectAsStateWithLifecycle()
    val sortByDistance by viewModel.sortByDistance.collectAsStateWithLifecycle()
    val radiusKm by viewModel.radiusFilterKm.collectAsStateWithLifecycle()
    val providers by viewModel.filteredProviders.collectAsStateWithLifecycle()
    val contactHistory by viewModel.contactHistory.collectAsStateWithLifecycle()

    val selectedDetail by viewModel.selectedProviderForDetail.collectAsStateWithLifecycle()
    val selectedWhatsApp by viewModel.selectedProviderForWhatsApp.collectAsStateWithLifecycle()
    val isLocationPickerOpen by viewModel.isLocationPickerOpen.collectAsStateWithLifecycle()
    val isAddProviderOpen by viewModel.isAddProviderOpen.collectAsStateWithLifecycle()
    val isHistoryOpen by viewModel.isHistoryOpen.collectAsStateWithLifecycle()
    val currentLanguage by viewModel.currentLanguage.collectAsStateWithLifecycle()
    val isTermsSheetOpen by viewModel.isTermsSheetOpen.collectAsStateWithLifecycle()
    val isServiceEscrowOpen by viewModel.isServiceEscrowOpen.collectAsStateWithLifecycle()
    val selectedProviderForEscrow by viewModel.selectedProviderForEscrow.collectAsStateWithLifecycle()
    val isHomeVisitSafetyOpen by viewModel.isHomeVisitSafetyOpen.collectAsStateWithLifecycle()
    val isCustomerRatingOpen by viewModel.isCustomerRatingOpen.collectAsStateWithLifecycle()
    val isHindi = currentLanguage == AppLanguage.HINDI

    // Permission launcher for Location & Call Phone
    val permissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestMultiplePermissions()
    ) { perms ->
        val fineGranted = perms[Manifest.permission.ACCESS_FINE_LOCATION] == true
        val coarseGranted = perms[Manifest.permission.ACCESS_COARSE_LOCATION] == true
        if (fineGranted || coarseGranted) {
            viewModel.detectGpsLocation(context)
        }
    }

    // Launch location detection once at startup if permitted
    LaunchedEffect(Unit) {
        if (LocationHelper.hasLocationPermission(context)) {
            viewModel.detectGpsLocation(context)
        } else {
            permissionLauncher.launch(
                arrayOf(
                    Manifest.permission.ACCESS_FINE_LOCATION,
                    Manifest.permission.ACCESS_COARSE_LOCATION,
                    Manifest.permission.CALL_PHONE
                )
            )
        }
    }

    // Listen to ViewModel notices for Snackbar
    LaunchedEffect(Unit) {
        viewModel.userNoticeEvent.collect { notice ->
            snackbarHostState.showSnackbar(notice)
        }
    }

    Scaffold(
        snackbarHost = { SnackbarHost(hostState = snackbarHostState) },
        topBar = {
            TopAppBar(
                title = {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Image(
                            painter = painterResource(id = R.drawable.img_loksetu_icon),
                            contentDescription = "LokSetu Emblem",
                            modifier = Modifier
                                .size(36.dp)
                                .clip(RoundedCornerShape(8.dp))
                        )
                        Spacer(modifier = Modifier.width(10.dp))
                        Column {
                            Text(
                                text = "LokSetu",
                                style = MaterialTheme.typography.titleLarge,
                                fontWeight = FontWeight.ExtraBold,
                                color = MaterialTheme.colorScheme.primary
                            )
                            Text(
                                text = if (isHindi) "किसान, कामगार व मोबिलिटी नेटवर्क" else "Direct Farmer, Worker & Cargo Network",
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                },
                actions = {
                    // Multi-Language Switcher (Hindi / English Toggle Button)
                    OutlinedButton(
                        onClick = { viewModel.toggleLanguage() },
                        shape = RoundedCornerShape(16.dp),
                        contentPadding = PaddingValues(horizontal = 8.dp, vertical = 2.dp),
                        border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.primary),
                        colors = ButtonDefaults.outlinedButtonColors(
                            containerColor = if (isHindi) MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.5f) else Color.Transparent
                        ),
                        modifier = Modifier.testTag("top_language_toggle_button")
                    ) {
                        Icon(
                            imageVector = Icons.Default.Translate,
                            contentDescription = "Language Switcher",
                            modifier = Modifier.size(14.dp),
                            tint = MaterialTheme.colorScheme.primary
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = if (isHindi) "हिंदी" else "EN",
                            fontWeight = FontWeight.Bold,
                            fontSize = 11.sp,
                            color = MaterialTheme.colorScheme.primary
                        )
                    }

                    Spacer(modifier = Modifier.width(2.dp))

                    // Terms of Use & Legal Guidelines Button
                    IconButton(
                        onClick = { viewModel.openTermsSheet() },
                        modifier = Modifier.testTag("top_open_terms_button")
                    ) {
                        Icon(
                            imageVector = Icons.Default.Gavel,
                            contentDescription = "Terms & Legal Guidelines",
                            tint = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }

                    // Customer Rating & Dispute Protection Button
                    IconButton(
                        onClick = { viewModel.openCustomerRating() },
                        modifier = Modifier.testTag("top_open_rating_button")
                    ) {
                        Icon(
                            imageVector = Icons.Default.Shield,
                            contentDescription = "Customer Rating & Dispute Protection",
                            tint = Color(0xFFD81B60)
                        )
                    }

                    // Emergency SOS 1-Tap Trigger Button
                    FilledTonalButton(
                        onClick = { viewModel.openSosDialog() },
                        colors = ButtonDefaults.filledTonalButtonColors(
                            containerColor = Color(0xFFFFEBEE),
                            contentColor = Color(0xFFD32F2F)
                        ),
                        shape = RoundedCornerShape(16.dp),
                        contentPadding = PaddingValues(horizontal = 10.dp, vertical = 2.dp),
                        modifier = Modifier.testTag("top_emergency_sos_button")
                    ) {
                        Icon(
                            imageVector = Icons.Default.Emergency,
                            contentDescription = "Emergency SOS",
                            modifier = Modifier.size(16.dp),
                            tint = Color(0xFFD32F2F)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = "SOS",
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFFD32F2F),
                            fontSize = 12.sp
                        )
                    }

                    Spacer(modifier = Modifier.width(4.dp))

                    // Contact History action
                    IconButton(
                        onClick = { viewModel.openHistory() },
                        modifier = Modifier.testTag("open_history_button")
                    ) {
                        Icon(
                            imageVector = Icons.Default.History,
                            contentDescription = "Contact History",
                            tint = MaterialTheme.colorScheme.onSurface
                        )
                    }

                    // Map View / GPS Picker
                    IconButton(
                        onClick = { viewModel.openLocationPicker() },
                        modifier = Modifier.testTag("top_open_map_picker_button")
                    ) {
                        Icon(
                            imageVector = Icons.Default.Map,
                            contentDescription = "GPS Location Picker",
                            tint = MaterialTheme.colorScheme.primary
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface
                )
            )
        },
        bottomBar = {
            NavigationBar(
                containerColor = MaterialTheme.colorScheme.surface,
                tonalElevation = 6.dp
            ) {
                NavigationBarItem(
                    selected = currentSection == AppSection.PROVIDERS,
                    onClick = { viewModel.setSection(AppSection.PROVIDERS) },
                    icon = { Icon(Icons.Default.Storefront, contentDescription = "Directory") },
                    label = { Text("Directory", fontSize = 10.sp, fontWeight = if (currentSection == AppSection.PROVIDERS) FontWeight.Bold else FontWeight.Normal) }
                )
                NavigationBarItem(
                    selected = currentSection == AppSection.STORE_DELIVERY,
                    onClick = { viewModel.setSection(AppSection.STORE_DELIVERY) },
                    icon = { Icon(Icons.Default.LocalShipping, contentDescription = "Store Delivery") },
                    label = { Text("Delivery", fontSize = 10.sp, fontWeight = if (currentSection == AppSection.STORE_DELIVERY) FontWeight.Bold else FontWeight.Normal) }
                )
                NavigationBarItem(
                    selected = currentSection == AppSection.JOBS,
                    onClick = { viewModel.setSection(AppSection.JOBS) },
                    icon = { Icon(Icons.Default.BusinessCenter, contentDescription = "Jobs") },
                    label = { Text("Jobs", fontSize = 10.sp, fontWeight = if (currentSection == AppSection.JOBS) FontWeight.Bold else FontWeight.Normal) }
                )
                NavigationBarItem(
                    selected = currentSection == AppSection.DIRECT_DEALS,
                    onClick = { viewModel.setSection(AppSection.DIRECT_DEALS) },
                    icon = { Icon(Icons.Default.Handshake, contentDescription = "Farm Deals") },
                    label = { Text("Deals", fontSize = 10.sp, fontWeight = if (currentSection == AppSection.DIRECT_DEALS) FontWeight.Bold else FontWeight.Normal) }
                )
                NavigationBarItem(
                    selected = currentSection == AppSection.WELFARE_FUND,
                    onClick = { viewModel.setSection(AppSection.WELFARE_FUND) },
                    icon = { Icon(Icons.Default.Shield, contentDescription = "Driver Welfare") },
                    label = { Text("Welfare", fontSize = 10.sp, fontWeight = if (currentSection == AppSection.WELFARE_FUND) FontWeight.Bold else FontWeight.Normal) }
                )
                NavigationBarItem(
                    selected = currentSection == AppSection.SETTLEMENT,
                    onClick = { viewModel.setSection(AppSection.SETTLEMENT) },
                    icon = { Icon(Icons.Default.Payments, contentDescription = "Settlement") },
                    label = { Text("Settlement", fontSize = 10.sp, fontWeight = if (currentSection == AppSection.SETTLEMENT) FontWeight.Bold else FontWeight.Normal) }
                )
            }
        },
        floatingActionButton = {
            if (currentSection == AppSection.PROVIDERS) {
                ExtendedFloatingActionButton(
                    onClick = { viewModel.openAddProvider() },
                    icon = { Icon(Icons.Default.Add, contentDescription = null) },
                    text = { Text("Register Service", fontWeight = FontWeight.SemiBold) },
                    containerColor = MaterialTheme.colorScheme.primary,
                    contentColor = MaterialTheme.colorScheme.onPrimary,
                    shape = RoundedCornerShape(24.dp),
                    modifier = Modifier.testTag("fab_register_provider")
                )
            }
        }
    ) { innerPadding ->
        when (currentSection) {
            AppSection.PROVIDERS -> {
                LazyColumn(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(innerPadding),
                    contentPadding = PaddingValues(bottom = 84.dp)
                ) {
            // 1. High-Accuracy GPS Location Status Bar
            item {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 6.dp)
                        .clickable { viewModel.openLocationPicker() }
                        .testTag("location_status_bar"),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
                    ),
                    border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant),
                    shape = RoundedCornerShape(24.dp)
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp, vertical = 12.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Row(
                            modifier = Modifier.weight(1f),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(36.dp)
                                    .background(MaterialTheme.colorScheme.primaryContainer, CircleShape),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Default.LocationOn,
                                    contentDescription = null,
                                    tint = MaterialTheme.colorScheme.primary,
                                    modifier = Modifier.size(20.dp)
                                )
                            }
                            Spacer(modifier = Modifier.width(12.dp))
                            Column {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Text(
                                        text = "Your GPS Location",
                                        style = MaterialTheme.typography.labelSmall,
                                        fontWeight = FontWeight.Bold,
                                        color = MaterialTheme.colorScheme.primary
                                    )
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text(
                                        text = "±${userLocation.accuracyMeters.toInt()}m accuracy",
                                        style = MaterialTheme.typography.labelSmall,
                                        color = Color(0xFF2E7D32),
                                        fontWeight = FontWeight.SemiBold
                                    )
                                }
                                Text(
                                    text = userLocation.address,
                                    style = MaterialTheme.typography.bodySmall,
                                    fontWeight = FontWeight.Medium,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis,
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                            }
                        }

                        Row(verticalAlignment = Alignment.CenterVertically) {
                            FilledTonalIconButton(
                                onClick = {
                                    if (LocationHelper.hasLocationPermission(context)) {
                                        viewModel.detectGpsLocation(context)
                                    } else {
                                        permissionLauncher.launch(
                                            arrayOf(
                                                Manifest.permission.ACCESS_FINE_LOCATION,
                                                Manifest.permission.ACCESS_COARSE_LOCATION
                                            )
                                        )
                                    }
                                },
                                modifier = Modifier.size(36.dp).testTag("refresh_gps_button"),
                                shape = CircleShape
                            ) {
                                if (isDetectingLocation) {
                                    CircularProgressIndicator(modifier = Modifier.size(18.dp), strokeWidth = 2.dp)
                                } else {
                                    Icon(
                                        imageVector = Icons.Default.MyLocation,
                                        contentDescription = "Refresh GPS",
                                        modifier = Modifier.size(18.dp)
                                    )
                                }
                            }
                        }
                    }
                }
            }

            // 2. Hero Visual Banner
            item {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 6.dp),
                    shape = RoundedCornerShape(24.dp),
                    border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant),
                    elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
                ) {
                    Box(modifier = Modifier.fillMaxWidth().height(160.dp)) {
                        Image(
                            painter = painterResource(id = R.drawable.img_loksetu_banner),
                            contentDescription = "LokSetu Banner",
                            modifier = Modifier.fillMaxSize(),
                            contentScale = ContentScale.Crop
                        )
                        Box(
                            modifier = Modifier
                                .fillMaxSize()
                                .background(
                                    androidx.compose.ui.graphics.Brush.verticalGradient(
                                        listOf(Color.Transparent, Color(0xDD1C1B1F))
                                    )
                                )
                        )
                        Column(
                            modifier = Modifier
                                .align(Alignment.BottomStart)
                                .padding(16.dp)
                        ) {
                            Text(
                                text = "Zero Brokerage. Direct Connect.",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                color = Color.White
                            )
                            Text(
                                text = "1-Tap Direct Calls & WhatsApp with nearby trusted providers",
                                style = MaterialTheme.typography.bodySmall,
                                color = Color(0xFFEADDFF)
                            )
                        }
                    }
                }
            }

            // 3. Category Selector Chips
            item {
                CategorySelectorRow(
                    selectedCategory = selectedCategory,
                    onSelectCategory = { viewModel.setCategory(it) },
                    isHindi = isHindi
                )
            }

            // Store-to-Customer Parcel Delivery & Pre-paid Escrow Banner
            item {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 4.dp)
                        .clickable { viewModel.setSection(AppSection.STORE_DELIVERY) }
                        .testTag("store_delivery_quick_card"),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.45f)),
                    border = BorderStroke(1.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.3f))
                ) {
                    Row(
                        modifier = Modifier.padding(12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(
                            modifier = Modifier
                                .size(40.dp)
                                .clip(CircleShape)
                                .background(MaterialTheme.colorScheme.primary),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.LocalShipping,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.onPrimary,
                                modifier = Modifier.size(20.dp)
                            )
                        }
                        Spacer(modifier = Modifier.width(12.dp))
                        Column(modifier = Modifier.weight(1f)) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text(
                                    text = if (isHindi) "दुकान से ग्राहक पार्सल डिलीवरी" else "Store-to-Customer Delivery",
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 13.sp,
                                    color = MaterialTheme.colorScheme.primary
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Surface(
                                    shape = RoundedCornerShape(8.dp),
                                    color = Color(0xFF2E7D32)
                                ) {
                                    Text(
                                        text = "100% Escrow",
                                        fontSize = 9.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = Color.White,
                                        modifier = Modifier.padding(horizontal = 5.dp, vertical = 2.dp)
                                    )
                                }
                            }
                            Text(
                                text = if (isHindi) "राइडर का ₹0 जेब खर्च • 4-अंकीय OTP पर तुरंत UPI स्प्लिट • COD सुरक्षा" else "Zero out-of-pocket for riders • 4-digit OTP instant UPI split • COD protection",
                                style = MaterialTheme.typography.bodySmall,
                                fontSize = 11.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }
            }

            // 3b. Home Service Safety, Upfront Escrow & Dispute Shield Banner
            item {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 4.dp)
                        .clickable { viewModel.openHomeVisitSafety() }
                        .testTag("home_service_safety_quick_card"),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = Color(0xFFFCE4EC)),
                    border = BorderStroke(1.dp, Color(0xFFF48FB1))
                ) {
                    Row(
                        modifier = Modifier.padding(12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(
                            modifier = Modifier
                                .size(40.dp)
                                .clip(CircleShape)
                                .background(Color(0xFFD81B60)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.Shield,
                                contentDescription = null,
                                tint = Color.White,
                                modifier = Modifier.size(20.dp)
                            )
                        }
                        Spacer(modifier = Modifier.width(12.dp))
                        Column(modifier = Modifier.weight(1f)) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text(
                                    text = if (isHindi) "होम सर्विस सुरक्षा व 100% अग्रिम एस्क्रो" else "Home Visit Safety & 100% Upfront Escrow",
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 13.sp,
                                    color = Color(0xFF880E4F)
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Surface(
                                    shape = RoundedCornerShape(8.dp),
                                    color = Color(0xFFD81B60)
                                ) {
                                    Text(
                                        text = "1-Tap SOS",
                                        fontSize = 9.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = Color.White,
                                        modifier = Modifier.padding(horizontal = 5.dp, vertical = 2.dp)
                                    )
                                }
                            }
                            Text(
                                text = if (isHindi) "महिला सुरक्षा • 100% एडवांस बुकिंग + यात्रा भत्ता लॉक • कस्टमर बिहेवियर रेटिंग" else "Women visit protection • 100% upfront booking + travel lock • Customer dispute blocking",
                                style = MaterialTheme.typography.bodySmall,
                                fontSize = 11.sp,
                                color = Color(0xFF4A148C)
                            )
                        }
                        Spacer(modifier = Modifier.width(4.dp))
                        FilledTonalButton(
                            onClick = { viewModel.openServiceEscrow() },
                            colors = ButtonDefaults.filledTonalButtonColors(
                                containerColor = Color(0xFF2E7D32),
                                contentColor = Color.White
                            ),
                            shape = RoundedCornerShape(12.dp),
                            contentPadding = PaddingValues(horizontal = 8.dp, vertical = 2.dp),
                            modifier = Modifier.testTag("banner_open_escrow_button")
                        ) {
                            Text("Escrow", fontSize = 10.sp, fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }

            // 4. Search Bar
            item {
                OutlinedTextField(
                    value = searchQuery,
                    onValueChange = { viewModel.setSearchQuery(it) },
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 4.dp)
                        .testTag("provider_search_bar"),
                    placeholder = { Text("Search produce, electrician, plumber, tempo...") },
                    leadingIcon = { Icon(Icons.Default.Search, contentDescription = null) },
                    trailingIcon = {
                        if (searchQuery.isNotEmpty()) {
                            IconButton(onClick = { viewModel.setSearchQuery("") }) {
                                Icon(Icons.Default.Clear, contentDescription = "Clear")
                            }
                        }
                    },
                    singleLine = true,
                    shape = RoundedCornerShape(28.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = MaterialTheme.colorScheme.primary,
                        unfocusedBorderColor = MaterialTheme.colorScheme.outlineVariant,
                        focusedContainerColor = MaterialTheme.colorScheme.surface,
                        unfocusedContainerColor = MaterialTheme.colorScheme.surface
                    )
                )
            }

            // 5. Filter and Sorting Chips
            item {
                FilterOptionsRow(
                    availableOnly = filterAvailableOnly,
                    onToggleAvailable = { viewModel.toggleAvailableOnly() },
                    verifiedOnly = filterVerifiedOnly,
                    onToggleVerified = { viewModel.toggleVerifiedOnly() },
                    sortByDistance = sortByDistance,
                    onToggleSortDistance = { viewModel.toggleSortByDistance() },
                    radiusKm = radiusKm,
                    onSetRadius = { viewModel.setRadiusFilter(it) }
                )
            }

            // 6. Provider Count & Header
            item {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 18.dp, vertical = 8.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "${providers.size} Available Nearby",
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )

                    Text(
                        text = if (sortByDistance) "Sorted by Distance" else "Sorted by Rating",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            // 7. Providers List
            if (providers.isEmpty()) {
                item {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 40.dp, horizontal = 24.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Icon(
                                imageVector = Icons.Default.Search,
                                contentDescription = null,
                                modifier = Modifier.size(48.dp),
                                tint = MaterialTheme.colorScheme.outline
                            )
                            Spacer(modifier = Modifier.height(10.dp))
                            Text(
                                text = "No providers found matching filters",
                                style = MaterialTheme.typography.bodyLarge,
                                fontWeight = FontWeight.SemiBold
                            )
                            Spacer(modifier = Modifier.height(6.dp))
                            Text(
                                text = "Try clearing search keywords or expanding distance radius",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            Spacer(modifier = Modifier.height(14.dp))
                            FilledTonalButton(onClick = {
                                viewModel.setCategory(null)
                                viewModel.setSearchQuery("")
                                viewModel.setRadiusFilter(null)
                            }) {
                                Text("Reset All Filters")
                            }
                        }
                    }
                }
            } else {
                items(providers, key = { it.provider.id }) { item ->
                    ProviderCard(
                        provider = item.provider,
                        distanceKm = item.distanceKm,
                        onClick = { viewModel.openDetail(item.provider) },
                        onCallClick = { viewModel.executePhoneCall(context, item.provider) },
                        onWhatsAppClick = { viewModel.openWhatsAppDialog(item.provider) },
                        onDirectionsClick = {
                            LocationHelper.openMapDirections(
                                context,
                                item.provider.latitude,
                                item.provider.longitude,
                                item.provider.name
                            )
                        },
                        onFavoriteClick = { viewModel.toggleFavorite(item.provider) },
                        modifier = Modifier.padding(horizontal = 16.dp, vertical = 6.dp)
                    )
                }
            }
        }
        }
        AppSection.STORE_DELIVERY -> {
            ParcelDeliverySection(viewModel = viewModel, modifier = Modifier.padding(innerPadding))
        }
        AppSection.JOBS -> {
            JobsSectionScreen(viewModel = viewModel, modifier = Modifier.padding(innerPadding))
        }
        AppSection.DIRECT_DEALS -> {
            FarmerDealsSection(viewModel = viewModel, modifier = Modifier.padding(innerPadding))
        }
        AppSection.WELFARE_FUND -> {
            AdWelfareFundSection(viewModel = viewModel, modifier = Modifier.padding(innerPadding))
        }
        AppSection.SETTLEMENT -> {
            PaymentSettlementSection(viewModel = viewModel, modifier = Modifier.padding(innerPadding))
        }
    }
    }

    // High-Accuracy GPS Map Picker Dialog
    if (isLocationPickerOpen) {
        GpsMapPickerDialog(
            initialLat = userLocation.latitude,
            initialLng = userLocation.longitude,
            accuracyMeters = userLocation.accuracyMeters,
            isDetecting = isDetectingLocation,
            nearbyProviders = providers.map { it.provider },
            onDetectGps = { viewModel.detectGpsLocation(context) },
            onLocationConfirmed = { lat, lng, addr ->
                viewModel.updateManualLocation(lat, lng, addr)
                viewModel.closeLocationPicker()
            },
            onDismiss = { viewModel.closeLocationPicker() }
        )
    }

    // WhatsApp Direct Dialog
    selectedWhatsApp?.let { provider ->
        WhatsAppMessageDialog(
            provider = provider,
            userLocationAddress = userLocation.address,
            onSendMessage = { msg ->
                viewModel.executeWhatsAppMessage(context, provider, msg)
            },
            onDismiss = { viewModel.closeWhatsAppDialog() }
        )
    }

    // Provider Detail Sheet
    selectedDetail?.let { provider ->
        val distance = LocationHelper.calculateDistanceKm(
            userLocation.latitude,
            userLocation.longitude,
            provider.latitude,
            provider.longitude
        )
        ProviderDetailSheet(
            provider = provider,
            distanceKm = distance,
            onCallClick = { viewModel.executePhoneCall(context, provider) },
            onWhatsAppClick = {
                viewModel.closeDetail()
                viewModel.openWhatsAppDialog(provider)
            },
            onDirectionsClick = {
                LocationHelper.openMapDirections(
                    context,
                    provider.latitude,
                    provider.longitude,
                    provider.name
                )
            },
            onFavoriteToggle = { viewModel.toggleFavorite(provider) },
            onSettleClick = {
                viewModel.openSettlementForProvider(provider)
            },
            onBookEscrowClick = {
                viewModel.closeDetail()
                viewModel.openServiceEscrow(provider)
            },
            onDismiss = { viewModel.closeDetail() }
        )
    }

    // Home Service 100% Upfront Escrow & Travel Lock Dialog
    if (isServiceEscrowOpen) {
        ServiceEscrowDialog(
            viewModel = viewModel,
            targetProvider = selectedProviderForEscrow,
            onDismiss = { viewModel.closeServiceEscrow() }
        )
    }

    // Discreet Women & Home-Visit Safety Sheet
    if (isHomeVisitSafetyOpen) {
        HomeVisitSafetySheet(
            viewModel = viewModel,
            onDismiss = { viewModel.closeHomeVisitSafety() }
        )
    }

    // Customer Safety & Dispute Rating Dialog
    if (isCustomerRatingOpen) {
        CustomerRatingDialog(
            viewModel = viewModel,
            onDismiss = { viewModel.closeCustomerRating() }
        )
    }

    // Add / Register Provider Dialog
    if (isAddProviderOpen) {
        AddProviderDialog(
            initialLat = userLocation.latitude,
            initialLng = userLocation.longitude,
            initialAddress = userLocation.address,
            isHindi = isHindi,
            onAdd = { newProvider ->
                viewModel.addNewProvider(newProvider)
            },
            onDismiss = { viewModel.closeAddProvider() }
        )
    }

    // Terms of Use & Legal Guidelines Sheet
    if (isTermsSheetOpen) {
        TermsAndLegalSheet(
            initialLanguage = currentLanguage,
            onDismiss = { viewModel.closeTermsSheet() }
        )
    }

    // Contact History Sheet
    if (isHistoryOpen) {
        HistorySheet(
            historyList = contactHistory,
            onClearHistory = { viewModel.clearHistory() },
            onCallAgain = { phone ->
                LocationHelper.makeDirectPhoneCall(context, phone)
            },
            onDismiss = { viewModel.closeHistory() }
        )
    }

    // Emergency SOS Dialog
    if (isSosDialogOpen) {
        SosEmergencyDialog(
            viewModel = viewModel,
            onDismiss = { viewModel.closeSosDialog() }
        )
    }

    // Instant UPI Settlement for Specific Provider
    selectedProviderForSettlement?.let { provider ->
        PaymentSettlementDialog(
            provider = provider,
            viewModel = viewModel,
            onDismiss = { viewModel.closeSettlementForProvider() }
        )
    }

    // Instant UPI Settlement Dialog (Global / Custom from Settlement section)
    if (isCustomSettlementOpen) {
        PaymentSettlementDialog(
            provider = null,
            viewModel = viewModel,
            onDismiss = { viewModel.closeCustomSettlement() }
        )
    }
}
