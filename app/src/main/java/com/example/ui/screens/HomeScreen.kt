package com.example.ui.screens

import android.Manifest
import android.content.Intent
import android.net.Uri
import android.speech.tts.TextToSpeech
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.Image
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
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
import java.util.Locale

// R फ़ाइल का सही पैकेज इंपोर्ट
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

    // नए फीचर्स के स्टेट्स
    var showAdDialog by remember { mutableStateOf(false) }
    var showInsuranceDialog by remember { mutableStateOf(false) }
    var showVerificationDialog by remember { mutableStateOf(false) }
    var adIncome by remember { mutableStateOf(12.50) }

    // वॉइस हेल्प गाइड (TextToSpeech)
    var tts by remember { mutableStateOf<TextToSpeech?>(null) }
    DisposableEffect(context) {
        val textToSpeech = TextToSpeech(context) { status ->
            if (status == TextToSpeech.SUCCESS) {
                // TTS initialized
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

    // Startup Location Check
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

    // Snackbar Notices
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
                    OutlinedButton(
                        onClick = { viewModel.toggleLanguage() },
                        shape = RoundedCornerShape(16.dp),
                        contentPadding = PaddingValues(horizontal = 8.dp, vertical = 2.dp),
                        border = BorderStroke(1.dp, MaterialTheme.colorScheme.primary),
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
                            color = MaterialTheme.colorScheme.primary
                        )
                    }

                    Spacer(modifier = Modifier.width(2.dp))

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

                    IconButton(
                        onClick = { viewModel.openCustomerRating() },
                        modifier = Modifier.testTag("top_open_rating_button")
                    ) {
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
                            color = Color(0xFFD32F2F)
                        )
                    }

                    Spacer(modifier = Modifier.width(4.dp))

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
                    label = { Text("Directory", style = MaterialTheme.typography.labelSmall) }
                )
                NavigationBarItem(
                    selected = currentSection == AppSection.STORE_DELIVERY,
                    onClick = { viewModel.setSection(AppSection.STORE_DELIVERY) },
                    icon = { Icon(Icons.Default.LocalShipping, contentDescription = "Store Delivery") },
                    label = { Text("Delivery", style = MaterialTheme.typography.labelSmall) }
                )
                NavigationBarItem(
                    selected = currentSection == AppSection.JOBS,
                    onClick = { viewModel.setSection(AppSection.JOBS) },
                    icon = { Icon(Icons.Default.BusinessCenter, contentDescription = "Jobs") },
                    label = { Text("Jobs", style = MaterialTheme.typography.labelSmall) }
                )
                NavigationBarItem(
                    selected = currentSection == AppSection.DIRECT_DEALS,
                    onClick = { viewModel.setSection(AppSection.DIRECT_DEALS) },
                    icon = { Icon(Icons.Default.Handshake, contentDescription = "Farm Deals") },
                    label = { Text("Deals", style = MaterialTheme.typography.labelSmall) }
                )
                NavigationBarItem(
                    selected = currentSection == AppSection.WELFARE_FUND,
                    onClick = { viewModel.setSection(AppSection.WELFARE_FUND) },
                    icon = { Icon(Icons.Default.Shield, contentDescription = "Driver Welfare") },
                    label = { Text("Welfare", style = MaterialTheme.typography.labelSmall) }
                )
                NavigationBarItem(
                    selected = currentSection == AppSection.SETTLEMENT,
                    onClick = { viewModel.setSection(AppSection.SETTLEMENT) },
                    icon = { Icon(Icons.Default.Payments, contentDescription = "Settlement") },
                    label = { Text("Settlement", style = MaterialTheme.typography.labelSmall) }
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
                    // 1. GPS Location Status Bar
                    item {
                        Card(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 16.dp, vertical = 6.dp)
                                .clickable { viewModel.openLocationPicker() }
                                .testTag("location_status_bar"),
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
                                        text = if (userLocation.isNotBlank()) userLocation else if (isHindi) "स्थान खोजा जा रहा है..." else "Detecting location...",
                                        style = MaterialTheme.typography.bodyMedium,
                                        fontWeight = FontWeight.SemiBold,
                                        maxLines = 1,
                                        overflow = TextOverflow.Ellipsis
                                    )
                                    Text(
                                        text = if (isHindi) "GPS मैप से अपना सटीक क्षेत्र चुनें" else "Tap to change radius or custom location",
                                        style = MaterialTheme.typography.labelSmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
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

                    // 2. वॉइस हेल्प कार्ड
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
                                Text(text = "🔊 ", style = MaterialTheme.typography.titleLarge)
                                Column {
                                    Text(
                                        text = "आवाज़ में सुनें (Voice Assistance)",
                                        style = MaterialTheme.typography.titleSmall,
                                        fontWeight = FontWeight.Bold,
                                        color = Color(0xFFF57F17)
                                    )
                                    Text(
                                        text = "ऐप की जानकारी हिंदी में सुनने के लिए यहाँ दबाएँ",
                                        style = MaterialTheme.typography.bodySmall,
                                        color = Color(0xFF424242)
                                    )
                                }
                            }
                        }
                    }

                    // 3. प्रोग्रेसिव ट्रस्ट मॉडल (15 दिन ग्रेस)
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
                                        style = MaterialTheme.typography.titleSmall,
                                        fontWeight = FontWeight.Bold,
                                        color = Color(0xFF512DA8)
                                    )
                                    Surface(
                                        shape = RoundedCornerShape(8.dp),
                                        color = Color(0xFF7E57C2)
                                    ) {
                                        Text(
                                            text = "15 दिन शेष",
                                            color = Color.White,
                                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
                                            style = MaterialTheme.typography.labelSmall
                                        )
                                    }
                                }
                                Spacer(modifier = Modifier.height(4.dp))
                                Text(
                                    text = "पहले ही दिन से काम शुरू करें। कागजी कार्रवाई के लिए 15 दिन का समय है। विवरण देखें ➔",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = Color(0xFF424242)
                                )
                            }
                        }
                    }

                    // 4. बायर व ग्राहक डायरेक्ट कनेक्टिविटी
                    item {
                        Card(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 16.dp, vertical = 4.dp),
                            shape = RoundedCornerShape(12.dp),
                            colors = CardDefaults.cardColors(containerColor = Color(0xFFE3F2FD))
                        ) {
                            Column(modifier = Modifier.padding(12.dp)) {
                                Text(
                                    text = "📞 बायर / ग्राहक से सीधे संपर्क करें",
                                    style = MaterialTheme.typography.titleSmall,
                                    fontWeight = FontWeight.Bold,
                                    color = Color(0xFF1565C0)
                                )
                                Spacer(modifier = Modifier.height(2.dp))
                                Text(
                                    text = "बिना किसी ठेकेदार के सीधी बात और 100% दैनिक भुगतान",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = Color(0xFF616161)
                                )
                                Spacer(modifier = Modifier.height(8.dp))
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Button(
                                        onClick = {
                                            val intent = Intent(Intent.ACTION_DIAL).apply {
                                                data = Uri.parse("tel:+919876543210")
                                            }
                                            context.startActivity(intent)
                                        },
                                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF1E88E5)),
                                        contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp)
                                    ) {
                                        Text("📞 डायरेक्ट कॉल", style = MaterialTheme.typography.labelMedium)
                                    }

                                    Button(
                                        onClick = {
                                            val intent = Intent(Intent.ACTION_VIEW).apply {
                                                data = Uri.parse("https://api.whatsapp.com/send?phone=919876543210&text=नमस्कार, LokSetu ऐप से संपर्क कर रहे हैं।")
                                            }
                                            context.startActivity(intent)
                                        },
                                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF43A047)),
                                        contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp)
                                    ) {
                                        Text("💬 WhatsApp", style = MaterialTheme.typography.labelMedium)
                                    }
                                }
                            }
                        }
                    }

                    // 5. दुर्घटना सुरक्षा बीमा
                    item {
                        Card(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 16.dp, vertical = 4.dp)
                                .clickable { showInsuranceDialog = true },
                            shape = RoundedCornerShape(12.dp),
                            colors = CardDefaults.cardColors(containerColor = Color(0xFFE8F5E9))
                        ) {
                            Column(modifier = Modifier.padding(12.dp)) {
                                Text(
                                    text = "🛡️ LokSetu दुर्घटना बीमा (₹25 लाख - ₹50 लाख)",
                                    style = MaterialTheme.typography.titleSmall,
                                    fontWeight = FontWeight.Bold,
                                    color = Color(0xFF2E7D32)
                                )
                                Spacer(modifier = Modifier.height(2.dp))
                                Text(
                                    text = "नाइट/हाईवे राइडर्स हेतु ₹50 लाख व डे-टाइम हेतु ₹25 लाख एक्सीडेंटल कवर। विवरण देखें ➔",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = Color(0xFF424242)
                                )
                            }
                        }
                    }

                    // 6. स्पॉन्सर्ड विज्ञापन
                    item {
                        Card(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 16.dp, vertical = 4.dp)
                                .clickable {
                                    showAdDialog = true
                                    adIncome += 0.50
                                },
                            shape = RoundedCornerShape(12.dp),
                            colors = CardDefaults.cardColors(containerColor = Color(0xFFFFF3E0))
                        ) {
                            Column(modifier = Modifier.padding(12.dp)) {
                                Text(
                                    text = "📢 प्रायोजित विज्ञापन (Ad Section)",
                                    style = MaterialTheme.typography.titleSmall,
                                    fontWeight = FontWeight.Bold,
                                    color = Color(0xFFE65100)
                                )
                                Spacer(modifier = Modifier.height(2.dp))
                                Text(
                                    text = "विज्ञापन देखें और वॉलेट बैलेंस बढ़ाएं। कुल ऐड बोनस: ₹" + String.format("%.2f", adIncome),
                                    style = MaterialTheme.typography.bodySmall,
                                    color = Color(0xFF424242)
                                )
                            }
                        }
                    }

                    // 7. Category Selector Row (Correct Parameter Signature)
                    item {
                        CategorySelectorRow(
                            selectedCategory = selectedCategory,
                            onSelectCategory = { viewModel.selectCategory(it) }
                        )
                    }

                    // 8. Filter Options Row (Correct Parameter Signature)
                    item {
                        FilterOptionsRow(
                            viewModel = viewModel
                        )
                    }

                    // 9. Provider List
                    items(providers) { provider ->
                        ProviderCard(
                            provider = provider,
                            onCallClick = { viewModel.initiateCall(context, provider) },
                            onWhatsAppClick = { viewModel.openWhatsApp(provider) },
                            onDetailClick = { viewModel.openDetail(provider) },
                            onEscrowClick = { viewModel.openServiceEscrow(provider) },
                            onHomeVisitSafetyClick = { viewModel.openHomeVisitSafety() }
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
                        text = "LokSetu ${currentSection.name} Module Active",
                        style = MaterialTheme.typography.titleMedium,
                        color = MaterialTheme.colorScheme.primary
                    )
                }
            }
        }
    }

    // --- DIALOGS (Correct Parameter Signatures) ---

    if (showVerificationDialog) {
        AlertDialog(
            onDismissRequest = { showVerificationDialog = false },
            title = { Text("🔰 15-दिन वर्किंग ग्रेस मॉडल", style = MaterialTheme.typography.titleMedium) },
            text = {
                Text(
                    "• कम पढ़े-लिखे कामगार और नए सदस्य पहले दिन से ही बिना किसी रुकावट के काम शुरू कर सकते हैं।\n" +
                    "• कागजी वेरिफिकेशन पूरा करने के लिए 15 दिनों की छूट मिलती है।\n" +
                    "• लगातार ईमानदारी से काम करने पर '5-Star Verified Worker' का दर्जा दिया जाता है।",
                    style = MaterialTheme.typography.bodyMedium
                )
            },
            confirmButton = {
                TextButton(onClick = { showVerificationDialog = false }) {
                    Text("समझ गया")
                }
            }
        )
    }

    if (showAdDialog) {
        AlertDialog(
            onDismissRequest = { showAdDialog = false },
            title = { Text("🎬 प्रायोजित विज्ञापन", style = MaterialTheme.typography.titleMedium) },
            text = {
                Text(
                    "विज्ञापन देखने के लिए धन्यवाद!\n₹0.50 आपके ऐप वॉलेट में जोड़ दिए गए हैं।",
                    style = MaterialTheme.typography.bodyMedium
                )
            },
            confirmButton = {
                TextButton(onClick = { showAdDialog = false }) {
                    Text("ठीक है")
                }
            }
        )
    }

    if (showInsuranceDialog) {
        AlertDialog(
            onDismissRequest = { showInsuranceDialog = false },
            title = { Text("🛡️ LokSetu दुर्घटना बीमा पॉलिसी", style = MaterialTheme.typography.titleMedium) },
            text = {
                Text(
                    "• 🌌 नाइट / हाईवे राइडर्स कवर: रात के समय व हाईवे राइडर/ड्राइवर के लिए दुर्घटना में मृत्यु या पूर्ण विकलांगता पर ₹50 लाख का बीमा कवर।\n" +
                    "• ☀️ डे-टाइम कामगार कवर: दिन के कामकाजी घंटों के दौरान ₹25 लाख का एक्सीडेंटल डेथ/सुरक्षा कवर।\n" +
                    "• 🚑 24x7 इमरजेंसी रोड-साइड व कानूनी सहायता।",
                    style = MaterialTheme.typography.bodyMedium
                )
            },
            confirmButton = {
                TextButton(onClick = { showInsuranceDialog = false }) {
                    Text("ठीक है")
                }
            }
        )
    }

    if (isSosDialogOpen) {
        SosEmergencyDialog(
            onDismiss = { viewModel.closeSosDialog() },
            onTriggerSos = { viewModel.triggerSosAlert(context) }
        )
    }

    if (isLocationPickerOpen) {
        GpsMapPickerDialog(
            onDismiss = { viewModel.closeLocationPicker() },
            onDetectGps = { viewModel.detectGpsLocation(context) },
            onLocationConfirmed = { loc -> viewModel.setCustomLocation(loc, 0.0, 0.0) }
        )
    }

    if (isTermsSheetOpen) {
        TermsAndLegalSheet(
            onDismiss = { viewModel.closeTermsSheet() }
        )
    }

    if (isServiceEscrowOpen && selectedProviderForEscrow != null) {
        ServiceEscrowDialog(
            viewModel = viewModel,
            onDismiss = { viewModel.closeServiceEscrow() }
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
            viewModel = viewModel,
            onDismiss = { viewModel.closeCustomSettlement() }
        )
    }

    if (selectedWhatsApp != null) {
        WhatsAppMessageDialog(
            userLocationAddress = userLocation,
            onDismiss = { viewModel.closeWhatsApp() },
            onSendMessage = { msg ->
                val intent = Intent(Intent.ACTION_VIEW).apply {
                    data = Uri.parse("https://api.whatsapp.com/send?phone=${selectedWhatsApp!!.phone}&text=${Uri.encode(msg)}")
                }
                context.startActivity(intent)
                viewModel.closeWhatsApp()
            }
        )
    }
}
