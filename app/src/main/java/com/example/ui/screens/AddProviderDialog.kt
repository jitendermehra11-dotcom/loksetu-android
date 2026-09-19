package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Badge
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.CreditCard
import androidx.compose.material.icons.filled.DirectionsCar
import androidx.compose.material.icons.filled.FileUpload
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Phone
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.VerifiedUser
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.example.data.model.ProviderCategory
import com.example.data.model.ProviderEntity

@Composable
fun AddProviderDialog(
    initialLat: Double,
    initialLng: Double,
    initialAddress: String,
    isHindi: Boolean = false,
    onAdd: (ProviderEntity) -> Unit,
    onDismiss: () -> Unit
) {
    var name by remember { mutableStateOf("") }
    var selectedCategory by remember { mutableStateOf(ProviderCategory.BIKE_DELIVERY) }
    var subCategory by remember { mutableStateOf("") }
    var speciality by remember { mutableStateOf("") }
    var phone by remember { mutableStateOf("") }
    var pricing by remember { mutableStateOf("") }
    var locationName by remember { mutableStateOf(initialAddress) }
    var experienceYears by remember { mutableIntStateOf(3) }
    var description by remember { mutableStateOf("") }

    // KYC Verification Fields
    var aadhaarNumber by remember { mutableStateOf("") }
    var dlNumber by remember { mutableStateOf("") }
    var vehicleRcNumber by remember { mutableStateOf("") }
    var isAadhaarUploaded by remember { mutableStateOf(false) }
    var isDlUploaded by remember { mutableStateOf(false) }
    var isRcUploaded by remember { mutableStateOf(false) }

    var hasError by remember { mutableStateOf(false) }

    val isMobility = selectedCategory == ProviderCategory.BIKE_DELIVERY ||
            selectedCategory == ProviderCategory.CAB_TAXI ||
            selectedCategory == ProviderCategory.CARGO_LOADER ||
            selectedCategory == ProviderCategory.LOADING_DRIVER

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Surface(
            modifier = Modifier
                .fillMaxWidth(0.96f)
                .clip(RoundedCornerShape(24.dp)),
            color = MaterialTheme.colorScheme.surface,
            border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant),
            tonalElevation = 2.dp
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState())
                    .padding(20.dp)
            ) {
                // Header
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(
                            text = if (isHindi) "LokSetu पर नया पंजीकरण" else "Register on LokSetu",
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = if (isHindi) "बाइक डिलीवरी, कैब/टैक्सी, माल वाहक, किसान या कारीगर" else "Bike delivery, Cab/Taxi, Cargo loader, Farmer or Artisan",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }

                    IconButton(onClick = onDismiss) {
                        Icon(imageVector = Icons.Default.Close, contentDescription = "Close")
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Category Selector (Explicit roles: Bike Delivery, Cab/Taxi, Cargo Loader, Farmers, Skilled Workers)
                Text(
                    text = if (isHindi) "पंजीकरण भूमिका चुनें (Select Registration Role) *" else "Select Registration Role *",
                    style = MaterialTheme.typography.labelMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary
                )
                Spacer(modifier = Modifier.height(6.dp))
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .horizontalScroll(rememberScrollState()),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    ProviderCategory.registrationCategories.forEach { cat ->
                        val isSelected = selectedCategory == cat
                        FilterChip(
                            selected = isSelected,
                            onClick = { selectedCategory = cat },
                            label = {
                                Text(
                                    when (cat) {
                                        ProviderCategory.BIKE_DELIVERY -> if (isHindi) "🏍️ बाइक डिलीवरी" else "🏍️ Bike Parcel & Delivery"
                                        ProviderCategory.CAB_TAXI -> if (isHindi) "🚗 कैब/टैक्सी" else "🚗 Cab/Taxi Ride"
                                        ProviderCategory.CARGO_LOADER, ProviderCategory.LOADING_DRIVER -> if (isHindi) "🚚 माल वाहक लोडर" else "🚚 Cargo Loader"
                                        ProviderCategory.FARMER_VENDOR -> if (isHindi) "🌾 किसान व मंडी" else "🌾 Farmers & Mandi"
                                        ProviderCategory.SKILLED_WORKER -> if (isHindi) "🛠️ कुशल कारीगर" else "🛠️ Skilled Workers"
                                        ProviderCategory.HOME_BEAUTY -> if (isHindi) "💅 होम ब्यूटी व केयर" else "💅 Home Beauty & Care"
                                        ProviderCategory.HOME_HEALTHCARE -> if (isHindi) "🩺 होम हेल्थकेयर व नर्स" else "🩺 Home Healthcare & Nurse"
                                        ProviderCategory.DOMESTIC_SERVICES -> if (isHindi) "🧹 घरेलू व सोसाइटी सेवाएं" else "🧹 Domestic & Society"
                                    },
                                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
                                )
                            },
                            leadingIcon = if (isSelected) {
                                { Icon(Icons.Default.Check, contentDescription = null, modifier = Modifier.size(16.dp)) }
                            } else null,
                            shape = RoundedCornerShape(20.dp),
                            modifier = Modifier.testTag("role_chip_${cat.name.lowercase()}")
                        )
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                // Name
                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it; hasError = false },
                    label = { Text(if (isHindi) "पूरा नाम *" else "Full Name *") },
                    leadingIcon = { Icon(Icons.Default.Person, contentDescription = null) },
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("add_provider_name"),
                    shape = RoundedCornerShape(16.dp),
                    isError = hasError && name.isBlank()
                )

                Spacer(modifier = Modifier.height(10.dp))

                // Sub-category (Vehicle Model / Specific Trade)
                OutlinedTextField(
                    value = subCategory,
                    onValueChange = { subCategory = it },
                    label = {
                        Text(
                            when (selectedCategory) {
                                ProviderCategory.BIKE_DELIVERY -> if (isHindi) "वाहन (जैसे: Splendor, Pulsar, Electric Bike)" else "Vehicle Model (e.g., Hero Splendor, EV Bike)"
                                ProviderCategory.CAB_TAXI -> if (isHindi) "कार/कैब (जैसे: Swift Dzire, WagonR, Ertiga)" else "Vehicle Model (e.g., Swift Dzire, WagonR Cab)"
                                ProviderCategory.CARGO_LOADER, ProviderCategory.LOADING_DRIVER -> if (isHindi) "लोडर (जैसे: छोटा हाथी, Bolero Maxi Truck)" else "Vehicle Model (e.g., Tata Ace, Bolero Pickup)"
                                ProviderCategory.FARMER_VENDOR -> if (isHindi) "उत्पाद (जैसे: गेहूं, ताज़ी सब्ज़ियां, दुग्ध उत्पाद)" else "Farm Trade (e.g., Organic Vegetables, Grains)"
                                ProviderCategory.SKILLED_WORKER -> if (isHindi) "कार्य (जैसे: प्लंबर, इलेक्ट्रीशियन, वेल्डर)" else "Trade Skill (e.g., Plumber, Electrician)"
                                ProviderCategory.HOME_BEAUTY -> if (isHindi) "सेवा प्रकार (जैसे: ब्राइडल मेहँदी, हेयर कट, फेशियल/मेकओवर)" else "Service (e.g., Bridal Mehendi, Haircut, Skin Care)"
                                ProviderCategory.HOME_HEALTHCARE -> if (isHindi) "केयर रोल (जैसे: जीएनएम नर्स, पेशेंट अटेंडेंट, बुजुर्ग देखभाल)" else "Care Role (e.g., GNM Nurse, Patient Attendant, Elder Care)"
                                ProviderCategory.DOMESTIC_SERVICES -> if (isHindi) "कार्य (जैसे: हाउस हेल्प, खाना बनाना, डीप क्लीनिंग)" else "Duty (e.g., House Help, Cook, Deep Cleaning)"
                            }
                        )
                    },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp)
                )

                Spacer(modifier = Modifier.height(10.dp))

                // Speciality / Services
                OutlinedTextField(
                    value = speciality,
                    onValueChange = { speciality = it; hasError = false },
                    label = { Text(if (isHindi) "मुख्य सेवाएं / विशेषताएं *" else "Key Services / Produce *") },
                    placeholder = {
                        Text(
                            when (selectedCategory) {
                                ProviderCategory.BIKE_DELIVERY -> "e.g. Express document parcel, food box delivery, helmet equipped"
                                ProviderCategory.CAB_TAXI -> "e.g. Airport drop, outstation AC cab, clean cab with music"
                                ProviderCategory.CARGO_LOADER, ProviderCategory.LOADING_DRIVER -> "e.g. 1-ton local city shifting, mandi bulk crates, ropes & tarpaulin"
                                ProviderCategory.FARMER_VENDOR -> "e.g. Fresh farm tomatoes, premium basmati grain, dairy milk"
                                ProviderCategory.SKILLED_WORKER -> "e.g. Complete home wiring, emergency pipe leakage repair"
                                ProviderCategory.HOME_BEAUTY -> "e.g. Organic herbal bridal mehendi, hygiene kit, salon at doorstep"
                                ProviderCategory.HOME_HEALTHCARE -> "e.g. Post-surgery injection, BP/Sugar monitor, 12hr ICU patient care"
                                ProviderCategory.DOMESTIC_SERVICES -> "e.g. Daily utensils & dusting, full apartment festival deep cleaning"
                            }
                        )
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("add_provider_speciality"),
                    shape = RoundedCornerShape(16.dp),
                    isError = hasError && speciality.isBlank()
                )

                Spacer(modifier = Modifier.height(10.dp))

                // Phone
                OutlinedTextField(
                    value = phone,
                    onValueChange = { phone = it; hasError = false },
                    label = { Text(if (isHindi) "मोबाइल फोन नंबर *" else "Mobile Phone Number *") },
                    placeholder = { Text("+91 98765 43210") },
                    leadingIcon = { Icon(Icons.Default.Phone, contentDescription = null) },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone),
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("add_provider_phone"),
                    shape = RoundedCornerShape(16.dp),
                    isError = hasError && phone.isBlank()
                )

                Spacer(modifier = Modifier.height(10.dp))

                // Rate / Pricing
                OutlinedTextField(
                    value = pricing,
                    onValueChange = { pricing = it },
                    label = { Text(if (isHindi) "दर / शुल्क (Pricing)" else "Standard Pricing / Rates") },
                    placeholder = {
                        Text(
                            when (selectedCategory) {
                                ProviderCategory.BIKE_DELIVERY -> "e.g. ₹40 base + ₹8/km"
                                ProviderCategory.CAB_TAXI -> "e.g. ₹12/km AC (0% platform cut)"
                                ProviderCategory.CARGO_LOADER, ProviderCategory.LOADING_DRIVER -> "e.g. ₹350 base + ₹25/km"
                                ProviderCategory.FARMER_VENDOR -> "e.g. ₹25/kg Mandi Farm Rate"
                                ProviderCategory.SKILLED_WORKER -> "e.g. ₹200 visiting fee"
                                ProviderCategory.HOME_BEAUTY -> "e.g. ₹450 service fee + ₹100 travel"
                                ProviderCategory.HOME_HEALTHCARE -> "e.g. ₹600/shift + ₹150 travel"
                                ProviderCategory.DOMESTIC_SERVICES -> "e.g. ₹350 visit + ₹80 travel"
                            }
                        )
                    },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp)
                )

                Spacer(modifier = Modifier.height(10.dp))

                // Location Name
                OutlinedTextField(
                    value = locationName,
                    onValueChange = { locationName = it },
                    label = { Text(if (isHindi) "स्थान / मंडी क्षेत्र" else "Operating Location / Mandi Area") },
                    leadingIcon = { Icon(Icons.Default.LocationOn, contentDescription = null) },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp)
                )

                Spacer(modifier = Modifier.height(16.dp))

                // ==========================================
                // KYC VERIFICATION SECTION (Aadhaar, DL, RC)
                // ==========================================
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("kyc_verification_card"),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.25f)
                    ),
                    border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.35f))
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Security,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.size(20.dp)
                            )
                            Text(
                                text = if (isHindi) "केवाईसी दस्तावेज़ सत्यापन (KYC Verification)" else "KYC Verification & Trust Documents",
                                style = MaterialTheme.typography.titleSmall,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                        }

                        Text(
                            text = if (isHindi) "विश्वास सत्यापन हेतु आधार, ड्राइविंग लाइसेंस व वाहन आरसी जोड़ें" else "Attach Government KYC for instant Verified Trust badge and legal safety",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.padding(top = 4.dp, bottom = 12.dp)
                        )

                        // 1. Aadhaar Card
                        OutlinedTextField(
                            value = aadhaarNumber,
                            onValueChange = { if (it.length <= 14) aadhaarNumber = it },
                            label = { Text(if (isHindi) "आधार कार्ड नंबर (12 अंक)" else "Aadhaar Card Number (12 Digits)") },
                            placeholder = { Text("XXXX XXXX 1234") },
                            leadingIcon = { Icon(Icons.Default.CreditCard, contentDescription = null) },
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("kyc_aadhaar_input"),
                            shape = RoundedCornerShape(12.dp)
                        )

                        Spacer(modifier = Modifier.height(6.dp))

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = if (isAadhaarUploaded) (if (isHindi) "आधार दस्तावेज़ अपलोड हो गया ✓" else "Aadhaar Photo Attached ✓")
                                else (if (isHindi) "आधार फोटो अपलोड नहीं है" else "No file chosen"),
                                style = MaterialTheme.typography.bodySmall,
                                color = if (isAadhaarUploaded) Color(0xFF2E7D32) else MaterialTheme.colorScheme.onSurfaceVariant,
                                fontWeight = if (isAadhaarUploaded) FontWeight.Bold else FontWeight.Normal
                            )

                            OutlinedButton(
                                onClick = { isAadhaarUploaded = !isAadhaarUploaded },
                                shape = RoundedCornerShape(12.dp),
                                modifier = Modifier.testTag("upload_aadhaar_button")
                            ) {
                                Icon(
                                    imageVector = if (isAadhaarUploaded) Icons.Default.Check else Icons.Default.FileUpload,
                                    contentDescription = null,
                                    modifier = Modifier.size(16.dp)
                                )
                                Spacer(modifier = Modifier.width(4.dp))
                                Text(
                                    text = if (isAadhaarUploaded) "Change" else (if (isHindi) "अपलोड आधार" else "Upload Aadhaar"),
                                    fontSize = 12.sp
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(10.dp))

                        // 2. Driving License (DL)
                        OutlinedTextField(
                            value = dlNumber,
                            onValueChange = { dlNumber = it.uppercase() },
                            label = { Text(if (isHindi) "ड्राइविंग लाइसेंस (DL) नंबर" else "Driving License (DL) Number") },
                            placeholder = { Text("DL-0420210012345") },
                            leadingIcon = { Icon(Icons.Default.Badge, contentDescription = null) },
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("kyc_dl_input"),
                            shape = RoundedCornerShape(12.dp)
                        )

                        Spacer(modifier = Modifier.height(6.dp))

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = if (isDlUploaded) (if (isHindi) "डीएल दस्तावेज़ अपलोड हो गया ✓" else "DL Copy Attached ✓")
                                else (if (isHindi) "डीएल कॉपी अपलोड नहीं है" else "No file chosen"),
                                style = MaterialTheme.typography.bodySmall,
                                color = if (isDlUploaded) Color(0xFF2E7D32) else MaterialTheme.colorScheme.onSurfaceVariant,
                                fontWeight = if (isDlUploaded) FontWeight.Bold else FontWeight.Normal
                            )

                            OutlinedButton(
                                onClick = { isDlUploaded = !isDlUploaded },
                                shape = RoundedCornerShape(12.dp),
                                modifier = Modifier.testTag("upload_dl_button")
                            ) {
                                Icon(
                                    imageVector = if (isDlUploaded) Icons.Default.Check else Icons.Default.FileUpload,
                                    contentDescription = null,
                                    modifier = Modifier.size(16.dp)
                                )
                                Spacer(modifier = Modifier.width(4.dp))
                                Text(
                                    text = if (isDlUploaded) "Change" else (if (isHindi) "अपलोड DL" else "Upload DL"),
                                    fontSize = 12.sp
                                )
                            }
                        }

                        if (isMobility) {
                            Spacer(modifier = Modifier.height(10.dp))

                            // 3. Vehicle RC (Registration Certificate)
                            OutlinedTextField(
                                value = vehicleRcNumber,
                                onValueChange = { vehicleRcNumber = it.uppercase() },
                                label = { Text(if (isHindi) "वाहन आरसी (Vehicle RC) नंबर" else "Vehicle RC Number") },
                                placeholder = { Text("DL 01 AB 1234") },
                                leadingIcon = { Icon(Icons.Default.DirectionsCar, contentDescription = null) },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .testTag("kyc_rc_input"),
                                shape = RoundedCornerShape(12.dp)
                            )

                            Spacer(modifier = Modifier.height(6.dp))

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = if (isRcUploaded) (if (isHindi) "वाहन आरसी अपलोड हो गई ✓" else "Vehicle RC Attached ✓")
                                    else (if (isHindi) "आरसी कॉपी अपलोड नहीं है" else "No file chosen"),
                                    style = MaterialTheme.typography.bodySmall,
                                    color = if (isRcUploaded) Color(0xFF2E7D32) else MaterialTheme.colorScheme.onSurfaceVariant,
                                    fontWeight = if (isRcUploaded) FontWeight.Bold else FontWeight.Normal
                                )

                                OutlinedButton(
                                    onClick = { isRcUploaded = !isRcUploaded },
                                    shape = RoundedCornerShape(12.dp),
                                    modifier = Modifier.testTag("upload_rc_button")
                                ) {
                                    Icon(
                                        imageVector = if (isRcUploaded) Icons.Default.Check else Icons.Default.FileUpload,
                                        contentDescription = null,
                                        modifier = Modifier.size(16.dp)
                                    )
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text(
                                        text = if (isRcUploaded) "Change" else (if (isHindi) "अपलोड RC" else "Upload RC"),
                                        fontSize = 12.sp
                                    )
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(10.dp))

                        // DigiLocker / Trust Status Preview
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .background(MaterialTheme.colorScheme.surface, RoundedCornerShape(10.dp))
                                .padding(horizontal = 10.dp, vertical = 8.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.VerifiedUser,
                                contentDescription = null,
                                tint = Color(0xFF2E7D32),
                                modifier = Modifier.size(18.dp)
                            )
                            Text(
                                text = if (isHindi) "पंजीकरण के बाद AI / DigiLocker से तुरंत सत्यापित बैज जारी होगा।"
                                else "DigiLocker / AI Verification will award a Green Verified Trust badge.",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurface,
                                fontSize = 11.sp
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(20.dp))

                // Action Buttons
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    FilledTonalButton(
                        onClick = onDismiss,
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(24.dp)
                    ) {
                        Text(if (isHindi) "रद्द करें" else "Cancel")
                    }

                    Button(
                        onClick = {
                            if (name.isBlank() || speciality.isBlank() || phone.isBlank()) {
                                hasError = true
                            } else {
                                val cleanDigits = phone.replace(Regex("[^0-9]"), "")
                                val formattedWa = if (cleanDigits.length == 10) "91$cleanDigits" else cleanDigits

                                val newProvider = ProviderEntity(
                                    name = name.trim(),
                                    category = selectedCategory.name,
                                    subCategory = if (subCategory.isNotBlank()) subCategory.trim() else selectedCategory.displayName,
                                    speciality = speciality.trim(),
                                    phone = phone.trim(),
                                    whatsAppNumber = formattedWa,
                                    latitude = initialLat,
                                    longitude = initialLng,
                                    locationName = if (locationName.isNotBlank()) locationName.trim() else "Near Mandi Point",
                                    pricing = if (pricing.isNotBlank()) pricing.trim() else "Direct 0% Cut Rate",
                                    rating = 5.0f,
                                    reviewCount = 1,
                                    isAvailableNow = true,
                                    isVerified = true,
                                    experienceYears = experienceYears,
                                    description = description.trim(),
                                    aadhaarNumber = aadhaarNumber.trim(),
                                    drivingLicenseNumber = dlNumber.trim(),
                                    vehicleRcNumber = vehicleRcNumber.trim(),
                                    aadhaarDocUri = if (isAadhaarUploaded) "content://loksetu.kyc/aadhaar_${System.currentTimeMillis()}.jpg" else "",
                                    dlDocUri = if (isDlUploaded) "content://loksetu.kyc/dl_${System.currentTimeMillis()}.jpg" else "",
                                    rcDocUri = if (isRcUploaded) "content://loksetu.kyc/rc_${System.currentTimeMillis()}.jpg" else "",
                                    kycStatus = if (aadhaarNumber.isNotBlank() || isAadhaarUploaded) "VERIFIED" else "PENDING"
                                )
                                onAdd(newProvider)
                            }
                        },
                        modifier = Modifier
                            .weight(1.4f)
                            .testTag("submit_add_provider_button"),
                        colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary),
                        shape = RoundedCornerShape(24.dp)
                    ) {
                        Icon(imageVector = Icons.Default.Add, contentDescription = null, modifier = Modifier.size(18.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(if (isHindi) "पंजीकरण करें" else "Register Role", fontWeight = FontWeight.SemiBold)
                    }
                }
            }
        }
    }
}
