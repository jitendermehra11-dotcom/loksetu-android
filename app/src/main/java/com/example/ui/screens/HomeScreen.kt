package com.example.ui.screens

import android.Manifest
import android.content.Intent
import android.net.Uri
import android.speech.tts.TextToSpeech
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import java.util.Locale

import com.aistudio.loksetu.vxqtmp.R
import com.example.location.LocationHelper
import com.example.ui.components.*
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

    val selectedWhatsApp by viewModel.selectedProviderForWhatsApp.collectAsStateWithLifecycle()
    val isLocationPickerOpen by viewModel.isLocationPickerOpen.collectAsStateWithLifecycle()
    val currentLanguage by viewModel.currentLanguage.collectAsStateWithLifecycle()
    val isTermsSheetOpen by viewModel.isTermsSheetOpen.collectAsStateWithLifecycle()
    val isHomeVisitSafetyOpen by viewModel.isHomeVisitSafetyOpen.collectAsStateWithLifecycle()
    val isCustomerRatingOpen by viewModel.isCustomerRatingOpen.collectAsStateWithLifecycle()
    val isHindi = (currentLanguage == AppLanguage.HINDI)

    var showAdDialog by remember { mutableStateOf(false) }
    var showInsuranceDialog by remember { mutableStateOf(false) }
    var showVerificationDialog by remember { mutableStateOf(false) }

    var tts by remember { mutableStateOf<TextToSpeech?>(null) }
    DisposableEffect(context) {
        val textToSpeech = TextToSpeech(context) { status ->
            if (status == TextToSpeech.SUCCESS) {
                // TTS Initialization
            }
        }
        textToSpeech.language = Locale("hi", "IN")
        tts = textToSpeech
        onDispose {
            textToSpeech.stop()
            textToSpeech.shutdown()
        }
    }

    fun speakText(text: String) {
        tts?.speak(text, TextToSpeech.QUEUE_FLUSH, null, null)
    }

    val permissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestMultiplePermissions()
    ) { perms ->
        val fineGranted = perms[Manifest.permission.ACCESS_FINE_LOCATION] == true
        val coarseGranted = perms[Manifest.permission.ACCESS_COARSE_LOCATION] == true
        if (fineGranted || coarseGranted) {
            viewModel.detectGpsLocation(context)
        }
    }

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
                                fontWeight = FontWeight.ExtraBold,
                                color = MaterialTheme.colorScheme.primary,
                                fontSize = 18.sp
                            )
                            Text(
                                text = if (isHindi) "किसान, कामगार व मोबिलिटी नेटवर्क" else "Direct Farmer, Worker & Cargo Network",
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                fontSize = 11.sp
                            )
                        }
                    }
                },
                actions = {
                    OutlinedButton(
                        onClick = { viewModel.toggleLanguage() },
                        shape = RoundedCornerShape(16.dp),
                        contentPadding = PaddingValues(horizontal = 8.dp, vertical = 2.dp),
                        border = BorderStroke(1.dp, MaterialTheme.colorScheme.primary),
                        colors = ButtonDefaults.outlinedButtonColors(
                            containerColor = if (isHindi) MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.5f) else Color.Transparent
                        )
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
                            color = MaterialTheme.colorScheme.primary,
                            fontSize = 11.sp
                        )
                    }

                    Spacer(modifier = Modifier.width(2.dp))

                    IconButton(onClick = { viewModel.openTermsSheet() }) {
                        Icon(
                            imageVector = Icons.Default.Gavel,
                            contentDescription = "Terms & Legal Guidelines",
                            tint = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }

                    IconButton(onClick = { viewModel.openCustomerRating() }) {
                        Icon(
                            imageVector = Icons.Default.Shield,
                            contentDescription = "Customer Rating & Protection",
                            tint = Color(0xFFD81B60)
                        )
                    }

                    FilledTonalButton(
                        onClick = { viewModel.openSosDialog() },
                        colors = ButtonDefaults.filledTonalButtonColors(
                            containerColor = Color(0xFFFFEBEE),
                            contentColor = Color(0xFFD32F2F)
                        ),
                        shape = RoundedCornerShape(16.dp),
                        contentPadding = PaddingValues(horizontal = 10.dp, vertical = 2.dp)
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

                    IconButton(onClick = { viewModel.openHistory() }) {
                        Icon(
                            imageVector = Icons.Default.History,
                            contentDescription = "Contact History",
                            tint = MaterialTheme.colorScheme.onSurface
                        )
                    }

                    IconButton(onClick = { viewModel.openLocationPicker() }) {
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
                    selected = (currentSection == AppSection.PROVIDERS),
                    onClick = { viewModel.setSection(AppSection.PROVIDERS) },
                    icon = { Icon(Icons.Default.Storefront, contentDescription = "Directory") },
                    label = { Text(text = "Directory", fontSize = 10.sp) }
                )
                NavigationBarItem(
                    selected = (currentSection == AppSection.STORE_DELIVERY),
                    onClick = { viewModel.setSection(AppSection.STORE_DELIVERY) },
                    icon = { Icon(Icons.Default.LocalShipping, contentDescription = "Store Delivery") },
                    label = { Text(text = "Delivery", fontSize = 10.sp) }
                )
                NavigationBarItem(
                    selected = (currentSection == AppSection.JOBS),
                    onClick = { viewModel.setSection(AppSection.JOBS) },
                    icon = { Icon(Icons.Default.BusinessCenter, contentDescription = "Jobs") },
                    label = { Text(text = "Jobs", fontSize = 10.sp) }
                )
                NavigationBarItem(
                    selected = (currentSection == AppSection.DIRECT_DEALS),
                    onClick = { viewModel.setSection(AppSection.DIRECT_DEALS) },
                    icon = { Icon(Icons.Default.Handshake, contentDescription = "Farm Deals") },
                    label = { Text(text = "Deals", fontSize = 10.sp) }
                )
                NavigationBarItem(
                    selected = (currentSection == AppSection.WELFARE_FUND),
                    onClick = { viewModel.setSection(AppSection.WELFARE_FUND) },
                    icon = { Icon(Icons.Default.Shield, contentDescription = "Driver Welfare") },
                    label = { Text(text = "Welfare", fontSize = 10.sp) }
                )
                NavigationBarItem(
                    selected = (currentSection == AppSection.SETTLEMENT),
                    onClick = { viewModel.setSection(AppSection.SETTLEMENT) },
                    icon = { Icon(Icons.Default.Payments, contentDescription = "Settlement") },
                    label = { Text(text = "Settlement", fontSize = 10.sp) }
                )
            }
        },
        floatingActionButton = {
            if (currentSection == AppSection.PROVIDERS) {
                ExtendedFloatingActionButton(
                    onClick = { viewModel.openAddProvider() },
                    icon = { Icon(Icons.Default.Add, contentDescription = null) },
                    text = { Text(text = "Register Service", fontWeight = FontWeight.SemiBold) },
                    containerColor = MaterialTheme.colorScheme.primary,
                    contentColor = MaterialTheme.colorScheme.onPrimary,
                    shape = RoundedCornerShape(24.dp)
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
                    item {
                        Card(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 16.dp, vertical = 6.dp)
                                .clickable { viewModel.openLocationPicker() },
                            colors = CardDefaults.cardColors(
                                containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
                            )
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(12.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(
                                    imageVector = Icons.Default.LocationOn,
                                    contentDescription = "Location",
                                    tint = MaterialTheme.colorScheme.primary,
                                    modifier = Modifier.size(20.dp)
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(
                                        text = if (userLocation.toString().isNotBlank()) userLocation.toString() else if (isHindi) "स्थान खोजा जा रहा है..." else "Detecting location...",
                                        fontWeight = FontWeight.SemiBold,
                                        fontSize = 13.sp,
                                        maxLines = 1,
                                        overflow = TextOverflow.Ellipsis
                                    )
                                    Text(
                                        text = if (isHindi) "GPS मैप से अपना सटीक क्षेत्र चुनें" else "Tap to change radius or custom location",
                                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                                        fontSize = 11.sp
                                    )
                                }
                                if (isDetectingLocation) {
                                    CircularProgressIndicator(
                                        modifier = Modifier.size(18.dp),
                                        strokeWidth = 2.dp
                                    )
                                } else {
                                    Icon(
                                        imageVector = Icons.Default.GpsFixed,
                                        contentDescription = "GPS active",
                                        tint = MaterialTheme.colorScheme.primary,
                                        modifier = Modifier.size(18.dp)
                                    )
                                }
                            }
                        }
                    }

                    item {
                        Card(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 16.dp, vertical = 4.dp)
                                .clickable {
                                    speakText("लोकसेतु ऐप में आपका स्वागत है। यहाँ आप सीधा काम पा सकते हैं, बायर से बात कर सकते हैं और ₹50 लाख तक का दुर्घटना सुरक्षा बीमा प्राप्त कर सकते हैं।")
                                },
                            shape = RoundedCornerShape(12.dp),
                            colors = CardDefaults.cardColors(containerColor = Color(0xFFFFF8E1))
                        ) {
                            Row(
                                modifier = Modifier.padding(12.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(text = "🔊 ", fontSize = 20.sp)
                                Column {
                                    Text(
                                        text = "आवाज़ में सुनें (Voice Assistance)",
                                        fontWeight = FontWeight.Bold,
                                        color = Color(0xFFF57F17),
                                        fontSize = 13.sp
                                    )
                                    Text(
                                        text = "ऐप की जानकारी हिंदी में सुनने के लिए यहाँ दबाएँ",
                                        color = Color(0xFF424242),
                                        fontSize = 11.sp
                                    )
                                }
                            }
                        }
                    }

                    item {
                        Card(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 16.dp, vertical = 4.dp)
                                .clickable { showVerificationDialog = true },
                            shape = RoundedCornerShape(12.dp),
                            colors = CardDefaults.cardColors(containerColor = Color(0xFFEDE7F6))
                        ) {
                            Column(modifier = Modifier.padding(12.dp)) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text(
                                        text = "🔰 प्रोफाइल स्टेटस: प्रोविशनल (15 दिन वर्किंग ग्रेस)",
                                        fontWeight = FontWeight.Bold,
                                        color = Color(0xFF512DA8),
                                        fontSize = 12.sp
                                    )
                                    Surface(
                                        shape = RoundedCornerShape(8.dp),
                                        color = Color(0xFF7E57C2)
                                    ) {
                                        Text(
                                            text = "15 दिन शेष",
                                            color = Color.White,
                                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
                                            fontSize = 10.sp
                                        )
                                    }
                                }
                                Spacer(modifier = Modifier.height(4.dp))
                                Text(
                                    text = "पहले ही दिन से काम शुरू करें। कागजी कार्रवाई के लिए 15 दिन का समय है। विवरण देखें ➔",
                                    color = Color(0xFF424242),
                                    fontSize = 11.sp
                                )
                            }
                        }
                    }

                    item {
                        OutlinedTextField(
                            value = searchQuery,
                            onValueChange = { viewModel.setSearchQuery(it) },
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 16.dp, vertical = 4.dp),
                            placeholder = { Text("खोजें (Search service or worker...)") },
                            leadingIcon = { Icon(Icons.Default.Search, contentDescription = null) },
                            singleLine = true,
                            shape = RoundedCornerShape(12.dp)
                        )
                    }

                    item {
                        CategorySelectorRow(
                            selectedCategory = selectedCategory,
                            onSelectCategory = { viewModel.setCategory(it) }
                        )
                    }

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

                    items(providers) { providerWithDist ->
                        ProviderCard(
                            provider = providerWithDist.provider,
                            distanceKm = providerWithDist.distanceKm,
                            onClick = { viewModel.openDetail(providerWithDist.provider) },
                            onCallClick = {
                                val intent = Intent(Intent.ACTION_DIAL).apply {
                                    data = Uri.parse("tel:${providerWithDist.provider.phone}")
                                }
                                context.startActivity(intent)
                            },
                            onWhatsAppClick = {
                                val intent = Intent(Intent.ACTION_VIEW).apply {
                                    data = Uri.parse("https://api.whatsapp.com/send?phone=${providerWithDist.provider.phone}")
                                }
                                context.startActivity(intent)
                            },
                            onDirectionsClick = { },
                            onFavoriteClick = { }
                        )
                    }
                }
            }
            else -> {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(innerPadding),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "LokSetu Active Module",
                        color = MaterialTheme.colorScheme.primary,
                        fontSize = 16.sp
                    )
                }
            }
        }
    }

    if (showVerificationDialog) {
        AlertDialog(
            onDismissRequest = { showVerificationDialog = false },
            title = { Text(text = "🔰 15-दिन वर्किंग ग्रेस मॉडल", fontSize = 16.sp, fontWeight = FontWeight.Bold) },
            text = {
                Text(
                    text = "• कम पढ़े-लिखे कामगार और नए सदस्य पहले दिन से ही बिना किसी रुकावट के काम शुरू कर सकते हैं।\n• कागजी वेरिफिकेशन पूरा करने के लिए 15 दिनों की छूट मिलती है।\n• लगातार ईमानदारी से काम करने पर '5-Star Verified Worker' का दर्जा दिया जाता है।",
                    fontSize = 13.sp
                )
            },
            confirmButton = {
                TextButton(onClick = { showVerificationDialog = false }) {
                    Text(text = "समझ गया")
                }
            }
        )
    }

    if (isSosDialogOpen) {
        SosEmergencyDialog(
            viewModel = viewModel,
            onDismiss = { viewModel.closeSosDialog() }
        )
    }

    if (isLocationPickerOpen) {
        GpsMapPickerDialog(
            initialLat = 0.0,
            initialLng = 0.0,
            accuracyMeters = 0.0f,
            isDetecting = isDetectingLocation,
            onDetectGps = { viewModel.detectGpsLocation(context) },
            onLocationConfirmed = { lat, lng, address ->
                viewModel.detectGpsLocation(context)
            },
            onDismiss = { viewModel.closeLocationPicker() }
        )
    }

    if (isTermsSheetOpen) {
        TermsAndLegalSheet(
            viewModel = viewModel,
            onDismiss = { viewModel.closeTermsSheet() }
        )
    }

    if (isHomeVisitSafetyOpen) {
        HomeVisitSafetySheet(
            viewModel = viewModel,
            onDismiss = { viewModel.closeHomeVisitSafety() }
        )
    }

    if (isCustomerRatingOpen) {
        CustomerRatingDialog(
            viewModel = viewModel,
            onDismiss = { viewModel.closeCustomerRating() }
        )
    }

    if (isCustomSettlementOpen && selectedProviderForSettlement != null) {
        PaymentSettlementDialog(
            provider = selectedProviderForSettlement!!,
            onDismiss = { viewModel.closeCustomSettlement() }
        )
    }

    if (selectedWhatsApp != null) {
        WhatsAppMessageDialog(
            provider = selectedWhatsApp!!,
            userLocationAddress = userLocation.toString(),
            onDismiss = { },
            onSendMessage = { message ->
                val intent = Intent(Intent.ACTION_VIEW).apply {
                    data = Uri.parse("https://api.whatsapp.com/send?phone=${selectedWhatsApp!!.phone}&text=${Uri.encode(message)}")
                }
                context.startActivity(intent)
            }
        )
    }
}
