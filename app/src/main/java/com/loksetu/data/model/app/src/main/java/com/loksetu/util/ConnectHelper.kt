package com.loksetu.util

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.widget.Toast

object ConnectHelper {

    fun makeDirectCall(context: Context, phoneNumber: String) {
        val cleanNumber = phoneNumber.replace(Regex("[^0-9+]"), "")
        val intent = Intent(Intent.ACTION_DIAL).apply {
            data = Uri.parse("tel:$cleanNumber")
        }
        context.startActivity(intent)
    }

    fun openWhatsAppMessage(context: Context, whatsappNumber: String, defaultMessage: String = "जय श्री कृष्णा! मुझे LokSetu से आपकी जानकारी मिली है।") {
        var cleanNumber = whatsappNumber.replace(Regex("[^0-9]"), "")
        if (!cleanNumber.startsWith("91") && cleanNumber.length == 10) {
            cleanNumber = "91$cleanNumber"
        }
        
        try {
            val intent = Intent(Intent.ACTION_VIEW).apply {
                data = Uri.parse("https://api.whatsapp.com/send?phone=$cleanNumber&text=${Uri.encode(defaultMessage)}")
            }
            context.startActivity(intent)
        } catch (e: Exception) {
            Toast.makeText(context, "WhatsApp स्थापित नहीं है", Toast.LENGTH_SHORT).show()
        }
    }

    fun openPinpointLocation(context: Context, latitude: Double, longitude: Double, label: String = "") {
        val uri = Uri.parse("geo:$latitude,$longitude?q=$latitude,$longitude($label)")
        val intent = Intent(Intent.ACTION_VIEW, uri).apply {
            setPackage("com.google.android.apps.maps")
        }
        try {
            context.startActivity(intent)
        } catch (e: Exception) {
            val browserIntent = Intent(Intent.ACTION_VIEW, Uri.parse("https://www.google.com/maps/search/?api=1&query=$latitude,$longitude"))
            context.startActivity(browserIntent)
        }
    }
}
