package com.example.ui.components

import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.GpsFixed
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.MyLocation
import androidx.compose.material.icons.filled.Navigation
import androidx.compose.material.icons.filled.ZoomIn
import androidx.compose.material.icons.filled.ZoomOut
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.FilledTonalIconButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableDoubleStateOf
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.example.data.model.ProviderEntity
import com.example.location.LocationHelper
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

@Composable
fun GpsMapPickerDialog(
    initialLat: Double,
    initialLng: Double,
    accuracyMeters: Float,
    isDetecting: Boolean,
    nearbyProviders: List<ProviderEntity> = emptyList(),
    onDetectGps: () -> Unit,
    onLocationConfirmed: (Double, Double, String) -> Unit,
    onDismiss: () -> Unit
) {
    val context = LocalContext.current
    var currentLat by remember { mutableDoubleStateOf(initialLat) }
    var currentLng by remember { mutableDoubleStateOf(initialLng) }
    var zoomLevel by remember { mutableFloatStateOf(1500f) } // scale factor
    var addressText by remember { mutableStateOf("Calculating address...") }
    var isReverseGeocoding by remember { mutableStateOf(false) }

    // Synchronize initial changes
    LaunchedEffect(initialLat, initialLng) {
        currentLat = initialLat
        currentLng = initialLng
    }

    // Geocode reverse lookup on coordinate changes
    LaunchedEffect(currentLat, currentLng) {
        isReverseGeocoding = true
        val addr = withContext(Dispatchers.IO) {
            LocationHelper.getReadableAddress(context, currentLat, currentLng)
        }
        addressText = addr
        isReverseGeocoding = false
    }

    val infiniteTransition = rememberInfiniteTransition(label = "pulse")
    val radarPulse by infiniteTransition.animateFloat(
        initialValue = 18f,
        targetValue = 68f,
        animationSpec = infiniteRepeatable(
            animation = tween(1400),
            repeatMode = RepeatMode.Restart
        ),
        label = "radarPulse"
    )

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Surface(
            modifier = Modifier
                .fillMaxSize()
                .padding(12.dp)
                .clip(RoundedCornerShape(24.dp)),
            color = MaterialTheme.colorScheme.surface,
            tonalElevation = 8.dp
        ) {
            Column(
                modifier = Modifier.fillMaxSize()
            ) {
                // Top Header Bar
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
                        .padding(horizontal = 16.dp, vertical = 12.dp),
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
                                imageVector = Icons.Default.GpsFixed,
                                contentDescription = "GPS Map Picker",
                                tint = MaterialTheme.colorScheme.onPrimaryContainer
                            )
                        }
                        Spacer(modifier = Modifier.width(12.dp))
                        Column {
                            Text(
                                text = "High-Accuracy GPS Picker",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                text = "Drag map to pin exact pickup/work location",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }

                    IconButton(
                        onClick = onDismiss,
                        modifier = Modifier.testTag("close_gps_picker_button")
                    ) {
                        Icon(imageVector = Icons.Default.Close, contentDescription = "Close")
                    }
                }

                // Interactive Map Canvas Area
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxWidth()
                        .background(Color(0xFFE8ECE9))
                        .pointerInput(zoomLevel) {
                            detectDragGestures { change, dragAmount ->
                                change.consume()
                                // Drag changes lat/long relative to zoom scale
                                val latDelta = (dragAmount.y / zoomLevel) * 0.04
                                val lngDelta = (-dragAmount.x / zoomLevel) * 0.04
                                currentLat += latDelta
                                currentLng += lngDelta
                            }
                        }
                ) {
                    // Custom GPS Map Rendering
                    Canvas(modifier = Modifier.fillMaxSize()) {
                        val centerX = size.width / 2f
                        val centerY = size.height / 2f

                        // Draw Grid lines representing GPS latitude/longitude grid
                        val gridSpacing = 64.dp.toPx()
                        val gridColor = Color(0xFFC7D3C9)
                        var x = 0f
                        while (x <= size.width) {
                            drawLine(
                                color = gridColor,
                                start = Offset(x, 0f),
                                end = Offset(x, size.height),
                                strokeWidth = 1f,
                                pathEffect = PathEffect.dashPathEffect(floatArrayOf(10f, 10f))
                            )
                            x += gridSpacing
                        }
                        var y = 0f
                        while (y <= size.height) {
                            drawLine(
                                color = gridColor,
                                start = Offset(0f, y),
                                end = Offset(size.width, y),
                                strokeWidth = 1f,
                                pathEffect = PathEffect.dashPathEffect(floatArrayOf(10f, 10f))
                            )
                            y += gridSpacing
                        }

                        // Concentric Range Rings (100m, 500m, 1km)
                        drawCircle(
                            color = Color(0x332E7D32),
                            radius = 90.dp.toPx(),
                            center = Offset(centerX, centerY),
                            style = Stroke(width = 2f, pathEffect = PathEffect.dashPathEffect(floatArrayOf(15f, 15f)))
                        )
                        drawCircle(
                            color = Color(0x222E7D32),
                            radius = 180.dp.toPx(),
                            center = Offset(centerX, centerY),
                            style = Stroke(width = 2f)
                        )

                        // Radar pulse from center GPS pinpoint
                        drawCircle(
                            color = Color(0xFF2E7D32).copy(alpha = (1f - (radarPulse / 70f)).coerceIn(0f, 0.4f)),
                            radius = radarPulse.dp.toPx(),
                            center = Offset(centerX, centerY)
                        )

                        // Draw Nearby Providers Relative to Picked Pin
                        nearbyProviders.forEach { provider ->
                            val dLat = provider.latitude - currentLat
                            val dLng = provider.longitude - currentLng
                            val provX = centerX + (dLng * zoomLevel * 20).toFloat()
                            val provY = centerY - (dLat * zoomLevel * 20).toFloat()

                            if (provX in 0f..size.width && provY in 0f..size.height) {
                                val dotColor = when (provider.category) {
                                    "FARMER_VENDOR" -> Color(0xFF2E7D32)
                                    "SKILLED_WORKER" -> Color(0xFF00695C)
                                    else -> Color(0xFFE65100)
                                }
                                drawCircle(
                                    color = dotColor,
                                    radius = 7.dp.toPx(),
                                    center = Offset(provX, provY)
                                )
                                drawCircle(
                                    color = Color.White,
                                    radius = 3.dp.toPx(),
                                    center = Offset(provX, provY)
                                )
                            }
                        }
                    }

                    // Centered Location Target Pin
                    Column(
                        modifier = Modifier.align(Alignment.Center),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Icon(
                            imageVector = Icons.Default.LocationOn,
                            contentDescription = "Pin Target",
                            modifier = Modifier
                                .size(46.dp)
                                .shadow(8.dp, CircleShape),
                            tint = MaterialTheme.colorScheme.primary
                        )
                        Box(
                            modifier = Modifier
                                .size(8.dp)
                                .background(MaterialTheme.colorScheme.primary, CircleShape)
                        )
                    }

                    // Zoom Controls & My Location Button
                    Column(
                        modifier = Modifier
                            .align(Alignment.TopEnd)
                            .padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        FilledTonalIconButton(
                            onClick = { onDetectGps() },
                            modifier = Modifier
                                .size(48.dp)
                                .testTag("gps_my_location_button"),
                            shape = CircleShape
                        ) {
                            if (isDetecting) {
                                CircularProgressIndicator(modifier = Modifier.size(24.dp), strokeWidth = 2.dp)
                            } else {
                                Icon(
                                    imageVector = Icons.Default.MyLocation,
                                    contentDescription = "Current GPS Location",
                                    tint = MaterialTheme.colorScheme.primary
                                )
                            }
                        }

                        FilledTonalIconButton(
                            onClick = { zoomLevel = (zoomLevel * 1.25f).coerceAtMost(4000f) },
                            modifier = Modifier.size(48.dp),
                            shape = CircleShape
                        ) {
                            Icon(imageVector = Icons.Default.ZoomIn, contentDescription = "Zoom In")
                        }

                        FilledTonalIconButton(
                            onClick = { zoomLevel = (zoomLevel / 1.25f).coerceAtLeast(600f) },
                            modifier = Modifier.size(48.dp),
                            shape = CircleShape
                        ) {
                            Icon(imageVector = Icons.Default.ZoomOut, contentDescription = "Zoom Out")
                        }
                    }

                    // Real-time GPS Accuracy Badge
                    Card(
                        modifier = Modifier
                            .align(Alignment.TopStart)
                            .padding(16.dp),
                        colors = CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.surface.copy(alpha = 0.95f)
                        ),
                        border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant),
                        shape = RoundedCornerShape(20.dp)
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(10.dp)
                                    .background(Color(0xFF2E7D32), CircleShape)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = "GPS Accuracy: ±${if (accuracyMeters > 0) accuracyMeters.toInt() else 4}m",
                                style = MaterialTheme.typography.labelSmall,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                        }
                    }
                }

                // Bottom Location Details & Confirmation Panel
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.6f)
                    ),
                    shape = RoundedCornerShape(16.dp)
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "Selected Pinned Location",
                                style = MaterialTheme.typography.labelMedium,
                                color = MaterialTheme.colorScheme.primary,
                                fontWeight = FontWeight.Bold
                            )

                            Text(
                                text = "%.5f, %.5f".format(currentLat, currentLng),
                                style = MaterialTheme.typography.labelSmall,
                                fontFamily = FontFamily.Monospace,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }

                        Spacer(modifier = Modifier.height(6.dp))

                        if (isReverseGeocoding) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                CircularProgressIndicator(modifier = Modifier.size(16.dp), strokeWidth = 2.dp)
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    text = "Updating geocoded street...",
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        } else {
                            Text(
                                text = addressText,
                                style = MaterialTheme.typography.bodyLarge,
                                fontWeight = FontWeight.SemiBold,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                        }

                        Spacer(modifier = Modifier.height(14.dp))

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            FilledTonalButton(
                                onClick = onDismiss,
                                modifier = Modifier.weight(1f),
                                shape = RoundedCornerShape(24.dp)
                            ) {
                                Text("Cancel")
                            }

                            Button(
                                onClick = {
                                    onLocationConfirmed(currentLat, currentLng, addressText)
                                },
                                modifier = Modifier
                                    .weight(1.5f)
                                    .testTag("confirm_gps_location_button"),
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = MaterialTheme.colorScheme.primary
                                ),
                                shape = RoundedCornerShape(24.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Check,
                                    contentDescription = null,
                                    modifier = Modifier.size(18.dp)
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Text("Set as My Location", fontWeight = FontWeight.SemiBold)
                            }
                        }
                    }
                }
            }
        }
    }
}
