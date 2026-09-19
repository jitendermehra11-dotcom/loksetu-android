package com.example.ui.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.LocalGasStation
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Route
import androidx.compose.material.icons.filled.Tune
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
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
import com.example.data.manager.ChainStopType
import com.example.data.manager.ChainedDeliveryRun
import com.example.data.manager.DeliveryChainStop
import com.example.data.manager.FuelIndexState
import com.example.data.manager.FuelType
import com.example.data.manager.ParcelDeliveryOrder
import com.example.data.manager.RiderProfile
import com.example.data.manager.SmartReturnOrderMatch

/**
 * 1. Live Fuel-Price Indexing Card
 * Displays daily mandi fuel benchmark rates, calculated dynamic per-km rates,
 * and allows switching fuel type or testing dynamic price surges.
 */
@Composable
fun FuelIndexingSummaryCard(
    fuelState: FuelIndexState,
    fuelPrices: Map<FuelType, Double>,
    onSelectFuelType: (FuelType) -> Unit,
    onUpdatePrice: (FuelType, Double) -> Unit,
    onResetPrices: () -> Unit,
    modifier: Modifier = Modifier
) {
    var showAdjustDialog by remember { mutableStateOf(false) }

    Card(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 6.dp)
            .testTag("fuel_indexing_summary_card"),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.tertiaryContainer.copy(alpha = 0.35f)),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.tertiary.copy(alpha = 0.3f))
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            // Header
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.LocalGasStation,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.tertiary,
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Column {
                        Text(
                            text = "Live Fuel Price Index",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.ExtraBold,
                            color = MaterialTheme.colorScheme.tertiary
                        )
                        Text(
                            text = "Dynamic Per-Km Payouts • 100% Surcharge to Rider",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }

                IconButton(
                    onClick = { showAdjustDialog = true },
                    modifier = Modifier.testTag("open_fuel_adjust_button")
                ) {
                    Icon(
                        imageVector = Icons.Default.Tune,
                        contentDescription = "Adjust Fuel Prices",
                        tint = MaterialTheme.colorScheme.tertiary
                    )
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Fuel Selector Chips
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                FuelType.values().forEach { fuel ->
                    val isSelected = fuelState.fuelType == fuel
                    val currentPrice = fuelPrices[fuel] ?: fuel.defaultCurrentPrice
                    FilterChip(
                        selected = isSelected,
                        onClick = { onSelectFuelType(fuel) },
                        label = {
                            Text(
                                text = "${fuel.iconEmoji} ${fuel.displayName}: ₹${"%.2f".format(currentPrice)}/${fuel.unit}",
                                fontSize = 11.sp,
                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
                            )
                        },
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = MaterialTheme.colorScheme.tertiary,
                            selectedLabelColor = MaterialTheme.colorScheme.onTertiary
                        ),
                        modifier = Modifier.testTag("fuel_chip_${fuel.name.lowercase()}")
                    )
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Dynamic Payout Rate Breakdown Pill
            Surface(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp),
                color = MaterialTheme.colorScheme.surface,
                border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(12.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(
                            text = "${fuelState.fuelType.iconEmoji} Active Dynamic Rate",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                text = "₹${"%.2f".format(fuelState.dynamicPerKmRate)}",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Black,
                                color = MaterialTheme.colorScheme.tertiary
                            )
                            Text(
                                text = " / km",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            if (fuelState.fuelSurchargePerKm > 0) {
                                Spacer(modifier = Modifier.width(6.dp))
                                Surface(
                                    shape = RoundedCornerShape(6.dp),
                                    color = Color(0xFFE8F5E9)
                                ) {
                                    Text(
                                        text = "+₹${"%.2f".format(fuelState.fuelSurchargePerKm)} fuel bonus",
                                        fontSize = 10.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = Color(0xFF2E7D32),
                                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                    )
                                }
                            }
                        }
                    }

                    Column(horizontalAlignment = Alignment.End) {
                        Text(
                            text = "Formula Breakdown",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Text(
                            text = "Base ₹${fuelState.fuelType.basePerKmRate}/km + Surcharge ₹${"%.2f".format(fuelState.fuelSurchargePerKm)}",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                    }
                }
            }
        }
    }

    if (showAdjustDialog) {
        FuelPriceAdjustmentDialog(
            fuelPrices = fuelPrices,
            onUpdatePrice = onUpdatePrice,
            onResetPrices = onResetPrices,
            onDismiss = { showAdjustDialog = false }
        )
    }
}

/**
 * Dialog to adjust daily local fuel prices or reset to mandi benchmarks.
 */
@Composable
fun FuelPriceAdjustmentDialog(
    fuelPrices: Map<FuelType, Double>,
    onUpdatePrice: (FuelType, Double) -> Unit,
    onResetPrices: () -> Unit,
    onDismiss: () -> Unit
) {
    var petrolInput by remember { mutableStateOf((fuelPrices[FuelType.PETROL] ?: FuelType.PETROL.defaultCurrentPrice).toString()) }
    var dieselInput by remember { mutableStateOf((fuelPrices[FuelType.DIESEL] ?: FuelType.DIESEL.defaultCurrentPrice).toString()) }
    var cngInput by remember { mutableStateOf((fuelPrices[FuelType.CNG] ?: FuelType.CNG.defaultCurrentPrice).toString()) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Default.LocalGasStation, contentDescription = null, tint = MaterialTheme.colorScheme.tertiary)
                Spacer(modifier = Modifier.width(8.dp))
                Text("Daily Fuel Price Index (₹)", fontWeight = FontWeight.Bold)
            }
        },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                Text(
                    text = "Update daily local petrol/diesel/CNG rates. Rider per-km payouts and fuel surcharges re-index immediately.",
                    fontSize = 12.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                OutlinedTextField(
                    value = petrolInput,
                    onValueChange = { petrolInput = it },
                    label = { Text("🏍️ Petrol Rate (₹/L) [Base ₹${FuelType.PETROL.baselinePrice}]") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.fillMaxWidth()
                )

                OutlinedTextField(
                    value = dieselInput,
                    onValueChange = { dieselInput = it },
                    label = { Text("🚚 Diesel Rate (₹/L) [Base ₹${FuelType.DIESEL.baselinePrice}]") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.fillMaxWidth()
                )

                OutlinedTextField(
                    value = cngInput,
                    onValueChange = { cngInput = it },
                    label = { Text("🛺 CNG Rate (₹/kg) [Base ₹${FuelType.CNG.baselinePrice}]") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.fillMaxWidth()
                )
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    petrolInput.toDoubleOrNull()?.let { onUpdatePrice(FuelType.PETROL, it) }
                    dieselInput.toDoubleOrNull()?.let { onUpdatePrice(FuelType.DIESEL, it) }
                    cngInput.toDoubleOrNull()?.let { onUpdatePrice(FuelType.CNG, it) }
                    onDismiss()
                },
                shape = RoundedCornerShape(10.dp)
            ) {
                Text("Save & Re-Index Payouts")
            }
        },
        dismissButton = {
            Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                TextButton(onClick = {
                    onResetPrices()
                    onDismiss()
                }) {
                    Text("Reset Benchmarks")
                }
                TextButton(onClick = onDismiss) {
                    Text("Cancel")
                }
            }
        }
    )
}

/**
 * 2. Smart Return Trip Corridor Card
 * Displays Rider's current location, Home Mandi Hub, and empty deadhead risk.
 */
@Composable
fun ReturnCorridorHeaderCard(
    activeRider: RiderProfile,
    totalEmptyMilesSaved: Double,
    onRefreshMatches: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 6.dp)
            .testTag("return_corridor_header_card"),
        shape = RoundedCornerShape(18.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.35f)),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.secondary.copy(alpha = 0.3f))
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.Route,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.secondary,
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "Smart Return Trip Corridor",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.ExtraBold,
                        color = MaterialTheme.colorScheme.secondary
                    )
                }

                IconButton(
                    onClick = onRefreshMatches,
                    modifier = Modifier.testTag("refresh_return_matches_button")
                ) {
                    Icon(Icons.Default.Refresh, contentDescription = "Refresh Matches", tint = MaterialTheme.colorScheme.secondary)
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Current Location to Home Hub
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.LocationOn, contentDescription = null, tint = Color(0xFFD32F2F), modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = "Drop Point: ${activeRider.currentLocationName}",
                            style = MaterialTheme.typography.bodySmall,
                            fontWeight = FontWeight.SemiBold
                        )
                    }
                    Spacer(modifier = Modifier.height(4.dp))
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.Home, contentDescription = null, tint = Color(0xFF1976D2), modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = "Home Hub: ${activeRider.homeHubName}",
                            style = MaterialTheme.typography.bodySmall,
                            fontWeight = FontWeight.SemiBold
                        )
                    }
                }

                Surface(
                    shape = RoundedCornerShape(12.dp),
                    color = Color(0xFFFFEBEE),
                    border = BorderStroke(1.dp, Color(0xFFFFCDD2))
                ) {
                    Column(
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            text = "Empty Deadhead",
                            fontSize = 10.sp,
                            color = Color(0xFFC62828),
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = "${"%.1f".format(activeRider.directEmptyReturnDistanceKm)} km (₹0)",
                            fontSize = 13.sp,
                            color = Color(0xFFC62828),
                            fontWeight = FontWeight.Black
                        )
                    }
                }
            }
        }
    }
}

/**
 * Single Backhaul Return Match Card
 */
@Composable
fun SmartReturnMatchCard(
    match: SmartReturnOrderMatch,
    onChainOrder: (ParcelDeliveryOrder) -> Unit,
    onAcceptReturnOrder: (ParcelDeliveryOrder) -> Unit,
    modifier: Modifier = Modifier
) {
    val order = match.order
    ElevatedCard(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 6.dp)
            .testTag("return_match_card_${order.id}"),
        shape = RoundedCornerShape(18.dp),
        colors = CardDefaults.elevatedCardColors(containerColor = MaterialTheme.colorScheme.surface)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            // Match Quality Header
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Surface(
                    shape = RoundedCornerShape(10.dp),
                    color = Color(0xFFE8F5E9),
                    border = BorderStroke(1.dp, Color(0xFFA5D6A7))
                ) {
                    Text(
                        text = "⚡ ${match.synergyPercent}% Return Match",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF1B5E20),
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                    )
                }

                Surface(
                    shape = RoundedCornerShape(10.dp),
                    color = Color(0xFFE3F2FD),
                    border = BorderStroke(1.dp, Color(0xFF90CAF9))
                ) {
                    Text(
                        text = "Saves ${"%.1f".format(match.emptyKmSaved)} km Deadhead",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF0D47A1),
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Order description & store
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = order.storeName,
                        fontWeight = FontWeight.ExtraBold,
                        style = MaterialTheme.typography.titleMedium,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Text(
                        text = order.parcelItemsDescription,
                        fontSize = 12.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                Column(horizontalAlignment = Alignment.End) {
                    Text(
                        text = "Rider Payout",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Text(
                        text = "₹${order.deliveryFee.toInt()}",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Black,
                        color = MaterialTheme.colorScheme.primary
                    )
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Route Breakdown
            Surface(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(10.dp),
                color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(8.dp),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        text = "🏬 Pickup: ${"%.1f".format(match.pickupDistanceKm)} km",
                        fontSize = 11.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Text(
                        text = "📍 Delivery: ${"%.1f".format(match.deliveryDistanceKm)} km",
                        fontSize = 11.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Text(
                        text = "🏁 To Base: ${"%.1f".format(match.dropToHomeDistanceKm)} km",
                        fontSize = 11.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Action Buttons
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                OutlinedButton(
                    onClick = { onAcceptReturnOrder(order) },
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier
                        .weight(1f)
                        .testTag("accept_return_button_${order.id}")
                ) {
                    Text("Accept Backhaul", fontSize = 12.sp)
                }

                Button(
                    onClick = { onChainOrder(order) },
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier
                        .weight(1.3f)
                        .testTag("chain_order_button_${order.id}")
                ) {
                    Icon(
                        imageVector = Icons.Default.Route,
                        contentDescription = null,
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("Chain 2-Way Route", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}

/**
 * 3. Multi-Stop Chained Run Component
 */
@Composable
fun ChainedDeliveryCard(
    run: ChainedDeliveryRun,
    onAdvanceStop: (chainId: String, stopNumber: Int) -> Unit,
    modifier: Modifier = Modifier
) {
    val completedStopsCount = run.stops.count { it.isCompleted }
    val totalStopsCount = run.stops.size

    ElevatedCard(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 6.dp)
            .testTag("chained_run_card_${run.chainId}"),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.elevatedCardColors(containerColor = MaterialTheme.colorScheme.surface)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            // Header: Chain ID + Efficiency Boost Badge
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            text = run.chainId,
                            fontWeight = FontWeight.ExtraBold,
                            style = MaterialTheme.typography.titleMedium,
                            color = MaterialTheme.colorScheme.primary
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Surface(
                            shape = RoundedCornerShape(8.dp),
                            color = if (run.isCompleted) Color(0xFFE8F5E9) else Color(0xFFFFF8E1)
                        ) {
                            Text(
                                text = if (run.isCompleted) "Completed" else "In Transit ($completedStopsCount/$totalStopsCount Stops)",
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold,
                                color = if (run.isCompleted) Color(0xFF2E7D32) else Color(0xFFF57F17),
                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                            )
                        }
                    }
                    Text(
                        text = run.routeTitle,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                Surface(
                    shape = RoundedCornerShape(12.dp),
                    color = Color(0xFFE8F5E9),
                    border = BorderStroke(1.dp, Color(0xFFA5D6A7))
                ) {
                    Column(
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            text = "+${run.efficiencyBoostPercent}% Boost",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Black,
                            color = Color(0xFF1B5E20)
                        )
                        Text(
                            text = "${"%.1f".format(run.kmSaved)} km saved",
                            fontSize = 9.sp,
                            color = Color(0xFF2E7D32)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Run Statistics Pill
            Surface(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp),
                color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(10.dp),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Column {
                        Text("Chained Distance", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        Text("${run.totalDistanceKm} km", style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.Bold)
                    }
                    Column {
                        Text("Fuel Saved", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        Text("${"%.1f".format(run.fuelSavedLiters)} Litres", style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.Bold, color = Color(0xFF2E7D32))
                    }
                    Column(horizontalAlignment = Alignment.End) {
                        Text("Total Rider Payout", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        Text("₹${run.totalRiderEarnings.toInt()}", style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.Black, color = MaterialTheme.colorScheme.primary)
                    }
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            // Sequential Stops Timeline
            Text(
                text = "Sequential Chain Route:",
                fontWeight = FontWeight.Bold,
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.onSurface
            )
            Spacer(modifier = Modifier.height(8.dp))

            run.stops.forEach { stop ->
                val isStopCompleted = stop.isCompleted
                val isStopActive = !stop.isCompleted && (run.activeStop?.stopNumber == stop.stopNumber)

                Surface(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 3.dp),
                    shape = RoundedCornerShape(10.dp),
                    color = when {
                        isStopCompleted -> Color(0xFFF1F8E9)
                        isStopActive -> MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.4f)
                        else -> MaterialTheme.colorScheme.surface
                    },
                    border = BorderStroke(
                        1.dp,
                        when {
                            isStopCompleted -> Color(0xFFA5D6A7)
                            isStopActive -> MaterialTheme.colorScheme.primary
                            else -> MaterialTheme.colorScheme.outlineVariant
                        }
                    )
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(10.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.weight(1f)
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(24.dp)
                                    .clip(CircleShape)
                                    .background(
                                        when {
                                            isStopCompleted -> Color(0xFF2E7D32)
                                            isStopActive -> MaterialTheme.colorScheme.primary
                                            else -> MaterialTheme.colorScheme.outlineVariant
                                        }
                                    ),
                                contentAlignment = Alignment.Center
                            ) {
                                if (isStopCompleted) {
                                    Icon(Icons.Default.CheckCircle, contentDescription = null, tint = Color.White, modifier = Modifier.size(16.dp))
                                } else {
                                    Text(
                                        text = "${stop.stopNumber}",
                                        color = if (isStopActive) Color.White else MaterialTheme.colorScheme.onSurfaceVariant,
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.Bold
                                    )
                                }
                            }

                            Spacer(modifier = Modifier.width(8.dp))

                            Column {
                                Text(
                                    text = "${stop.type.icon} ${stop.type.label} - ${stop.locationName}",
                                    style = MaterialTheme.typography.bodySmall,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                                Text(
                                    text = "${stop.contactName} • ${stop.address}",
                                    fontSize = 10.sp,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }

                        if (isStopActive) {
                            Button(
                                onClick = { onAdvanceStop(run.chainId, stop.stopNumber) },
                                shape = RoundedCornerShape(8.dp),
                                modifier = Modifier
                                    .height(32.dp)
                                    .testTag("advance_stop_${run.chainId}_${stop.stopNumber}")
                            ) {
                                Text(
                                    text = if (stop.otpCode != null) "Verify OTP & Complete" else "Confirm Stop",
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        } else if (isStopCompleted) {
                            Text(
                                text = "Done ✓",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFF2E7D32)
                            )
                        }
                    }
                }
            }
        }
    }
}
