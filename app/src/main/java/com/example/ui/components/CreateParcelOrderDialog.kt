package com.example.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.LocalShipping
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Payments
import androidx.compose.material.icons.filled.Shield
import androidx.compose.material.icons.filled.Storefront
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Checkbox
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
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
import com.example.data.manager.DeliveryPaymentMode
import com.example.data.manager.FuelType
import com.example.ui.viewmodel.LokSetuViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CreateParcelOrderDialog(
    viewModel: LokSetuViewModel,
    onDismiss: () -> Unit
) {
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)

    var storeName by remember { mutableStateOf("Gupta Daily Needs & Ration Store") }
    var storeAddress by remember { mutableStateOf("Shop 7, Main Market, Najafgarh") }
    var storePhone by remember { mutableStateOf("+91 98112 34567") }
    var storeUpiId by remember { mutableStateOf("guptastore@icici") }

    var customerName by remember { mutableStateOf("Sunil Yadav") }
    var customerPhone by remember { mutableStateOf("+91 98711 55667") }
    var customerAddress by remember { mutableStateOf("Flat 204, Pocket 1, Sector 11 Dwarka") }

    var itemsDescription by remember { mutableStateOf("5kg Atta, 2L Mustard Oil, 1kg Daal") }
    var itemCostInput by remember { mutableStateOf("450") }
    var deliveryFeeInput by remember { mutableStateOf("60") }
    var distanceKmInput by remember { mutableStateOf("5.2") }
    var selectedFuelType by remember { mutableStateOf(FuelType.PETROL) }

    var paymentMode by remember { mutableStateOf(DeliveryPaymentMode.PREPAID_ESCROW) }
    var codCustomerConfirmed by remember { mutableStateOf(true) }

    val itemCost = itemCostInput.toDoubleOrNull() ?: 0.0
    val deliveryFee = deliveryFeeInput.toDoubleOrNull() ?: 0.0
    val distanceKm = distanceKmInput.toDoubleOrNull() ?: 5.2
    val fuelResult = remember(distanceKm, selectedFuelType) {
        viewModel.paymentManager.calculateFuelLinkedPayout(distanceKm, selectedFuelType)
    }
    val totalAmount = itemCost + deliveryFee

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        containerColor = MaterialTheme.colorScheme.surface,
        modifier = Modifier.testTag("create_parcel_order_sheet")
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .navigationBarsPadding()
                .padding(horizontal = 20.dp)
                .verticalScroll(rememberScrollState())
        ) {
            // Header
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(38.dp)
                            .clip(CircleShape)
                            .background(MaterialTheme.colorScheme.primaryContainer),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.LocalShipping,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                    Spacer(modifier = Modifier.width(10.dp))
                    Column {
                        Text(
                            text = "New Store Parcel Delivery",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = "Pre-paid Escrow & Zero-Rider-Spending Policy",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
                IconButton(onClick = onDismiss) {
                    Icon(Icons.Default.Close, contentDescription = "Close")
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            // Policy Guarantee Banner
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(14.dp),
                colors = CardDefaults.cardColors(containerColor = Color(0xFFE8F5E9)),
                border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFA5D6A7))
            ) {
                Row(modifier = Modifier.padding(12.dp), verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.Shield,
                        contentDescription = null,
                        tint = Color(0xFF2E7D32),
                        modifier = Modifier.size(22.dp)
                    )
                    Spacer(modifier = Modifier.width(10.dp))
                    Column {
                        Text(
                            text = "Zero Out-of-Pocket Spending Guarantee",
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFF1B5E20),
                            fontSize = 12.sp
                        )
                        Text(
                            text = "Riders never pay shopkeepers cash. 100% item cost + delivery fee locked upfront in LokSetu Escrow.",
                            fontSize = 11.sp,
                            color = Color(0xFF2E7D32)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            // Payment Mode Selector
            Text(
                text = "Select Payment & Protection Mode:",
                fontWeight = FontWeight.Bold,
                style = MaterialTheme.typography.titleSmall
            )
            Spacer(modifier = Modifier.height(6.dp))

            // Option 1: 100% Pre-paid Escrow
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { paymentMode = DeliveryPaymentMode.PREPAID_ESCROW },
                shape = RoundedCornerShape(12.dp),
                colors = CardDefaults.cardColors(
                    containerColor = if (paymentMode == DeliveryPaymentMode.PREPAID_ESCROW)
                        MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.4f)
                    else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f)
                ),
                border = if (paymentMode == DeliveryPaymentMode.PREPAID_ESCROW)
                    androidx.compose.foundation.BorderStroke(2.dp, MaterialTheme.colorScheme.primary)
                else null
            ) {
                Row(
                    modifier = Modifier.padding(12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    RadioButton(
                        selected = paymentMode == DeliveryPaymentMode.PREPAID_ESCROW,
                        onClick = { paymentMode = DeliveryPaymentMode.PREPAID_ESCROW }
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Column {
                        Text(
                            text = "🛡️ 100% Pre-paid Escrow (Recommended)",
                            fontWeight = FontWeight.Bold,
                            fontSize = 13.sp,
                            color = MaterialTheme.colorScheme.primary
                        )
                        Text(
                            text = "Item (₹$itemCost) + Delivery (₹$deliveryFee) locked in escrow upfront. Rider spends ₹0 at shop.",
                            fontSize = 11.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Option 2: Cash on Delivery (COD)
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { paymentMode = DeliveryPaymentMode.CASH_ON_DELIVERY },
                shape = RoundedCornerShape(12.dp),
                colors = CardDefaults.cardColors(
                    containerColor = if (paymentMode == DeliveryPaymentMode.CASH_ON_DELIVERY)
                        MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.4f)
                    else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f)
                ),
                border = if (paymentMode == DeliveryPaymentMode.CASH_ON_DELIVERY)
                    androidx.compose.foundation.BorderStroke(2.dp, MaterialTheme.colorScheme.secondary)
                else null
            ) {
                Column(modifier = Modifier.padding(12.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        RadioButton(
                            selected = paymentMode == DeliveryPaymentMode.CASH_ON_DELIVERY,
                            onClick = { paymentMode = DeliveryPaymentMode.CASH_ON_DELIVERY }
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Column {
                            Text(
                                text = "💵 Cash on Delivery (COD) with Upfront Confirmation",
                                fontWeight = FontWeight.Bold,
                                fontSize = 13.sp
                            )
                            Text(
                                text = "Requires customer verification before rider assignment. Rider retains fee from collected cash.",
                                fontSize = 11.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }

                    if (paymentMode == DeliveryPaymentMode.CASH_ON_DELIVERY) {
                        Spacer(modifier = Modifier.height(8.dp))
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Checkbox(
                                checked = codCustomerConfirmed,
                                onCheckedChange = { codCustomerConfirmed = it }
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = "Customer has verified mobile and confirmed upfront payment upon drop-off",
                                fontSize = 11.sp,
                                color = Color(0xFFB71C1C),
                                fontWeight = FontWeight.SemiBold
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            // Merchant / Store Details
            Text(
                text = "1. Local Merchant / Store Details",
                fontWeight = FontWeight.Bold,
                style = MaterialTheme.typography.titleSmall,
                color = MaterialTheme.colorScheme.primary
            )
            Spacer(modifier = Modifier.height(6.dp))

            OutlinedTextField(
                value = storeName,
                onValueChange = { storeName = it },
                label = { Text("Store / Merchant Name") },
                singleLine = true,
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier.fillMaxWidth()
            )
            Spacer(modifier = Modifier.height(6.dp))
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                OutlinedTextField(
                    value = storeAddress,
                    onValueChange = { storeAddress = it },
                    label = { Text("Store Address") },
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(12.dp)
                )
                OutlinedTextField(
                    value = storeUpiId,
                    onValueChange = { storeUpiId = it },
                    label = { Text("Store UPI ID") },
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(12.dp)
                )
            }

            Spacer(modifier = Modifier.height(14.dp))

            // Customer Details
            Text(
                text = "2. Customer & Drop-off Location",
                fontWeight = FontWeight.Bold,
                style = MaterialTheme.typography.titleSmall,
                color = MaterialTheme.colorScheme.primary
            )
            Spacer(modifier = Modifier.height(6.dp))

            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                OutlinedTextField(
                    value = customerName,
                    onValueChange = { customerName = it },
                    label = { Text("Customer Name") },
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(12.dp)
                )
                OutlinedTextField(
                    value = customerPhone,
                    onValueChange = { customerPhone = it },
                    label = { Text("Phone Number") },
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(12.dp)
                )
            }
            Spacer(modifier = Modifier.height(6.dp))
            OutlinedTextField(
                value = customerAddress,
                onValueChange = { customerAddress = it },
                label = { Text("Delivery Address / House / Flat") },
                singleLine = true,
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(modifier = Modifier.height(14.dp))

            // Items & Pricing Breakdown
            Text(
                text = "3. Items & Payment Breakdown",
                fontWeight = FontWeight.Bold,
                style = MaterialTheme.typography.titleSmall,
                color = MaterialTheme.colorScheme.primary
            )
            Spacer(modifier = Modifier.height(6.dp))

            OutlinedTextField(
                value = itemsDescription,
                onValueChange = { itemsDescription = it },
                label = { Text("Items Description (e.g. Atta, Milk, Medicine)") },
                singleLine = true,
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier.fillMaxWidth()
            )
            Spacer(modifier = Modifier.height(6.dp))

            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                OutlinedTextField(
                    value = itemCostInput,
                    onValueChange = { itemCostInput = it },
                    label = { Text("Item Cost (₹)") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(12.dp)
                )
                OutlinedTextField(
                    value = distanceKmInput,
                    onValueChange = { distanceKmInput = it },
                    label = { Text("Est. Distance (km)") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(12.dp)
                )
            }
            Spacer(modifier = Modifier.height(8.dp))

            // Dynamic Fuel Price Indexing Box
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.35f)),
                border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.secondary.copy(alpha = 0.4f))
            ) {
                Column(modifier = Modifier.padding(10.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "⛽ Live Fuel-Linked Surcharge Index",
                            fontWeight = FontWeight.Bold,
                            fontSize = 12.sp,
                            color = MaterialTheme.colorScheme.secondary
                        )
                        Surface(
                            shape = RoundedCornerShape(8.dp),
                            color = MaterialTheme.colorScheme.secondary.copy(alpha = 0.2f)
                        ) {
                            Text(
                                text = "Base ₹${selectedFuelType.basePerKmRate}/km + Surcharge",
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.secondary,
                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                            )
                        }
                    }
                    Spacer(modifier = Modifier.height(6.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        FuelType.values().forEach { type ->
                            FilterChip(
                                selected = selectedFuelType == type,
                                onClick = { selectedFuelType = type },
                                label = { Text("${type.iconEmoji} ${type.displayName}", fontSize = 11.sp) },
                                colors = FilterChipDefaults.filterChipColors(
                                    selectedContainerColor = MaterialTheme.colorScheme.secondary,
                                    selectedLabelColor = MaterialTheme.colorScheme.onSecondary
                                )
                            )
                        }
                    }
                    Spacer(modifier = Modifier.height(6.dp))
                    Text(
                        text = "Fare Calc: Base ₹30 + Dist ₹${fuelResult.baseDistanceFare} + Fuel Surcharge ₹${fuelResult.totalFuelSurcharge} = ₹${fuelResult.grossPayout.toInt()}",
                        fontSize = 11.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.End
                    ) {
                        Button(
                            onClick = {
                                deliveryFeeInput = fuelResult.grossPayout.toInt().toString()
                            },
                            shape = RoundedCornerShape(8.dp),
                            modifier = Modifier.height(34.dp)
                        ) {
                            Text("⚡ Apply Fuel-Indexed Fee (₹${fuelResult.grossPayout.toInt()})", fontSize = 11.sp)
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            OutlinedTextField(
                value = deliveryFeeInput,
                onValueChange = { deliveryFeeInput = it },
                label = { Text("Rider Delivery Payout Fee (₹)") },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp)
            )

            Spacer(modifier = Modifier.height(12.dp))

            // Total Summary Card
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(14.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.6f))
            ) {
                Column(modifier = Modifier.padding(14.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(text = "Store Item Cost:", fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        Text(text = "₹$itemCost", fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
                    }
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(text = "Rider Delivery Payout:", fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        Text(text = "₹$deliveryFee", fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
                    }
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(text = "LokSetu Platform Commission:", fontSize = 12.sp, color = Color(0xFF2E7D32), fontWeight = FontWeight.Bold)
                        Text(text = "₹0 (Zero Commission)", fontSize = 12.sp, color = Color(0xFF2E7D32), fontWeight = FontWeight.Bold)
                    }
                    Spacer(modifier = Modifier.height(6.dp))
                    HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant)
                    Spacer(modifier = Modifier.height(6.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = if (paymentMode == DeliveryPaymentMode.PREPAID_ESCROW)
                                "Total Pre-paid Escrow Locked:"
                            else "Total Cash to Collect at Drop-off:",
                            fontWeight = FontWeight.Bold,
                            fontSize = 13.sp
                        )
                        Text(
                            text = "₹$totalAmount",
                            fontWeight = FontWeight.ExtraBold,
                            fontSize = 16.sp,
                            color = MaterialTheme.colorScheme.primary
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(18.dp))

            Button(
                onClick = {
                    if (paymentMode == DeliveryPaymentMode.PREPAID_ESCROW) {
                        viewModel.createPrepaidParcelOrder(
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
                            fuelType = selectedFuelType,
                            fuelSurchargeBonus = fuelResult.totalFuelSurcharge
                        )
                    } else {
                        viewModel.createCodParcelOrder(
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
                            customerConfirmed = codCustomerConfirmed,
                            distanceKm = distanceKm,
                            fuelType = selectedFuelType,
                            fuelSurchargeBonus = fuelResult.totalFuelSurcharge
                        )
                    }
                    onDismiss()
                },
                shape = RoundedCornerShape(16.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(52.dp)
                    .testTag("submit_create_parcel_order_button"),
                colors = ButtonDefaults.buttonColors(
                    containerColor = if (paymentMode == DeliveryPaymentMode.PREPAID_ESCROW)
                        MaterialTheme.colorScheme.primary
                    else Color(0xFFE65100)
                )
            ) {
                Icon(
                    imageVector = if (paymentMode == DeliveryPaymentMode.PREPAID_ESCROW) Icons.Default.Lock else Icons.Default.Payments,
                    contentDescription = null,
                    modifier = Modifier.size(18.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = if (paymentMode == DeliveryPaymentMode.PREPAID_ESCROW)
                        "Lock 100% Escrow & Dispatch Order (₹$totalAmount)"
                    else "Dispatch Upfront-Confirmed COD Order (₹$totalAmount)",
                    fontWeight = FontWeight.Bold,
                    fontSize = 13.sp
                )
            }

            Spacer(modifier = Modifier.height(24.dp))
        }
    }
}
