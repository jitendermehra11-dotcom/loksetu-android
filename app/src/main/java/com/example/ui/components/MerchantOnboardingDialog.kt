package com.example.ui.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.MoneyOff
import androidx.compose.material.icons.filled.Shield
import androidx.compose.material.icons.filled.Store
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Checkbox
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
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
import com.example.data.manager.CatalogPresetTemplate
import com.example.data.manager.MerchantOnboardingManager
import com.example.data.network.OndcCategory
import com.example.data.network.OndcProductItem

/**
 * Merchant Auto-Onboarding & Catalog Sync Dialog
 *
 * Streamlines 60-second merchant registration and instant catalog broadcasting
 * with zero hidden listing fees, direct UPI settlement, and ONDC open network discovery.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MerchantOnboardingDialog(
    initialStoreAddress: String = "Shop 14, Main Mandi Gate, Azamgarh",
    initialDistrict: String = "Azamgarh",
    userLatitude: Double = 28.6139,
    userLongitude: Double = 77.2090,
    onboardManager: MerchantOnboardingManager = MerchantOnboardingManager.getInstance(),
    onOnboardingComplete: (
        storeName: String,
        ownerName: String,
        category: OndcCategory,
        phone: String,
        upiId: String,
        address: String,
        district: String,
        selectedPresets: List<CatalogPresetTemplate>
    ) -> Unit,
    onDismiss: () -> Unit
) {
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)

    var storeName by remember { mutableStateOf("") }
    var ownerName by remember { mutableStateOf("") }
    var phone by remember { mutableStateOf("") }
    var upiId by remember { mutableStateOf("") }
    var selectedCategory by remember { mutableStateOf(OndcCategory.KIRANA) }
    var address by remember { mutableStateOf(initialStoreAddress) }
    var district by remember { mutableStateOf(initialDistrict) }

    val selectedPresets = remember { mutableStateListOf<CatalogPresetTemplate>() }

    // Update preset items when category changes
    LaunchedEffect(selectedCategory) {
        selectedPresets.clear()
        selectedPresets.addAll(onboardManager.getPresetTemplatesForCategory(selectedCategory))
    }

    var errorMessage by remember { mutableStateOf<String?>(null) }

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        containerColor = MaterialTheme.colorScheme.surface,
        modifier = Modifier.testTag("merchant_onboarding_sheet")
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .fillMaxHeight(0.94f)
                .padding(bottom = 24.dp)
        ) {
            // Header
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp, vertical = 8.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(36.dp)
                            .clip(CircleShape)
                            .background(Color(0xFFE8F5E9)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.Store,
                            contentDescription = null,
                            tint = Color(0xFF2E7D32),
                            modifier = Modifier.size(20.dp)
                        )
                    }
                    Spacer(modifier = Modifier.width(10.dp))
                    Column {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                text = "Merchant Auto-Onboarding",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.ExtraBold
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Surface(
                                shape = RoundedCornerShape(6.dp),
                                color = Color(0xFFE8F5E9)
                            ) {
                                Text(
                                    text = "₹0 Fee",
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Color(0xFF2E7D32),
                                    modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                )
                            }
                        }
                        Text(
                            text = "Broadcast store catalog to ONDC in 60 seconds",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }

                IconButton(
                    onClick = onDismiss,
                    modifier = Modifier.testTag("close_onboarding_btn")
                ) {
                    Icon(Icons.Default.Close, contentDescription = "Close")
                }
            }

            LazyColumn(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                // Zero-commission commitment card
                item {
                    Surface(
                        shape = RoundedCornerShape(14.dp),
                        color = Color(0xFFE8F5E9),
                        border = BorderStroke(1.dp, Color(0xFFA5D6A7)),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(modifier = Modifier.padding(14.dp)) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    Icons.Default.Shield,
                                    contentDescription = null,
                                    tint = Color(0xFF2E7D32),
                                    modifier = Modifier.size(20.dp)
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    text = "LokSetu Zero-Hidden-Fee Guarantee",
                                    fontWeight = FontWeight.Bold,
                                    style = MaterialTheme.typography.titleSmall,
                                    color = Color(0xFF1B5E20)
                                )
                            }
                            Spacer(modifier = Modifier.height(6.dp))
                            Text(
                                text = "• 0% Platform Commission on product sales\n• ₹0 Catalog Listing or Onboarding Charges\n• 100% Direct Instant Settlement to your Shop UPI ID\n• Live decentralized discoverability across ONDC network",
                                fontSize = 11.sp,
                                color = Color(0xFF2E7D32),
                                lineHeight = 16.sp
                            )
                        }
                    }
                }

                // Category selection
                item {
                    Text(
                        text = "Store Category",
                        style = MaterialTheme.typography.labelMedium,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                    Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                        Row(
                            horizontalArrangement = Arrangement.spacedBy(6.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            listOf(OndcCategory.KIRANA, OndcCategory.AGRI_MANDI, OndcCategory.AGRI_INPUTS).forEach { cat ->
                                FilterChip(
                                    selected = selectedCategory == cat,
                                    onClick = { selectedCategory = cat },
                                    label = { Text("${cat.iconEmoji} ${cat.displayName}", fontSize = 11.sp) },
                                    modifier = Modifier.weight(1f)
                                )
                            }
                        }
                        Row(
                            horizontalArrangement = Arrangement.spacedBy(6.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            listOf(OndcCategory.HARDWARE, OndcCategory.PHARMACY, OndcCategory.DAIRY).forEach { cat ->
                                FilterChip(
                                    selected = selectedCategory == cat,
                                    onClick = { selectedCategory = cat },
                                    label = { Text("${cat.iconEmoji} ${cat.displayName}", fontSize = 11.sp) },
                                    modifier = Modifier.weight(1f)
                                )
                            }
                        }
                    }
                }

                // Store details fields
                item {
                    OutlinedTextField(
                        value = storeName,
                        onValueChange = { storeName = it },
                        label = { Text("Shop / Business Name*") },
                        placeholder = { Text("e.g. Kisan Kirana & General Store") },
                        singleLine = true,
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("onboard_store_name_input")
                    )
                }

                item {
                    OutlinedTextField(
                        value = ownerName,
                        onValueChange = { ownerName = it },
                        label = { Text("Proprietor / Shopkeeper Name*") },
                        placeholder = { Text("e.g. Ramprasad Yadav") },
                        singleLine = true,
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("onboard_owner_name_input")
                    )
                }

                item {
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(10.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        OutlinedTextField(
                            value = phone,
                            onValueChange = { if (it.length <= 10) phone = it },
                            label = { Text("Mobile / WhatsApp*") },
                            placeholder = { Text("10 digits") },
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone),
                            singleLine = true,
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier
                                .weight(1f)
                                .testTag("onboard_phone_input")
                        )

                        OutlinedTextField(
                            value = upiId,
                            onValueChange = { upiId = it },
                            label = { Text("Direct UPI ID*") },
                            placeholder = { Text("shop@upi") },
                            singleLine = true,
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier
                                .weight(1f)
                                .testTag("onboard_upi_input")
                        )
                    }
                }

                item {
                    OutlinedTextField(
                        value = address,
                        onValueChange = { address = it },
                        label = { Text("Mandi / Street Address*") },
                        singleLine = true,
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("onboard_address_input")
                    )
                }

                // Catalog Selection
                item {
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "Broadcast Catalog Presets (Tap to select/unselect)",
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = "Instant Mandi benchmark items ready for 1-click broadcast",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                val templates = onboardManager.getPresetTemplatesForCategory(selectedCategory)
                items(templates) { template ->
                    val isChecked = selectedPresets.any { it.title == template.title }

                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable {
                                if (isChecked) {
                                    selectedPresets.removeAll { it.title == template.title }
                                } else {
                                    selectedPresets.add(template)
                                }
                            },
                        shape = RoundedCornerShape(12.dp),
                        colors = CardDefaults.cardColors(
                            containerColor = if (isChecked) MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.25f) else MaterialTheme.colorScheme.surface
                        ),
                        border = BorderStroke(
                            1.dp,
                            if (isChecked) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.outlineVariant
                        )
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(12.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.weight(1f)) {
                                Checkbox(
                                    checked = isChecked,
                                    onCheckedChange = { checked ->
                                        if (checked) {
                                            selectedPresets.add(template)
                                        } else {
                                            selectedPresets.removeAll { it.title == template.title }
                                        }
                                    }
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(template.iconEmoji, fontSize = 20.sp)
                                Spacer(modifier = Modifier.width(8.dp))
                                Column {
                                    Text(
                                        text = template.title,
                                        fontWeight = FontWeight.Bold,
                                        style = MaterialTheme.typography.bodyMedium
                                    )
                                    Text(
                                        text = "${template.hindiTitle} • ${template.unit}",
                                        fontSize = 11.sp,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                            }

                            Text(
                                text = "₹${template.defaultPrice.toInt()}",
                                fontWeight = FontWeight.Bold,
                                fontSize = 14.sp,
                                color = MaterialTheme.colorScheme.primary
                            )
                        }
                    }
                }

                if (errorMessage != null) {
                    item {
                        Text(
                            text = errorMessage ?: "",
                            color = MaterialTheme.colorScheme.error,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }

                // Submit Button
                item {
                    Spacer(modifier = Modifier.height(8.dp))
                    Button(
                        onClick = {
                            if (storeName.isBlank()) {
                                errorMessage = "Please enter your store name"
                                return@Button
                            }
                            if (ownerName.isBlank()) {
                                errorMessage = "Please enter proprietor name"
                                return@Button
                            }
                            if (phone.length < 10) {
                                errorMessage = "Please enter a valid 10-digit mobile number"
                                return@Button
                            }
                            val safeUpi = if (upiId.isBlank()) "${phone}@upi" else upiId

                            errorMessage = null
                            onOnboardingComplete(
                                storeName.trim(),
                                ownerName.trim(),
                                selectedCategory,
                                phone.trim(),
                                safeUpi.trim(),
                                address.trim(),
                                district.trim(),
                                selectedPresets.toList()
                            )
                            onDismiss()
                        },
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(50.dp)
                            .testTag("submit_merchant_onboarding_btn")
                    ) {
                        Icon(Icons.Default.CheckCircle, contentDescription = null, modifier = Modifier.size(18.dp))
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Broadcast Catalog on ONDC (₹0 Fee)", fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    }
}
