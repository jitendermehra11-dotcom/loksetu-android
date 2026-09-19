package com.example.ui.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Agriculture
import androidx.compose.material.icons.filled.Cancel
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Gavel
import androidx.compose.material.icons.filled.Handshake
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.manager.DealStage
import com.example.data.manager.FarmDeal
import com.example.ui.viewmodel.LokSetuViewModel

@Composable
fun FarmerDealsSection(
    viewModel: LokSetuViewModel,
    modifier: Modifier = Modifier
) {
    val deals by viewModel.activeFarmDeals.collectAsState()
    val isCreateOpen by viewModel.isCreateDealOpen.collectAsState()

    Scaffold(
        modifier = modifier,
        containerColor = Color.Transparent,
        floatingActionButton = {
            FloatingActionButton(
                onClick = { viewModel.openCreateDeal() },
                containerColor = MaterialTheme.colorScheme.primary,
                contentColor = MaterialTheme.colorScheme.onPrimary,
                shape = RoundedCornerShape(20.dp),
                modifier = Modifier.testTag("lock_new_deal_fab")
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(Icons.Default.Lock, contentDescription = null, modifier = Modifier.size(18.dp))
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Lock 15% Escrow Deal", fontWeight = FontWeight.Bold)
                }
            }
        }
    ) { innerPadding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxWidth()
                .padding(innerPadding)
                .padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            // Header summary banner
            item {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 8.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.45f)),
                    shape = RoundedCornerShape(24.dp),
                    border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant)
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(18.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(42.dp)
                                    .background(MaterialTheme.colorScheme.primaryContainer, CircleShape),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Handshake,
                                    contentDescription = null,
                                    tint = MaterialTheme.colorScheme.onPrimaryContainer
                                )
                            }
                            Spacer(modifier = Modifier.width(12.dp))
                            Column {
                                Text(
                                    text = "Direct Farm-Gate Deal Lock",
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.Bold
                                )
                                Text(
                                    text = "15% Token Escrow Advance & Penalty Protection",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(14.dp))

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Column {
                                Text("Escrow Advance Rate", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                Text("15% Locked", fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
                            }
                            Column {
                                Text("Buyer Cancellation Penalty", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                Text("15% to Farmer", fontWeight = FontWeight.Bold, color = Color(0xFF2E7D32))
                            }
                            Column {
                                Text("Farmer Default Penalty", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                Text("Refund + 5%", fontWeight = FontWeight.Bold, color = Color(0xFFD32F2F))
                            }
                        }
                    }
                }
            }

            items(deals, key = { it.id }) { deal ->
                FarmDealCard(
                    deal = deal,
                    onComplete = { viewModel.completeDeal(deal.id) },
                    onBuyerCancel = { reason -> viewModel.cancelDealByBuyer(deal.id, reason) },
                    onFarmerCancel = { reason -> viewModel.cancelDealByFarmer(deal.id, reason) }
                )
            }

            item {
                Spacer(modifier = Modifier.height(80.dp))
            }
        }

        if (isCreateOpen) {
            CreateFarmDealDialog(
                onDismiss = { viewModel.closeCreateDeal() },
                onSubmit = { crop, variety, qty, rate, fName, fPhone, fLoc, bName, bPhone ->
                    viewModel.createFarmerDeal(crop, variety, qty, rate, fName, fPhone, fLoc, bName, bPhone)
                }
            )
        }
    }
}

@Composable
fun FarmDealCard(
    deal: FarmDeal,
    onComplete: () -> Unit,
    onBuyerCancel: (String) -> Unit,
    onFarmerCancel: (String) -> Unit
) {
    var showCancelActions by remember { mutableStateOf(false) }

    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        shape = RoundedCornerShape(24.dp),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            // Top Row: Crop & Stage Badge
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = deal.cropName,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = "${deal.variety} • Deal #${deal.id}",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                // Stage Chip
                val (chipBg, chipText) = when (deal.stage) {
                    DealStage.ESCROW_LOCKED -> Color(0xFFE8F5E9) to Color(0xFF1B5E20)
                    DealStage.HARVEST_DISPATCH_READY -> Color(0xFFFFF3E0) to Color(0xFFE65100)
                    DealStage.COMPLETED -> Color(0xFFE3F2FD) to Color(0xFF0D47A1)
                    DealStage.CANCELLED_BY_BUYER -> Color(0xFFFFEBEE) to Color(0xFFB71C1C)
                    DealStage.CANCELLED_BY_FARMER -> Color(0xFFFFEBEE) to Color(0xFFB71C1C)
                    DealStage.PENDING_ADVANCE -> Color(0xFFF5F5F5) to Color(0xFF616161)
                }

                Box(
                    modifier = Modifier
                        .background(chipBg, RoundedCornerShape(14.dp))
                        .padding(horizontal = 10.dp, vertical = 5.dp)
                ) {
                    Text(
                        text = when (deal.stage) {
                            DealStage.ESCROW_LOCKED -> "15% Escrow Locked"
                            DealStage.HARVEST_DISPATCH_READY -> "Ready for Dispatch"
                            DealStage.COMPLETED -> "100% Completed"
                            DealStage.CANCELLED_BY_BUYER -> "Buyer Cancelled (Penalty Paid)"
                            DealStage.CANCELLED_BY_FARMER -> "Farmer Defaulted (Refunded)"
                            DealStage.PENDING_ADVANCE -> "Pending Advance"
                        },
                        color = chipText,
                        fontWeight = FontWeight.Bold,
                        fontSize = 11.sp
                    )
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Value Grid
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.35f), RoundedCornerShape(16.dp))
                    .padding(12.dp),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Column {
                    Text("Total Deal Value", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    Text("₹${String.format("%,.0f", deal.totalDealValue)}", fontWeight = FontWeight.Black, style = MaterialTheme.typography.titleSmall)
                    Text("${deal.quantityQuintals.toInt()} Qtl @ ₹${deal.ratePerQuintal.toInt()}/Q", style = MaterialTheme.typography.bodySmall, fontSize = 11.sp)
                }

                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text("15% Advance Locked", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.primary, fontWeight = FontWeight.SemiBold)
                    Text("₹${String.format("%,.0f", deal.advanceEscrowAmount)}", fontWeight = FontWeight.Black, style = MaterialTheme.typography.titleSmall, color = MaterialTheme.colorScheme.primary)
                    Text("In Safe Escrow", style = MaterialTheme.typography.bodySmall, fontSize = 11.sp, color = Color(0xFF2E7D32))
                }

                Column(horizontalAlignment = Alignment.End) {
                    Text("85% Balance Due", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    Text("₹${String.format("%,.0f", deal.remainingBalanceAmount)}", fontWeight = FontWeight.Bold, style = MaterialTheme.typography.titleSmall)
                    Text("At Weighment", style = MaterialTheme.typography.bodySmall, fontSize = 11.sp)
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Parties Info
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text("Farmer:", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.outline)
                    Text("${deal.farmerName} (${deal.farmerPhone})", style = MaterialTheme.typography.bodySmall, fontWeight = FontWeight.SemiBold)
                    Text(deal.farmerLocation, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant, fontSize = 11.sp)
                }
                Spacer(modifier = Modifier.width(8.dp))
                Column(modifier = Modifier.weight(1f)) {
                    Text("Buyer:", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.outline)
                    Text("${deal.buyerName} (${deal.buyerPhone})", style = MaterialTheme.typography.bodySmall, fontWeight = FontWeight.SemiBold)
                }
            }

            // Audit Note
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = "📌 ${deal.auditNote}",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                fontSize = 11.sp
            )

            // Actions for Active Deals
            if (deal.stage == DealStage.ESCROW_LOCKED || deal.stage == DealStage.HARVEST_DISPATCH_READY) {
                Spacer(modifier = Modifier.height(12.dp))
                HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant)
                Spacer(modifier = Modifier.height(10.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Button(
                        onClick = onComplete,
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(20.dp)
                    ) {
                        Icon(Icons.Default.CheckCircle, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("Weigh & Release 100%")
                    }

                    OutlinedButton(
                        onClick = { showCancelActions = !showCancelActions },
                        shape = RoundedCornerShape(20.dp),
                        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant)
                    ) {
                        Icon(Icons.Default.Gavel, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Penalty Rules")
                    }
                }

                if (showCancelActions) {
                    Spacer(modifier = Modifier.height(10.dp))
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)),
                        shape = RoundedCornerShape(16.dp),
                        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant)
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(12.dp)
                        ) {
                            Text(
                                text = "Simulate Deal Cancellation / Dispute:",
                                style = MaterialTheme.typography.labelMedium,
                                fontWeight = FontWeight.Bold
                            )
                            Spacer(modifier = Modifier.height(6.dp))

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                Button(
                                    onClick = {
                                        onBuyerCancel("Buyer failed to send transport truck after harvest")
                                    },
                                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF2E7D32)),
                                    shape = RoundedCornerShape(16.dp),
                                    modifier = Modifier.weight(1f)
                                ) {
                                    Text("Buyer Cancels\n(15% ₹${deal.advanceEscrowAmount.toInt()} to Farmer)", fontSize = 11.sp, textAlign = androidx.compose.ui.text.style.TextAlign.Center)
                                }

                                Button(
                                    onClick = {
                                        onFarmerCancel("Farmer sold crop to third party")
                                    },
                                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFD32F2F)),
                                    shape = RoundedCornerShape(16.dp),
                                    modifier = Modifier.weight(1f)
                                ) {
                                    val pen5 = (deal.totalDealValue * 0.05).toInt()
                                    Text("Farmer Defaults\n(Refund + ₹$pen5 Pen)", fontSize = 11.sp, textAlign = androidx.compose.ui.text.style.TextAlign.Center)
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CreateFarmDealDialog(
    onDismiss: () -> Unit,
    onSubmit: (
        crop: String,
        variety: String,
        qty: Double,
        rate: Double,
        fName: String,
        fPhone: String,
        fLoc: String,
        bName: String,
        bPhone: String
    ) -> Unit
) {
    var crop by remember { mutableStateOf("Wheat (गेहूं)") }
    var variety by remember { mutableStateOf("Sharbati Premium") }
    var qtyStr by remember { mutableStateOf("100") }
    var rateStr by remember { mutableStateOf("2400") }

    var farmerName by remember { mutableStateOf("Rameshwar Farmer") }
    var farmerPhone by remember { mutableStateOf("+91 98123 45678") }
    var farmerLoc by remember { mutableStateOf("Mandi Farm Gate, Block A") }

    var buyerName by remember { mutableStateOf("Kisan Traders Ltd") }
    var buyerPhone by remember { mutableStateOf("+91 98765 43210") }

    val qty = qtyStr.toDoubleOrNull() ?: 0.0
    val rate = rateStr.toDoubleOrNull() ?: 0.0
    val total = qty * rate
    val advance15 = total * 0.15

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true),
        containerColor = MaterialTheme.colorScheme.surface,
        shape = RoundedCornerShape(topStart = 28.dp, topEnd = 28.dp),
        dragHandle = null
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .navigationBarsPadding()
                .padding(20.dp)
                .verticalScroll(rememberScrollState()),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Lock Direct Farm Deal",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
                IconButton(onClick = onDismiss) {
                    Icon(Icons.Default.Close, contentDescription = "Close")
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Escrow 15% Summary Card
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.5f)),
                shape = RoundedCornerShape(20.dp),
                border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(14.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text("Total Estimated Value", style = MaterialTheme.typography.bodySmall)
                        Text("₹${String.format("%,.0f", total)}", fontWeight = FontWeight.Black, style = MaterialTheme.typography.titleMedium)
                    }
                    Column(horizontalAlignment = Alignment.End) {
                        Text("15% Escrow Advance to Lock", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.primary, fontWeight = FontWeight.Bold)
                        Text("₹${String.format("%,.0f", advance15)}", fontWeight = FontWeight.Black, style = MaterialTheme.typography.titleMedium, color = MaterialTheme.colorScheme.primary)
                    }
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            OutlinedTextField(
                value = crop,
                onValueChange = { crop = it },
                label = { Text("Crop Name (e.g. Wheat, Basmati, Potato)") },
                shape = RoundedCornerShape(16.dp),
                modifier = Modifier.fillMaxWidth(),
                singleLine = true
            )

            Spacer(modifier = Modifier.height(8.dp))

            OutlinedTextField(
                value = variety,
                onValueChange = { variety = it },
                label = { Text("Variety / Grade") },
                shape = RoundedCornerShape(16.dp),
                modifier = Modifier.fillMaxWidth(),
                singleLine = true
            )

            Spacer(modifier = Modifier.height(8.dp))

            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                OutlinedTextField(
                    value = qtyStr,
                    onValueChange = { qtyStr = it.filter { c -> c.isDigit() || c == '.' } },
                    label = { Text("Quantity (Quintals)") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    shape = RoundedCornerShape(16.dp),
                    modifier = Modifier.weight(1f),
                    singleLine = true
                )
                OutlinedTextField(
                    value = rateStr,
                    onValueChange = { rateStr = it.filter { c -> c.isDigit() || c == '.' } },
                    label = { Text("Rate (₹ / Quintal)") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    shape = RoundedCornerShape(16.dp),
                    modifier = Modifier.weight(1f),
                    singleLine = true
                )
            }

            Spacer(modifier = Modifier.height(12.dp))
            HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant)
            Spacer(modifier = Modifier.height(10.dp))

            Text("Farmer Details", style = MaterialTheme.typography.labelMedium, fontWeight = FontWeight.Bold, modifier = Modifier.align(Alignment.Start))
            Spacer(modifier = Modifier.height(6.dp))

            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                OutlinedTextField(
                    value = farmerName,
                    onValueChange = { farmerName = it },
                    label = { Text("Farmer Name") },
                    shape = RoundedCornerShape(16.dp),
                    modifier = Modifier.weight(1f),
                    singleLine = true
                )
                OutlinedTextField(
                    value = farmerPhone,
                    onValueChange = { farmerPhone = it },
                    label = { Text("Phone") },
                    shape = RoundedCornerShape(16.dp),
                    modifier = Modifier.weight(1f),
                    singleLine = true
                )
            }

            Spacer(modifier = Modifier.height(6.dp))

            OutlinedTextField(
                value = farmerLoc,
                onValueChange = { farmerLoc = it },
                label = { Text("Farm Gate / Dispatch Location") },
                shape = RoundedCornerShape(16.dp),
                modifier = Modifier.fillMaxWidth(),
                singleLine = true
            )

            Spacer(modifier = Modifier.height(12.dp))
            Text("Buyer Details", style = MaterialTheme.typography.labelMedium, fontWeight = FontWeight.Bold, modifier = Modifier.align(Alignment.Start))
            Spacer(modifier = Modifier.height(6.dp))

            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                OutlinedTextField(
                    value = buyerName,
                    onValueChange = { buyerName = it },
                    label = { Text("Buyer / Mill Name") },
                    shape = RoundedCornerShape(16.dp),
                    modifier = Modifier.weight(1f),
                    singleLine = true
                )
                OutlinedTextField(
                    value = buyerPhone,
                    onValueChange = { buyerPhone = it },
                    label = { Text("Buyer Phone") },
                    shape = RoundedCornerShape(16.dp),
                    modifier = Modifier.weight(1f),
                    singleLine = true
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            Button(
                onClick = {
                    if (crop.isNotBlank() && qty > 0 && rate > 0) {
                        onSubmit(crop, variety, qty, rate, farmerName, farmerPhone, farmerLoc, buyerName, buyerPhone)
                    }
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(50.dp),
                shape = RoundedCornerShape(24.dp),
                enabled = qty > 0 && rate > 0
            ) {
                Icon(Icons.Default.Lock, contentDescription = null)
                Spacer(modifier = Modifier.width(8.dp))
                Text("Lock 15% Escrow Advance (₹${String.format("%,.0f", advance15)})", fontWeight = FontWeight.Bold)
            }
        }
    }
}
