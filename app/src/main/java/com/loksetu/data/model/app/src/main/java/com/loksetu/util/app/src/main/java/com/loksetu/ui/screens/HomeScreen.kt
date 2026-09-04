package com.loksetu.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import com.loksetu.data.model.LokSetuListing
import com.loksetu.data.model.UserRole
import com.loksetu.util.ConnectHelper

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    listings: List<LokSetuListing>,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    var selectedCategory by remember { mutableStateOf<UserRole?>(null) }

    val filteredListings = if (selectedCategory == null) {
        listings
    } else {
        listings.filter { it.category == selectedCategory }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("LokSetu - लोकसेतु") }
            )
        }
    ) { paddingValues ->
        Column(
            modifier = modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(16.dp)
        ) {
            Text(
                text = "Direct Connection Platform",
                style = MaterialTheme.typography.titleMedium
            )
            
            Spacer(modifier = Modifier.height(16.dp))

            LazyColumn(
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                items(filteredListings) { listing ->
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                    ) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Text(text = listing.title, style = MaterialTheme.typography.titleLarge)
                            Text(text = "${listing.subCategory} • ${listing.priceOrRate}")
                            Spacer(modifier = Modifier.height(8.dp))
                            
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Button(onClick = {
                                    ConnectHelper.makeDirectCall(context, listing.providerInfo.phoneNumber)
                                }) {
                                    Text("1-Tap Call")
                                }
                                
                                Button(
                                    onClick = {
                                        ConnectHelper.openWhatsAppMessage(context, listing.providerInfo.whatsappNumber)
                                    },
                                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.tertiary)
                                ) {
                                    Text("WhatsApp")
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}
