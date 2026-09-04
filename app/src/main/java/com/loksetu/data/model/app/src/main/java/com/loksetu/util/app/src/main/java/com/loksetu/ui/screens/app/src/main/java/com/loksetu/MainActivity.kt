package com.loksetu

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.ui.Modifier
import com.loksetu.data.model.DirectContactInfo
import com.loksetu.data.model.LokSetuListing
import com.loksetu.data.model.PrecisionLocation
import com.loksetu.data.model.UserRole
import com.loksetu.ui.screens.HomeScreen

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        
        val sampleListings = listOf(
            LokSetuListing(
                title = "ताज़ा देसी टमाटर और हरी मिर्च",
                category = UserRole.MANDI_VENDOR,
                subCategory = "सब्जी मंडी",
                priceOrRate = "₹30 - ₹50/kg",
                description = "सीधा खेत से ताज़ा माल उपलब्ध है।",
                providerInfo = DirectContactInfo(
                    phoneNumber = "+919876543210",
                    whatsappNumber = "+919876543210",
                    name = "रामेश यादव"
                ),
                location = PrecisionLocation(
                    latitude = 28.6139,
                    longitude = 77.2090,
                    fullAddress = "किसान सब्जी मंडी, पास गेट नं. 2"
                )
            ),
            LokSetuListing(
                title = "इलेक्ट्रीशियन एवं प्लंबर सेवाएं",
                category = UserRole.SKILLED_WORKER,
                subCategory = "हाउस रिपेयर",
                priceOrRate = "₹200 विजिटिंग चार्ज",
                description = "वायरिंग, मोटर रिपेयरिंग और फिटिंग के मास्टर।",
                providerInfo = DirectContactInfo(
                    phoneNumber = "+919876543211",
                    whatsappNumber = "+919876543211",
                    name = "सुरेश मिस्त्री"
                ),
                location = PrecisionLocation(
                    latitude = 28.6129,
                    longitude = 77.2080,
                    fullAddress = "मेन मार्केट रोड"
                )
            )
        )

        setContent {
            MaterialTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    HomeScreen(listings = sampleListings)
                }
            }
        }
    }
}
