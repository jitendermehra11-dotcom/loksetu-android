package com.example.ui.screens

import android.content.Intent
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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountBalance
import androidx.compose.material.icons.filled.AccountBalanceWallet
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.HealthAndSafety
import androidx.compose.material.icons.filled.Payments
import androidx.compose.material.icons.filled.QrCode2
import androidx.compose.material.icons.filled.Receipt
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.manager.SettlementRecord
import com.example.ui.components.PaymentSettlementDialog
import com.example.ui.viewmodel.LokSetuViewModel

@Composable
fun PaymentSettlementSection(
    viewModel: LokSetuViewModel,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val history by viewModel.paymentManager.settlementHistory.collectAsState()
    val totalFee by viewModel.paymentManager.totalPlatformFeeCollected.collectAsState()
    val totalIns by viewModel.paymentManager.totalMicroInsurancePool.collectAsState()
    val totalPayout by viewModel.paymentManager.totalPayoutsDisbursed.collectAsState()
    val isCustomOpen by viewModel.isCustomSettlementOpen.collectAsState()
    val selectedProvider by viewModel.selectedProviderForSettlement.collectAsState()

    Scaffold(
        modifier = modifier,
        containerColor = Color.Transparent,
        floatingActionButton = {
            FloatingActionButton(
                onClick = { viewModel.openCustomSettlement() },
                containerColor = MaterialTheme.colorScheme.primary,
                contentColor = MaterialTheme.colorScheme.onPrimary,
                shape = RoundedCornerShape(20.dp),
                modifier = Modifier.testTag("new_settlement_fab")
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(Icons.Default.Payments, contentDescription = null, modifier = Modifier.size(18.dp))
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Instant UPI Settlement (97%)", fontWeight = FontWeight.Bold)
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
            // Overview Metric Card
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
                                    imageVector = Icons.Default.AccountBalanceWallet,
                                    contentDescription = null,
                                    tint = MaterialTheme.colorScheme.onPrimaryContainer
                                )
                            }
                            Spacer(modifier = Modifier.width(12.dp))
                            Column {
                                Text(
                                    text = "Payment & Settlement Protocol",
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.Bold
                                )
                                Text(
                                    text = "3% Platform Fee • ₹1 Micro-Insurance • 97% Payout",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(16.dp))

                        // Stats Grid
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Column {
                                Text("Total Net Payouts", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                Text("₹${String.format("%,.0f", totalPayout)}", fontWeight = FontWeight.Black, style = MaterialTheme.typography.titleMedium, color = MaterialTheme.colorScheme.primary)
                                Text("97% Direct UPI", style = MaterialTheme.typography.bodySmall, color = Color(0xFF2E7D32), fontSize = 11.sp)
                            }

                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Text("Platform Fee (3%)", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                Text("₹${String.format("%,.0f", totalFee)}", fontWeight = FontWeight.Black, style = MaterialTheme.typography.titleMedium)
                                Text("Low Fair Fee", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.outline, fontSize = 11.sp)
                            }

                            Column(horizontalAlignment = Alignment.End) {
                                Text("Micro-Insurance", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                Text("₹${String.format("%,.0f", totalIns)}", fontWeight = FontWeight.Black, style = MaterialTheme.typography.titleMedium, color = Color(0xFF2E7D32))
                                Text("₹1 per settlement", style = MaterialTheme.typography.bodySmall, color = Color(0xFF2E7D32), fontSize = 11.sp)
                            }
                        }
                    }
                }
            }

            // Settlement History Title
            item {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 8.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Instant UPI Disbursal Log",
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = "${history.size} Settled",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.primary,
                        fontWeight = FontWeight.SemiBold
                    )
                }
            }

            items(history, key = { it.id }) { record ->
                SettlementItemCard(
                    record = record,
                    onOpenUpi = {
                        val uri = viewModel.paymentManager.buildUpiPaymentUri(
                            payeeUpi = record.providerUpiId,
                            payeeName = record.providerName,
                            amount = record.breakdown.netPayoutAmount,
                            transactionNote = "LokSetu #${record.id}"
                        )
                        try {
                            context.startActivity(Intent(Intent.ACTION_VIEW, uri))
                        } catch (_: Exception) {}
                    }
                )
            }

            item {
                Spacer(modifier = Modifier.height(80.dp))
            }
        }

        if (isCustomOpen) {
            PaymentSettlementDialog(
                provider = null,
                viewModel = viewModel,
                onDismiss = { viewModel.closeCustomSettlement() }
            )
        }

        if (selectedProvider != null) {
            PaymentSettlementDialog(
                provider = selectedProvider,
                viewModel = viewModel,
                onDismiss = { viewModel.closeSettlementForProvider() }
            )
        }
    }
}

@Composable
fun SettlementItemCard(
    record: SettlementRecord,
    onOpenUpi: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        shape = RoundedCornerShape(20.dp),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = record.providerName,
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = "${record.jobDescription} • ${record.formattedDate}",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        fontSize = 11.sp
                    )
                }

                Box(
                    modifier = Modifier
                        .background(Color(0xFFE8F5E9), RoundedCornerShape(12.dp))
                        .padding(horizontal = 8.dp, vertical = 4.dp)
                ) {
                    Text(
                        text = "Settled",
                        color = Color(0xFF1B5E20),
                        fontWeight = FontWeight.Bold,
                        fontSize = 11.sp
                    )
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Breakdown bar
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.35f), RoundedCornerShape(14.dp))
                    .padding(10.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text("Gross: ₹${record.breakdown.grossAmount.toInt()}", style = MaterialTheme.typography.bodySmall)
                    Text("Fee (3%): -₹${record.breakdown.platformFeeAmount}", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.error, fontSize = 11.sp)
                    Text("Insurance: -₹${record.breakdown.microInsuranceAmount}", style = MaterialTheme.typography.bodySmall, color = Color(0xFF2E7D32), fontSize = 11.sp)
                }

                Column(horizontalAlignment = Alignment.End) {
                    Text("Net UPI Payout (97%)", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    Text(
                        "₹${record.breakdown.netPayoutAmount}",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Black,
                        color = MaterialTheme.colorScheme.primary
                    )
                    Text(record.providerUpiId, style = MaterialTheme.typography.bodySmall, fontSize = 11.sp, color = MaterialTheme.colorScheme.outline)
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Ref: ${record.transactionReference}",
                    style = MaterialTheme.typography.bodySmall,
                    fontSize = 11.sp,
                    color = MaterialTheme.colorScheme.outline
                )

                OutlinedButton(
                    onClick = onOpenUpi,
                    shape = RoundedCornerShape(16.dp),
                    border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant)
                ) {
                    Icon(Icons.Default.QrCode2, contentDescription = null, modifier = Modifier.size(14.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Re-trigger UPI", fontSize = 12.sp)
                }
            }
        }
    }
}
