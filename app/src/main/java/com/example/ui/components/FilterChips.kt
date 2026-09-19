package com.example.ui.components

import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Agriculture
import androidx.compose.material.icons.filled.Build
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.ElectricBolt
import androidx.compose.material.icons.filled.FilterAlt
import androidx.compose.material.icons.filled.LocalShipping
import androidx.compose.material.icons.filled.NearMe
import androidx.compose.material.icons.filled.Verified
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.data.model.ProviderCategory
import com.example.ui.theme.AgriGreen
import com.example.ui.theme.DriverAmber
import com.example.ui.theme.WorkerTeal

@Composable
fun CategorySelectorRow(
    selectedCategory: String?,
    onSelectCategory: (String?) -> Unit,
    isHindi: Boolean = false,
    modifier: Modifier = Modifier
) {
    val chipShape = androidx.compose.foundation.shape.RoundedCornerShape(20.dp)
    val defaultChipColors = FilterChipDefaults.filterChipColors(
        containerColor = MaterialTheme.colorScheme.surface,
        selectedContainerColor = MaterialTheme.colorScheme.primaryContainer,
        labelColor = MaterialTheme.colorScheme.onSurfaceVariant,
        selectedLabelColor = MaterialTheme.colorScheme.onPrimaryContainer
    )

    Row(
        modifier = modifier
            .fillMaxWidth()
            .horizontalScroll(rememberScrollState())
            .padding(horizontal = 16.dp, vertical = 4.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        // "All" Chip
        FilterChip(
            selected = selectedCategory == null,
            onClick = { onSelectCategory(null) },
            label = { Text(if (isHindi) "सभी श्रेणियां" else "All Categories", fontWeight = FontWeight.Medium) },
            leadingIcon = if (selectedCategory == null) {
                { Icon(imageVector = Icons.Default.Check, contentDescription = null, modifier = Modifier.size(16.dp)) }
            } else null,
            shape = chipShape,
            colors = defaultChipColors,
            border = FilterChipDefaults.filterChipBorder(
                enabled = true,
                selected = selectedCategory == null,
                borderColor = MaterialTheme.colorScheme.outlineVariant,
                selectedBorderColor = MaterialTheme.colorScheme.primary
            ),
            modifier = Modifier.testTag("filter_chip_all")
        )

        // 1. 🏍️ Bike Parcel & Delivery
        val isBike = selectedCategory == ProviderCategory.BIKE_DELIVERY.name
        FilterChip(
            selected = isBike,
            onClick = {
                onSelectCategory(if (isBike) null else ProviderCategory.BIKE_DELIVERY.name)
            },
            label = { Text(if (isHindi) "🏍️ बाइक पार्सल व डिलीवरी" else "🏍️ Bike Parcel & Delivery", fontWeight = FontWeight.Medium) },
            shape = chipShape,
            colors = defaultChipColors,
            border = FilterChipDefaults.filterChipBorder(
                enabled = true,
                selected = isBike,
                borderColor = MaterialTheme.colorScheme.outlineVariant,
                selectedBorderColor = MaterialTheme.colorScheme.primary
            ),
            modifier = Modifier.testTag("filter_chip_bike_delivery")
        )

        // 2. 🚗 Cab/Taxi Ride
        val isCab = selectedCategory == ProviderCategory.CAB_TAXI.name
        FilterChip(
            selected = isCab,
            onClick = {
                onSelectCategory(if (isCab) null else ProviderCategory.CAB_TAXI.name)
            },
            label = { Text(if (isHindi) "🚗 कैब व टैक्सी राइड" else "🚗 Cab/Taxi Ride", fontWeight = FontWeight.Medium) },
            shape = chipShape,
            colors = defaultChipColors,
            border = FilterChipDefaults.filterChipBorder(
                enabled = true,
                selected = isCab,
                borderColor = MaterialTheme.colorScheme.outlineVariant,
                selectedBorderColor = MaterialTheme.colorScheme.primary
            ),
            modifier = Modifier.testTag("filter_chip_cab_taxi")
        )

        // 3. 🚚 Cargo Loader (Tata Ace, Pickup, Cargo Auto)
        val isCargo = selectedCategory == ProviderCategory.CARGO_LOADER.name || selectedCategory == ProviderCategory.LOADING_DRIVER.name
        FilterChip(
            selected = isCargo,
            onClick = {
                onSelectCategory(if (isCargo) null else ProviderCategory.CARGO_LOADER.name)
            },
            label = { Text(if (isHindi) "🚚 माल वाहक लोडर" else "🚚 Cargo Loader", fontWeight = FontWeight.Medium) },
            shape = chipShape,
            colors = defaultChipColors,
            border = FilterChipDefaults.filterChipBorder(
                enabled = true,
                selected = isCargo,
                borderColor = MaterialTheme.colorScheme.outlineVariant,
                selectedBorderColor = MaterialTheme.colorScheme.primary
            ),
            modifier = Modifier.testTag("filter_chip_cargo_loader")
        )

        // 4. 🌾 Farmers & Mandi
        val isFarmer = selectedCategory == ProviderCategory.FARMER_VENDOR.name
        FilterChip(
            selected = isFarmer,
            onClick = {
                onSelectCategory(if (isFarmer) null else ProviderCategory.FARMER_VENDOR.name)
            },
            label = { Text(if (isHindi) "🌾 किसान व मंडी" else "🌾 Farmers & Mandi", fontWeight = FontWeight.Medium) },
            shape = chipShape,
            colors = defaultChipColors,
            border = FilterChipDefaults.filterChipBorder(
                enabled = true,
                selected = isFarmer,
                borderColor = MaterialTheme.colorScheme.outlineVariant,
                selectedBorderColor = MaterialTheme.colorScheme.primary
            ),
            modifier = Modifier.testTag("filter_chip_farmers")
        )

        // 5. 🛠️ Skilled Workers
        val isWorker = selectedCategory == ProviderCategory.SKILLED_WORKER.name
        FilterChip(
            selected = isWorker,
            onClick = {
                onSelectCategory(if (isWorker) null else ProviderCategory.SKILLED_WORKER.name)
            },
            label = { Text(if (isHindi) "🛠️ कुशल कारीगर" else "🛠️ Skilled Workers", fontWeight = FontWeight.Medium) },
            shape = chipShape,
            colors = defaultChipColors,
            border = FilterChipDefaults.filterChipBorder(
                enabled = true,
                selected = isWorker,
                borderColor = MaterialTheme.colorScheme.outlineVariant,
                selectedBorderColor = MaterialTheme.colorScheme.primary
            ),
            modifier = Modifier.testTag("filter_chip_workers")
        )

        // 6. 💅 Home Beauty & Personal Care (ब्यूटीशियन / मेहँदी / मेकओवर)
        val isBeauty = selectedCategory == ProviderCategory.HOME_BEAUTY.name
        FilterChip(
            selected = isBeauty,
            onClick = {
                onSelectCategory(if (isBeauty) null else ProviderCategory.HOME_BEAUTY.name)
            },
            label = { Text(if (isHindi) "💅 होम ब्यूटी व केयर (ब्यूटीशियन / मेहँदी)" else "💅 Home Beauty & Care", fontWeight = FontWeight.Medium) },
            shape = chipShape,
            colors = defaultChipColors,
            border = FilterChipDefaults.filterChipBorder(
                enabled = true,
                selected = isBeauty,
                borderColor = MaterialTheme.colorScheme.outlineVariant,
                selectedBorderColor = MaterialTheme.colorScheme.primary
            ),
            modifier = Modifier.testTag("filter_chip_home_beauty")
        )

        // 7. 🩺 Home Healthcare & Nursing (होम नर्स / पेशेंट केयर)
        val isHealthcare = selectedCategory == ProviderCategory.HOME_HEALTHCARE.name
        FilterChip(
            selected = isHealthcare,
            onClick = {
                onSelectCategory(if (isHealthcare) null else ProviderCategory.HOME_HEALTHCARE.name)
            },
            label = { Text(if (isHindi) "🩺 होम हेल्थकेयर व नर्स (पेशेंट केयर)" else "🩺 Home Healthcare & Nursing", fontWeight = FontWeight.Medium) },
            shape = chipShape,
            colors = defaultChipColors,
            border = FilterChipDefaults.filterChipBorder(
                enabled = true,
                selected = isHealthcare,
                borderColor = MaterialTheme.colorScheme.outlineVariant,
                selectedBorderColor = MaterialTheme.colorScheme.primary
            ),
            modifier = Modifier.testTag("filter_chip_home_healthcare")
        )

        // 8. 🧹 Domestic & Society Services (हाउस हेल्प / सफाई कर्मचारी / सोसाइटी वर्कर)
        val isDomestic = selectedCategory == ProviderCategory.DOMESTIC_SERVICES.name
        FilterChip(
            selected = isDomestic,
            onClick = {
                onSelectCategory(if (isDomestic) null else ProviderCategory.DOMESTIC_SERVICES.name)
            },
            label = { Text(if (isHindi) "🧹 घरेलू व सोसाइटी सेवाएं (हाउस हेल्प / सफाई)" else "🧹 Domestic & Society Services", fontWeight = FontWeight.Medium) },
            shape = chipShape,
            colors = defaultChipColors,
            border = FilterChipDefaults.filterChipBorder(
                enabled = true,
                selected = isDomestic,
                borderColor = MaterialTheme.colorScheme.outlineVariant,
                selectedBorderColor = MaterialTheme.colorScheme.primary
            ),
            modifier = Modifier.testTag("filter_chip_domestic_services")
        )
    }
}

@Composable
fun FilterOptionsRow(
    availableOnly: Boolean,
    onToggleAvailable: () -> Unit,
    verifiedOnly: Boolean,
    onToggleVerified: () -> Unit,
    sortByDistance: Boolean,
    onToggleSortDistance: () -> Unit,
    radiusKm: Double?,
    onSetRadius: (Double?) -> Unit,
    modifier: Modifier = Modifier
) {
    val chipShape = androidx.compose.foundation.shape.RoundedCornerShape(20.dp)
    val defaultChipColors = FilterChipDefaults.filterChipColors(
        containerColor = MaterialTheme.colorScheme.surface,
        selectedContainerColor = MaterialTheme.colorScheme.primaryContainer,
        labelColor = MaterialTheme.colorScheme.onSurfaceVariant,
        selectedLabelColor = MaterialTheme.colorScheme.onPrimaryContainer
    )

    Row(
        modifier = modifier
            .fillMaxWidth()
            .horizontalScroll(rememberScrollState())
            .padding(horizontal = 16.dp, vertical = 2.dp),
        horizontalArrangement = Arrangement.spacedBy(6.dp)
    ) {
        // Nearest First
        FilterChip(
            selected = sortByDistance,
            onClick = onToggleSortDistance,
            label = { Text(if (sortByDistance) "Nearest First" else "Top Rated") },
            leadingIcon = {
                Icon(
                    imageVector = Icons.Default.NearMe,
                    contentDescription = null,
                    modifier = Modifier.size(14.dp)
                )
            },
            shape = chipShape,
            colors = defaultChipColors,
            border = FilterChipDefaults.filterChipBorder(
                enabled = true,
                selected = sortByDistance,
                borderColor = MaterialTheme.colorScheme.outlineVariant,
                selectedBorderColor = MaterialTheme.colorScheme.primary
            ),
            modifier = Modifier.testTag("filter_chip_nearest")
        )

        // Available Now
        FilterChip(
            selected = availableOnly,
            onClick = onToggleAvailable,
            label = { Text("Available Now") },
            shape = chipShape,
            colors = FilterChipDefaults.filterChipColors(
                containerColor = MaterialTheme.colorScheme.surface,
                selectedContainerColor = Color(0xFFE8F5E9),
                labelColor = MaterialTheme.colorScheme.onSurfaceVariant,
                selectedLabelColor = Color(0xFF1B5E20)
            ),
            border = FilterChipDefaults.filterChipBorder(
                enabled = true,
                selected = availableOnly,
                borderColor = MaterialTheme.colorScheme.outlineVariant,
                selectedBorderColor = Color(0xFF2E7D32)
            ),
            modifier = Modifier.testTag("filter_chip_available")
        )

        // Verified
        FilterChip(
            selected = verifiedOnly,
            onClick = onToggleVerified,
            label = { Text("Verified") },
            leadingIcon = {
                Icon(
                    imageVector = Icons.Default.Verified,
                    contentDescription = null,
                    modifier = Modifier.size(14.dp)
                )
            },
            shape = chipShape,
            colors = defaultChipColors,
            border = FilterChipDefaults.filterChipBorder(
                enabled = true,
                selected = verifiedOnly,
                borderColor = MaterialTheme.colorScheme.outlineVariant,
                selectedBorderColor = MaterialTheme.colorScheme.primary
            ),
            modifier = Modifier.testTag("filter_chip_verified")
        )

        // Radius: Within 2km
        FilterChip(
            selected = radiusKm == 2.0,
            onClick = { onSetRadius(if (radiusKm == 2.0) null else 2.0) },
            label = { Text("< 2 km") },
            shape = chipShape,
            colors = defaultChipColors,
            border = FilterChipDefaults.filterChipBorder(
                enabled = true,
                selected = radiusKm == 2.0,
                borderColor = MaterialTheme.colorScheme.outlineVariant,
                selectedBorderColor = MaterialTheme.colorScheme.primary
            )
        )

        // Radius: Within 5km
        FilterChip(
            selected = radiusKm == 5.0,
            onClick = { onSetRadius(if (radiusKm == 5.0) null else 5.0) },
            label = { Text("< 5 km") },
            shape = chipShape,
            colors = defaultChipColors,
            border = FilterChipDefaults.filterChipBorder(
                enabled = true,
                selected = radiusKm == 5.0,
                borderColor = MaterialTheme.colorScheme.outlineVariant,
                selectedBorderColor = MaterialTheme.colorScheme.primary
            )
        )

        // Radius: Within 10km
        FilterChip(
            selected = radiusKm == 10.0,
            onClick = { onSetRadius(if (radiusKm == 10.0) null else 10.0) },
            label = { Text("< 10 km") },
            shape = chipShape,
            colors = defaultChipColors,
            border = FilterChipDefaults.filterChipBorder(
                enabled = true,
                selected = radiusKm == 10.0,
                borderColor = MaterialTheme.colorScheme.outlineVariant,
                selectedBorderColor = MaterialTheme.colorScheme.primary
            )
        )
    }
}
