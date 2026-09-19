package com.example.ui.components

import android.content.Intent
import android.widget.Toast
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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountBalanceWallet
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.HealthAndSafety
import androidx.compose.material.icons.filled.QrCode2
import androidx.compose.material.icons.filled.ReceiptLong
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.manager.PaymentBreakdown
import com.example.data.manager.SettlementRecord
import com.example.data.model.ProviderEntity
import com.example.ui.viewmodel.LokSetuViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PaymentSettlementDialog(
    provider: ProviderEntity?,
    viewModel: LokSetuViewModel,
    onDismiss: () -> Unit
) {
    val context = LocalContext.current

    var grossAmountStr by remember {
        mutableStateOf(
            if (provider != null) {
                // Extract digits from pricing string e.g. "₹450" -> 450
                Regex("\\d+").find(provider.pricing)?.value ?: "1000"
            } else "1000"
        )
    }

    val providerName = provider?.name ?: "Local Service Provider"
    val providerCategory = provider?.category ?: "SKILLED_WORKER"
    var providerUpi by remember {
        mutableStateOf(
            if (provider != null) {
                "${provider.name.lowercase().replace(" ", "")}@okhdfcbank"
            } else "loksetu.partner@upi"
        )
    }
    var jobDesc by remember {
        mutableStateOf(
            if (provider != null) "Completed service: ${provider.speciality}"
            else "Local Mandi / Logistics Delivery Job"
        )
    }

    var completedRecord by remember { mutableStateOf<SettlementRecord?>(null) }

    val grossAmount = grossAmountStr.toDoubleOrNull() ?: 0.0
    val breakdown = PaymentBreakdown.compute(grossAmount)

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
            // Header
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(40.dp)
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
                            text = "Job Payment & Settlement",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = "3% Fee • ₹1 Micro-Insurance • 97% Payout",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }

                IconButton(onClick = onDismiss) {
                    Icon(imageVector = Icons.Default.Close, contentDescription = "Close")
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            if (completedRecord == null) {
                // Input Form & Live Breakdown
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f)),
                    shape = RoundedCornerShape(24.dp),
                    border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant)
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp)
                    ) {
                        Text(
                            text = "Beneficiary: $providerName",
                            style = MaterialTheme.typography.bodyMedium,
                            fontWeight = FontWeight.Bold
                        )

                        Spacer(modifier = Modifier.height(10.dp))

                        OutlinedTextField(
                            value = grossAmountStr,
                            onValueChange = { grossAmountStr = it.filter { c -> c.isDigit() || c == '.' } },
                            label = { Text("Total Gross Job Amount (₹)") },
                            prefix = { Text("₹ ", fontWeight = FontWeight.Bold) },
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                            shape = RoundedCornerShape(16.dp),
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("settlement_gross_input"),
                            singleLine = true
                        )

                        Spacer(modifier = Modifier.height(10.dp))

                        OutlinedTextField(
                            value = providerUpi,
                            onValueChange = { providerUpi = it },
                            label = { Text("Provider Instant UPI ID") },
                            shape = RoundedCornerShape(16.dp),
                            modifier = Modifier.fillMaxWidth(),
                            singleLine = true
                        )

                        Spacer(modifier = Modifier.height(10.dp))

                        OutlinedTextField(
                            value = jobDesc,
                            onValueChange = { jobDesc = it },
                            label = { Text("Job Description") },
                            shape = RoundedCornerShape(16.dp),
                            modifier = Modifier.fillMaxWidth(),
                            singleLine = true
                        )
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                // Breakdown Calculation Card
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
                        Text(
                            text = "Transparent Settlement Breakdown",
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = FontWeight.Bold
                        )

                        Spacer(modifier = Modifier.height(12.dp))

                        // Gross Amount
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text("Total Job Value (Gross)", style = MaterialTheme.typography.bodyMedium)
                            Text("₹${String.format("%.2f", breakdown.grossAmount)}", fontWeight = FontWeight.SemiBold)
                        }

                        Spacer(modifier = Modifier.height(8.dp))

                        // Platform Fee (3%)
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text("Platform Maintenance Fee (3%)", style = MaterialTheme.typography.bodyMedium)
                            }
                            Text(
                                "- ₹${String.format("%.2f", breakdown.platformFeeAmount)}",
                                color = MaterialTheme.colorScheme.error,
                                fontWeight = FontWeight.SemiBold
                            )
                        }

                        Spacer(modifier = Modifier.height(8.dp))

                        // Micro-Insurance (₹1)
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    imageVector = Icons.Default.HealthAndSafety,
                                    contentDescription = null,
                                    tint = Color(0xFF2E7D32),
                                    modifier = Modifier.size(16.dp)
                                )
                                Spacer(modifier = Modifier.width(4.dp))
                                Text(
                                    "Accidental Micro-Insurance Pool",
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = Color(0xFF2E7D32)
                                )
                            }
                            Text(
                                "- ₹${String.format("%.2f", breakdown.microInsuranceAmount)}",
                                color = Color(0xFF2E7D32),
                                fontWeight = FontWeight.SemiBold
                            )
                        }

                        Spacer(modifier = Modifier.height(10.dp))
                        HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant)
                        Spacer(modifier = Modifier.height(10.dp))

                        // Net Payout (97%)
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column {
                                Text(
                                    "Instant UPI Payout to Provider",
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.Bold
                                )
                                Text(
                                    "Net 97% direct to ${providerUpi.take(20)}",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                            Text(
                                "₹${String.format("%.2f", breakdown.netPayoutAmount)}",
                                style = MaterialTheme.typography.titleLarge,
                                fontWeight = FontWeight.Black,
                                color = MaterialTheme.colorScheme.primary
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Settle Button
                Button(
                    onClick = {
                        val record = viewModel.processJobPayment(
                            providerId = provider?.id ?: 0L,
                            providerName = providerName,
                            providerCategory = providerCategory,
                            providerUpiId = providerUpi,
                            jobDescription = jobDesc,
                            grossAmount = grossAmount
                        )
                        completedRecord = record

                        // Launch UPI deep link
                        val upiUri = viewModel.paymentManager.buildUpiPaymentUri(
                            payeeUpi = providerUpi,
                            payeeName = providerName,
                            amount = breakdown.netPayoutAmount,
                            transactionNote = "LokSetu #${record.id} Settlement"
                        )
                        val intent = Intent(Intent.ACTION_VIEW, upiUri)
                        try {
                            context.startActivity(intent)
                        } catch (e: Exception) {
                            Toast.makeText(context, "UPI intent logged: ₹${breakdown.netPayoutAmount} to $providerUpi", Toast.LENGTH_SHORT).show()
                        }
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(52.dp)
                        .testTag("settle_payout_btn"),
                    shape = RoundedCornerShape(24.dp),
                    enabled = grossAmount > 0
                ) {
                    Icon(Icons.Default.QrCode2, contentDescription = null)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        "Disburse ₹${String.format("%.2f", breakdown.netPayoutAmount)} via Instant UPI",
                        fontWeight = FontWeight.Bold,
                        fontSize = 16.sp
                    )
                }
            } else {
                // Settlement Completed Receipt View
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = Color(0xFFE8F5E9)),
                    shape = RoundedCornerShape(24.dp),
                    border = BorderStroke(1.dp, Color(0xFFA5D6A7))
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(20.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Icon(
                            imageVector = Icons.Default.CheckCircle,
                            contentDescription = "Success",
                            tint = Color(0xFF2E7D32),
                            modifier = Modifier.size(52.dp)
                        )
                        Spacer(modifier = Modifier.height(10.dp))
                        Text(
                            text = "Instant Payout Disbursed!",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFF1B5E20)
                        )
                        Text(
                            text = "₹${completedRecord!!.breakdown.netPayoutAmount} transferred to ${completedRecord!!.providerUpiId}",
                            style = MaterialTheme.typography.bodyMedium,
                            color = Color(0xFF2E7D32),
                            textAlign = TextAlign.Center
                        )

                        Spacer(modifier = Modifier.height(14.dp))
                        HorizontalDivider(color = Color(0xFFA5D6A7))
                        Spacer(modifier = Modifier.height(14.dp))

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text("Transaction Ref / UTR:", style = MaterialTheme.typography.bodySmall)
                            Text(completedRecord!!.transactionReference, fontWeight = FontWeight.Bold, style = MaterialTheme.typography.bodySmall)
                        }
                        Spacer(modifier = Modifier.height(6.dp))
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text("3% LokSetu Platform Fee:", style = MaterialTheme.typography.bodySmall)
                            Text("₹${completedRecord!!.breakdown.platformFeeAmount}", style = MaterialTheme.typography.bodySmall)
                        }
                        Spacer(modifier = Modifier.height(6.dp))
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text("₹1 Accidental Micro-Insurance:", style = MaterialTheme.typography.bodySmall)
                            Text("₹1.00 Credited", style = MaterialTheme.typography.bodySmall, color = Color(0xFF1B5E20))
                        }
                        Spacer(modifier = Modifier.height(6.dp))
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text("Time:", style = MaterialTheme.typography.bodySmall)
                            Text(completedRecord!!.formattedDate, style = MaterialTheme.typography.bodySmall)
                        }

                        Spacer(modifier = Modifier.height(18.dp))

                        OutlinedButton(
                            onClick = onDismiss,
                            shape = RoundedCornerShape(20.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Icon(Icons.Default.ReceiptLong, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("Close & Back to App")
                        }
                    }
                }
            }
        }
    }
}
