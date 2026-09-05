package com.loksetu.util

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.loksetu.data.model.*
import com.loksetu.ui.viewmodel.HomeViewModel

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val viewModel = HomeViewModel()

        viewModel.addNewListing(
            LokSetuListing(
                title = "छोटा हाथी / पिकअप (1000kg क्षमता)",
                category = UserRole.TRANSPORT_DRIVER,
                subCategory = "टेंपो स्टैंड मंडी",
                priceOrRate = "₹15/km",
                description = "सब्जी मंडी से माल ढोया जाता है। कोई छिपा हुआ चार्ज नहीं।",
                providerInfo = DirectContactInfo("9876543210", "9876543210", "रामेश्वर ड्राइवर"),
                location = PrecisionLocation(city = "जयपुर", pincode = "302001"),
                transportDetails = TransportPricingDetails(
                    vehicleType = VehicleType.TEMPO_SMALL,
                    exactWeightKg = 850.0,
                    baseFare = 200.0,
                    perKmRate = 15.0,
                    loadingCharges = 100.0
                )
            )
        )

        setContent {
            MaterialTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    LokSetuHomeScreen(viewModel)
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LokSetuHomeScreen(viewModel: HomeViewModel) {
    val listings by viewModel.allListings.collectAsState(initial = emptyList())

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("लोकसेतु (LokSetu)", fontWeight = FontWeight.Bold) },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Color(0xFF1B5E20), titleContentColor = Color.White)
            )
        }
    ) { padding ->
        Column(modifier = Modifier.padding(padding).fillMaxSize()) {
            Text(
                text = "उपलब्ध गाड़ियाँ व पार्सल सेवाएँ",
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(16.dp)
            )

            LazyColumn(modifier = Modifier.fillMaxSize().padding(horizontal = 16.dp)) {
                items(listings) { listing ->
                    ListingCard(listing)
                }
            }
        }
    }
}

@Composable
fun ListingCard(listing: LokSetuListing) {
    Card(
        modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(text = listing.title, fontWeight = FontWeight.Bold, fontSize = 16.sp)
            Spacer(modifier = Modifier.height(4.dp))
            Text(text = "प्रदाता: ${listing.providerInfo.name} | शहर: ${listing.location.city}", fontSize = 14.sp, color = Color.Gray)
            
            listing.transportDetails?.let { transport ->
                Spacer(modifier = Modifier.height(8.dp))
                Divider()
                Spacer(modifier = Modifier.height(8.dp))
                Text(text = "गाड़ी का प्रकार: ${transport.vehicleType.displayName}", fontWeight = FontWeight.SemiBold, color = Color(0xFF2E7D32))
                Text(text = "बेस किराया: ₹${transport.baseFare} | दर: ₹${transport.perKmRate}/km", fontSize = 13.sp)
                Text(text = "लोडिंग चार्ज: ₹${transport.loadingCharges}", fontSize = 13.sp)
            }
        }
    }
}
