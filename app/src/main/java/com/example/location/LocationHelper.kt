package com.example.location

import android.Manifest
import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.location.Address
import android.location.Geocoder
import android.net.Uri
import android.os.Build
import androidx.core.content.ContextCompat
import com.example.data.model.UserLocation
import com.google.android.gms.location.LocationServices
import com.google.android.gms.location.Priority
import com.google.android.gms.tasks.CancellationTokenSource
import kotlinx.coroutines.suspendCancellableCoroutine
import java.io.IOException
import java.util.Locale
import kotlin.coroutines.resume
import kotlin.math.atan2
import kotlin.math.cos
import kotlin.math.sin
import kotlin.math.sqrt

object LocationHelper {

    fun hasLocationPermission(context: Context): Boolean {
        val fine = ContextCompat.checkSelfPermission(
            context,
            Manifest.permission.ACCESS_FINE_LOCATION
        ) == PackageManager.PERMISSION_GRANTED
        val coarse = ContextCompat.checkSelfPermission(
            context,
            Manifest.permission.ACCESS_COARSE_LOCATION
        ) == PackageManager.PERMISSION_GRANTED
        return fine || coarse
    }

    fun hasCallPhonePermission(context: Context): Boolean {
        return ContextCompat.checkSelfPermission(
            context,
            Manifest.permission.CALL_PHONE
        ) == PackageManager.PERMISSION_GRANTED
    }

    @SuppressLint("MissingPermission")
    suspend fun fetchCurrentGpsLocation(context: Context): UserLocation? {
        if (!hasLocationPermission(context)) return null

        return suspendCancellableCoroutine { continuation ->
            val fusedClient = LocationServices.getFusedLocationProviderClient(context)
            val cts = CancellationTokenSource()

            continuation.invokeOnCancellation {
                cts.cancel()
            }

            fusedClient.getCurrentLocation(Priority.PRIORITY_HIGH_ACCURACY, cts.token)
                .addOnSuccessListener { loc ->
                    if (loc != null) {
                        val address = getReadableAddress(context, loc.latitude, loc.longitude)
                        continuation.resume(
                            UserLocation(
                                latitude = loc.latitude,
                                longitude = loc.longitude,
                                address = address,
                                accuracyMeters = loc.accuracy,
                                isManualPin = false
                            )
                        )
                    } else {
                        // Fallback to last location
                        fusedClient.lastLocation.addOnSuccessListener { lastLoc ->
                            if (lastLoc != null) {
                                val address = getReadableAddress(context, lastLoc.latitude, lastLoc.longitude)
                                continuation.resume(
                                    UserLocation(
                                        latitude = lastLoc.latitude,
                                        longitude = lastLoc.longitude,
                                        address = address,
                                        accuracyMeters = lastLoc.accuracy,
                                        isManualPin = false
                                    )
                                )
                            } else {
                                continuation.resume(null)
                            }
                        }.addOnFailureListener {
                            continuation.resume(null)
                        }
                    }
                }
                .addOnFailureListener {
                    continuation.resume(null)
                }
        }
    }

    fun getReadableAddress(context: Context, lat: Double, lng: Double): String {
        return try {
            val geocoder = Geocoder(context, Locale.getDefault())
            val list: List<Address>? = geocoder.getFromLocation(lat, lng, 1)
            if (!list.isNullOrEmpty()) {
                val addr = list[0]
                val subLoc = addr.subLocality ?: addr.locality ?: addr.subAdminArea ?: ""
                val thoroughfare = addr.thoroughfare ?: addr.featureName ?: ""
                val city = addr.locality ?: addr.adminArea ?: ""
                val parts = listOf(thoroughfare, subLoc, city).filter { it.isNotBlank() }
                if (parts.isNotEmpty()) parts.joinToString(", ")
                else "Lat: %.4f, Lng: %.4f".format(lat, lng)
            } else {
                "Near Mandi / Town Hub (%.4f, %.4f)".format(lat, lng)
            }
        } catch (e: Exception) {
            "GPS Pin (%.4f, %.4f)".format(lat, lng)
        }
    }

    fun calculateDistanceKm(lat1: Double, lon1: Double, lat2: Double, lon2: Double): Double {
        val r = 6371.0 // Radius of earth in km
        val dLat = Math.toRadians(lat2 - lat1)
        val dLon = Math.toRadians(lon2 - lon1)
        val a = sin(dLat / 2) * sin(dLat / 2) +
                cos(Math.toRadians(lat1)) * cos(Math.toRadians(lat2)) *
                sin(dLon / 2) * sin(dLon / 2)
        val c = 2 * atan2(sqrt(a), sqrt(1 - a))
        return r * c
    }

    fun formatDistance(distanceKm: Double): String {
        return if (distanceKm < 1.0) {
            "${(distanceKm * 1000).toInt()} m"
        } else {
            "%.1f km".format(distanceKm)
        }
    }

    // Direct 1-Tap Phone Calling
    fun makeDirectPhoneCall(context: Context, rawPhone: String) {
        val cleanPhone = rawPhone.replace(Regex("[^0-9+]"), "")
        val uri = Uri.parse("tel:$cleanPhone")
        try {
            if (hasCallPhonePermission(context)) {
                val callIntent = Intent(Intent.ACTION_CALL, uri).apply {
                    flags = Intent.FLAG_ACTIVITY_NEW_TASK
                }
                context.startActivity(callIntent)
            } else {
                val dialIntent = Intent(Intent.ACTION_DIAL, uri).apply {
                    flags = Intent.FLAG_ACTIVITY_NEW_TASK
                }
                context.startActivity(dialIntent)
            }
        } catch (e: Exception) {
            val dialIntent = Intent(Intent.ACTION_DIAL, uri).apply {
                flags = Intent.FLAG_ACTIVITY_NEW_TASK
            }
            context.startActivity(dialIntent)
        }
    }

    // WhatsApp Direct Messaging
    fun openWhatsAppMessage(context: Context, rawPhone: String, message: String) {
        val digitsOnly = rawPhone.replace(Regex("[^0-9]"), "")
        val formattedPhone = if (digitsOnly.length == 10) "91$digitsOnly" else digitsOnly
        val encodedMessage = Uri.encode(message)
        val url = "https://wa.me/$formattedPhone?text=$encodedMessage"

        val intent = Intent(Intent.ACTION_VIEW).apply {
            data = Uri.parse(url)
            flags = Intent.FLAG_ACTIVITY_NEW_TASK
        }
        try {
            context.startActivity(intent)
        } catch (e: Exception) {
            // Fallback to browser
            val browserIntent = Intent(Intent.ACTION_VIEW, Uri.parse(url)).apply {
                flags = Intent.FLAG_ACTIVITY_NEW_TASK
            }
            context.startActivity(browserIntent)
        }
    }

    // Google Maps Navigation
    fun openMapDirections(context: Context, lat: Double, lng: Double, label: String) {
        val uri = Uri.parse("geo:$lat,$lng?q=$lat,$lng(${Uri.encode(label)})")
        val mapIntent = Intent(Intent.ACTION_VIEW, uri).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK
        }
        try {
            context.startActivity(mapIntent)
        } catch (e: Exception) {
            val webUri = Uri.parse("https://www.google.com/maps/search/?api=1&query=$lat,$lng")
            context.startActivity(Intent(Intent.ACTION_VIEW, webUri).apply {
                flags = Intent.FLAG_ACTIVITY_NEW_TASK
            })
        }
    }
}
