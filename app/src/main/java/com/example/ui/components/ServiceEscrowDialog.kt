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
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.DirectionsCar
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Payments
import androidx.compose.material.icons.filled.Phone
import androidx.compose.material.icons.filled.Pin
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.Star
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
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
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
import com.example.data.manager.EscrowBookingStatus
import com.example.data.manager.ServiceEscrowBooking
import com.example.data.model.ProviderEntity
import com.example.ui.viewmodel.LokSetuViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ServiceEscrowDialog(
    viewModel: LokSetuViewModel,
    targetProvider: ProviderEntity? = null,
    onDismiss: () -> Unit
) {
    val bookings by viewModel.serviceEscrowManager.bookings.collectAsState()
    val totalLocked by viewModel.serviceEscrowManager.totalEscrowLocked.collectAsState()
    val totalDisbursed by viewModel.serviceEscrowManager.totalDisbursedToProviders.collectAsState()

    var customerName by remember { mutableStateOf("Mrs. Ananya Sen") }
    var customerPhone by remember { mutableStateOf("+91 98765 43210") }
    var customerAddress by remember { mutableStateOf("Tower 3, Apt 704, Green Heights, Delhi") }
    var serviceFeeText by remember { mutableStateOf(if (targetProvider != null) "1200" else "1000") }
    var travelAllowanceText by remember { mutableStateOf("150") }

    var selectedBookingForOtp by remember { mutableStateOf<ServiceEscrowBooking?>(null) }
    var enteredOtp by remember { mutableStateOf("") }
    var otpResultMessage by remember { mutableStateOf<String?>(null) }

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
                            .background(Color(0xFFE8F5E9), CircleShape),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.Lock,
                            contentDescription = null,
                            tint = Color(0xFF2E7D32)
                        )
                    }
                    Spacer(modifier = Modifier.width(12.dp))
                    Column {
                        Text(
                            text = "Home Service Escrow Lock",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFF1B5E20)
                        )
                        Text(
                            text = "100% Upfront Fee + Travel Allowance Protection",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }

                IconButton(onClick = onDismiss, modifier = Modifier.testTag("close_escrow_dialog")) {
                    Icon(imageVector = Icons.Default.Close, contentDescription = "Close")
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            // Escrow Metric Pills
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Card(
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = Color(0xFFE8F5E9)),
                    border = BorderStroke(1.dp, Color(0xFF81C784))
                ) {
                    Column(modifier = Modifier.padding(12.dp)) {
                        Text("Active Escrow Locked", style = MaterialTheme.typography.labelSmall, color = Color(0xFF1B5E20))
                        Text("₹${totalLocked.toInt()}", fontSize = 18.sp, fontWeight = FontWeight.Bold, color = Color(0xFF2E7D32))
                        Text("100% Guaranteed", fontSize = 10.sp, color = Color(0xFF2E7D32))
                    }
                }

                Card(
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)),
                    border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant)
                ) {
                    Column(modifier = Modifier.padding(12.dp)) {
                        Text("Disbursed to Providers", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        Text("₹${totalDisbursed.toInt()}", fontSize = 18.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
                        Text("Instant UPI Release", fontSize = 10.sp, color = MaterialTheme.colorScheme.primary)
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Form: Upfront Lock for Home Visit
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = if (targetProvider != null) "Lock Escrow for ${targetProvider.name}" else "Lock Upfront Booking Escrow",
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = "Providers travel only after full payment is secured in escrow. Zero out-of-pocket travel risk.",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    OutlinedTextField(
                        value = customerName,
                        onValueChange = { customerName = it },
                        label = { Text("Customer Name") },
                        modifier = Modifier.fillMaxWidth().testTag("escrow_customer_name"),
                        shape = RoundedCornerShape(14.dp)
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    OutlinedTextField(
                        value = customerAddress,
                        onValueChange = { customerAddress = it },
                        label = { Text("Home Visit Address") },
                        modifier = Modifier.fillMaxWidth().testTag("escrow_customer_address"),
                        shape = RoundedCornerShape(14.dp)
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        OutlinedTextField(
                            value = serviceFeeText,
                            onValueChange = { serviceFeeText = it },
                            label = { Text("Service Fee (₹)") },
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                            modifier = Modifier.weight(1f).testTag("escrow_service_fee"),
                            shape = RoundedCornerShape(14.dp)
                        )

                        OutlinedTextField(
                            value = travelAllowanceText,
                            onValueChange = { travelAllowanceText = it },
                            label = { Text("Travel Lock (₹)") },
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                            modifier = Modifier.weight(1f).testTag("escrow_travel_fee"),
                            shape = RoundedCornerShape(14.dp)
                        )
                    }

                    val sFee = serviceFeeText.toDoubleOrNull() ?: 0.0
                    val tFee = travelAllowanceText.toDoubleOrNull() ?: 0.0
                    val totalCalc = sFee + tFee

                    Spacer(modifier = Modifier.height(12.dp))

                    Surface(
                        color = Color(0xFFE8F5E9),
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text("Total 100% Upfront Lock:", fontWeight = FontWeight.SemiBold, fontSize = 12.sp, color = Color(0xFF1B5E20))
                            Text("₹${totalCalc.toInt()} (UPI Escrow)", fontWeight = FontWeight.Bold, fontSize = 14.sp, color = Color(0xFF2E7D32))
                        }
                    }

                    Spacer(modifier = Modifier.height(14.dp))

                    Button(
                        onClick = {
                            val provider = targetProvider
                            val pId = provider?.id ?: 201L
                            val pName = provider?.name ?: "Sunita Sharma"
                            val pPhone = provider?.phone ?: "+91 98110 54321"
                            val pCat = provider?.category ?: "HOME_BEAUTY"

                            viewModel.lockServiceEscrow(
                                providerId = pId,
                                providerName = pName,
                                providerPhone = pPhone,
                                providerCategory = pCat,
                                customerName = customerName,
                                customerPhone = customerPhone,
                                customerAddress = customerAddress,
                                serviceFee = sFee,
                                travelAllowance = tFee
                            )
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF2E7D32)),
                        shape = RoundedCornerShape(20.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(48.dp)
                            .testTag("submit_lock_escrow_button")
                    ) {
                        Icon(Icons.Default.Lock, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("Lock ₹${totalCalc.toInt()} in Escrow & Dispatch Professional", fontWeight = FontWeight.Bold)
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // OTP Verification Sheet/Card if selected
            val activeOtpBooking = selectedBookingForOtp
            if (activeOtpBooking != null) {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(20.dp),
                    colors = CardDefaults.cardColors(containerColor = Color(0xFFF3E5F5)),
                    border = BorderStroke(1.5.dp, Color(0xFF7B1FA2))
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text(
                            text = "Release Payment for ${activeOtpBooking.providerName}",
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFF4A148C)
                        )
                        Text(
                            text = "Customer verifies completion by providing the 4-Digit OTP shown on customer's booking confirmation.",
                            style = MaterialTheme.typography.bodySmall,
                            color = Color(0xFF6A1B9A)
                        )

                        Spacer(modifier = Modifier.height(10.dp))

                        Surface(
                            color = Color.White,
                            shape = RoundedCornerShape(12.dp),
                            border = BorderStroke(1.dp, Color(0xFFCE93D8))
                        ) {
                            Text(
                                text = "🔒 Customer Escrow OTP: ${activeOtpBooking.completionOtp}",
                                fontWeight = FontWeight.Black,
                                fontSize = 16.sp,
                                color = Color(0xFF7B1FA2),
                                modifier = Modifier.padding(horizontal = 14.dp, vertical = 6.dp)
                            )
                        }

                        Spacer(modifier = Modifier.height(10.dp))

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            OutlinedTextField(
                                value = enteredOtp,
                                onValueChange = { enteredOtp = it },
                                label = { Text("Enter 4-Digit OTP") },
                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                                modifier = Modifier.weight(1f).testTag("escrow_otp_input"),
                                shape = RoundedCornerShape(14.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Button(
                                onClick = {
                                    val (success, msg) = viewModel.verifyEscrowOtpAndRelease(activeOtpBooking.id, enteredOtp)
                                    otpResultMessage = msg
                                    if (success) {
                                        selectedBookingForOtp = null
                                        enteredOtp = ""
                                    }
                                },
                                shape = RoundedCornerShape(14.dp),
                                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF7B1FA2)),
                                modifier = Modifier.testTag("verify_escrow_otp_button")
                            ) {
                                Text("Release UPI")
                            }
                        }

                        if (otpResultMessage != null) {
                            Spacer(modifier = Modifier.height(6.dp))
                            Text(
                                text = otpResultMessage ?: "",
                                style = MaterialTheme.typography.bodySmall,
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFF4A148C)
                            )
                        }
                    }
                }
                Spacer(modifier = Modifier.height(16.dp))
            }

            // Existing Bookings List
            Text(
                text = "Live Escrow Bookings (${bookings.size})",
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.align(Alignment.Start)
            )

            Spacer(modifier = Modifier.height(8.dp))

            bookings.forEach { booking ->
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 4.dp),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant)
                ) {
                    Column(modifier = Modifier.padding(14.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = booking.providerName,
                                fontWeight = FontWeight.Bold,
                                fontSize = 14.sp
                            )
                            Surface(
                                shape = RoundedCornerShape(8.dp),
                                color = when (booking.status) {
                                    EscrowBookingStatus.COMPLETED -> Color(0xFFE8F5E9)
                                    EscrowBookingStatus.PAYMENT_LOCKED -> Color(0xFFFFF3E0)
                                    else -> Color(0xFFE1F5FE)
                                }
                            ) {
                                Text(
                                    text = booking.status.label,
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = when (booking.status) {
                                        EscrowBookingStatus.COMPLETED -> Color(0xFF2E7D32)
                                        EscrowBookingStatus.PAYMENT_LOCKED -> Color(0xFFE65100)
                                        else -> Color(0xFF0277BD)
                                    },
                                    modifier = Modifier.padding(horizontal = 6.dp, vertical = 3.dp)
                                )
                            }
                        }

                        Text(
                            text = "${booking.customerAddress} • Client: ${booking.customerName}",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )

                        Spacer(modifier = Modifier.height(6.dp))

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "Locked: ₹${booking.serviceFee.toInt()} + Travel ₹${booking.travelAllowance.toInt()} = ₹${booking.totalLockedAmount.toInt()}",
                                style = MaterialTheme.typography.bodySmall,
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFF2E7D32)
                            )

                            if (booking.status != EscrowBookingStatus.COMPLETED) {
                                Button(
                                    onClick = {
                                        selectedBookingForOtp = booking
                                        enteredOtp = ""
                                        otpResultMessage = null
                                    },
                                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF2E7D32)),
                                    shape = RoundedCornerShape(12.dp),
                                    contentPadding = androidx.compose.foundation.layout.PaddingValues(horizontal = 10.dp, vertical = 2.dp),
                                    modifier = Modifier.testTag("verify_otp_for_${booking.id}")
                                ) {
                                    Text("Verify OTP", fontSize = 11.sp)
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}
