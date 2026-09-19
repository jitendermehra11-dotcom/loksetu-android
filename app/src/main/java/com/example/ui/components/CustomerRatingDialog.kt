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
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Block
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Gavel
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.ReportProblem
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
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
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.manager.DisputeFlagType
import com.example.ui.viewmodel.LokSetuViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CustomerRatingDialog(
    viewModel: LokSetuViewModel,
    onDismiss: () -> Unit
) {
    val reviews by viewModel.customerRatingManager.reviews.collectAsState()
    val profiles by viewModel.customerRatingManager.customerProfiles.collectAsState()
    val blockedCount by viewModel.customerRatingManager.blockedCustomersCount.collectAsState()

    var customerName by remember { mutableStateOf("") }
    var customerPhone by remember { mutableStateOf("+91 99119 ") }
    var providerName by remember { mutableStateOf("Sunita Sharma") }
    var providerRole by remember { mutableStateOf("Home Beautician") }
    var ratingScore by remember { mutableStateOf(1) }
    var selectedFlag by remember { mutableStateOf(DisputeFlagType.UNSAFE_BEHAVIOR) }
    var feedbackNote by remember { mutableStateOf("") }
    var dropdownExpanded by remember { mutableStateOf(false) }

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
                            .background(Color(0xFFFFEBEE), CircleShape),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.Gavel,
                            contentDescription = null,
                            tint = Color(0xFFC62828)
                        )
                    }
                    Spacer(modifier = Modifier.width(12.dp))
                    Column {
                        Text(
                            text = "Customer Safety & Dispute Rating",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFFB71C1C)
                        )
                        Text(
                            text = "Rate Behavior • Auto-Block Repeat Harassers",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }

                IconButton(onClick = onDismiss, modifier = Modifier.testTag("close_rating_dialog")) {
                    Icon(imageVector = Icons.Default.Close, contentDescription = "Close")
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            // Blocked repeat offenders indicator
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = Color(0xFFFFEBEE)),
                border = BorderStroke(1.dp, Color(0xFFEF9A9A))
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(14.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.Block, contentDescription = null, tint = Color(0xFFC62828), modifier = Modifier.size(20.dp))
                        Spacer(modifier = Modifier.width(8.dp))
                        Column {
                            Text("Platform Protection Engine Active", fontWeight = FontWeight.Bold, fontSize = 13.sp, color = Color(0xFFB71C1C))
                            Text("Repeat payment defaulters & abusive clients blocked", fontSize = 11.sp, color = Color(0xFF5F2120))
                        }
                    }
                    Surface(
                        shape = RoundedCornerShape(8.dp),
                        color = Color(0xFFC62828)
                    ) {
                        Text(
                            text = "$blockedCount Blocked",
                            color = Color.White,
                            fontWeight = FontWeight.Bold,
                            fontSize = 11.sp,
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Rate / Flag Customer Form
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = "Submit Provider Review for Customer",
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = "Protects fellow home beauty professionals, nurses, and workers from dangerous locations or non-payment.",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    OutlinedTextField(
                        value = customerPhone,
                        onValueChange = { customerPhone = it },
                        label = { Text("Customer Mobile Number") },
                        modifier = Modifier.fillMaxWidth().testTag("rating_customer_phone"),
                        shape = RoundedCornerShape(14.dp)
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    OutlinedTextField(
                        value = customerName,
                        onValueChange = { customerName = it },
                        label = { Text("Customer Name / Society") },
                        modifier = Modifier.fillMaxWidth().testTag("rating_customer_name"),
                        shape = RoundedCornerShape(14.dp)
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    Text("Behavior & Safety Rating:", style = MaterialTheme.typography.labelMedium)
                    Row(
                        modifier = Modifier.padding(vertical = 4.dp),
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        (1..5).forEach { star ->
                            IconButton(
                                onClick = { ratingScore = star },
                                modifier = Modifier.size(36.dp).testTag("rate_star_$star")
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Star,
                                    contentDescription = "$star Stars",
                                    tint = if (star <= ratingScore) Color(0xFFFFB300) else Color(0xFFE0E0E0),
                                    modifier = Modifier.size(28.dp)
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    // Flag Type Dropdown
                    ExposedDropdownMenuBox(
                        expanded = dropdownExpanded,
                        onExpandedChange = { dropdownExpanded = !dropdownExpanded }
                    ) {
                        OutlinedTextField(
                            value = selectedFlag.displayName,
                            onValueChange = {},
                            readOnly = true,
                            label = { Text("Dispute / Behavior Category") },
                            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = dropdownExpanded) },
                            modifier = Modifier.menuAnchor().fillMaxWidth(),
                            shape = RoundedCornerShape(14.dp)
                        )
                        ExposedDropdownMenu(
                            expanded = dropdownExpanded,
                            onDismissRequest = { dropdownExpanded = false }
                        ) {
                            DisputeFlagType.values().forEach { flag ->
                                DropdownMenuItem(
                                    text = { Text(flag.displayName) },
                                    onClick = {
                                        selectedFlag = flag
                                        dropdownExpanded = false
                                    }
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    OutlinedTextField(
                        value = feedbackNote,
                        onValueChange = { feedbackNote = it },
                        label = { Text("Detailed Incident Description / Observations") },
                        modifier = Modifier.fillMaxWidth().testTag("rating_feedback_note"),
                        shape = RoundedCornerShape(14.dp),
                        maxLines = 3
                    )

                    Spacer(modifier = Modifier.height(14.dp))

                    Button(
                        onClick = {
                            viewModel.submitCustomerRating(
                                customerPhone = customerPhone,
                                customerName = customerName.ifBlank { "Client" },
                                providerName = providerName,
                                providerRole = providerRole,
                                ratingScore = ratingScore,
                                flagType = selectedFlag,
                                feedbackNote = feedbackNote.ifBlank { selectedFlag.displayName }
                            )
                            feedbackNote = ""
                            customerName = ""
                        },
                        colors = ButtonDefaults.buttonColors(
                            containerColor = if (selectedFlag == DisputeFlagType.UNSAFE_BEHAVIOR || selectedFlag == DisputeFlagType.NON_PAYMENT)
                                Color(0xFFD32F2F) else MaterialTheme.colorScheme.primary
                        ),
                        shape = RoundedCornerShape(20.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(48.dp)
                            .testTag("submit_customer_rating_button")
                    ) {
                        Icon(Icons.Default.ReportProblem, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = if (selectedFlag == DisputeFlagType.UNSAFE_BEHAVIOR || selectedFlag == DisputeFlagType.NON_PAYMENT)
                                "Submit Dispute & Blacklist Profile" else "Submit Verified Customer Rating",
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Verified Provider Dispute Reports List
            Text(
                text = "Recent Behavior Reports & Blocked Profiles (${reviews.size})",
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.align(Alignment.Start)
            )

            Spacer(modifier = Modifier.height(8.dp))

            reviews.forEach { review ->
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 4.dp),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = if (review.isBlacklisted) Color(0xFFFFF5F5) else MaterialTheme.colorScheme.surface
                    ),
                    border = BorderStroke(
                        1.dp,
                        if (review.isBlacklisted) Color(0xFFFFCDD2) else MaterialTheme.colorScheme.outlineVariant
                    )
                ) {
                    Column(modifier = Modifier.padding(14.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column {
                                Text(
                                    text = review.customerName,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 14.sp
                                )
                                Text(
                                    text = review.customerPhone,
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }

                            if (review.isBlacklisted) {
                                Surface(
                                    shape = RoundedCornerShape(8.dp),
                                    color = Color(0xFFD32F2F)
                                ) {
                                    Text(
                                        text = "AUTO-BLOCKED",
                                        color = Color.White,
                                        fontSize = 10.sp,
                                        fontWeight = FontWeight.Black,
                                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 3.dp)
                                    )
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(6.dp))

                        Text(
                            text = "Reported by: ${review.providerName} (${review.providerRole})",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.primary,
                            fontWeight = FontWeight.SemiBold
                        )

                        Text(
                            text = "Issue: ${review.flagType.displayName} • ${review.reportedDateFormatted}",
                            style = MaterialTheme.typography.bodySmall,
                            fontWeight = FontWeight.Medium,
                            color = if (review.isBlacklisted) Color(0xFFB71C1C) else MaterialTheme.colorScheme.onSurface
                        )

                        Text(
                            text = "\"${review.feedbackNote}\"",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.padding(top = 2.dp)
                        )
                    }
                }
            }
        }
    }
}
